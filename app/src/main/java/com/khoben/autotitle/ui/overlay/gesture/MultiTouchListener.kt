package com.khoben.autotitle.ui.overlay.gesture

import android.annotation.SuppressLint
import android.graphics.Rect
import android.graphics.RectF
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import timber.log.Timber
import kotlin.math.max
import kotlin.math.min

/**
 * MultiTouchListener class
 * @author [Burhanuddin Rashid](https://github.com/burhanrashid52)
 */
class MultiTouchListener(
    /**
     * Boundary rect
     */
    parentRect: Rect,
    /**
     * List of item controls
     */
    private val controls: List<Pair<ControlType, RectF>>?,
    /**
     * Enables pinch to zoom
     */
    private val isPinchToZoomEnabled: Boolean = true,
    /**
     * Enables rotation
     */
    private val isRotateEnabled: Boolean = true,
    /**
     * Enables translation
     */
    private val isTranslateEnabled: Boolean = true,
    /**
     * Enables scaling
     */
    private val isScaleEnabled: Boolean = true
) :
    View.OnTouchListener {

    private val mScaleGestureDetector = ScaleGestureDetector(ScaleGestureListener())
    private val mGestureListener = GestureDetector(GestureListener())
    private val mControlClickDetector = OnControlClickDetector()

    private val minimumScale = 0.5f
    private val maximumScale = 10.0f
    private var mActivePointerId = INVALID_POINTER_ID
    private var mPrevX = 0f
    private var mPrevY = 0f
    private var mPrevRawX = 0f
    private var mPrevRawY = 0f

    private var mOnGestureControl: OnGestureControl? = null

    private var parentCenterX = 0F
    private var parentCenterY = 0F

    private var parentWidth = 0F
    private var parentHeight = 0F
    private var parentX = 0F
    private var parentY = 0F

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(view: View, event: MotionEvent): Boolean {
        mControlClickDetector.onTouchEvent(event)
        mScaleGestureDetector.onTouchEvent(view, event)
        mGestureListener.onTouchEvent(event)
        if (!isTranslateEnabled) {
            return true
        }
        val action = event.action
        val x = event.rawX.toInt()
        val y = event.rawY.toInt()
        when (action and event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                mPrevX = event.x
                mPrevY = event.y
                mPrevRawX = event.rawX
                mPrevRawY = event.rawY
                mActivePointerId = event.getPointerId(0)
                view.bringToFront()
            }
            MotionEvent.ACTION_MOVE -> {
                val pointerIndexMove = event.findPointerIndex(mActivePointerId)
                if (pointerIndexMove != -1) {
                    val currX = event.getX(pointerIndexMove)
                    val currY = event.getY(pointerIndexMove)
                    if (!mScaleGestureDetector.isInProgress) {
                        adjustTranslation(view, currX - mPrevX, currY - mPrevY, parentWidth, parentHeight)
                    }
                }
                mOnGestureControl?.onMove()
            }
            MotionEvent.ACTION_CANCEL -> mActivePointerId = INVALID_POINTER_ID
            MotionEvent.ACTION_UP -> {
                mActivePointerId = INVALID_POINTER_ID
            }
            MotionEvent.ACTION_POINTER_UP -> {
                val pointerIndexPointerUp =
                    action and MotionEvent.ACTION_POINTER_INDEX_MASK shr MotionEvent.ACTION_POINTER_INDEX_SHIFT
                val pointerId = event.getPointerId(pointerIndexPointerUp)
                if (pointerId == mActivePointerId) {
                    val newPointerIndex = if (pointerIndexPointerUp == 0) 1 else 0
                    mPrevX = event.getX(newPointerIndex)
                    mPrevY = event.getY(newPointerIndex)
                    mActivePointerId = event.getPointerId(newPointerIndex)
                }
            }
        }
        return true
    }

    private inner class ScaleGestureListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        private var mPivotX = 0f
        private var mPivotY = 0f
        private val mPrevSpanVector: Vector2D = Vector2D()
        override fun onScaleBegin(view: View?, detector: ScaleGestureDetector?): Boolean {
            mPivotX = detector!!.getFocusX()
            mPivotY = detector.getFocusY()
            mPrevSpanVector.set(detector.getCurrentSpanVector())
            return isPinchToZoomEnabled
        }

        override fun onScale(view: View?, detector: ScaleGestureDetector?): Boolean {
            val info = TransformInfo()
            info.deltaScale = if (isScaleEnabled) detector!!.getScaleFactor() else 1.0f
            info.deltaAngle = if (isRotateEnabled) Vector2D.getAngle(
                mPrevSpanVector,
                detector!!.getCurrentSpanVector()
            ) else 0.0f
            info.deltaX = if (isTranslateEnabled) detector!!.getFocusX() - mPivotX else 0.0f
            info.deltaY = if (isTranslateEnabled) detector!!.getFocusY() - mPivotY else 0.0f
            info.pivotX = mPivotX
            info.pivotY = mPivotY
            info.minimumScale = minimumScale
            info.maximumScale = maximumScale
            info.parentWidth = parentWidth
            info.parentHeight = parentHeight
            move(view!!, info)
            return !isPinchToZoomEnabled
        }
    }

    inner class TransformInfo {
        var parentHeight = 0f
        var parentWidth = 0f
        var deltaX = 0f
        var deltaY = 0f
        var deltaScale = 0f
        var deltaAngle = 0f
        var pivotX = 0f
        var pivotY = 0f
        var minimumScale = 0f
        var maximumScale = 0f
    }

    interface OnGestureControl {
        fun onClick()
        fun onLongClick()
        fun onDoubleTap()
        fun onMove()

        /**
         * Fires when one of [ControlType] has been clicked
         *
         * @param which ControlType
         */
        fun onControlClicked(which: ControlType)
    }

    fun setOnGestureControl(onGestureControl: OnGestureControl?) {
        mOnGestureControl = onGestureControl
    }

    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapUp(e: MotionEvent): Boolean {
            mOnGestureControl?.onClick()
            return true
        }

        override fun onDoubleTap(e: MotionEvent?): Boolean {
            mOnGestureControl?.onDoubleTap()
            return true
        }

        override fun onLongPress(e: MotionEvent) {
            super.onLongPress(e)
            mOnGestureControl?.onLongClick()
        }
    }

    private inner class OnControlClickDetector {
        fun onTouchEvent(event: MotionEvent) {
            if (controls == null) return
            if (event.action == MotionEvent.ACTION_UP) {
                for ((type, rect) in controls) {
                    if (rect.contains(event.x, event.y)) {
                        Timber.d("Clicked on ${type.name} control")
                        mOnGestureControl?.onControlClicked(type)
                        break
                    }
                }
            }
        }
    }

    companion object {
        private const val INVALID_POINTER_ID = -1
        private fun adjustAngle(degrees: Float): Float {
            var deg = degrees
            if (deg > 180.0f) {
                deg -= 360.0f
            } else if (deg < -180.0f) {
                deg += 360.0f
            }
            return deg
        }

        internal fun move(view: View, info: TransformInfo) {
            computeRenderOffset(view, info.pivotX, info.pivotY)
            adjustTranslation(view, info.deltaX, info.deltaY, info.parentWidth, info.parentHeight)
            var scale = view.scaleX * info.deltaScale
            scale = max(info.minimumScale, min(info.maximumScale, scale))
            view.scaleX = scale
            view.scaleY = scale
            val rotation = adjustAngle(view.rotation + info.deltaAngle)
            view.rotation = rotation
        }

        private var boundingRect = Rect()
        internal fun adjustTranslation(
            view: View,
            deltaX: Float,
            deltaY: Float,
            parentWidth: Float,
            parentHeight: Float
        ) {
            val deltaVector = floatArrayOf(deltaX, deltaY)
            view.matrix.mapVectors(deltaVector)

            view.getHitRect(boundingRect)
            val centerX = boundingRect.exactCenterX() + deltaVector[0]
            val centerY = boundingRect.exactCenterY() + deltaVector[1]

            if (centerX in 0F..parentWidth) {
                view.translationX += deltaVector[0]
            }

            if (centerY in 0F..parentHeight) {
                view.translationY += deltaVector[1]
            }

        }

        private fun computeRenderOffset(view: View, pivotX: Float, pivotY: Float) {
            if (view.pivotX == pivotX && view.pivotY == pivotY) {
                return
            }
            val prevPoint = floatArrayOf(0.0f, 0.0f)
            view.matrix.mapPoints(prevPoint)
            view.pivotX = pivotX
            view.pivotY = pivotY
            val currPoint = floatArrayOf(0.0f, 0.0f)
            view.matrix.mapPoints(currPoint)
            val offsetX = currPoint[0] - prevPoint[0]
            val offsetY = currPoint[1] - prevPoint[1]
            view.translationX = view.translationX - offsetX
            view.translationY = view.translationY - offsetY
        }
    }

    init {
        parentX = parentRect.left.toFloat()
        parentY = parentRect.top.toFloat()
        parentWidth = parentRect.width().toFloat()
        parentHeight = parentRect.height().toFloat()

        parentCenterX = parentX + parentWidth / 2
        parentCenterY = parentX + parentHeight / 2
    }
}