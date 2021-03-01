package com.khoben.autotitle.ui.overlay.gesture

import android.annotation.SuppressLint
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View

/**
 * MultiTouchListener class
 * @author [Burhanuddin Rashid](https://github.com/burhanrashid52)
 */
class MultiTouchListener2(
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

    private val minimumScale = 0.5f
    private val maximumScale = 10.0f
    private var mActivePointerId = INVALID_POINTER_ID
    private var mPrevX = 0f
    private var mPrevY = 0f
    private var mPrevRawX = 0f
    private var mPrevRawY = 0f

    private var mOnGestureControl: OnGestureControl? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(view: View, event: MotionEvent): Boolean {
        mScaleGestureDetector.onTouchEvent(view, event)
        mGestureListener.onTouchEvent(event)
        if (!isTranslateEnabled) {
            return true
        }
        val action = event.action
        when (action and event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                mPrevX = event.x
                mPrevY = event.y
                mPrevRawX = event.rawX
                mPrevRawY = event.rawY
                mActivePointerId = event.getPointerId(0)
                mOnGestureControl?.setSelectedAt(event)
            }
            MotionEvent.ACTION_MOVE -> {
                val pointerIndexMove = event.findPointerIndex(mActivePointerId)
                if (pointerIndexMove != -1) {
                    val currX = event.getX(pointerIndexMove)
                    val currY = event.getY(pointerIndexMove)
                    if (!mScaleGestureDetector.isInProgress) {
                        mOnGestureControl?.onMove(currX - mPrevX, currY - mPrevY)
                    }
                }
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
            applyTransformation(info)
            return !isPinchToZoomEnabled
        }
    }

    inner class TransformInfo {
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
        fun onClick(e: MotionEvent)
        fun onLongClick(e: MotionEvent)
        fun onDoubleTap(e: MotionEvent)

        fun onMove(deltaX: Float, deltaY: Float)
        fun onScale(delta: Float)
        fun onRotate(delta: Float)
        fun setSelectedAt(event: MotionEvent)
        fun computeRenderOffset(pivotX: Float, pivotY: Float)
    }

    fun setOnGestureControl(onGestureControl: OnGestureControl?) {
        mOnGestureControl = onGestureControl
    }

    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapUp(e: MotionEvent): Boolean {
            mOnGestureControl?.onClick(e)
            return true
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            mOnGestureControl?.onDoubleTap(e)
            return true
        }

        override fun onLongPress(e: MotionEvent) {
            super.onLongPress(e)
            mOnGestureControl?.onLongClick(e)
        }
    }

    private fun applyTransformation(info: TransformInfo) {
        mOnGestureControl?.computeRenderOffset(info.pivotX, info.pivotY)
        mOnGestureControl?.onMove(info.deltaX, info.deltaY)
        mOnGestureControl?.onScale(info.deltaScale)
        mOnGestureControl?.onRotate(info.deltaAngle)
    }

    companion object {
        private const val INVALID_POINTER_ID = -1
    }
}