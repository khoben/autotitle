package com.khoben.autotitle.huawei.ui.player.seekbar

interface SeekBarListener {
    fun updateVideoPositionWithSeekBar(time: Long)
    fun changeTimeRangeSelectedOverlay(startTime: Long, endTime: Long)
    fun seekBarRewind(currentTime: Long)
    fun seekBarOnTouch()
    fun seekBarOnDoubleTap()
}