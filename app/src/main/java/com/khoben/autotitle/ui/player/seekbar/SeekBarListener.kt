package com.khoben.autotitle.ui.player.seekbar

interface SeekBarListener {
    fun syncCurrentPlaybackTimeWithSeekBar(time: Long, isSeeking: Boolean = false)
    fun changeTimeRangeSelectedOverlay(startTime: Long, endTime: Long)
    fun seekBarCompletePlaying()
    fun seekBarOnTouch()
    fun seekBarOnDoubleTap()
    fun onPlayPauseButtonClicked()
}