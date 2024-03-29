package com.iammert.rangeview.library

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min


class RangeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    interface OnRangeValueListener {
        fun rangeChanged(
            maxValue: Float,
            minValue: Float,
            currentLeftValue: Float,
            currentRightValue: Float,
            draggingStateData: DraggingState
        )
    }

    interface OnRangePositionListener {
        fun leftTogglePositionChanged(xCoordinate: Float, value: Float)

        fun rightTogglePositionChanged(xCoordinate: Float, value: Float)
    }

    interface OnRangeDraggingListener {
        fun onDraggingStateChanged(draggingState: DraggingState)
    }

    var rangeValueChangeListener: OnRangeValueListener? = null

    var rangePositionChangeListener: OnRangePositionListener? = null

    var rangeDraggingChangeListener: OnRangeDraggingListener? = null

    private var maxValue: Float = DEFAULT_MAX_VALUE

    private var minValue: Float = DEFAULT_MIN_VALUE

    private var currentLeftValue: Float? = null

    private var currentRightValue: Float? = null

    private var bgColor: Int = context.resources.getColor(R.color.rangeView_colorBackground)

    private var strokeColor: Int = context.resources.getColor(R.color.rangeView_colorStroke)

    private var maskColor: Int = Color.TRANSPARENT

    private var strokeWidth: Float = resources.getDimension(R.dimen.rangeView_StrokeWidth)

    private var toggleRadius: Float = resources.getDimension(R.dimen.rangeView_ToggleRadius)

    private var horizontalMargin: Float = resources.getDimension(R.dimen.rangeView_HorizontalSpace)

    private var bitmap: Bitmap? = null

    private var canvas: Canvas? = null

    private var backgroundBitmap: Bitmap? = null

    private var backgroundCanvas: Canvas? = null

    private val eraser: Paint = Paint().apply {
        color = -0x1
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        flags = Paint.ANTI_ALIAS_FLAG
    }

    private var backgroundPaint: Paint = Paint().apply {
        style = Paint.Style.FILL
        flags = Paint.ANTI_ALIAS_FLAG
    }

    private var maskPaint: Paint = Paint().apply {
        style = Paint.Style.FILL
        color = maskColor
        flags = Paint.ANTI_ALIAS_FLAG
    }

    private var rangeStrokePaint: Paint = Paint().apply {
        style = Paint.Style.STROKE
        flags = Paint.ANTI_ALIAS_FLAG
        strokeWidth = this@RangeView.strokeWidth
    }

    private var rangeTogglePaint: Paint = Paint().apply {
        style = Paint.Style.FILL
        color = strokeColor
        flags = Paint.ANTI_ALIAS_FLAG
    }

    private var draggingStateData: DraggingStateData = DraggingStateData.idle()

    private val totalValueRect: RectF = RectF()

    private val rangeValueRectF: RectF = RectF()

    private val rangeStrokeRectF: RectF = RectF()

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.RangeView)
        bgColor = a.getColor(R.styleable.RangeView_colorBackground, bgColor)
        strokeColor = a.getColor(R.styleable.RangeView_strokeColor, strokeColor)
        minValue = a.getFloat(R.styleable.RangeView_minValue, minValue)
        maxValue = a.getFloat(R.styleable.RangeView_maxValue, maxValue)

        backgroundPaint.color = bgColor
        rangeStrokePaint.color = strokeColor
        rangeTogglePaint.color = strokeColor
        a.recycle()

        isHapticFeedbackEnabled = true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        totalValueRect.set(
            0f + horizontalMargin,
            0f,
            measuredWidth.toFloat() - horizontalMargin,
            measuredHeight.toFloat()
        )

        if (currentLeftValue == null || currentRightValue == null) {
            rangeValueRectF.set(
                totalValueRect.left,
                totalValueRect.top,
                totalValueRect.right,
                totalValueRect.bottom
            )
        } else {
            val leftRangePosition = ((totalValueRect.width()) * currentLeftValue!!) / maxValue
            val rightRangePosition = (totalValueRect.width() * currentRightValue!!) / maxValue
            rangeValueRectF.set(
                leftRangePosition + horizontalMargin,
                totalValueRect.top,
                rightRangePosition + horizontalMargin,
                totalValueRect.bottom
            )
        }
        rangeStrokeRectF.set(
            rangeValueRectF.left,
            rangeValueRectF.top + strokeWidth / 2,
            rangeValueRectF.right,
            rangeValueRectF.bottom - strokeWidth / 2
        )
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        initializeBitmap()

        //Draw full range background color
        this.backgroundCanvas?.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        // this.backgroundCanvas?.drawRect(totalValueRect, backgroundPaint)

        //Draw mask
        this.canvas?.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        this.canvas?.drawRect(totalValueRect, maskPaint)

        //Clear range rectangle
        this.canvas?.drawRect(rangeValueRectF, eraser)

        //Draw range rectangle stroke
        this.canvas?.drawRect(rangeStrokeRectF, rangeStrokePaint)

        // Draw range rectangle background
        this.canvas?.drawRect(rangeStrokeRectF, backgroundPaint)

        //Draw left toggle over range stroke
        val cxLeft = rangeValueRectF.left
        val cyLeft = height.toFloat() / 2
        this.canvas?.drawCircle(cxLeft, cyLeft, toggleRadius, rangeTogglePaint)

        //Draw right toggle over range stroke
        val cxRight = rangeValueRectF.right
        val cyRight = height.toFloat() / 2
        this.canvas?.drawCircle(cxRight, cyRight, toggleRadius, rangeTogglePaint)

        //Draw background bitmap to original canvas
        backgroundBitmap?.let {
            canvas?.drawBitmap(it, 0f, 0f, null)
        }

        //Draw prepared bitmap to original canvas
        bitmap?.let {
            canvas?.drawBitmap(it, 0f, 0f, null)
        }
    }

    fun setLongPressState(state: Boolean, event: MotionEvent) {
        if (isTouchRange(event)) {
            longpressed = state
            if (longpressed && visibility == VISIBLE) {
                performHapticFeedback(
                    HapticFeedbackConstants.LONG_PRESS,
                    HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
                )
            }
        } else {
            longpressed = false
        }
    }

    fun getLongPressState() = longpressed

    private var longpressed = false

    private var accessible: Boolean = true
    fun toggleTouchAccessibility(state: Boolean) {
        accessible = state
    }

    private var touchedControls = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (!accessible) return false
        when (event!!.action) {
            MotionEvent.ACTION_DOWN -> {
                touchedControls = true
                draggingStateData = when {
                    isTouchOnLeftToggle(event) && isTouchOnRightToggle(event) ->
                        DraggingStateData.createConflict(event)
                    isTouchOnLeftToggle(event) -> DraggingStateData.left(event)
                    isTouchOnRightToggle(event) -> DraggingStateData.right(event)
                    isTouchRange(event) -> {
                        touchedControls = longpressed
                        DraggingStateData.range(event)
                    }
                    else -> {
                        touchedControls = longpressed
                        DraggingStateData.idle()
                    }
                }
            }
            MotionEvent.ACTION_MOVE -> {
                when (draggingStateData.draggingState) {
                    DraggingState.DRAGGING_CONFLICT_TOGGLE -> {
                        if (abs(draggingStateData.motionX - event.x) < SLOP_DIFF) {
                            return true
                        }

                        val direction = resolveMovingWay(event, draggingStateData)
                        draggingStateData = when (direction) {
                            Direction.LEFT -> {
                                draggingLeftToggle(event)
                                DraggingStateData.left(event)
                            }
                            Direction.RIGHT -> {
                                draggingRightToggle(event)
                                DraggingStateData.right(event)
                            }
                        }
                    }
                    DraggingState.DRAGGING_RIGHT_TOGGLE -> {
                        draggingRightToggle(event)
                    }
                    DraggingState.DRAGGING_LEFT_TOGGLE -> {
                        draggingLeftToggle(event)
                    }
                    DraggingState.DRAGGING_RANGE -> {
                        if (!longpressed) return false
                        val diff = event.rawX - draggingStateData.motionX
                        Log.d(TAG, "DRAGGING_RANGE ${rangeValueRectF.left} $diff")
                        draggingRange(diff)
                        draggingStateData.motionX = event.rawX
                        return true
                    }
                    else -> {
                        return false
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                rangeDraggingChangeListener?.onDraggingStateChanged(DraggingState.DRAGGING_END)
                draggingStateData = DraggingStateData.idle()
                longpressed = false
            }
        }

        rangeDraggingChangeListener?.onDraggingStateChanged(draggingStateData.draggingState)

        return touchedControls || longpressed
    }

    fun setMaxValue(maxValue: Float) {
        this.maxValue = maxValue
        postInvalidate()
    }

    fun setMinValue(minValue: Float) {
        this.minValue = minValue
        postInvalidate()
    }

    fun setCurrentValues(leftValue: Float, rightValue: Float) {
        currentLeftValue = leftValue
        currentRightValue = rightValue
        requestLayout()
        postInvalidate()
    }

    fun getXPositionOfValue(value: Float): Float {
        if (value < minValue || value > maxValue) {
            return 0f
        }
        return (((totalValueRect.width()) * value) / maxValue) + horizontalMargin
    }

    private fun resolveMovingWay(
        motionEvent: MotionEvent,
        stateData: DraggingStateData
    ): Direction {
        return if (motionEvent.x > stateData.motionX) Direction.RIGHT else Direction.LEFT
    }

    private fun initializeBitmap() {
        if (bitmap == null || canvas == null) {
            bitmap?.recycle()

            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            backgroundBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bitmap?.let {
                this.canvas = Canvas(it)
            }

            backgroundBitmap?.let {
                this.backgroundCanvas = Canvas(it)
            }
        }
    }

    private fun draggingLeftToggle(motionEvent: MotionEvent) {
        val xCoordinate = min(
            totalValueRect.right,
            max(totalValueRect.left, min(motionEvent.x, totalValueRect.right))
        )
        rangeValueRectF.set(
            xCoordinate,
            rangeValueRectF.top,
            rangeValueRectF.right,
            rangeValueRectF.bottom
        )
        rangeStrokeRectF.set(
            rangeValueRectF.left,
            rangeValueRectF.top + strokeWidth / 2,
            rangeValueRectF.right,
            rangeValueRectF.bottom - strokeWidth / 2
        )
        rangePositionChangeListener?.leftTogglePositionChanged(rangeValueRectF.left, getLeftValue())
        postInvalidate()
        notifyRangeChanged(DraggingState.DRAGGING_LEFT_TOGGLE)
    }

    private fun draggingRightToggle(motionEvent: MotionEvent) {
        val xCoordinate = max(
            totalValueRect.left,
            max(totalValueRect.left, min(motionEvent.x, totalValueRect.right))
        )
        rangeValueRectF.set(
            rangeValueRectF.left,
            rangeValueRectF.top,
            xCoordinate,
            rangeValueRectF.bottom
        )
        rangeStrokeRectF.set(
            rangeValueRectF.left,
            rangeValueRectF.top + strokeWidth / 2,
            rangeValueRectF.right,
            rangeValueRectF.bottom - strokeWidth / 2
        )
        rangePositionChangeListener?.rightTogglePositionChanged(
            rangeValueRectF.right,
            getRightValue()
        )
        postInvalidate()
        notifyRangeChanged(DraggingState.DRAGGING_RIGHT_TOGGLE)
    }

    private fun draggingRange(xDiff: Float) {

        val newLeft = rangeValueRectF.left + xDiff
        val newRight = rangeValueRectF.right + xDiff

        if (newLeft <= totalValueRect.left || newLeft >= totalValueRect.right
            || newRight <= totalValueRect.left || newRight >= totalValueRect.right
        )
            return
        rangeValueRectF.set(
            newLeft,
            rangeValueRectF.top,
            newRight,
            rangeValueRectF.bottom
        )
        rangeStrokeRectF.set(
            rangeValueRectF.left,
            rangeValueRectF.top + strokeWidth / 2,
            rangeValueRectF.right,
            rangeValueRectF.bottom - strokeWidth / 2
        )
        rangePositionChangeListener?.rightTogglePositionChanged(
            rangeValueRectF.right,
            getRightValue()
        )
        rangePositionChangeListener?.leftTogglePositionChanged(
            rangeValueRectF.left,
            getLeftValue()
        )
        postInvalidate()
        if (xDiff < 0)
            notifyRangeChanged(DraggingState.DRAGGING_LEFT_TOGGLE)
        else
            notifyRangeChanged(DraggingState.DRAGGING_RIGHT_TOGGLE)
    }

    private fun isLeftToggleExceed(motionEvent: MotionEvent): Boolean {
        return motionEvent.x < totalValueRect.left || motionEvent.x > rangeValueRectF.right
    }

    private fun isRightToggleExceed(motionEvent: MotionEvent): Boolean {
        return motionEvent.x < rangeValueRectF.left || motionEvent.x > totalValueRect.right
    }

    private fun isTouchOnLeftToggle(motionEvent: MotionEvent): Boolean {
        return motionEvent.x > rangeValueRectF.left - toggleRadius && motionEvent.x < rangeValueRectF.left + toggleRadius
    }

    private fun isTouchOnRightToggle(motionEvent: MotionEvent): Boolean {
        return motionEvent.x > rangeValueRectF.right - toggleRadius && motionEvent.x < rangeValueRectF.right + toggleRadius
    }

    private fun isTouchRange(motionEvent: MotionEvent): Boolean {
        return motionEvent.x > rangeValueRectF.left + toggleRadius && motionEvent.x < rangeValueRectF.right - toggleRadius
    }

    private fun getLeftValue(): Float {
        val totalDiffInPx = totalValueRect.right - totalValueRect.left
        val firstValueInPx = rangeValueRectF.left - totalValueRect.left
        return maxValue * firstValueInPx / totalDiffInPx
    }

    private fun getRightValue(): Float {
        val totalDiffInPx = totalValueRect.right - totalValueRect.left
        val secondValueInPx = rangeValueRectF.right - totalValueRect.left
        return maxValue * secondValueInPx / totalDiffInPx
    }

    private fun notifyRangeChanged(state: DraggingState) {
        val firstValue = getLeftValue()
        val secondValue = getRightValue()

        val leftValue = min(firstValue, secondValue)
        val rightValue = max(firstValue, secondValue)

        currentLeftValue = leftValue
        currentRightValue = rightValue

        rangeValueChangeListener?.rangeChanged(maxValue, minValue, leftValue, rightValue, state)
    }

    companion object {

        private const val DEFAULT_MAX_VALUE = 1f

        private const val DEFAULT_MIN_VALUE = 0f

        private const val SLOP_DIFF = 20f

        private const val TAG = "RangeView"
    }

}