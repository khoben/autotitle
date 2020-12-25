package com.khoben.autotitle.service.videosaver

import android.content.Context
import android.net.Uri
import com.khoben.autotitle.model.VideoInfo
import com.khoben.autotitle.ui.overlay.OverlayText

interface VideoProcessor {
    fun start()
    fun cancel()
    fun setup(
        overlays: List<OverlayText>,
        sourceUri: Uri,
        outputPath: String,
        context: Context,
        videoInfo: VideoInfo,
        parentViewSize: Pair<Int, Int>
    )

    fun setup(
        overlays: List<OverlayText>,
        sourcePath: String,
        outputPath: String,
        videoInfo: VideoInfo,
        parentViewSize: Pair<Int, Int>
    )
}

abstract class VideoProcessorBase : VideoProcessor {
    var listener: VideoProcessorListener? = null
}