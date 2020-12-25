package com.khoben.autotitle.service.mediaplayer

import android.net.Uri
import com.khoben.autotitle.App
import com.khoben.autotitle.model.PlaybackEvent
import com.khoben.autotitle.model.PlaybackState
import javax.inject.Inject

class MediaController: MediaPlayerSurfaceCallback {
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
    }

    fun setVideoSource(uri: Uri) = mediaPlayer.setDataSourceUri(uri)

    val videoDetails
        get() = mediaPlayer.getVideoInfo()
    val videoDuration
        get() = mediaPlayer.getVideoDuration()
    val currentPosition
        get() = mediaPlayer.getCurrentPosition()

    fun toggleMute(state: Boolean) {
        if (isMuted == state) return
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

    fun toggle() = setPlayState(!mediaPlayer.isPlaying())

    /**
     * Sets playback state
     * @param playState True -- Play; False -- Pause
     */
    fun setPlayState(playState: Boolean) {
        if (playState == mediaPlayer.isPlaying()) return
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
        handleEvent(PlaybackEvent(PlaybackState.REWIND, timestamp))
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