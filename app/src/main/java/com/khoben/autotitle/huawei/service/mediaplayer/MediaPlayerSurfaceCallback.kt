package com.khoben.autotitle.huawei.service.mediaplayer

interface MediaPlayerSurfaceCallback {
    fun onMediaPlayerStarted()
    fun onMediaPlayerPaused()
    fun onMediaPlayerPrepared()
    fun onMediaPlayerCompletion()
}