/* Copyright 2015-2019 dinuscxj

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.khoben.autotitle.ui.overlay.gesture

import android.content.Context
import android.view.MotionEvent
import android.view.ViewConfiguration

/**
 * The MultiTouchGestureDetector Detects scaling, rotating and moving transformation gestures using
 * the supplied MotionEvents. The OnMultiTouchGestureListener callback will notify users when
 * a particular gesture event has occurred, besides, SimpleOnMultiTouchGestureListener is offered
 * as a helper class that you can extend if you don’t care about all of the reported events.
 * This class should only be used with MotionEvents reported via touch.
 *
 **/
class MultiTouchGestureDetector(
    context: Context,
    private val listener: OnMultiTouchGestureListener
) {
    private var mCurrentFocusX = 0f
    private var mCurrentFocusY = 0f
    private var mPreviousFocusX = 0f
    private var mPreviousFocusY = 0f
    private var mCurrentSpan = 0f
    private var mPreviousSpan = 0f
    private var mCurrentRotation = 0f
    private var mPreviousRotation = 0f
    private var mCurrTime: Long = 0
    private var mPrevTime: Long = 0

    /**
     * Returns `true` if a scale gesture is in progress.
     */
    var isInProgress = false
        private set
    private var mInitialSpan = 0f
    private val mSpanSlop: Int
    private var mInitialFocusX = 0f
    private var mInitialFocusY = 0f
    private val mTouchSlopSquare: Int

    /**
     * Accepts MotionEvents and dispatches events to a [OnMultiTouchGestureListener]
     * when appropriate.
     *
     *
     *
     * Applications should pass a complete and consistent event stream to this method.
     * A complete and consistent event stream involves all MotionEvents from the initial
     * ACTION_DOWN to the final ACTION_UP or ACTION_CANCEL.
     *
     *
     * @param event The event to process
     * @return true if the event was processed and the detector wants to receive the
     * rest of the MotionEvents in this event stream.
     */
    fun onTouchEvent(event: MotionEvent): Boolean {
        mCurrTime = event.eventTime
        val action = event.actionMasked
        val count = event.pointerCount
        val touchComplete = action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL
        val touchStart = action == MotionEvent.ACTION_DOWN
        if (touchStart || touchComplete) {
            if (isInProgress) {
                listener.onEnd(this)
                isInProgress = false
            }
            if (touchComplete) {
                return true
            }
        }
        val configChanged =
            action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_UP || action == MotionEvent.ACTION_POINTER_DOWN
        val pointerUp = action == MotionEvent.ACTION_POINTER_UP
        val skipIndex = if (pointerUp) event.actionIndex else -1

        // Determine focal point
        var sumX = 0f
        var sumY = 0f
        val focusX: Float
        val focusY: Float
        val div = if (pointerUp) count - 1 else count

        // compute focusX, focusY
        for (i in 0 until count) {
            if (skipIndex == i) {
                continue
            }
            sumX += event.getX(i)
            sumY += event.getY(i)
        }
        focusX = sumX / div
        focusY = sumY / div

        // Determine average deviation from focal point
        var devSumX = 0f
        var devSumY = 0f
        for (i in 0 until count) {
            if (skipIndex == i) {
                continue
            }

            // Convert the resulting diameter into a radius.
            devSumX += Math.abs(event.getX(i) - focusX)
            devSumY += Math.abs(event.getY(i) - focusY)
        }
        val devX = devSumX / div
        val devY = devSumY / div

        // Span is the average distance between touch points through the focal point;
        // i.e. the diameter of the circle with a radius of the average deviation from
        // the focal point.
        val spanX = devX * 2
        val spanY = devY * 2
        val span = Math.hypot(spanX.toDouble(), spanY.toDouble()).toFloat()

        // compute rotate
        var rotation = 0f
        outer@ for (i in 0 until count) {
            if (skipIndex == i) {
                continue
            }
            inner@ for (j in i + 1 until count) {
                if (skipIndex == j) {
                    continue
                }
                val deltaX = (event.getX(i) - event.getX(j)).toDouble()
                val deltaY = (event.getY(i) - event.getY(j)).toDouble()

                // Convert the resulting diameter into a radius.
                rotation += ((Math.toDegrees(
                    Math.atan2(
                        deltaY,
                        deltaX
                    )
                ) + MAX_ROTATION) % MAX_ROTATION).toFloat()
                break@outer
            }
        }

        // Dispatch begin/end events as needed.
        // If the configuration changes, notify the app to reset its current state by beginning
        // a fresh scale event stream.
        val wasInProgress = isInProgress
        if (isInProgress && configChanged) {
            listener.onEnd(this)
            isInProgress = false
        }
        if (configChanged) {
            mCurrentSpan = span
            mPreviousSpan = mCurrentSpan
            mInitialSpan = mPreviousSpan
            mCurrentFocusX = focusX
            mPreviousFocusX = mCurrentFocusX
            mInitialFocusX = mPreviousFocusX
            mCurrentFocusY = focusY
            mPreviousFocusY = mCurrentFocusY
            mInitialFocusY = mPreviousFocusY
            mCurrentRotation = rotation
            mPreviousRotation = mCurrentRotation
        }
        if (!isInProgress && (wasInProgress
                    || Math.abs(span - mInitialSpan) > mSpanSlop || Math.pow(
                (mCurrentFocusX - mInitialFocusX).toDouble(),
                2.0
            ) +
                    Math.pow((mCurrentFocusY - mInitialFocusY).toDouble(), 2.0) > mTouchSlopSquare)
        ) {
            mCurrentSpan = span
            mPreviousSpan = mCurrentSpan
            mPrevTime = mCurrTime
            mCurrentFocusX = focusX
            mPreviousFocusX = mCurrentFocusX
            mCurrentFocusY = focusY
            mPreviousFocusY = mCurrentFocusY
            mCurrentRotation = rotation
            mPreviousRotation = mCurrentRotation
            isInProgress = listener.onBegin(this)
        }

        // Handle motion; focal point and span/scale factor are changing.
        if (action == MotionEvent.ACTION_MOVE) {
            mCurrentSpan = span
            mCurrentFocusX = focusX
            mCurrentFocusY = focusY
            mCurrentRotation = rotation
            if (isInProgress) {
                if (getScale() != NO_SCALE) {
                    listener.onScale(this)
                }
                if (getRotation() != NO_ROTATE) {
                    listener.onRotate(this)
                }
                if (getMoveX() != NO_MOVE || getMoveY() != NO_MOVE) {
                    listener.onMove(this)
                }
            }
            mPreviousSpan = mCurrentSpan
            mPreviousFocusX = mCurrentFocusX
            mPreviousFocusY = mCurrentFocusY
            mPreviousRotation = mCurrentRotation
            mPrevTime = mCurrTime
        }
        return true
    }

    /**
     * Get the X coordinate of the current gesture's focal point.
     * If a gesture is in progress, the focal point is between
     * each of the pointers forming the gesture.
     *
     * If [.isInProgress] would return false, the result of this
     * function is undefined.
     *
     * @return X coordinate of the focal point in pixels.
     */
    fun getFocusX(): Float {
        return mCurrentFocusX
    }

    /**
     * Get the Y coordinate of the current gesture's focal point.
     * If a gesture is in progress, the focal point is between
     * each of the pointers forming the gesture.
     *
     * If [.isInProgress] would return false, the result of this
     * function is undefined.
     *
     * @return Y coordinate of the focal point in pixels.
     */
    fun getFocusY(): Float {
        return mCurrentFocusY
    }

    /**
     * Return the X coordinate distance from the previous focus event to the current
     * event. This value is defined as
     * ([.mCurrentFocusX] - [.mPreviousFocusX]).
     *
     * @return X coordinate Distance between focal points in pixels.
     */
    fun getMoveX(): Float {
        return mCurrentFocusX - mPreviousFocusX
    }

    /**
     * Return the Y coordinate distance from the previous focus event to the current
     * event. This value is defined as
     * ([.mCurrentFocusY] - [.mPreviousFocusY]).
     *
     * @return Y coordinate Distance between focal points in pixels.
     */
    fun getMoveY(): Float {
        return mCurrentFocusY - mPreviousFocusY
    }

    /**
     * Return the average rotate between each of the pointers forming the
     * gesture in progress through the focal point.
     *
     * ([.mCurrentRotation] - [.mPreviousRotation]).
     *
     * @return rotate between pointers in degrees.
     */
    fun getRotation(): Float {
        return mCurrentRotation - mPreviousRotation
    }

    /**
     * Return the scaling factor from the previous scale event to the current
     * event. This value is defined as
     * ([.mCurrentSpan] / [.mPreviousSpan]).
     *
     * @return The current scaling factor.
     */
    fun getScale(): Float {
        return if (mPreviousSpan > 0) mCurrentSpan / mPreviousSpan else 1F
    }

    /**
     * Return the time difference in milliseconds between the previous
     * accepted scaling event and the current scaling event.
     *
     * @return Time difference since the last scaling event in milliseconds.
     */
    fun getTimeDelta(): Long {
        return mCurrTime - mPrevTime
    }

    /**
     * Return the event time of the current event being processed.
     *
     * @return Current event time in milliseconds.
     */
    fun getEventTime(): Long {
        return mCurrTime
    }

    /**
     * The listener for receiving notifications when gestures occur.
     * If you want to listen for all the different gestures then implement
     * this interface. If you only want to listen for a subset it might
     * be easier to extend [SimpleOnMultiTouchGestureListener].
     *
     * An application will receive events in the following order:
     *
     *  * One [OnMultiTouchGestureListener.onBegin]
     *  * Zero or more [OnMultiTouchGestureListener.onScale]
     *  * One [OnMultiTouchGestureListener.onEnd]
     *
     */
    interface OnMultiTouchGestureListener {
        /**
         * Responds to scaling events for a gesture in progress.
         * Reported by pointer motion.
         *
         * @param detector The detector reporting the event - use this to
         * retrieve extended info about event state.
         * @return Whether or not the detector should consider this event
         * as handled. If an event was not handled, the detector
         * will continue to accumulate movement until an event is
         * handled. This can be useful if an application, for example,
         * only wants to update scaling factors if the change is
         * greater than 0.01.
         */
        fun onScale(detector: MultiTouchGestureDetector?)

        /**
         * Responds to moving events for a gesture in progress.
         * Reported by pointer motion.
         *
         * @param detector The detector reporting the event - use this to
         * retrieve extended info about event state.
         * @return Whether or not the detector should consider this event
         * as handled. If an event was not handled, the detector
         * will continue to accumulate movement until an event is
         * handled. This can be useful if an application, for example,
         * only wants to update scaling factors if the change is
         * greater than 0.01.
         */
        fun onMove(detector: MultiTouchGestureDetector?)

        /**
         * Responds to rotating events for a gesture in progress.
         * Reported by pointer motion.
         *
         * @param detector The detector reporting the event - use this to
         * retrieve extended info about event state.
         * @return Whether or not the detector should consider this event
         * as handled. If an event was not handled, the detector
         * will continue to accumulate movement until an event is
         * handled. This can be useful if an application, for example,
         * only wants to update scaling factors if the change is
         * greater than 0.01.
         */
        fun onRotate(detector: MultiTouchGestureDetector?)

        /**
         * Responds to the beginning of a touch gesture. Reported by
         * new pointers going down.
         *
         * @param detector The detector reporting the event - use this to
         * retrieve extended info about event state.
         * @return Whether or not the detector should continue recognizing
         * this gesture. For example, if a gesture is beginning
         * with a focal point outside of a region where it makes
         * sense, onBegin() may return false to ignore the
         * rest of the gesture.
         */
        fun onBegin(detector: MultiTouchGestureDetector?): Boolean

        /**
         * Responds to the end of a scale gesture. Reported by existing
         * pointers going up.
         *
         * Once a touch has ended, [MultiTouchGestureDetector.getFocusX]
         * and [MultiTouchGestureDetector.getFocusY] will return focal point
         * of the pointers remaining on the screen.
         *
         * @param detector The detector reporting the event - use this to
         * retrieve extended info about event state.
         */
        fun onEnd(detector: MultiTouchGestureDetector?)
    }

    open class SimpleOnMultiTouchGestureListener : OnMultiTouchGestureListener {
        override fun onScale(detector: MultiTouchGestureDetector?) {}
        override fun onMove(detector: MultiTouchGestureDetector?) {}
        override fun onRotate(detector: MultiTouchGestureDetector?) {}
        override fun onBegin(detector: MultiTouchGestureDetector?): Boolean {
            return true
        }

        override fun onEnd(detector: MultiTouchGestureDetector?) {
            // Intentionally empty
        }
    }

    companion object {
        const val TAG = "MultiTouchGestureDetector"
        const val MAX_ROTATION = 360
        const val NO_SCALE = 1.0f
        const val NO_ROTATE = 0.0f
        const val NO_MOVE = 0.0f
    }

    /**
     * Creates a MultiTouchGestureDetector with the supplied listener.
     * You may only use this constructor from a [Looper][android.os.Looper] thread.
     *
     * @param context the application's context
     * @param listener the listener invoked for all the callbacks, this must
     * not be null.
     *
     * @throws NullPointerException if `listener` is null.
     */
    init {
        val configuration = ViewConfiguration.get(context)
        val touchSlop = configuration.scaledTouchSlop
        mTouchSlopSquare = touchSlop * touchSlop
        mSpanSlop = configuration.scaledTouchSlop * 2
    }
}