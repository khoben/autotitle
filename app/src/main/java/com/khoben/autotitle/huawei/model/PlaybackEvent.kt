package com.khoben.autotitle.huawei.model

enum class PlaybackPlayer {
    COMPLETION,
    PREPARED
}

enum class PlaybackUIState {
    PLAY,
    PAUSED,
    TOGGLE,
    STOP,
    REWIND
}

data class PlaybackUI(var playState: PlaybackUIState, var currentPosition: Long = 0L)