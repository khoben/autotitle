package com.khoben.autotitle.ui.recyclerview.overlays

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat

class TextSelectionHandleView : View {
    private var mDrawable: Drawable? = null
    var hotspotX = 0
        private set
    private var mMoveOffsetX = 0f
    private var mMoveOffsetY = 0f
    private var mMoveListener: MoveListener? = null

    constructor(context: Context?, drawable: Drawable?, hotspotX: Int) : super(context) {
        mDrawable = drawable
        this.hotspotX = hotspotX
        isFocusableInTouchMode = true
    }

    constructor(context: Context?, drawable: Drawable?, rightHandle: Boolean) : super(context) {
        mDrawable = drawable
        val width = drawable!!.intrinsicWidth
        hotspotX = if (rightHandle) width / 4 else width * 3 / 4
        isFocusableInTouchMode = true
    }

    constructor(context: Context, rightHandle: Boolean) : this(
        context,
        getDrawable(context, rightHandle),
        rightHandle
    )

    fun setOnMoveListener(listener: MoveListener?) {
        mMoveListener = listener
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(mDrawable!!.intrinsicWidth, mDrawable!!.intrinsicHeight)
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        mDrawable!!.setBounds(0, 0, width, height)
        mDrawable!!.draw(canvas)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                mMoveOffsetX = hotspotX - event.x
                mMoveOffsetY = -event.y
                if (mMoveListener != null) mMoveListener!!.onMoveStarted()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (mMoveListener != null) mMoveListener!!.onMoved(
                    event.rawX + mMoveOffsetX,
                    event.rawY + mMoveOffsetY
                )
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (mMoveListener != null) mMoveListener!!.onMoveFinished()
                return true
            }
        }
        return false
    }

    interface MoveListener {
        fun onMoveStarted()
        fun onMoveFinished()
        fun onMoved(x: Float, y: Float)
    }

    companion object {
        fun getDrawable(context: Context, rightHandle: Boolean): Drawable? {
            val typedValue = TypedValue()
            context.theme.resolveAttribute(
                if (rightHandle) android.R.attr.textSelectHandleRight else android.R.attr.textSelectHandleLeft,
                typedValue,
                true
            )
            return ContextCompat.getDrawable(context, typedValue.resourceId)
        }
    }
}