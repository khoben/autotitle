package com.khoben.autotitle.huawei.mvp.presenter;

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.provider.MediaStore
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import com.khoben.autotitle.huawei.App
import com.khoben.autotitle.huawei.BuildConfig
import com.khoben.autotitle.huawei.R
import com.khoben.autotitle.huawei.mvp.view.ResultActivityView
import com.khoben.autotitle.huawei.service.mediaplayer.MediaExoPlayerUIWrapper
import moxy.InjectViewState
import moxy.MvpPresenter
import java.io.File

@InjectViewState
class ResultViewActivityPresenter : MvpPresenter<ResultActivityView>() {

    private val mediaPlayer = MediaExoPlayerUIWrapper()
    private var videoPath: String? = null

    fun getPlayer() = mediaPlayer

    fun play(path: String) {
        mediaPlayer.init(path)
        videoPath = path
    }

    fun save(context: Context) {
        val file = File(videoPath!!)
        val values = ContentValues(3)
        values.put(MediaStore.Video.Media.MIME_TYPE, App.VIDEO_MIME_TYPE)
        values.put(MediaStore.Video.Media.DATA, file.absolutePath)
        val uri =
            context.contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
        if (uri != null) {
            viewState.showVideoSavedToast(uri.path)
        }
    }

    fun share(context: Context) {
        val fileUri =
            FileProvider.getUriForFile(
                context,
                "${BuildConfig.APPLICATION_ID}.fileprovider",
                File(videoPath!!)
            )
        ShareCompat.IntentBuilder.from(context as Activity)
            .setStream(fileUri)
            .setType(App.VIDEO_MIME_TYPE)
            .setChooserTitle(context.getString(R.string.share_video_title))
            .startChooser()
    }

    fun releasePlayer() = mediaPlayer.releasePlayer()

    fun setMediaSessionState(isActive: Boolean) {
        mediaPlayer.setMediaSessionState(isActive)
    }

    fun deactivate() {}
    fun pause() = mediaPlayer.pause()
}
