package com.khoben.autotitle.service.mediaplayer

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.view.Surface
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.khoben.autotitle.model.VideoInfo
import timber.log.Timber

class MediaExoPlayerSurfaceWrapper(private var context: Context) :
    MediaSurfacePlayer,
    Player.EventListener {

    private var mediaPlayer: SimpleExoPlayer? = null
    private var dataSourceUri: Uri? = null
    private var mediaFileInfo: VideoInfo? = null
    private var mediaPlayerCallback: MediaPlayerSurfaceCallback? = null
    private var isPreparing = true

    init {
        mediaPlayer = SimpleExoPlayer.Builder(context)
            .build()
            .apply {
                addListener(this@MediaExoPlayerSurfaceWrapper)
            }
    }

    override fun setDataSourceUri(uri: Uri) {
        dataSourceUri = uri
        mediaPlayer!!.setMediaItem(MediaItem.fromUri(dataSourceUri!!))
        val mediaRetriever =
            MediaMetadataRetriever().apply { setDataSource(context, dataSourceUri) }
        val rotation =
            mediaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)
        val width = mediaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
        val height =
            mediaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
        val duration = mediaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        val info = VideoInfo()
        try {
            info.apply {
                this.uri = dataSourceUri
                this.rotation = rotation!!.toInt()
                this.width = width!!.toInt()
                this.height = height!!.toInt()
                this.duration = duration!!.toLong()
            }
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
        Timber.d("set surface")
        mediaPlayer!!.setVideoSurface(surface)
    }

    override fun prepare() {
        isPreparing = true
        mediaPlayer!!.prepare()
    }

    override fun play() {
        if (!isNotPaused()) {
            mediaPlayer!!.play()
            Timber.d("Playing started")
        }
    }

    override fun pause() {
        if (isNotPaused()) {
            mediaPlayer?.pause()
            Timber.d("Paused")
        }
    }

    override fun toggle() {
        if (isNotPaused()) {
            pause()
        } else {
            play()
        }
    }

    override fun isNotPaused(): Boolean {
        return mediaPlayer != null &&
                ((mediaPlayer!!.playWhenReady &&
                        mediaPlayer!!.playbackState == Player.STATE_READY) ||
                        mediaPlayer!!.playbackState == Player.STATE_ENDED)

    }

    override fun stop() {
        mediaPlayer!!.stop()
    }

    override fun release() {
        mediaPlayer!!.release()
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        when (playbackState) {
            ExoPlayer.STATE_BUFFERING -> {
            }
            ExoPlayer.STATE_ENDED -> {
                // onCompletion
                mediaPlayerCallback?.onMediaPlayerCompletion()
            }
            ExoPlayer.STATE_IDLE -> {
            }
            ExoPlayer.STATE_READY -> {
                if (playWhenReady) {
                    // onPrepared
                    if (isPreparing) {
                        mediaPlayerCallback?.onMediaPlayerPrepared()
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
        Timber.d("Seek to $timestamp")
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