package com.khoben.autotitle.service.mediaplayer

import android.net.Uri
import com.khoben.autotitle.App
import com.khoben.autotitle.model.PlaybackEvent
import com.khoben.autotitle.model.PlaybackState
import timber.log.Timber
import javax.inject.Inject

class MediaController : MediaPlayerSurfaceCallback {
    @Inject
    lateinit var mediaPlayer: MediaSurfacePlayer

    private val subscribers = mutableListOf<Callback>()

    var isMuted = true
        private set

    interface Callback {
        fun handlePlaybackState(state: PlaybackEvent)
    }

    init {
        App.applicationComponent.inject(this)
        mediaPlayer.setMediaCallbackListener(this)
        Timber.d("MediaController created")
    }

    fun setVideoSource(uri: Uri) = mediaPlayer.setDataSourceUri(uri)

    val videoDetails
        get() = mediaPlayer.getVideoInfo()
    val videoDuration
        get() = mediaPlayer.getVideoDuration()
    val currentPosition
        get() = mediaPlayer.getCurrentPosition()

    fun toggleMute(state: Boolean) {
        isMuted = state
        when (state) {
            true -> {
                mediaPlayer.setVolumeLevel(0F)
            }
            else -> {
                mediaPlayer.setVolumeLevel(1F)
            }
        }
    }

    fun toggle() = setPlayState(!mediaPlayer.isNotPaused())

    /**
     * Sets playback state
     * @param playState True -- Play; False -- Pause
     */
    fun setPlayState(playState: Boolean) {
        if (playState == mediaPlayer.isNotPaused()) return
        when (playState) {
            true -> {
                mediaPlayer.play()
                handleEvent(PlaybackEvent(PlaybackState.PLAY))
            }
            false -> {
                mediaPlayer.pause()
                handleEvent(PlaybackEvent(PlaybackState.PAUSED))
            }
        }
    }

    fun seekTo(timestamp: Long) {
        mediaPlayer.seekTo(timestamp)
        handleEvent(PlaybackEvent(PlaybackState.SEEK, timestamp))
    }

    fun addSubscription(sub: Callback) {
        subscribers.add(sub)
    }

    private fun handleEvent(event: PlaybackEvent) {
        subscribers.forEach {
            it.handlePlaybackState(event)
        }
    }

    override fun onMediaPlayerStarted() {
    }

    override fun onMediaPlayerPaused() {
    }

    override fun onMediaPlayerPrepared() {
    }

    override fun onMediaPlayerCompletion() {
    }
}