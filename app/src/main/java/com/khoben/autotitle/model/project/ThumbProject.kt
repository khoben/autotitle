package com.khoben.autotitle.model.project

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import android.net.Uri
import android.provider.MediaStore
import com.khoben.autotitle.App
import com.khoben.autotitle.common.BitmapUtils
import com.khoben.autotitle.common.FileUtils
import com.khoben.autotitle.service.frameretriever.AndroidNativeMetadataProvider
import kotlinx.serialization.Serializable


/**
 * Item of recent projects on main screen
 * @property id Unique id
 * @property title Visible name of project
 * @property sourceVideoThumbPath Path to thumb
 * @property dateCreated Unix timestamp of first time opened
 * @property dateUpdated Unix timestamp of last update
 * @property videoDuration Duration of video, in ms
 * @property videoFileSizeBytes Video file size, in bytes
 */
@Serializable
data class ThumbProject(
    val id: String,
    var title: String? = null,
    val sourceVideoThumbPath: String = "thumb",
    var dateCreated: Long = -1,
    var dateUpdated: Long = -1,
    var videoDuration: Long = -1,
    var videoFileSizeBytes: Long = -1,
    var videoSourceFilePath: String? = null
) {
    fun getThumbPath() = "${App.PROJECTS_FOLDER}/${id}/${sourceVideoThumbPath}"
    fun createThumb(uri: Uri, context: Context) {
        AndroidNativeMetadataProvider(context, uri)
            .getFrameAt(0L)?.let { BitmapUtils.cropCenter(it, 512, 384) }
            ?.let {
                FileUtils.writeBitmap(getThumbPath(), it, Bitmap.CompressFormat.WEBP, 75)
            }
    }

    fun createProjectFolderIfNotExists() {
        FileUtils.createDirIfNotExists("${App.PROJECTS_FOLDER}/${id}")
    }
}