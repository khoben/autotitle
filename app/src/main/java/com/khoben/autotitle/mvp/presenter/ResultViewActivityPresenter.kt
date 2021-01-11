package com.khoben.autotitle.mvp.presenter

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.provider.MediaStore
import androidx.core.app.ShareCompat

import com.khoben.autotitle.App
import com.khoben.autotitle.R
import com.khoben.autotitle.common.FileUtils
import com.khoben.autotitle.common.NotificationUtils
import com.khoben.autotitle.mvp.view.ResultActivityView
import com.khoben.autotitle.service.mediaplayer.MediaPlayer
import moxy.InjectViewState
import moxy.MvpPresenter
import java.io.File
import javax.inject.Inject

@InjectViewState
class ResultViewActivityPresenter : MvpPresenter<ResultActivityView>() {

    @Inject
    lateinit var mediaPlayer: MediaPlayer

    @Inject
    lateinit var appContext: Context

    private var videoPath: String? = null

    private var alreadySaved = false
    private var savedPath: String? = null

    init {
        App.applicationComponent.inject(this)
    }

    fun init(path: String) {
        videoPath = path
        mediaPlayer.init(path)
    }

    fun save() {
        if (alreadySaved) {
            viewState.showVideoSavedToast(savedPath)
            return
        }
        val file = File(videoPath!!)
        val values = ContentValues(3)
        values.put(MediaStore.Video.Media.MIME_TYPE, App.VIDEO_MIME_TYPE)
        values.put(MediaStore.Video.Media.DATA, file.absolutePath)
        val uri = appContext.contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
        if (uri != null) {
            alreadySaved = true
            savedPath = uri.path
            viewState.showVideoSavedToast(savedPath)
            NotificationUtils.show(appContext, text = "Saved with path:\n$savedPath")
            viewState.alreadySaved()
        }
    }

    fun share(context: Context) {
        val fileUri = FileUtils.getUriFromPath(appContext, videoPath!!)
        ShareCompat.IntentBuilder.from(context as Activity)
                .setStream(fileUri)
                .setType(App.VIDEO_MIME_TYPE)
                .setChooserTitle(context.getString(R.string.share_video_title))
                .startChooser()
    }

    fun getPlayer() = mediaPlayer.getPlayerImpl(appContext)
    fun releasePlayer() {
        mediaPlayer.releasePlayer()
    }

    fun setMediaSessionState(isActive: Boolean) = mediaPlayer.setMediaSessionState(isActive)
    fun deactivate() {}
    fun pause() = mediaPlayer.pause()
}
