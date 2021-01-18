package com.khoben.autotitle.ui.player.seekbar

import android.graphics.Bitmap

data class FramesHolder(
    val status: FrameStatus,
    val frameTime: Long? = null,
    var emptyFramesCount: Long? = null,
    var singleFrame: Bitmap? = null,
    var listFrames: List<Bitmap>? = null
)
