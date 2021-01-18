package com.khoben.autotitle.ui.player.seekbar

interface SeekBarListener {
    /**
     * Syncing playback with SeekBar
     *
     * @param time Timestamp, in ms
     * @param isSeeking Indicates if it was seeking
     */
    fun syncCurrentPlaybackTimeWithSeekBar(time: Long, isSeeking: Boolean = false)

    /**
     * Changes start and end time for current selected overlay
     *
     * @param startTime Start timestamp, in ms
     * @param endTime   End timestamp, in ms
     */
    fun changeTimeRangeSelectedOverlay(startTime: Long, endTime: Long)

    /**
     * SeekBar completed playing
     */
    fun seekBarCompletePlaying()

    /**
     * SeekBar has been touched
     */
    fun seekBarOnTouch()

    /**
     * SeekBar has been double-clicked
     */
    fun seekBarOnDoubleTap()
}