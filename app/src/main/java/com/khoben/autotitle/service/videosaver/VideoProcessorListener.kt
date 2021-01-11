package com.khoben.autotitle.service.videosaver

interface VideoProcessorListener {
    fun onProgress(progress: Double)
    fun onComplete(filepath: String)
    fun onCanceled()
    fun onError(message: String)
}