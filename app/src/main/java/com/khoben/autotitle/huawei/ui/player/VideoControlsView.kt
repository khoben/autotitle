package com.khoben.autotitle.huawei.ui.player

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.khoben.autotitle.huawei.App
import com.khoben.autotitle.huawei.R
import com.khoben.autotitle.huawei.common.DisplayUtils
import com.khoben.autotitle.huawei.extension.toReadableTimeString
import com.khoben.autotitle.huawei.ui.overlay.OverlayText

class VideoControlsView(context: Context, attrs: AttributeSet) :
    RelativeLayout(context, attrs),
    VideoSeekBarView.SeekBarListener {

    var videoSeekBarView: VideoSeekBarView? = null
    var llPlayVideoView: LinearLayout? = null
    private var ivCenter: ImageView? = null
    private var viewWidth = 0
    private var viewHeight = 0
    private var screenWidth = 0

    private var playPauseButton: ImageView? = null
    private var avd: AnimatedVectorDrawableCompat? = null
    private var avd2: AnimatedVectorDrawable? = null

    private var rlCurrentLayout: RelativeLayout? = null
    private var tvTotalTime: TextView? = null
    private var tvCurrentTime: TextView? = null

    private lateinit var playIcon: Drawable
    private lateinit var pauseIcon: Drawable

    init {
        initView(context, attrs)
    }

    companion object {
        private val TAG = VideoControlsView::class.java.simpleName
    }

    private fun initView(context: Context, attrs: AttributeSet) {
        playIcon = ContextCompat.getDrawable(context, R.drawable.play_to_pause_btn)!!
        pauseIcon = ContextCompat.getDrawable(context, R.drawable.pause_to_play_btn)!!
        val resources = context.resources
        val dm = resources.displayMetrics
        screenWidth = dm.widthPixels
        rlCurrentLayout = LayoutInflater.from(context)
            .inflate(R.layout.current_total_time_layout, null) as RelativeLayout?
        val rlCurrentParams =
            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        addView(rlCurrentLayout, rlCurrentParams)
        tvTotalTime = rlCurrentLayout!!.findViewById<View>(R.id.tv_totalTime) as TextView
        tvCurrentTime = rlCurrentLayout!!.findViewById<View>(R.id.tv_currentTime) as TextView
        tvCurrentTime!!.text =
            context.getString(R.string.time_second_string, 0L.toReadableTimeString())
        videoSeekBarView = VideoSeekBarView(context, attrs)
        val videoEditParams =
            LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT)
        val params: ViewGroup.LayoutParams = LayoutParams(200, ViewGroup.LayoutParams.MATCH_PARENT)
        videoSeekBarView!!.layoutParams = params
        videoSeekBarView!!.seekBarListener = this
        videoEditParams.addRule(CENTER_VERTICAL, TRUE)
        addView(videoSeekBarView, videoEditParams)

        llPlayVideoView = LayoutInflater.from(context)
            .inflate(R.layout.play_pause_button_layout, null) as LinearLayout?
        val rlParams =
            LayoutParams(DisplayUtils.dipToPx(context, 60F), DisplayUtils.dipToPx(context, 60F))
        rlParams.addRule(CENTER_VERTICAL, TRUE)
        rlParams.addRule(ALIGN_PARENT_LEFT, TRUE)
        addView(llPlayVideoView, rlParams)

        ivCenter = ImageView(context)
        ivCenter!!.setImageResource(R.drawable.ic_vertical_line_rounded)
        val centerLineParams =
            LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT)
        centerLineParams.addRule(CENTER_IN_PARENT, TRUE)
        addView(ivCenter, centerLineParams)
        playPauseButton = findViewById(R.id.bigicon_play)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        viewWidth = videoSeekBarView!!.measuredWidth
        viewHeight = measuredHeight
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        videoSeekBarView!!.layout(screenWidth / 2, 0, screenWidth / 2 + viewWidth, viewHeight)
    }

    fun addFramesToSeekBar(bitmaps: List<Bitmap?>?) {
        if (bitmaps != null) {
            val width = screenWidth * bitmaps.size / App.FRAMES_PER_SCREEN
            val layoutParams: ViewGroup.LayoutParams =
                LayoutParams(width, ViewGroup.LayoutParams.MATCH_PARENT)
            videoSeekBarView!!.layoutParams = layoutParams
            videoSeekBarView!!.addFramesToSeekBar(bitmaps)
        }
    }

    fun addFramesToSeekBar(bitmaps: List<Bitmap?>?, frameTime: Long) {
        if (bitmaps != null) {
            val width = screenWidth * bitmaps.size / App.FRAMES_PER_SCREEN
            val layoutParams: ViewGroup.LayoutParams =
                LayoutParams(width, ViewGroup.LayoutParams.MATCH_PARENT)
            videoSeekBarView!!.layoutParams = layoutParams
            videoSeekBarView!!.addFramesToSeekBar(bitmaps, frameTime)
        }
    }

    private var lastPlayPauseButtonState = false
    private fun togglePlayPauseButton(isVideoPlaying: Boolean) {
        if (lastPlayPauseButtonState == isVideoPlaying) return
        lastPlayPauseButtonState = isVideoPlaying
        if (isVideoPlaying) {
            playPauseButton!!.setImageDrawable(playIcon)
            val drawable = playPauseButton!!.drawable
            if (drawable is AnimatedVectorDrawableCompat) {
                avd = drawable
                avd!!.start()
            } else if (drawable is AnimatedVectorDrawable) {
                avd2 = drawable
                avd2!!.start()
            }
        } else {
            playPauseButton!!.setImageDrawable(pauseIcon)
            val drawable = playPauseButton!!.drawable
            if (drawable is AnimatedVectorDrawableCompat) {
                avd = drawable
                avd!!.start()
            } else if (drawable is AnimatedVectorDrawable) {
                avd2 = drawable
                avd2!!.start()
            }
        }
    }

    fun setMediaDuration(totalTime: Long) {
        tvTotalTime?.text =
            context.getString(R.string.time_second_string, totalTime.toReadableTimeString())
        videoSeekBarView?.setMediaDuration(totalTime)
    }

    fun videoPlay(baseImageViews: List<OverlayText>?, isVideoPlaying: Boolean) {
        togglePlayPauseButton(isVideoPlaying)
        videoSeekBarView!!.playingTimeRange(isVideoPlaying, baseImageViews)
    }

    fun drawOverlayTimeRange(
        overlays: List<OverlayText>?,
        selectedOverlay: OverlayText?,
        isEdit: Boolean
    ) {
        videoSeekBarView?.drawOverlaysTimeRange(overlays, selectedOverlay, isEdit)
    }

    fun setToDefaultState(saveCurrentTime: Boolean = false) {
//        playPauseButton!!.setImageDrawable(pauseIcon)
        tvCurrentTime!!.text = 0L.toReadableTimeString()
        togglePlayPauseButton(false)
        videoSeekBarView?.setToDefaultState(saveCurrentTime)
    }

    fun setToState(pos: Long) {
        tvCurrentTime!!.text = pos.toReadableTimeString()
        togglePlayPauseButton(false)
        videoSeekBarView?.setToState(pos)
    }

    var seekBarListener: VideoSeekBarView.SeekBarListener? = null
    override fun changeTimeRangeSelectedOverlay(startTime: Long, endTime: Long) {
        seekBarListener?.changeTimeRangeSelectedOverlay(startTime, endTime)
    }

    override fun seekBarRewind(currentTime: Long) {
        tvCurrentTime!!.text = currentTime.toReadableTimeString()
        togglePlayPauseButton(false)
//        playPauseButton!!.setImageDrawable(playIcon)
        seekBarListener?.seekBarRewind(currentTime)
    }

    override fun seekBarOnTouch() {
        seekBarListener?.seekBarOnTouch()
    }

    override fun seekBarOnDoubleTap() {
        seekBarListener?.seekBarOnDoubleTap()
    }

    override fun updateVideoPositionWithSeekBar(time: Long) {
        tvCurrentTime!!.text =
            context.getString(R.string.time_second_string, time.toReadableTimeString())
        seekBarListener?.updateVideoPositionWithSeekBar(time)
    }
}
