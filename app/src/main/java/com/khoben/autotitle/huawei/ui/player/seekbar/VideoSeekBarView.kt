package com.khoben.autotitle.huawei.ui.player.seekbar

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.iammert.rangeview.library.RangeView
import com.khoben.autotitle.huawei.App
import com.khoben.autotitle.huawei.App.Companion.FRAMES_PER_SCREEN
import com.khoben.autotitle.huawei.R
import com.khoben.autotitle.huawei.common.DisplayUtils
import com.khoben.autotitle.huawei.ui.overlay.OverlayText


class VideoSeekBarView(
    context: Context,
    attrs: AttributeSet?
) : RelativeLayout(context, attrs) {

    private var maxScrollWidth = 0
    private var minScrollWidth = 0

    private var screenWidth = 0

    private var rangeView: RangeView? = null

    private var overlayTimeRangeLayout: RelativeLayout? = null
    private var overlayTimeRangeLayoutParam: LayoutParams? = null
    private val overlayTimeRangeBackgrounds = mutableListOf<View?>()

    private var videoSeekBarViewWidth = 0

    private var frameTimeInMs = 0L
    private var currentPlaybackTime = 0L
    private var videoDuration = 0L

    private var minSelectTimeWidth = 0F

    private var progressPlaybackAnimator: ValueAnimator? = null
    private var playbackState = false

    // gray color
    private val timeRangeBackgroundColor = Color.parseColor("#7f000000")

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

    private var startX = 0f
    private var lastTimeClicked = 0L

    /**
     * Current playback position control
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        super.onTouchEvent(event)
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val clickTime = System.currentTimeMillis()
                startX = event.x
                playbackState = false
                seekBarListener?.seekBarOnTouch()
                // TODO("Constant")
                if (clickTime - lastTimeClicked < 300L) {
                    seekBarListener?.seekBarOnDoubleTap()
                }
                lastTimeClicked = clickTime
            }
            MotionEvent.ACTION_MOVE -> {
                val endX = event.x
                val distanceX = endX - startX

                var toX = x + distanceX
                if (toX < minScrollWidth) {
                    toX = minScrollWidth.toFloat()
                }
                if (toX > maxScrollWidth) {
                    toX = maxScrollWidth.toFloat()
                }
                x = toX
                currentPlaybackTime =
                    (videoDuration * (screenWidth / 2 - x) / measuredWidth).toLong()
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
        this.frameTimeInMs = frameTime
        addFramesToSeekBar(bitmaps)
    }

    private fun addFramesToSeekBar(bitmaps: List<Bitmap?>) {
        val imageWidth = screenWidth / FRAMES_PER_SCREEN
        val layoutParams: ViewGroup.LayoutParams = LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        imageList!!.layoutParams = layoutParams
        for (bitmap in bitmaps) {
            val imageView = ImageView(context)
            val params = LayoutParams(imageWidth, App.SEEKBAR_HEIGHT_DP_PIXELS)
            imageView.layoutParams = params
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            imageView.setImageBitmap(bitmap)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                imageView.isForceDarkAllowed = false
            }
            imageList!!.addView(imageView)
        }

        if (rangeView == null) {
            // adds range view selectors to layout
            addRangeView(imageWidth * bitmaps.size, App.SEEKBAR_HEIGHT_DP_PIXELS)
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
                val l = if (currentLeftValue < MIN_ROUND_RANGEVIEW_VAL) 0f
                else currentLeftValue
                val r = if (currentRightValue > MAX_ROUND_RANGEVIEW_VAL) 1f
                else currentRightValue

                seekBarListener?.changeTimeRangeSelectedOverlay(
                    (l * videoDuration).toLong(),
                    (r * videoDuration).toLong()
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
        progressPlaybackAnimator = ValueAnimator.ofFloat(0f, -width)
        var lastAnimatedValue = 0f
        progressPlaybackAnimator!!.addUpdateListener { valueAnimator: ValueAnimator ->
            val value = valueAnimator.animatedValue as Float
            val diff = value - lastAnimatedValue
            lastAnimatedValue = value
            if (x >= minScrollWidth && playbackState) {
                currentPlaybackTime =
                    (videoDuration * (screenWidth / 2 - x) / measuredWidth).toLong()
                seekBarListener?.updateVideoPositionWithSeekBar(currentPlaybackTime)
                x += diff
                // at the end of time
                if (x + diff < minScrollWidth) {
                    Log.d(TAG, "seekBarAnimation end")
                    stopSeekBarAnimation()
                    x = maxScrollWidth.toFloat()
                    seekBarListener?.seekBarRewind(0L)
                }
            } else {
                stopSeekBarAnimation()
            }
            invalidate()
        }
        progressPlaybackAnimator!!.interpolator = LinearInterpolator()
        progressPlaybackAnimator!!.duration = duration
        post {
            progressPlaybackAnimator?.start()
        }
    }

    private fun stopSeekBarAnimation() {
        progressPlaybackAnimator?.cancel()
        progressPlaybackAnimator = null
    }

    /**
     * Draws background for each overlay time range
     * @param playState Boolean
     * @param overlays List<OverlayObject?>?
     */
    fun playingTimeRange(playState: Boolean, overlays: List<OverlayText?>?) {
        this.playbackState = playState
        if (playState) {
            overlayTimeRangeLayout!!.visibility = GONE
            rangeView?.visibility = GONE

            overlayTimeRangeBackgrounds.forEach { removeView(it) }

            if (overlays != null && overlays.isNotEmpty()) {
                overlayTimeRangeBackgrounds.clear()
                for (overlay in overlays) {
                    val startX = overlay!!.startTime * videoSeekBarViewWidth / videoDuration
                    val endX = overlay.endTime * videoSeekBarViewWidth / videoDuration
                    val width = (endX - startX).toInt()

                    val timeRangeBackground = LinearLayout(context).apply {
                        x = startX.toFloat()
                        setBackgroundColor(timeRangeBackgroundColor)
                    }

                    addView(
                        timeRangeBackground,
                        LayoutParams(width, App.SEEKBAR_HEIGHT_DP_PIXELS).apply {
                            addRule(CENTER_VERTICAL, TRUE)
                        }
                    )
                    overlayTimeRangeBackgrounds.add(timeRangeBackground)
                }
            }
        }
        startSeekBarAnimation(measuredWidth.toFloat(), videoDuration)
    }

    fun setToState(pos: Long) {
        stopSeekBarAnimation()
        currentPlaybackTime = pos
        x = screenWidth / 2F - pos * measuredWidth.toFloat() / videoDuration
        rewind(pos)
    }

    fun setToDefaultState(saveCurrentTime: Boolean) {
        Log.d(TAG, "setToDefaultState")
        rangeView?.visibility = GONE
//        overlayTimeRangeBackgrounds!!.visibility = GONE
        stopSeekBarAnimation()
        x = maxScrollWidth.toFloat()
        seekBarListener?.seekBarRewind(0)
    }

    fun setMediaDuration(totalTime: Long) {
        this.videoDuration = totalTime
    }

    fun drawOverlaysTimeRange(
        overlays: List<OverlayText?>?,
        selectedOverlay: OverlayText?,
        isEdit: Boolean
    ) {
        overlayTimeRangeBackgrounds.forEach { removeView(it) }
        if (overlays != null && overlays.isNotEmpty()) {
            overlayTimeRangeBackgrounds.clear()
            if (selectedOverlay != null && isEdit) {
                rangeView?.setCurrentValues(
                    selectedOverlay.startTime.toFloat() / videoDuration,
                    selectedOverlay.endTime.toFloat() / videoDuration
                )
            }
            for (overlay in overlays) {
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
        removeView(overlayTimeRangeLayout)
        addView(overlayTimeRangeLayout, overlayTimeRangeLayoutParam)
        if (!isEdit) {
            minSelectTimeWidth =
                (videoSeekBarViewWidth * frameTimeInMs / videoDuration + DisplayUtils.dipToPx(10)).toFloat()
        }
        if (overlays!!.indexOf(selectedOverlay) == -1) {
            overlayTimeRangeLayout!!.visibility = GONE
            rangeView?.visibility = GONE
        } else {
            overlayTimeRangeLayout!!.visibility = VISIBLE
            rangeView?.visibility = VISIBLE
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

        /**********Colored overlay******************/
        overlayTimeRangeLayoutParam =
            LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                .apply {
                    addRule(CENTER_VERTICAL, TRUE)
                }
        overlayTimeRangeLayout = RelativeLayout(context)
            .apply {
                setBackgroundColor(Color.parseColor("#3fff0000"))
            }
        addView(overlayTimeRangeLayout, overlayTimeRangeLayoutParam)
        /********************************************/

        overlayTimeRangeLayout!!.visibility = GONE
        rangeView?.visibility = GONE
    }

    companion object {
        private val TAG = VideoSeekBarView::class.java.simpleName
        private val MIN_ROUND_RANGEVIEW_VAL = 0.002f
        private val MAX_ROUND_RANGEVIEW_VAL = 0.996f
    }
}
