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
import androidx.core.view.get
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.FlingAnimation
import androidx.dynamicanimation.animation.FloatValueHolder
import com.iammert.rangeview.library.DraggingState
import com.iammert.rangeview.library.RangeView
import com.khoben.autotitle.App
import com.khoben.autotitle.App.Companion.FRAMES_PER_SCREEN
import com.khoben.autotitle.R
import com.khoben.autotitle.common.SharedPrefsHelper
import com.khoben.autotitle.extension.dp
import com.khoben.autotitle.ui.overlay.OverlayObject
import com.khoben.autotitle.ui.overlay.OverlayText
import timber.log.Timber
import java.util.*
import kotlin.math.abs


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
     * Async adding frames to seekbar
     *
     * @param frames FramesHolder
     */
    fun onFramesLoad(frames: FramesHolder) {
        Timber.d("$frames")
        when (frames.status) {
            FrameStatus.PRELOAD -> {
                if (frames.emptyFramesCount != null && frames.frameTime != null) {
                    preloadFrames(frames.emptyFramesCount!!, frames.frameTime)
                }
            }
            FrameStatus.LOAD_SINGLE -> {
                frames.singleFrame?.let { loadSingleFrame(it) }
            }
            FrameStatus.COMPLETED -> {
                frames.listFrames?.let { loadAllFrames(it) }
            }
        }
    }

    private fun preloadFrames(amountFrames: Long, frameTime: Long) {
        imageList!!.removeAllViews()
        this.frameTimeInMs = frameTime
        val singleFrameWidthPx = screenWidth / FRAMES_PER_SCREEN
        repeat(amountFrames.toInt()) {
            imageList!!.addView(
                FrameImageView(context).apply {
                    layoutParams =
                        LayoutParams(singleFrameWidthPx, App.SEEKBAR_HEIGHT_DP_PIXELS)
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    // keep original colors in dark mode
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                        isForceDarkAllowed = false
                }
            )
        }
        // update layout size
        measure(0, 0)

        // adds range view selectors to layout
        if (rangeView == null) {
            addRangeView((singleFrameWidthPx * amountFrames).toInt(), App.SEEKBAR_HEIGHT_DP_PIXELS)
        }
    }

    private var curLoadedSingleFrame = 0
    private fun loadSingleFrame(frame: Bitmap) {
        if (curLoadedSingleFrame >= imageList!!.childCount) return
        val frameContainer = imageList!![curLoadedSingleFrame]
        if (frameContainer is FrameImageView) {
            frameContainer.setImageBitmap(frame)
            frameContainer.postInvalidate()
            curLoadedSingleFrame++
        } else {
            throw RuntimeException("For frames line allowed only FrameImageView as child")
        }
    }

    private fun loadAllFrames(frames: List<Bitmap>) {
        if (imageList!!.childCount != 0) return
        val singleFrameWidthPx = screenWidth / FRAMES_PER_SCREEN
        frames.forEach { bitmap ->
            imageList!!.addView(
                FrameImageView(context).apply {
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

        // update layout size
        measure(0, 0)

        if (rangeView == null) {
            // adds range view selectors to layout
            addRangeView(singleFrameWidthPx * frames.size, App.SEEKBAR_HEIGHT_DP_PIXELS)
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
                        if (rightTime >= rightLimit && rightLimit < videoDuration) movableFrameLineContainer.x =
                            (timestampToXCoordinate(currentTime + (rightTime - rightLimit)))

                    }
                    else -> {
                    }
                }

                seekBarListener?.changeTimeRangeSelectedOverlay(leftTime, rightTime)
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

            val backgroundOverlay = if (overlay is OverlayText) { overlay.timeline } else { null }

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
                        setMargins(0, 0, 0, (movableFrameLineContainer.height / 2F).toInt())
                    }
                )
                overlayTimeRangeBackgrounds[overlay.uuid] = backgroundOverlay
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

        Timber.d("updatePlayback $selectedOverlay")

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

    private val FRICTION = 1.1F
    private val VELOCITY_UNITS = 1000
    private val VELOCITY_MAX = 3000
    private val VELOCITY_MIN = 500
    private var direction = 0f
    private var downX = 0f
    private var xVelocity = 0f
    private var velocityTracker: VelocityTracker? = null
    private var xFling: FlingAnimation? = null
    private var lastActionX = 0f

    private fun seekTouchEvent(view: View, event: MotionEvent) {
        // detect long press
        gestureDetector.onTouchEvent(event)
        // we are moving rangeView with long pressed state
        if (rangeView?.getLongPressState() == true && rangeView?.visibility == View.VISIBLE) return

        when (event.action) {
            MotionEvent.ACTION_DOWN -> onActionDown(event)
            MotionEvent.ACTION_MOVE -> onActionMove(event)
            MotionEvent.ACTION_UP -> onActionUp(event)
            MotionEvent.ACTION_CANCEL -> onActionCancel()
        }
    }

    private fun onActionDown(event: MotionEvent) {
        Timber.d("DOWN")
        // stop animation
        xFling?.cancel()
        xFling = null
        xVelocity = 0F

        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain()
        } else {
            velocityTracker?.clear()
        }
        downX = event.rawX
        lastActionX = event.x
        seekBarListener?.seekBarOnTouch()
        event.offsetLocation(event.rawX - downX, 0F)
        velocityTracker?.addMovement(event)
    }

    private fun onActionMove(event: MotionEvent) {
        Timber.d("MOVE")
        val deltaX = event.x - lastActionX
        updatePosition(movableFrameLineContainer.x + deltaX)
        event.offsetLocation(event.rawX - downX, 0F)
        velocityTracker?.addMovement(event)
    }

    private fun onActionUp(event: MotionEvent) {
        Timber.d("UP")
        velocityTracker?.let {
            event.offsetLocation(event.rawX - downX, 0F)
            velocityTracker?.addMovement(event)
            it.computeCurrentVelocity(VELOCITY_UNITS, VELOCITY_MAX.toFloat())
            xVelocity = it.xVelocity
            direction = event.rawX - downX
            Timber.d("Direction = $direction velocity = $xVelocity")
            // Limit velocity by xDiff
            if (abs(xVelocity) > abs(10 * direction)) {
                xVelocity = 10 * direction
            }
        }

        velocityTracker?.recycle()
        velocityTracker = null
        if (abs(xVelocity) > VELOCITY_MIN && SharedPrefsHelper.seekBarSmoothAnimation == true) {
            startXAnimation()
        } else {
            seekTo(xCoordinateToTimestamp(movableFrameLineContainer.x))
        }
        xVelocity = 0F
    }

    private fun onActionCancel() {
        Timber.d("CANCEL")
        xVelocity = 0F
        velocityTracker?.recycle()
        velocityTracker = null
    }

    private fun startXAnimation() {
        xFling = FlingAnimation(FloatValueHolder(movableFrameLineContainer.x))
            .setStartVelocity(xVelocity)
            .setMaxValue(maxScrollWidth.toFloat())
            .setMinValue(minScrollWidth.toFloat())
            .setMinimumVisibleChange(DynamicAnimation.MIN_VISIBLE_CHANGE_PIXELS)
            .setFriction(FRICTION)
            .apply {
                addUpdateListener(xAnimationUpdate)
                addEndListener(xAnimationEnd)
                start()
            }
    }

    private val xAnimationUpdate = DynamicAnimation.OnAnimationUpdateListener { _, newX, _ ->
        updatePosition(newX)
    }

    private fun updatePosition(newX: Float) {
        val validX = (newX).coerceIn(minScrollWidth.toFloat(), maxScrollWidth.toFloat())
        movableFrameLineContainer.x = validX
        syncPlaybackTime(xCoordinateToTimestamp(movableFrameLineContainer.x))
    }

    private val xAnimationEnd = DynamicAnimation.OnAnimationEndListener { _, _, _, _ ->
        seekTo(xCoordinateToTimestamp(movableFrameLineContainer.x))
    }

    private fun moveViewToCenterOfScreen(view: View) {
//        view.layout(screenWidth / 2, 0, screenWidth / 2 + measuredWidth, measuredHeight)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initView(context: Context, attrs: AttributeSet?) {

        /***************Frame line*******************/
        movableFrameLineContainer = object : FrameLayout(context) {}.apply {
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
