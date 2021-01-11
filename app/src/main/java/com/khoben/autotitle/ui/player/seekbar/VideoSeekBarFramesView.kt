package com.khoben.autotitle.ui.player.seekbar

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.util.AttributeSet
import android.view.*
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import com.iammert.rangeview.library.DraggingState
import com.iammert.rangeview.library.RangeView
import com.khoben.autotitle.App
import com.khoben.autotitle.App.Companion.FRAMES_PER_SCREEN
import com.khoben.autotitle.R
import com.khoben.autotitle.extension.dp
import com.khoben.autotitle.ui.overlay.OverlayObject
import com.khoben.autotitle.ui.overlay.OverlayText
import timber.log.Timber
import java.util.*
import kotlin.math.max
import kotlin.math.min


class VideoSeekBarFramesView(
        context: Context,
        attrs: AttributeSet?
) : FrameLayout(context, attrs) {
    private var rangeView: RangeView? = null

    private val overlayTimeRangeBackgrounds = mutableMapOf<UUID, View?>()
    private var existingOverlaysBackground = mutableMapOf<UUID, View?>()

    private var videoSeekBarViewWidth = 0
    private var maxScrollWidth = 0
    private var minScrollWidth = 0

    private var screenWidth = context.resources.displayMetrics.widthPixels

    private var frameTimeInMs = 0L
    private var videoDuration = 0L

    private lateinit var movableFrameLineContainer: FrameLayout
    private var imageList: LinearLayout? = null
    private var progressPlaybackAnimator: ValueAnimator? = null
    private var playbackState = false


    var seekBarListener: SeekBarListener? = null

    init {
        initView(context, attrs)
    }

    override fun dispatchDraw(canvas: Canvas) {
        // clip left side before playPauseButton
        canvas.clipRect(
                16F.dp(),
                top.toFloat(),
                right.toFloat(),
                bottom.toFloat()
        )
        super.dispatchDraw(canvas)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        moveViewToCenterOfScreen(movableFrameLineContainer)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(imageList!!.measuredWidth, heightMeasureSpec)
        minScrollWidth = screenWidth / 2 - imageList!!.measuredWidth
        maxScrollWidth = screenWidth / 2
        videoSeekBarViewWidth = measuredWidth
    }

    private fun xCoordinateToTimestamp(xCoordinate: Float): Long {
        // assume that start (0, 0) from center of screen
        // width from start to center screen   // duration per width
        return ((screenWidth / 2F - xCoordinate) * (videoDuration.toFloat() / measuredWidth)).toLong()
    }

    private fun timestampToXCoordinate(timestamp: Long): Float {
        return screenWidth / 2F - timestamp * measuredWidth.toFloat() / videoDuration
    }

    /**
     * Adds frames to seekbar
     * @param bitmaps
     * @param frameTime
     */
    fun addFramesToSeekBar(bitmaps: List<Bitmap?>, frameTime: Long) {
        if (imageList!!.childCount > 0) {
            Timber.e("Frames added already")
            return
        }
        frameTimeInMs = frameTime
        addFramesToSeekBar(bitmaps)
    }

    private fun addFramesToSeekBar(bitmaps: List<Bitmap?>) {
        val singleFrameWidthPx = screenWidth / FRAMES_PER_SCREEN
        bitmaps.forEach { bitmap ->
            imageList!!.addView(
                    ImageView(context).apply {
                        layoutParams =
                                LayoutParams(singleFrameWidthPx, App.SEEKBAR_HEIGHT_DP_PIXELS)
                        scaleType = ImageView.ScaleType.CENTER_CROP
                        // keep original colors in dark mode
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                            isForceDarkAllowed = false
                        setImageBitmap(bitmap)
                    }
            )
        }

        // calc seekbar width
        measure(0, 0)

        if (rangeView == null) {
            // adds range view selectors to layout
            addRangeView(singleFrameWidthPx * bitmaps.size, App.SEEKBAR_HEIGHT_DP_PIXELS)
        }

    }

    private fun addRangeView(w: Int, h: Int) {
        rangeView = LayoutInflater.from(context)
                .inflate(R.layout.range_view, null) as RangeView

        movableFrameLineContainer.addView(
                rangeView,
                LayoutParams(w, h)
                        .apply {
                            gravity = Gravity.CENTER_VERTICAL
                        }
        )
        rangeView?.visibility = View.GONE

        rangeView!!.setMinValue(0F)
        rangeView!!.setMaxValue(1F)

        rangeView!!.rangeValueChangeListener = object : RangeView.OnRangeValueListener {
            override fun rangeChanged(
                    maxValue: Float,
                    minValue: Float,
                    currentLeftValue: Float,
                    currentRightValue: Float,
                    draggingStateData: DraggingState
            ) {
                val leftTime = (currentLeftValue * videoDuration).toLong()
                val rightTime = (currentRightValue * videoDuration).toLong()

                val currentTime = xCoordinateToTimestamp(movableFrameLineContainer.x)
                // TODO: Hardcoded constants
                val leftLimit =
                        currentTime + 500L - (screenWidth / 2 - 60.dp()) * videoDuration / videoSeekBarViewWidth
                val rightLimit =
                        currentTime - 500L + screenWidth / 2 * videoDuration / videoSeekBarViewWidth

                when (draggingStateData) {
                    DraggingState.DRAGGING_LEFT_TOGGLE -> {
                        if (leftTime <= leftLimit && leftLimit > 0L) movableFrameLineContainer.x =
                                timestampToXCoordinate(currentTime - (leftLimit - leftTime))
                    }
                    DraggingState.DRAGGING_RIGHT_TOGGLE -> {
                        if (rightTime >= rightLimit && rightLimit < videoDuration) movableFrameLineContainer.x = (timestampToXCoordinate(currentTime + (rightTime - rightLimit)))

                    }
                    else -> {
                    }
                }

                seekBarListener?.changeTimeRangeSelectedOverlay(
                        leftTime,
                        rightTime
                )
            }
        }
        rangeView!!.visibility = GONE
    }

    private fun seekTo(currentTime: Long) {
        seekBarListener?.syncCurrentPlaybackTimeWithSeekBar(currentTime, isSeeking = true)
    }

    private fun syncPlaybackTime(currentTime: Long) {
        seekBarListener?.syncCurrentPlaybackTimeWithSeekBar(currentTime, isSeeking = false)
    }

    private fun startSeekBarAnimation(width: Float, duration: Long) {
        stopSeekBarAnimation()
        val animationEndValue = movableFrameLineContainer.x - width
        var lastAnimatedValue = 0f
        progressPlaybackAnimator = ValueAnimator.ofFloat(0f, -width)
        progressPlaybackAnimator!!.addUpdateListener { valueAnimator: ValueAnimator ->
            val value = valueAnimator.animatedValue as Float
            val diff = value - lastAnimatedValue
            lastAnimatedValue = value
            val currentPlaybackTime = xCoordinateToTimestamp(movableFrameLineContainer.x)
            syncPlaybackTime(currentPlaybackTime)
            if (!playbackState) { // paused
                Timber.d("Paused")
                stopSeekBarAnimation()
            } else if (playbackState && currentPlaybackTime < videoDuration) { // playing
                movableFrameLineContainer.x += diff
                if (movableFrameLineContainer.x + diff <= animationEndValue) { // completed by animationEndValue
                    Timber.d("Completed")
                    stopSeekBarAnimation()
                    seekBarListener?.seekBarCompletePlaying()
                }
            } else if (currentPlaybackTime >= videoDuration) { // completed
                Timber.d("Completed")
                stopSeekBarAnimation()
                seekBarListener?.seekBarCompletePlaying()
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
     * Sets UI controls to desired [timestamp]
     *
     * @param timestamp Timestamp
     */
    fun setControlsToTime(timestamp: Long) {
        stopSeekBarAnimation()
        // convert playback time to x coordinate
        movableFrameLineContainer.x = timestampToXCoordinate(timestamp)
        seekTo(timestamp)
    }

    /**
     * Set video duration time
     *
     * @param totalTime Video duration time in milliseconds
     */
    fun setMediaDuration(totalTime: Long) {
        this.videoDuration = totalTime
    }

    private fun syncRangeViewWithPlayback(selectedOverlay: OverlayObject?) {
        if (selectedOverlay != null) {
            rangeView?.setCurrentValues(
                    selectedOverlay.startTime.toFloat() / videoDuration,
                    selectedOverlay.endTime.toFloat() / videoDuration
            )
            rangeView?.visibility = VISIBLE
        } else {
            rangeView?.visibility = GONE
        }
    }

    private fun syncOverlaysRangesWithPlayback(
            selectedOverlay: OverlayObject?,
            overlays: List<OverlayObject?>
    ) {
        existingOverlaysBackground = overlayTimeRangeBackgrounds.toMutableMap()
        for (overlay in overlays) {
            // show time ranges for all provided overlays

            val timeRangeStartX = overlay!!.startTime * videoSeekBarViewWidth / videoDuration
            val timeRangeEndX = overlay.endTime * videoSeekBarViewWidth / videoDuration
            val timeRangeWidth = (timeRangeEndX - timeRangeStartX).toInt()

            val backgroundOverlay: View?
            if (overlay is OverlayText) {
                backgroundOverlay = overlay.timeline
            } else {
                backgroundOverlay = null
            }

            if (backgroundOverlay != null && backgroundOverlay.parent != movableFrameLineContainer) { // attach view
                if (backgroundOverlay.parent != null) { // if has been detached from
                    (backgroundOverlay.parent as ViewGroup).removeView(backgroundOverlay)
                }
                backgroundOverlay.x = timeRangeStartX.toFloat()
                backgroundOverlay.setBackgroundColor((overlay as OverlayText).badgeColor)
                movableFrameLineContainer.addView(
                        backgroundOverlay,
                        LayoutParams(timeRangeWidth, 10.dp()).apply {
                            gravity = Gravity.CENTER_VERTICAL
                            setMargins(0, 0, 0, 35.dp())
                        }
                )
                overlayTimeRangeBackgrounds[overlay.uuid!!] = backgroundOverlay
            } else { // update existing

                existingOverlaysBackground.remove(overlay.uuid)

                backgroundOverlay?.x = timeRangeStartX.toFloat()
                backgroundOverlay?.layoutParams?.width = timeRangeWidth
            }
        }
        existingOverlaysBackground.forEach { (key, view) ->
            movableFrameLineContainer.removeView(view)
            overlayTimeRangeBackgrounds.remove(key)
        }
        existingOverlaysBackground.clear()
    }

    /**
     * Update playback state
     *
     * @param overlays List of [OverlayObject]
     * @param selectedOverlay Currently selected [OverlayObject]
     * @param isPlaying Is playing
     */
    fun updatePlayback(
            overlays: List<OverlayObject?>,
            selectedOverlay: OverlayObject?,
            isPlaying: Boolean
    ) {
        playbackState = isPlaying

        // disable touches on rangeView when playing
        rangeView?.toggleTouchAccessibility(!playbackState)

        if (playbackState) {
            startSeekBarAnimation(
                    width = movableFrameLineContainer.measuredWidth.toFloat(),
                    duration = videoDuration
            )
        }
        syncRangeViewWithPlayback(selectedOverlay)
        syncOverlaysRangesWithPlayback(selectedOverlay, overlays)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        // Dispatch LongPress event to rangeView
        if (rangeView?.getLongPressState() == true) {
            rangeView!!.dispatchTouchEvent(ev)
        }
        return super.onInterceptTouchEvent(ev)
    }

    /**
     * Long press gesture detector
     */
    private val gestureDetector = GestureDetector(
            context,
            object : GestureDetector.SimpleOnGestureListener() {
                override fun onDown(e: MotionEvent): Boolean {
                    rangeView?.setLongPressState(false, e)
                    return super.onDown(e)
                }

                override fun onSingleTapUp(e: MotionEvent): Boolean {
                    rangeView?.setLongPressState(false, e)
                    return false
                }

                override fun onLongPress(e: MotionEvent) {
                    rangeView?.setLongPressState(true, e)
                    super.onLongPress(e)
                }
            }
    )

    private fun seekTouchEvent(view: View, event: MotionEvent) {
        // detect long press
        gestureDetector.onTouchEvent(event)
        // we are moving rangeView with long pressed state
        if (rangeView?.getLongPressState() == true && rangeView?.visibility == View.VISIBLE) return
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                onTouchEventMotionStartX = event.x
                seekBarListener?.seekBarOnTouch()
            }
            MotionEvent.ACTION_MOVE -> {
                // x diff
                val toX = view.x + event.x - onTouchEventMotionStartX
                //   min  <= x <=  max
                view.x = max(minScrollWidth.toFloat(), min(toX, maxScrollWidth.toFloat()))
                seekTo(xCoordinateToTimestamp(view.x))
            }
            MotionEvent.ACTION_UP -> {
                // seek at the end of ACTION_MOVE
//                seekTo(xCoordinateToTimestamp(view.x))
            }
        }
    }

    private fun moveViewToCenterOfScreen(view: View) {
        view.layout(screenWidth / 2, 0, screenWidth / 2 + measuredWidth, measuredHeight)
    }

    private var onTouchEventMotionStartX = 0f

    @SuppressLint("ClickableViewAccessibility")
    private fun initView(context: Context, attrs: AttributeSet?) {

        /***************Frame line*******************/
        movableFrameLineContainer = FrameLayout(context).apply {
            setOnTouchListener { view, event ->
                seekTouchEvent(view, event)
                true
            }
        }

        addView(movableFrameLineContainer, LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER_VERTICAL
        })

        imageList = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        movableFrameLineContainer.addView(
                imageList,
                LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = Gravity.CENTER_VERTICAL
                }
        )
        moveViewToCenterOfScreen(movableFrameLineContainer)
        /*********************************************/
    }
}
