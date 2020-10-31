package com.khoben.autotitle.huawei.service.mediaplayer

import android.opengl.GLSurfaceView

abstract class VideoRender : GLSurfaceView.Renderer {
    abstract fun setMediaPlayer(player: MediaSurfacePlayer?)
}