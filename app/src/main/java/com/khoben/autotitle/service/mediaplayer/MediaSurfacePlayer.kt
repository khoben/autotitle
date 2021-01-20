package com.khoben.autotitle.service.mediaplayer

import android.net.Uri
import android.view.Surface
import androidx.annotation.FloatRange
import com.khoben.autotitle.model.VideoInfo

interface MediaSurfacePlayer {
    /**
     * Sets data source uri
     * @param uri Media item's uri
     */
    fun setDataSourceUri(uri: Uri)

    /**
     * Prepare media player for playback
     */
    fun prepare()

    /**
     * Set output surface
     *
     * @param surface Output surface
     */
    fun setSurface(surface: Surface?)

    /**
     * Starts playback
     */
    fun play()

    /**
     * Stops playback
     */
    fun stop()

    /**
     * Pauses playback
     */
    fun pause()

    /**
     * Toggles playback between paused and playing state
     */
    fun toggle()

    /**
     * Seek to [timestamp]
     *
     * @param timestamp Timestamp, in ms
     */
    fun seekTo(timestamp: Long)

    /**
     * Releases media player
     */
    fun release()

    /**
     * Checks if current playback state isn't paused
     *
     * @return Paused or not
     */
    fun isNotPaused(): Boolean

    /**
     * Get current media item info
     *
     * @return Media info
     */
    fun getVideoInfo(): VideoInfo?

    /**
     * Get current loaded video duration
     *
     * @return Duration, in ms
     */
    fun getVideoDuration(): Long

    /**
     * Get current playback timestamp, in ms
     *
     * @return Timestamp, in ms
     */
    fun getCurrentPosition(): Long

    /**
     * Sets volume level
     *
     * @param volume Volume level, from 0.0 to 1.0
     */
    fun setVolumeLevel(@FloatRange(from = 0.0, to = 1.0) volume: Float)

    /**
     * Set media event listener
     *
     * @param mediaPlayerCallback MediaPlayerSurfaceCallback?
     */
    fun setMediaCallbackListener(mediaPlayerCallback: MediaPlayerSurfaceCallback?)
}