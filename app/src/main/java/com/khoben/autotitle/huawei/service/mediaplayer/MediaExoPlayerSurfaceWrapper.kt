package com.khoben.autotitle.huawei.service.mediaplayer

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import android.view.Surface
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.khoben.autotitle.huawei.model.VideoInfo
import org.greenrobot.eventbus.EventBus


class MediaExoPlayerSurfaceWrapper(private var context: Context) : Player.EventListener,
    MediaSurfacePlayer {

    private var mediaPlayer: SimpleExoPlayer? = null
    private var dataSourceUri: Uri? = null
    private var mediaFileInfo: VideoInfo? = null
    private var surface: Surface? = null
    private var mediaPlayerCallback: MediaPlayerSurfaceCallback? = null
    private var isPreparing = true

    companion object {
        private var TAG = MediaExoPlayerSurfaceWrapper::class.java.simpleName
    }

    override fun setDataSourceUri(uri: Uri) {
        dataSourceUri = uri
        val mediaRetriever = MediaMetadataRetriever()
        val info = VideoInfo()
        mediaRetriever.setDataSource(context, dataSourceUri)
        val rotation =
            mediaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)
        val width = mediaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
        val height =
            mediaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
        val duration = mediaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        info.uri = dataSourceUri
        try {
            info.rotation = rotation!!.toInt()
            info.width = width!!.toInt()
            info.height = height!!.toInt()
            info.duration = duration!!.toLong()
        } catch (e: NumberFormatException) {
            e.printStackTrace()
        } finally {
            mediaFileInfo = info
        }
    }

    override fun getVideoInfo(): VideoInfo? {
        return mediaFileInfo
    }

    override fun setSurface(surface: Surface?) {
        this.surface = surface
    }

    override fun prepare() {
        mediaPlayer = SimpleExoPlayer.Builder(context).build()
        mediaPlayer!!.addListener(this)
        val mediaItem: MediaItem = MediaItem.fromUri(dataSourceUri!!)
        mediaPlayer!!.setMediaItem(mediaItem)
        isPreparing = true
        mediaPlayer!!.prepare()
    }

    override fun initSurface() {
        Log.d(TAG, "Surface init")
        mediaPlayer!!.setVideoSurface(surface)
    }

    override fun play() {
        Log.d(TAG, "Playing")
        mediaPlayer!!.play()
//        mediaPlayerCallback?.onMediaPlayerStarted()
    }

    override fun pause() {
        Log.d(TAG, "Paused")
        mediaPlayer?.pause()
//        mediaPlayerCallback?.onMediaPlayerPaused()
    }

    override fun toggle() {
        if (mediaPlayer!!.isPlaying) {
            pause()
        } else {
            play()
        }
    }

    override fun isPlaying(): Boolean {
        return if (mediaPlayer != null) {
            mediaPlayer!!.isPlaying
        } else {
            false
        }
    }

    override fun stop() {
        mediaPlayer!!.stop()
    }

    override fun release() {
        EventBus.getDefault().unregister(this)
        mediaPlayer!!.release()
    }

//    @Subscribe
//    fun playbackEvent(event: PlaybackUI) {
//        when(event.playState) {
//        when(event.playState) {
//            PlaybackUIState.PLAY -> {
//                start()
//            }
//            PlaybackUIState.PAUSED -> {
//                pause()
//            }
//            PlaybackUIState.STOP -> {
//                stop()
//            }
//            PlaybackUIState.REWIND -> {
//                Log.d(TAG, "REWIND TO ${event.currentPosition}")
//                pause()
//                seekTo(event.currentPosition)
//            }
//            PlaybackUIState.TOGGLE -> {
//                toggle()
//            }
//        }
//    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        when (playbackState) {
            ExoPlayer.STATE_BUFFERING -> {
            }
            ExoPlayer.STATE_ENDED -> {
                // onCompletion
                mediaPlayerCallback?.onMediaPlayerCompletion()
//                EventController.Player.sendCompletion()
            }
            ExoPlayer.STATE_IDLE -> {
            }
            ExoPlayer.STATE_READY -> {
                if (playWhenReady) {
                    // onPrepared
                    if (isPreparing) {
                        mediaPlayerCallback?.onMediaPlayerPrepared()
//                        EventController.Player.sendPrepared()
                        isPreparing = false
                    }
                    // onStart
                    else {
                        mediaPlayerCallback?.onMediaPlayerStarted()
                    }
                } else {
                    // onPause
                    mediaPlayerCallback?.onMediaPlayerPaused()
                }
            }
        }
    }

    override fun seekTo(timestamp: Long) {
        mediaPlayer!!.seekTo(timestamp)
    }

    override fun getVideoDuration(): Long {
        return mediaFileInfo!!.duration
    }

    override fun getCurrentPosition(): Long {
        return mediaPlayer!!.currentPosition
    }

    override fun setVolumeLevel(volume: Float) {
        mediaPlayer!!.volume = volume
    }

    override fun setMediaCallbackListener(mediaPlayerCallback: MediaPlayerSurfaceCallback?) {
        this.mediaPlayerCallback = mediaPlayerCallback
    }
}