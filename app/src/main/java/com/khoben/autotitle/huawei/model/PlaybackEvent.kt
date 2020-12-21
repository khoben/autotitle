package com.khoben.autotitle.huawei.model

/**
 * Playback event for syncing components depending on current playback state
 *
 * @property playState current [PlaybackState]
 * @property currentPosition current position of playback
 */
data class PlaybackEvent(val playState: PlaybackState, val currentPosition: Long = 0L)