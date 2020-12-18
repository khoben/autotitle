package com.khoben.autotitle.huawei.model

data class PlaybackEvent(val playState: PlaybackState, val currentPosition: Long = 0L)