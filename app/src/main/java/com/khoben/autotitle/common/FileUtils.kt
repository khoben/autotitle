package com.khoben.autotitle.common

import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.res.AssetFileDescriptor
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.annotation.IntRange
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import com.google.gson.GsonBuilder
import com.khoben.autotitle.App
import com.khoben.autotitle.BuildConfig
import com.khoben.autotitle.R
import timber.log.Timber
import java.io.*
import java.lang.reflect.Type


object FileUtils {

    /**
     * Deserializes JSON string to specified object type
     *
     * @param jsonString JSON string
     * @param typeToken Type of object
     * @return Object
     */
    inline fun <reified T> getObjectFromJson(
        jsonString: String,
        typeToken: Type
    ): T {
        return GsonBuilder().create().fromJson(jsonString, typeToken)
    }

    /**
     * Reads [inputStream] and returns it as String
     *
     * @param inputStream InputStream
     * @return String
     */
    fun inputStreamToString(inputStream: InputStream): String {
        return try {
            val bytes = ByteArray(inputStream.available())
            inputStream.read(bytes, 0, bytes.size)
            String(bytes)
        } catch (e: IOException) {
            ""
        }
    }

    /**
     * @see [File.deleteRecursively]
     * @param path Folder path
     */
    fun removeFileFolderRecursive(path: String) {
        val folder = File(path)
        if (!folder.exists()) {
            Timber.e("Removing not existing folder")
            return
        } else if (!folder.isDirectory)
            throw RuntimeException("Only folders allowed")
        val deleted = folder.deleteRecursively()
        if (deleted) {
            Timber.d("Folder $path was deleted")
        } else {
            Timber.e("Folder $path wasn't deleted")
        }
    }

    /**
     * Get size of data abstracted by uri
     * @param context context to access uri
     * @param uri uri
     * @return size in bytes, -1 if unknown
     */
    fun getSizeBytes(context: Context, uri: Uri): Long {
        return if (ContentResolver.SCHEME_CONTENT == uri.scheme) {
            var fileDescriptor: AssetFileDescriptor? = null
            try {
                fileDescriptor = context.contentResolver.openAssetFileDescriptor(uri, "r")
                val size = fileDescriptor?.parcelFileDescriptor?.statSize ?: 0
                if (size < 0) -1 else size
            } catch (e: FileNotFoundException) {
                Timber.e(e, "Unable to extract length from targetFile: $uri")
                -1
            } catch (e: IllegalStateException) {
                Timber.e(e, "Unable to extract length from targetFile: $uri")
                -1
            } finally {
                if (fileDescriptor != null) {
                    try {
                        fileDescriptor.close()
                    } catch (e: IOException) {
                        Timber.e(e, "Unable to close file descriptor from targetFile: $uri")
                    }
                }
            }
        } else if (ContentResolver.SCHEME_FILE == uri.scheme && uri.path != null) {
            File(uri.path!!).length()
        } else {
            -1
        }
    }

    /**
     * Writes [bitmap] to specified [path]
     *
     * @param path Output path
     * @param bitmap Source bitmap
     * @param format CompressFormat
     * @param quality Quality level, 0-100
     */
    fun writeBitmap(
        path: String,
        bitmap: Bitmap,
        format: Bitmap.CompressFormat,
        @IntRange(from = 0, to = 100) quality: Int
    ) {
        File(path).outputStream().use { out ->
            bitmap.compress(format, quality, out)
            out.flush()
        }
    }

    /**
     * Checks if [path] is existing
     * @param path
     * @return Boolean
     */
    fun checkIfExists(path: String) = File(path).exists()

    /**
     * Get filename of file with this Uri
     *
     * @return File name
     */
    fun Uri.getFileName(context: Context): String? = when (scheme) {
        ContentResolver.SCHEME_FILE -> File(path!!).name
        ContentResolver.SCHEME_CONTENT -> getCursorContent(context)
        else -> null
    }

    private fun Uri.getCursorContent(context: Context): String? = try {
        context.contentResolver.query(this, null, null, null, null)?.let { cursor ->
            cursor.run {
                if (moveToFirst()) getString(getColumnIndex(OpenableColumns.DISPLAY_NAME))
                else null
            }.also { cursor.close() }
        }
    } catch (e: Exception) {
        null
    }

    /**
     * Creates directory with [filepath] if it not exists
     *
     * @param filepath File path
     */
    fun createDirIfNotExists(filepath: String) {
        val file = File(filepath)
        if (!file.exists()) {
            try {
                when (file.mkdir()) {
                    true -> Timber.d("Directory $filepath was created")
                    else -> Timber.e("Directory $filepath dir wasn't created")
                }
            } catch (e: SecurityException) {
                Timber.e(e, "Directory $filepath dir wasn't created")
            }
        } else {
            Timber.d("Directory $filepath already created")
        }
    }

    /**
     * Get uri from provided filepath
     *
     * @param context Context
     * @param path String
     * @return Uri
     */
    fun getUriFromPath(context: Context, path: String): Uri {
        return FileProvider.getUriForFile(
            context,
            "${BuildConfig.APPLICATION_ID}.fileprovider",
            File(path)
        )
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    /**
     * Get filepath from provided [uri] and [context]
     * @param context Context
     * @param uri Uri
     * @return String?
     */
    fun getRealPathFromURI(context: Context, uri: Uri): String? {
        var _uri = uri
        val needToCheckUri = Build.VERSION.SDK_INT >= 19
        var selection: String? = null
        var selectionArgs: Array<String>? = null
        if (needToCheckUri && DocumentsContract.isDocumentUri(context, _uri)) {
            if (isExternalStorageDocument(_uri)) {
                val docId = DocumentsContract.getDocumentId(_uri)
                val split = docId.split(":".toRegex()).toTypedArray()
                return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
            } else if (isDownloadsDocument(_uri)) {
                val id = DocumentsContract.getDocumentId(_uri)
                if (id.startsWith("raw:")) {
                    return id.replaceFirst("raw:".toRegex(), "")
                }
                _uri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id)
                )
            } else if (isMediaDocument(_uri)) {
                val docId = DocumentsContract.getDocumentId(_uri)
                val split = docId.split(":".toRegex()).toTypedArray()
                when (split[0]) {
                    "image" -> _uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    "video" -> _uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    "audio" -> _uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
                selection = "_id=?"
                selectionArgs = arrayOf(split[1])
            }
        }
        if ("content".equals(_uri.scheme, ignoreCase = true)) {
            val projection = arrayOf(MediaStore.Images.Media.DATA)
            try {
                context.contentResolver.query(_uri, projection, selection, selectionArgs, null)
                    .use { cursor ->
                        if (cursor != null && cursor.moveToFirst()) {
                            val columnIndex =
                                cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                            return cursor.getString(columnIndex)
                        }
                    }
            } catch (e: java.lang.Exception) {
                Timber.e(e)
            }
        } else if ("file".equals(_uri.scheme, ignoreCase = true)) {
            return _uri.path
        }
        return null
    }

    /**
     * Deletes file from provided path
     * @param context Context
     * @param path String
     */
    fun deleteFile(context: Context, path: String) {
        val file = File(path)
        val deleted = file.delete()
        MediaScannerConnection.scanFile(
            context, arrayOf(file.toString()),
            arrayOf(file.name), null
        )
        if (deleted) {
            Timber.d("File $path was deleted")
        } else {
            Timber.e("File $path wasn't deleted")
        }
    }

    /**
     * Puts [videoFile] into gallery directory
     *
     * @param context Context
     * @param videoFile File
     * @return Output file Uri
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    fun saveVideoToGallery(context: Context, videoFile: File): Uri? {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, videoFile.name)
            put(MediaStore.Images.Media.MIME_TYPE, App.VIDEO_MIME_TYPE)
            put(
                MediaStore.Images.Media.RELATIVE_PATH,
                Environment.DIRECTORY_MOVIES + "/${context.getString(R.string.app_name)}/"
            )
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        val collection = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val fileUri = context.contentResolver.insert(collection, values)
        fileUri?.let {
            context.contentResolver.openFileDescriptor(fileUri, "w").use { descriptor ->
                descriptor?.let {
                    FileOutputStream(descriptor.fileDescriptor).use { out ->
                        FileInputStream(videoFile).use { inputStream ->
                            val buf = ByteArray(8192)
                            while (true) {
                                val sz = inputStream.read(buf)
                                if (sz <= 0) break
                                out.write(buf, 0, sz)
                            }
                        }
                    }
                }
            }
            values.clear()
            values.put(MediaStore.Video.Media.IS_PENDING, 0)
            context.contentResolver.update(fileUri, values, null, null)
        }
        return fileUri
    }

    /**
     * Gets the base name, without extension, of given file name.
     *
     * e.g. getBaseName("file.txt") will return "file"
     *
     * @param fileName
     * @return the base name
     */
    fun getBaseName(fileName: String): String {
        val index = fileName.lastIndexOf('.')
        return if (index == -1) {
            fileName
        } else {
            fileName.substring(0, index)
        }
    }

    /**
     * Returns temp random filepath
     *
     * @return Filepath
     */
    fun getInternalRandomFilepath(): String {
        val tempDirPath = "${App.APP_MAIN_FOLDER}/tmp"
        createDirIfNotExists(tempDirPath)
        return "$tempDirPath/${System.currentTimeMillis()}"
    }

    /**
     * Returns video output filepath
     *
     * @param context Context
     * @param appPrefix Application prefix
     * @return Filepath
     */
    fun getOutputVideoFilePath(
        context: Context,
        appPrefix: String = "autotitle_"
    ): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            "${getInternalMoviesFolder(context)}/$appPrefix${System.currentTimeMillis()}.${App.VIDEO_EXTENSION}"
        else
            "${getExternalMoviesFolder(context)}/$appPrefix${System.currentTimeMillis()}.${App.VIDEO_EXTENSION}"
    }

    /**
     * Get movie folder filepath in internal specified directory
     *
     * @param context Context
     * @return Filepath
     */
    fun getInternalMoviesFolder(context: Context): String {
        return "${context.getExternalFilesDir(Environment.DIRECTORY_MOVIES)?.absolutePath}"
    }

    /**
     * Get movie folder filepath in external specified directory
     *
     * Same as [getInternalMoviesFolder], but uses deprecated [Environment.getExternalStoragePublicDirectory]
     *
     * @return Public external filepath
     */
    fun getExternalMoviesFolder(context: Context): String {
        return "${Environment.DIRECTORY_MOVIES}/${context.getString(R.string.app_name)}"
    }
}