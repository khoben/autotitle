package com.khoben.autotitle.service.mediaplayer

import android.content.Context
import com.google.android.exoplayer2.ExoPlayer

interface MediaPlayer {
    /**
     * Init media player with media item with [path]
     *
     * @param path String
     */
    fun init(path: String)

    /**
     * Init new player instance
     *
     * @param context Application context
     * @return ExoPlayer instance
     */
    fun initNewPlayer(context: Context): ExoPlayer

    /**
     * Release player instance
     */
    fun releasePlayer()

    /**
     * Sets current media session state to active or not
     * @param isActive
     */
    fun setMediaSessionState(isActive: Boolean)

    /**
     * Pause playback
     */
    fun pause()

    /**
     * Stops playback
     */
    fun stop()
}