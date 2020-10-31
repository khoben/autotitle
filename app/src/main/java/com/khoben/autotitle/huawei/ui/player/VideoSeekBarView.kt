package com.khoben.autotitle.huawei.ui.player

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
import android.widget.TextView
import com.khoben.autotitle.huawei.App.Companion.FRAMES_PER_SCREEN
import com.khoben.autotitle.huawei.R
import com.khoben.autotitle.huawei.common.DisplayUtils
import com.khoben.autotitle.huawei.extension.toReadableTimeString
import com.khoben.autotitle.huawei.ui.overlay.OverlayText


class VideoSeekBarView(
    context: Context,
    attrs: AttributeSet?
) :
    RelativeLayout(context, attrs) {

    private var maxScrollWidth = 0
    private var minScrollWidth = 0
    private var screenWidth = 0
    private var editBarLeft: LinearLayout? = null
    private var editBarRight: LinearLayout? = null
    private var ivEditBarLeft: ImageView? = null
    private var ivEditBarRight: ImageView? = null
    private var tvStartTime: TextView? = null
    private var tvEndTime: TextView? = null
    var imageList: LinearLayout? = null
    private var editBarLeftWidth = 0
    private var editBarLeftHeight = 0
    private var editBarRightWidth = 0
    private var editBarRightHeight = 0
    private var editBarLeftParamsBar: LayoutParams? = null
    private var editBarRightParamsBar: LayoutParams? = null
    private var selectedParams: LayoutParams? = null
    private var videoEditProgressWidth = 0
    private var overlayTimeRangeBackgrounds
            : LinearLayout? = null
    private var totalTime = 0L
    private var startTime: Long = 0
    private var endTime: Long = 0L
    private var currentTime: Long = 0
    private var minSelectTimeWidth = 0f
    private var tvStartTimeParams: LinearLayout.LayoutParams? = null
    private var tvEndTimeParams: LinearLayout.LayoutParams? = null
    private var frameTime = 0L

    private var progressAnim: ValueAnimator? = null
    private var playState = false
    private val timeRangeBackgrounds = mutableListOf<View?>()

    // gray color
    private val timeRangeBackgroundColor: Int = Color.parseColor("#7f000000")
    private val selectedEditBarResourceId = R.drawable.camera_select_selected
    private val unselectedEditBarResourceId = R.drawable.camera_select_normal

    fun addFramesToSeekBar(bitmaps: List<Bitmap?>) {
        val imageWidth = screenWidth / FRAMES_PER_SCREEN
        val layoutParams: ViewGroup.LayoutParams = LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        imageList!!.layoutParams = layoutParams
        for (bitmap in bitmaps) {
            val imageView = ImageView(context)
            val params = LayoutParams(imageWidth, DisplayUtils.dipToPx(60))
            imageView.layoutParams = params
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            imageView.setImageBitmap(bitmap)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                imageView.isForceDarkAllowed = false
            }
            imageList!!.addView(imageView)
        }
    }

    fun addFramesToSeekBar(bitmaps: List<Bitmap?>, frameTime: Long) {
        this.frameTime = frameTime
        addFramesToSeekBar(bitmaps)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(imageList!!.measuredWidth, heightMeasureSpec)
        minScrollWidth = screenWidth / 2 - measuredWidth
        maxScrollWidth = screenWidth / 2
        editBarLeftWidth = editBarLeft!!.measuredWidth
        editBarLeftHeight = measuredHeight
        videoEditProgressWidth = measuredWidth
        editBarRightWidth = editBarRight!!.measuredWidth
        editBarRightHeight = measuredHeight
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        editBarLeft!!.layout(
            -DisplayUtils.dipToPx(20),
            DisplayUtils.dipToPx(8),
            editBarLeftWidth,
            editBarLeftHeight + DisplayUtils.dipToPx(8)
        )
        editBarRight!!.layout(
            editBarRight!!.left,
            DisplayUtils.dipToPx(8),
            editBarRight!!.right,
            editBarRightHeight + DisplayUtils.dipToPx(8)
        )
        overlayTimeRangeBackgrounds!!.layout(
            editBarLeft!!.x.toInt() + DisplayUtils.dipToPx(20),
            DisplayUtils.dipToPx(26),
            editBarRight!!.x
                .toInt() + DisplayUtils.dipToPx(10),
            DisplayUtils.dipToPx(85)
        )
    }

    private var startX = 0f
    private var startLeftBarX = 0f
    private var startRightBarX = 0f
    private var lastTimeClicked = 0L

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        super.onTouchEvent(event)
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                Log.d(TAG, "ACTION_DOWN")
                val clickTime = System.currentTimeMillis()
                startX = event.x
                playState = false
                seekBarListener?.seekBarOnTouch()
                // TODO("Constant")
                if (clickTime - lastTimeClicked < 300L) {
                    Log.d(TAG, "DOUBLE TAP")
                    seekBarListener?.seekBarOnDoubleTap()
                }
                lastTimeClicked = clickTime
//                handler.removeCallbacksAndMessages(null)
            }
            MotionEvent.ACTION_MOVE -> {
                Log.d(TAG, "ACTION_MOVE")
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
                currentTime = (totalTime * (screenWidth / 2 - x) / measuredWidth).toLong()
                rewind(currentTime)
            }
        }
        return true
    }

    private fun rewind(currentTime: Long) {
        seekBarListener?.seekBarRewind(currentTime)
        seekBarListener?.updateVideoPositionWithSeekBar(currentTime)
    }

    private fun startSeekBarAnimation(width: Float, duration: Long) {
        stopSeekBarAnimation()
        progressAnim = ValueAnimator.ofFloat(0f, -width)
        var lastAnimatedValue = 0f
        progressAnim!!.addUpdateListener { valueAnimator: ValueAnimator ->
            val value = valueAnimator.animatedValue as Float
            val diff = value - lastAnimatedValue
            lastAnimatedValue = value
            if (x >= minScrollWidth && playState) {
                currentTime = (totalTime * (screenWidth / 2 - x) / measuredWidth).toLong()
                seekBarListener?.updateVideoPositionWithSeekBar(currentTime)
                x += diff
                if (x + diff < minScrollWidth) {
                    x = maxScrollWidth.toFloat()
                    seekBarListener?.seekBarRewind(0)
                    stopSeekBarAnimation()
                }
            } else {
                stopSeekBarAnimation()
            }
            invalidate()
        }
        progressAnim!!.interpolator = LinearInterpolator()
        progressAnim!!.duration = duration
        post {
            progressAnim?.start()
        }
    }

    private fun stopSeekBarAnimation() {
        progressAnim?.cancel()
        progressAnim = null
    }

    /**
     * Draws background for each overlay time range
     * @param playState Boolean
     * @param overlays List<OverlayObject?>?
     */
    fun playingTimeRange(playState: Boolean, overlays: List<OverlayText?>?) {
        this.playState = playState
        if (playState) {
            overlayTimeRangeBackgrounds!!.visibility = GONE
            editBarLeft!!.visibility = GONE
            editBarRight!!.visibility = GONE
            for (view in timeRangeBackgrounds) {
                removeView(view)
            }
            if (overlays != null && overlays.isNotEmpty()) {
                timeRangeBackgrounds.clear()
                for (overlay in overlays) {
                    val startX: Long =
                        overlay!!.startTime * videoEditProgressWidth / totalTime
                    val endX: Long = overlay.endTime * videoEditProgressWidth / totalTime
                    var width = (endX - startX).toInt()
                    width += if (totalTime - overlay.endTime <= frameTime) {
                        DisplayUtils.dipToPx(10)
                    } else {
                        DisplayUtils.dipToPx(4)
                    }
                    val timeRangeBackground = LinearLayout(context)
                    val timeRangeBackgroundParams = LayoutParams(width, DisplayUtils.dipToPx(60))
                    timeRangeBackground.x = startX.toFloat()
                    timeRangeBackground.setBackgroundColor(timeRangeBackgroundColor)
                    timeRangeBackgroundParams.addRule(CENTER_VERTICAL, TRUE)
                    addView(timeRangeBackground, timeRangeBackgroundParams)
                    timeRangeBackgrounds.add(timeRangeBackground)
                }
            }
        }
        startSeekBarAnimation(measuredWidth.toFloat(), totalTime)
    }

    fun setToState(pos: Long) {
        stopSeekBarAnimation()
        // currentTime = totalTime * (screenWidth / 2 - x) / measuredWidth
        currentTime = pos
        x = screenWidth / 2F - pos * measuredWidth.toFloat() / totalTime
        rewind(pos)
    }

    fun setToDefaultState(saveCurrentTime: Boolean) {
        editBarLeft!!.visibility = GONE
        editBarRight!!.visibility = GONE
//        overlayTimeRangeBackgrounds!!.visibility = GONE
        stopSeekBarAnimation()
        x = maxScrollWidth.toFloat()
        seekBarListener?.seekBarRewind(0)
    }

    var seekBarListener: SeekBarListener? = null

    interface SeekBarListener {
        fun updateVideoPositionWithSeekBar(time: Long)
        fun changeTimeRangeSelectedOverlay(startTime: Long, endTime: Long)
        fun seekBarRewind(currentTime: Long)
        fun seekBarOnTouch()
        fun seekBarOnDoubleTap()
    }

    fun setMediaDuration(totalTime: Long) {
        this.totalTime = totalTime
    }

    fun drawOverlaysTimeRange(
        overlays: List<OverlayText?>?,
        selectedOverlay: OverlayText?,
        isEdit: Boolean
    ) {
        for (view in timeRangeBackgrounds) {
            removeView(view)
        }
        if (overlays != null && overlays.isNotEmpty()) {
            timeRangeBackgrounds.clear()
            for (overlay in overlays) {
                if (selectedOverlay != null && isEdit &&
                    selectedOverlay.timestamp == overlay!!.timestamp
                ) {
                    // show time range selector for baseImageView
                    val timeRangeStartX = selectedOverlay.startTime * videoEditProgressWidth /
                            totalTime - DisplayUtils.dipToPx(20)
                    var timeRangeEndX = selectedOverlay.endTime * videoEditProgressWidth /
                            totalTime - DisplayUtils.dipToPx(10)
                    editBarLeft!!.x = timeRangeStartX.toFloat()
                    if (timeRangeEndX > videoEditProgressWidth - DisplayUtils.dipToPx(17)) {
                        timeRangeEndX = (videoEditProgressWidth - DisplayUtils.dipToPx(17)).toLong()
                    }
                    editBarRight!!.x = timeRangeEndX.toFloat()
                } else {
                    // show time ranges for all provided overlays
                    val timeRangeStartX = overlay!!.startTime *
                            videoEditProgressWidth / totalTime
                    val timeRangeEndX = overlay.endTime *
                            videoEditProgressWidth / totalTime
                    var timeRangeWidth = (timeRangeEndX - timeRangeStartX).toInt()
                    timeRangeWidth += if (totalTime - overlay.endTime <= frameTime) {
                        DisplayUtils.dipToPx(10)
                    } else {
                        DisplayUtils.dipToPx(4)
                    }
                    val timeRangeBackground = LinearLayout(context)
                    val timeRangeBackgroundParams =
                        LayoutParams(timeRangeWidth, DisplayUtils.dipToPx(60))
                    timeRangeBackground.x = timeRangeStartX.toFloat()
                    timeRangeBackground.setBackgroundColor(timeRangeBackgroundColor)
                    timeRangeBackgroundParams.addRule(CENTER_VERTICAL, TRUE)
                    addView(timeRangeBackground, timeRangeBackgroundParams)
                    timeRangeBackgrounds.add(timeRangeBackground)
                }
            }
        }
        removeView(editBarRight)
        removeView(editBarLeft)
        removeView(overlayTimeRangeBackgrounds)
        addView(overlayTimeRangeBackgrounds, selectedParams)
        addView(editBarLeft, editBarLeftParamsBar)
        addView(editBarRight, editBarRightParamsBar)
        if (!isEdit) {
            val leftX: Float = screenWidth / 2 - x - DisplayUtils.dipToPx(20)
            editBarLeft!!.x = leftX
            minSelectTimeWidth =
                (videoEditProgressWidth * frameTime / totalTime + DisplayUtils.dipToPx(10)).toFloat()
            val rightX =
                if (leftX + minSelectTimeWidth > measuredWidth - DisplayUtils.dipToPx(16))
                    measuredWidth - DisplayUtils.dipToPx(16)
                else
                    leftX + minSelectTimeWidth
            editBarRight!!.x = rightX.toFloat()
        }
        if (overlays!!.isEmpty()) {
            editBarLeft!!.visibility = GONE
            editBarRight!!.visibility = GONE
            overlayTimeRangeBackgrounds!!.visibility = GONE
        } else {
            editBarLeft!!.visibility = VISIBLE
            editBarRight!!.visibility = VISIBLE
            overlayTimeRangeBackgrounds!!.visibility = VISIBLE
        }

        if (overlays.indexOf(selectedOverlay) == -1) {
            editBarLeft!!.visibility = GONE
            editBarRight!!.visibility = GONE
            overlayTimeRangeBackgrounds!!.visibility = GONE
        }
    }

    companion object {
        private val TAG = VideoSeekBarView::class.java.simpleName
    }

    init {
        initView(context, attrs)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initView(context: Context, attrs: AttributeSet?) {
        val resources = context.resources
        val dm = resources.displayMetrics
        screenWidth = dm.widthPixels
        minSelectTimeWidth = 1.0f * screenWidth / FRAMES_PER_SCREEN + DisplayUtils.dipToPx(10)

        imageList = LinearLayout(context)
        val layoutParams =
            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        imageList!!.orientation = LinearLayout.HORIZONTAL
        imageList!!.gravity = Gravity.CENTER_VERTICAL
        addView(imageList, layoutParams)

        selectedParams =
            LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        overlayTimeRangeBackgrounds = LinearLayout(context)
        overlayTimeRangeBackgrounds!!.setBackgroundColor(Color.parseColor("#3fff0000"))
        selectedParams!!.addRule(CENTER_VERTICAL, TRUE)
        addView(overlayTimeRangeBackgrounds, selectedParams)

        editBarRightParamsBar =
            LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        editBarRight = LayoutInflater.from(context)
            .inflate(R.layout.edit_bar_two_layout, null) as LinearLayout?
        editBarRightParamsBar!!.addRule(CENTER_VERTICAL, TRUE)
        ivEditBarRight = editBarRight!!.findViewById<View>(R.id.iv_edit_bar_right) as ImageView
        addView(editBarRight, editBarRightParamsBar)

        editBarLeftParamsBar =
            LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        editBarLeft = LayoutInflater.from(context)
            .inflate(R.layout.edit_bar_layout, null) as LinearLayout?
        editBarLeftParamsBar!!.addRule(CENTER_VERTICAL, TRUE)
        editBarLeftParamsBar!!.addRule(ALIGN_PARENT_LEFT, TRUE)
        ivEditBarLeft = editBarLeft!!.findViewById<View>(R.id.iv_edit_bar_left) as ImageView
        addView(editBarLeft, editBarLeftParamsBar)

        tvStartTime = editBarLeft!!.findViewById<View>(R.id.tv_start_time) as TextView
        tvStartTimeParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        tvStartTimeParams!!.leftMargin = DisplayUtils.dipToPx(3)
        tvStartTime!!.layoutParams = tvStartTimeParams

        tvEndTime = editBarRight!!.findViewById<View>(R.id.tv_end_time) as TextView
        tvEndTimeParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        overlayTimeRangeBackgrounds!!.visibility = GONE
        editBarLeft!!.visibility = GONE
        editBarRight!!.visibility = GONE

        editBarLeft!!.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startLeftBarX = event.x
                    ivEditBarLeft!!.setImageResource(selectedEditBarResourceId)
                }
                MotionEvent.ACTION_MOVE -> {
                    val endX = event.x
                    val distanceX = endX - startLeftBarX
                    var toX = editBarLeft!!.x + distanceX
                    val point = IntArray(2)
                    editBarLeft!!.getLocationOnScreen(point)
                    val realX = point[0]
                    if (realX + distanceX <= DisplayUtils.dipToPx(40)) return@setOnTouchListener true
                    if (toX < -DisplayUtils.dipToPx(20)) {
                        toX = (-DisplayUtils.dipToPx(20)).toFloat()
                    }
                    if (toX > editBarRight!!.x - minSelectTimeWidth - editBarLeft!!.width) {
                        toX = editBarRight!!.x - minSelectTimeWidth - editBarLeft!!.width
                    }
                    overlayTimeRangeBackgrounds!!.layout(
                        editBarLeft!!.x.toInt() + DisplayUtils.dipToPx(20),
                        DisplayUtils.dipToPx(26),
                        editBarRight!!.x
                            .toInt() + DisplayUtils.dipToPx(10),
                        DisplayUtils.dipToPx(85)
                    )
                    editBarLeft!!.x = toX
                    startTime = if (toX == -DisplayUtils.dipToPx(20).toFloat()) {
                        0
                    } else {
                        totalTime * overlayTimeRangeBackgrounds!!.left / measuredWidth
                    }
                    endTime =
                        if (toX == videoEditProgressWidth - DisplayUtils.dipToPx(17).toFloat()) {
                            totalTime
                        } else {
                            totalTime * overlayTimeRangeBackgrounds!!.right / measuredWidth
                        }
                    if (tvStartTime != null) {
//                        tvStartTime!!.visibility = View.VISIBLE
                        tvStartTime!!.text = context.getString(
                            R.string.time_second_string, startTime.toReadableTimeString()
                        )
                        if (startTime == 0L) {
                            tvStartTimeParams!!.leftMargin = DisplayUtils.dipToPx(3)
                        } else {
                            tvStartTimeParams!!.leftMargin = 0
                        }
                        tvStartTime!!.layoutParams = tvStartTimeParams
                    }
                }
                MotionEvent.ACTION_UP -> {
//                    tvStartTime!!.visibility = View.GONE
                    overlayTimeRangeBackgrounds!!.layout(
                        editBarLeft!!.x.toInt() + DisplayUtils.dipToPx(20),
                        DisplayUtils.dipToPx(26),
                        editBarRight!!.x
                            .toInt() + DisplayUtils.dipToPx(10),
                        DisplayUtils.dipToPx(85)
                    )
                    seekBarListener?.changeTimeRangeSelectedOverlay(startTime, endTime)
                    ivEditBarLeft!!.setImageResource(unselectedEditBarResourceId)
                }
            }
            true
        }
        editBarRight!!.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startRightBarX = event.x
                    ivEditBarRight!!.setImageResource(selectedEditBarResourceId)
                }
                MotionEvent.ACTION_MOVE -> {
                    val endX = event.x
                    val distanceX = endX - startRightBarX
                    var toX = editBarRight!!.x + distanceX

                    val point = IntArray(2)
                    editBarRight!!.getLocationOnScreen(point)
                    val realX = point[0]
                    if (realX + distanceX <= DisplayUtils.dipToPx(48)) return@setOnTouchListener true

                    if (toX < editBarLeft!!.x + editBarLeft!!.width + minSelectTimeWidth) {
                        toX = editBarLeft!!.x + editBarLeft!!.width + minSelectTimeWidth
                    }
                    if (toX > videoEditProgressWidth - DisplayUtils.dipToPx(17)) {
                        toX = (videoEditProgressWidth - DisplayUtils.dipToPx(17)).toFloat()
                    }
                    overlayTimeRangeBackgrounds!!.layout(
                        editBarLeft!!.x.toInt() + DisplayUtils.dipToPx(20),
                        DisplayUtils.dipToPx(26),
                        editBarRight!!.x
                            .toInt() + DisplayUtils.dipToPx(10),
                        DisplayUtils.dipToPx(85)
                    )
                    editBarRight!!.x = toX
                    startTime = if (toX == -DisplayUtils.dipToPx(20).toFloat()) {
                        0
                    } else {
                        totalTime * overlayTimeRangeBackgrounds!!.left / measuredWidth
                    }
                    endTime =
                        if (toX == videoEditProgressWidth - DisplayUtils.dipToPx(17).toFloat()) {
                            totalTime
                        } else {
                            totalTime * overlayTimeRangeBackgrounds!!.right / measuredWidth
                        }
                    if (tvEndTime != null) {
//                        tvEndTime!!.visibility = View.VISIBLE
                        tvEndTime!!.text = context.getString(
                            R.string.time_second_string,
                            endTime.toReadableTimeString()
                        )
                        tvEndTimeParams!!.rightMargin = 0
                        tvEndTime!!.layoutParams = tvEndTimeParams
                    }
                }
                MotionEvent.ACTION_UP -> {
//                    tvEndTime!!.visibility = View.GONE
                    ivEditBarRight!!.setImageResource(unselectedEditBarResourceId)
                    overlayTimeRangeBackgrounds!!.layout(
                        editBarLeft!!.x.toInt() + DisplayUtils.dipToPx(20),
                        DisplayUtils.dipToPx(26),
                        editBarRight!!.x
                            .toInt() + DisplayUtils.dipToPx(10),
                        DisplayUtils.dipToPx(85)
                    )
                    seekBarListener?.changeTimeRangeSelectedOverlay(startTime, endTime)
                }
            }
            true
        }
    }
}
