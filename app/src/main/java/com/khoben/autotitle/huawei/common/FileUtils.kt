package com.khoben.autotitle.huawei.common

import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import com.khoben.autotitle.huawei.BuildConfig
import java.io.File

object FileUtils {

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
        directory: String = Environment.DIRECTORY_PICTURES
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
        directory: String = Environment.DIRECTORY_PICTURES
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
            Log.d("FileUtils", "File $path was deleted")
        } else {
            Log.e("FileUtils", "File $path wasnot deleted")
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