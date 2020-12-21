package com.khoben.autotitle.huawei.service.mediaplayer

import android.net.Uri
import com.khoben.autotitle.huawei.App
import com.khoben.autotitle.huawei.model.PlaybackEvent
import com.khoben.autotitle.huawei.model.PlaybackState
import javax.inject.Inject

class MediaController {
    @Inject
    lateinit var mediaPlayer: MediaSurfacePlayer

    private val subscribers = mutableListOf<Callback>()

    interface Callback {
        fun handlePlaybackState(state: PlaybackEvent)
    }

    init {
        App.applicationComponent.inject(this)
    }

    fun setVideoSource(uri: Uri) {
        mediaPlayer.setDataSourceUri(uri)
    }

    val videoDetails
        get() = mediaPlayer.getVideoInfo()
    val videoDuration
        get() = mediaPlayer.getVideoDuration()
    val currentPosition
        get() = mediaPlayer.getCurrentPosition()

    fun toggle() {
        val isPlaying = !mediaPlayer.isPlaying()
        setPlayState(isPlaying)
    }

    fun toggleMute(state: Boolean) {
        when(state) {
            true -> {
                mediaPlayer.setVolumeLevel(0F)
            }
            else -> {
                mediaPlayer.setVolumeLevel(1F)
            }
        }
    }

    /**
     * Sets playback state
     * @param playState True -- Play; False -- Pause
     */
    fun setPlayState(playState: Boolean) {
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
}