package com.khoben.autotitle.huawei.service.mediaplayer

import android.net.Uri
import android.view.Surface
import com.khoben.autotitle.huawei.model.VideoInfo

interface MediaSurfacePlayer {
    fun setDataSourceUri(uri: Uri)
    fun prepare()
    fun initSurface()
    fun play()
    fun stop()
    fun pause()
    fun toggle()
    fun seekTo(timestamp: Long)
    fun release()
    fun isPlaying(): Boolean
    fun getVideoInfo(): VideoInfo?
    fun getVideoDuration(): Long
    fun getCurrentPosition(): Long
    fun setSurface(surface: Surface?)
    fun setVolumeLevel(volume: Float)
    fun setMediaCallbackListener(mediaPlayerCallback: MediaPlayerSurfaceCallback?)
}