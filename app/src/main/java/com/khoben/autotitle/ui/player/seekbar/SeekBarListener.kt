package com.khoben.autotitle.ui.player.seekbar

interface SeekBarListener {
    fun updateVideoPositionWithSeekBar(time: Long)
    fun changeTimeRangeSelectedOverlay(startTime: Long, endTime: Long)
    fun seekBarRewind(currentTime: Long)
    fun seekBarCompletePlaying()
    fun seekBarOnTouch()
    fun seekBarOnDoubleTap()
}