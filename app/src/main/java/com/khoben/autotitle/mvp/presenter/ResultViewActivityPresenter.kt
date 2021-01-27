package com.khoben.autotitle.mvp.presenter

import android.app.Activity
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.media.ThumbnailUtils
import android.provider.MediaStore
import androidx.core.app.ShareCompat
import com.khoben.autotitle.App
import com.khoben.autotitle.R
import com.khoben.autotitle.common.FileUtils
import com.khoben.autotitle.common.FileUtils.getFileName
import com.khoben.autotitle.common.NotificationUtils
import com.khoben.autotitle.mvp.view.ResultActivityView
import com.khoben.autotitle.service.mediaplayer.MediaPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
        viewState.onSaveStarted()

        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                val file = File(videoPath!!)
                val uri =
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                        // saves externally and put it in gallery
                        FileUtils.saveVideoToGallery(appContext, file)
                    } else {
                        // already saved externally, just put it in gallery
                        val values = ContentValues(2).apply {
                            put(MediaStore.Video.Media.MIME_TYPE, App.VIDEO_MIME_TYPE)
                            put(MediaStore.Video.Media.DATA, file.absolutePath)
                        }
                        appContext.contentResolver.insert(
                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                            values
                        )
                    }
                if (uri != null) {
                    alreadySaved = true
                    savedPath = uri.getFileName(appContext)
                    NotificationUtils.show(
                        appContext,
                        text = savedPath!!,
                        title = appContext.getString(R.string.video_saved_title),
                        largeIcon = ThumbnailUtils.createVideoThumbnail(
                            videoPath!!,
                            MediaStore.Images.Thumbnails.MICRO_KIND
                        ),
                        notificationIntent = PendingIntent.getActivity(
                            appContext,
                            0,
                            Intent(Intent.ACTION_VIEW).apply {
                                data = uri
                            },
                            PendingIntent.FLAG_CANCEL_CURRENT
                        )
                    )
                }
                viewState.onSavingEnd()
                viewState.showVideoSavedToast(savedPath)
            }
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

    fun initNewPlayer() = mediaPlayer.initNewPlayer(appContext)
    fun releasePlayer() {
        mediaPlayer.releasePlayer()
    }

    fun setMediaSessionState(isActive: Boolean) = mediaPlayer.setMediaSessionState(isActive)
    fun deactivate() {}
    fun pause() = mediaPlayer.pause()
}
