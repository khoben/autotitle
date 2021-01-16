package com.khoben.autotitle.model.project

import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.provider.MediaStore
import com.khoben.autotitle.App
import com.khoben.autotitle.common.FileUtils
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
    fun createThumb() {
        videoSourceFilePath?.let {
            ThumbnailUtils.createVideoThumbnail(
                    it,
                    MediaStore.Images.Thumbnails.MINI_KIND
            )
        }?.let {
            FileUtils.writeBitmap(getThumbPath(), it, Bitmap.CompressFormat.WEBP, 75)
        }
    }

    fun createProjectFolderIfNotExists() {
        FileUtils.createDirIfNotExists("${App.PROJECTS_FOLDER}/${id}")
    }
}