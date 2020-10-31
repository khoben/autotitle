package com.khoben.autotitle.huawei.model

import android.net.Uri

data class VideoInfo(
    var externalFilepath: String? = null,
    var uri: Uri? = null, var rotation: Int = 0, var width: Int = 0,
    var height: Int = 0, var bitRate: Int = 0, var frameRate: Double = 0.0,
    var frameInterval: Long = 0, var duration: Long = 0, var expWidth: Int = 0,
    var expHeight: Int = 0, var cutPoint: Int = 0, var cutDuration: Int = 0
)