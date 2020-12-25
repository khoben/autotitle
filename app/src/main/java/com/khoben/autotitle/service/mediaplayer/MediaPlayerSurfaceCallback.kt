package com.khoben.autotitle.service.mediaplayer

interface MediaPlayerSurfaceCallback {
    fun onMediaPlayerStarted()
    fun onMediaPlayerPaused()
    fun onMediaPlayerPrepared()
    fun onMediaPlayerCompletion()
}