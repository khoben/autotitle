package com.khoben.autotitle.huawei.service.mediaplayer

import android.content.Context
import com.google.android.exoplayer2.ExoPlayer

interface MediaPlayer {
    fun init(url: String)

    fun getPlayerImpl(context: Context): ExoPlayer

    fun releasePlayer()

    fun setMediaSessionState(isActive: Boolean)
    fun pause()
}