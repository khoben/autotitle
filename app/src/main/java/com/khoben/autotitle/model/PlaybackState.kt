package com.khoben.autotitle.model

import androidx.annotation.IntDef

@Target(AnnotationTarget.TYPE)
@IntDef(
    value = [PLAY,
        PAUSED,
        STOP,
        REWIND]
)
@Retention(AnnotationRetention.SOURCE)
/**
 * Playback state
 */
annotation class PlaybackState

const val PLAY = 0
const val PAUSED = 1
const val STOP = 2
const val REWIND = 3