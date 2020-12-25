package com.khoben.autotitle.ui.player.seekbar

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import com.iammert.rangeview.library.RangeView
import com.khoben.autotitle.App
import com.khoben.autotitle.App.Companion.FRAMES_PER_SCREEN
import com.khoben.autotitle.R
import com.khoben.autotitle.common.DisplayUtils
import com.khoben.autotitle.ui.overlay.OverlayText
import kotlin.math.max
import kotlin.math.min


class VideoSeekBarView(
    context: Context,
    attrs: AttributeSet?
) : RelativeLayout(context, attrs) {

    private var maxScrollWidth = 0
    private var minScrollWidth = 0

    private var screenWidth = 0

    private var rangeView: RangeView? = null

    private val overlayTimeRangeBackgrounds = mutableListOf<View?>()

    private var videoSeekBarViewWidth = 0

    private var frameTimeInMs = 0L
    private var videoDuration = 0L

    private var minSelectTimeWidth = 0F

    private var progressPlaybackAnimator: ValueAnimator? = null
    private var playbackState = false

    private val timeRangeBackgroundColor =
        ContextCompat.getColor(context, R.color.timelineBackgroundColor)

    private var imageList: LinearLayout? = null

    var seekBarListener: SeekBarListener? = null

    init {
        initView(context, attrs)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(imageList!!.measuredWidth, heightMeasureSpec)
        minScrollWidth = screenWidth / 2 - measuredWidth
        maxScrollWidth = screenWidth / 2
        videoSeekBarViewWidth = measuredWidth
    }

    private fun xCoordToTimestamp(xCoordinate: Float): Long {
        return (videoDuration * (screenWidth / 2F - x) / measuredWidth).toLong()
    }

    private fun timestampToXCoord(timestamp: Long): Float {
        return screenWidth / 2F - timestamp * measuredWidth.toFloat() / videoDuration
    }

    private var onTouchEventMotionStartX = 0f

    /**
     * Current playback position control
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        super.onTouchEvent(event)
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                onTouchEventMotionStartX = event.x
                seekBarListener?.seekBarOnTouch()
            }
            MotionEvent.ACTION_MOVE -> {
                val endX = event.x
                val distanceX = endX - onTouchEventMotionStartX
                x = max(minScrollWidth.toFloat(), min(x + distanceX, maxScrollWidth.toFloat()))
                val currentPlaybackTime = xCoordToTimestamp(x)
                rewind(currentPlaybackTime)
            }
        }
        return true
    }

    /**
     * Adds frames to seekbar
     * @param bitmaps
     * @param frameTime
     */
    fun addFramesToSeekBar(bitmaps: List<Bitmap?>, frameTime: Long) {
        if (imageList!!.childCount > 0) {
            Log.e(TAG, "Frames added already")
            return
        }
        frameTimeInMs = frameTime
        minSelectTimeWidth =
            (videoSeekBarViewWidth * frameTimeInMs / videoDuration + DisplayUtils.dipToPx(10)).toFloat()
        addFramesToSeekBar(bitmaps)
    }

    private fun addFramesToSeekBar(bitmaps: List<Bitmap?>) {
        Log.d(TAG, "addFrames")
        val oneFrameWidthPx = screenWidth / FRAMES_PER_SCREEN
        imageList!!.layoutParams = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT
        ).apply {
            addRule(CENTER_VERTICAL, TRUE)
        }
        bitmaps.forEach { bitmap ->
            val imageView = ImageView(context).apply {
                layoutParams = LayoutParams(oneFrameWidthPx, App.SEEKBAR_HEIGHT_DP_PIXELS)
                scaleType = ImageView.ScaleType.CENTER_CROP
                setImageBitmap(bitmap)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    isForceDarkAllowed = false
                }
            }
            imageList!!.addView(imageView)
        }

        measure(0, 0)

        if (rangeView == null) {
            // adds range view selectors to layout
            addRangeView(oneFrameWidthPx * bitmaps.size, App.SEEKBAR_HEIGHT_DP_PIXELS)
        }

    }

    private fun addRangeView(w: Int, h: Int) {
        rangeView = LayoutInflater.from(context)
            .inflate(R.layout.range_view, null) as RangeView

        addView(
            rangeView,
            LayoutParams(w, h).apply {
                addRule(CENTER_VERTICAL, TRUE)
            }
        )

        rangeView!!.setMinValue(0F)
        rangeView!!.setMaxValue(1F)

        rangeView!!.rangeValueChangeListener = object : RangeView.OnRangeValueListener {
            override fun rangeChanged(
                maxValue: Float,
                minValue: Float,
                currentLeftValue: Float,
                currentRightValue: Float
            ) {
                val leftTime = (currentLeftValue * videoDuration).toLong()
                val rightTime = (currentRightValue * videoDuration).toLong()

                seekBarListener?.changeTimeRangeSelectedOverlay(
                    leftTime,
                    rightTime
                )
            }
        }
        rangeView!!.visibility = GONE
    }

    private fun rewind(currentTime: Long) {
        seekBarListener?.seekBarRewind(currentTime)
        seekBarListener?.updateVideoPositionWithSeekBar(currentTime)
    }

    private fun startSeekBarAnimation(width: Float, duration: Long) {
        stopSeekBarAnimation()
        val animationEndValue = x - width
        progressPlaybackAnimator = ValueAnimator.ofFloat(0f, -width)
        var lastAnimatedValue = 0f
        progressPlaybackAnimator!!.addUpdateListener { valueAnimator: ValueAnimator ->
            val value = valueAnimator.animatedValue as Float
            val diff = value - lastAnimatedValue
            lastAnimatedValue = value
            val currentPlaybackTime = xCoordToTimestamp(x)
            if (!playbackState) { // paused
                Log.d(TAG, "Paused")
                stopSeekBarAnimation()
            }else if (playbackState && currentPlaybackTime < videoDuration) { // playing
                x += diff
                seekBarListener?.updateVideoPositionWithSeekBar(currentPlaybackTime)
                if (x + diff <= animationEndValue) { // completed by animationEndValue
                    stopSeekBarAnimation()
                    seekBarListener?.seekBarCompletePlaying()
                    Log.d(TAG, "Complete")
                }
            } else if (currentPlaybackTime >= videoDuration) { // completed
                stopSeekBarAnimation()
                seekBarListener?.seekBarCompletePlaying()
                Log.d(TAG, "Complete")
            }
            invalidate()
        }

        progressPlaybackAnimator!!.apply {
            interpolator = LinearInterpolator()
            this.duration = duration
        }
        post {
            progressPlaybackAnimator?.start()
        }
    }

    private fun stopSeekBarAnimation() {
        progressPlaybackAnimator?.cancel()
        progressPlaybackAnimator = null
    }

    /**
     * Sets UI controls to desired timestamp
     *
     * @param pos Timestamp
     */
    fun setToState(pos: Long) {
        stopSeekBarAnimation()
        // convert playback time to x coordinate
        x = timestampToXCoord(pos)
        rewind(pos)
    }

    /**
     * Set video duration time
     *
     * @param totalTime Video duration time in milliseconds
     */
    fun setMediaDuration(totalTime: Long) {
        this.videoDuration = totalTime
    }

    /**
     * Update playback state
     *
     * @param overlays List of [OverlayText]
     * @param selectedOverlay Currently selected [OverlayText]
     * @param isPlaying Is playing
     */
    fun updatePlayback(
        overlays: List<OverlayText?>,
        selectedOverlay: OverlayText?,
        isPlaying: Boolean
    ) {
        playbackState = isPlaying

        if (playbackState) {
            //  rangeView?.visibility = GONE
            startSeekBarAnimation(width = measuredWidth.toFloat(), duration = videoDuration)
        }

        rangeView?.toggleAccessibility(!isPlaying)

        overlayTimeRangeBackgrounds.forEach { removeView(it) }
        if (overlays.isNotEmpty()) {
            overlayTimeRangeBackgrounds.clear()
            if (selectedOverlay != null) {
                rangeView?.setCurrentValues(
                    selectedOverlay.startTime.toFloat() / videoDuration,
                    selectedOverlay.endTime.toFloat() / videoDuration
                )
                rangeView?.visibility = VISIBLE
            } else {
                rangeView?.visibility = GONE
            }
            for (overlay in overlays) {
                if (overlay == selectedOverlay) continue
                // show time ranges for all provided overlays
                val timeRangeStartX = overlay!!.startTime *
                        videoSeekBarViewWidth / videoDuration
                val timeRangeEndX = overlay.endTime *
                        videoSeekBarViewWidth / videoDuration
                val timeRangeWidth = (timeRangeEndX - timeRangeStartX).toInt()

                val timeRangeBackground = LinearLayout(context).apply {
                    x = timeRangeStartX.toFloat()
                    setBackgroundColor(timeRangeBackgroundColor)
                }

                overlayTimeRangeBackgrounds.add(timeRangeBackground)

                addView(
                    timeRangeBackground,
                    LayoutParams(timeRangeWidth, App.SEEKBAR_HEIGHT_DP_PIXELS).apply {
                        addRule(CENTER_VERTICAL, TRUE)
                    }
                )
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initView(context: Context, attrs: AttributeSet?) {

        screenWidth = context.resources.displayMetrics.widthPixels
        minSelectTimeWidth = screenWidth.toFloat() / FRAMES_PER_SCREEN + DisplayUtils.dipToPx(10)

        /***************Frames list*******************/
        imageList = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        addView(
            imageList,
            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        )
        /*********************************************/

        rangeView?.visibility = GONE
    }

    companion object {
        private val TAG = VideoSeekBarView::class.java.simpleName
    }
}
