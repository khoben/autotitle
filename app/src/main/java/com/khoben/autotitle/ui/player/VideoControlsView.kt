package com.khoben.autotitle.ui.player

import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.khoben.autotitle.R
import com.khoben.autotitle.extension.formattedTime
import com.khoben.autotitle.ui.overlay.OverlayObject
import com.khoben.autotitle.ui.player.seekbar.FramesHolder
import com.khoben.autotitle.ui.player.seekbar.SeekBarListener
import com.khoben.autotitle.ui.player.seekbar.VideoSeekBarFramesView

class VideoControlsView(context: Context, attrs: AttributeSet) :
    ConstraintLayout(context, attrs),
    SeekBarListener {

    private lateinit var playPauseButton: PlayPauseMaterialButton
    private lateinit var videoSeekBarFramesView: VideoSeekBarFramesView

    private lateinit var currentVideoTime: TextView
    private lateinit var totalVideoTime: TextView

    // compute on onMeasure()
    private var viewWidth = 0
    private var viewHeight = 0

    override fun onFinishInflate() {
        super.onFinishInflate()
        /****Current and total video time****/
        currentVideoTime = findViewById(R.id.tv_currentTime)
        currentVideoTime.text = 0L.formattedTime()
        totalVideoTime = findViewById(R.id.tv_totalTime)
        /************************************/

        /*********SeekBar view*********/
        videoSeekBarFramesView = findViewById(R.id.video_seekbar_view_item)
        videoSeekBarFramesView.seekBarListener = this@VideoControlsView
        /*************************************/

        /**********Play/pause button**************/
        playPauseButton = findViewById(R.id.pp_btn)
        playPauseButton.setOnClickListener { onPlayPauseButtonClicked() }
        /************************************/
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        viewWidth = videoSeekBarFramesView.measuredWidth
        viewHeight = measuredHeight
    }

    fun loadFrames(data: FramesHolder) {
        videoSeekBarFramesView.onFramesLoad(data)
    }

    /**
     * Set video duration time
     *
     * @param totalTime Video duration time in milliseconds
     */
    fun setMediaDuration(totalTime: Long) {
        totalVideoTime.text = totalTime.formattedTime()
        videoSeekBarFramesView.setMediaDuration(totalTime)
    }

    /**
     * Update playback state
     *
     * @param overlays List of [OverlayObject]
     * @param selectedOverlay Currently selected [OverlayObject]
     * @param isPlaying Is playing
     */
    fun updatePlayback(
        overlays: List<OverlayObject>,
        selectedOverlay: OverlayObject?,
        isPlaying: Boolean
    ) {
        playPauseButton.toggle(isPlaying)
        videoSeekBarFramesView.updatePlayback(overlays, selectedOverlay, isPlaying)
    }

    /**
     * Sets UI controls to desired timestamp
     *
     * @param timestamp Timestamp
     */
    fun setControlsToTime(timestamp: Long) {
        currentVideoTime.text = timestamp.formattedTime()
        playPauseButton.toggle(false)
        videoSeekBarFramesView.setControlsToTime(timestamp)
    }

    var seekBarListener: SeekBarListener? = null
    override fun changeTimeRangeSelectedOverlay(startTime: Long, endTime: Long) {
        seekBarListener?.changeTimeRangeSelectedOverlay(startTime, endTime)
    }

    override fun syncCurrentPlaybackTimeWithSeekBar(time: Long, isSeeking: Boolean) {
        currentVideoTime.text = time.formattedTime()
        seekBarListener?.syncCurrentPlaybackTimeWithSeekBar(time, isSeeking)
    }

    override fun seekBarCompletePlaying() {
        seekBarListener?.seekBarCompletePlaying()
    }

    override fun seekBarOnTouch() {
        seekBarListener?.seekBarOnTouch()
    }

    override fun seekBarOnDoubleTap() {
        seekBarListener?.seekBarOnDoubleTap()
    }

    override fun onPlayPauseButtonClicked() {
        seekBarListener?.onPlayPauseButtonClicked()
    }
}
