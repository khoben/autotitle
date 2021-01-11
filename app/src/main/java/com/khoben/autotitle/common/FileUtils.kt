package com.khoben.autotitle.common

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import com.khoben.autotitle.App
import com.khoben.autotitle.BuildConfig
import timber.log.Timber
import java.io.File


object FileUtils {

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

    fun getApplicationMainDir() = App.appContext.getExternalFilesDir(null)

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
     * Get random filepath in external specified directory
     * @param context Context
     * @param extension String
     * @param directory String
     * @return Filepath
     */
    fun getRandomFilepath(
        context: Context,
        extension: String,
        directory: String = Environment.DIRECTORY_MOVIES
    ): String {
        return "${context.getExternalFilesDir(directory)?.absolutePath}/${System.currentTimeMillis()}.$extension"
    }

    /**
     * Get random Uri in external specified directory
     * @param context Context
     * @param extension String
     * @param directory String
     * @return Uri
     */
    fun getRandomUri(
        context: Context,
        extension: String,
        directory: String = Environment.DIRECTORY_MOVIES
    ): Uri {
        return getUriFromPath(context, getRandomFilepath(context, extension, directory))
    }

    /**
     * Get uri from provided filepath
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
     * Get filepath from provided [uri] and [context]
     * @param context Context
     * @param uri Uri
     * @return String?
     */
    fun getRealPathFromURI(context: Context, uri: Uri): String? {
        var cursor: Cursor? = null
        return try {
            val projection = arrayOf(MediaStore.Images.Media.DATA)
            cursor = context.contentResolver.query(uri, projection, null, null, null)
            val columnIndex = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            cursor.getString(columnIndex)
        } catch (e: java.lang.Exception) {
            Timber.e(e, "getRealPathFromURI() Exception")
            null
        } finally {
            cursor?.close()
        }
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
     * Get path of external movie directory
     * @return File
     */
    fun getAndroidMoviesFolder(): File {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
    }

    /**
     * Same as getRandomFilepath
     * @param extension String
     * @return String
     */
    fun getPublicFilepath(extension: String): String {
        return "${getAndroidMoviesFolder().absolutePath}/${System.currentTimeMillis()}.$extension"
    }
}