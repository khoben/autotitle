package com.khoben.autotitle.ui.overlay

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.toBitmap
import com.khoben.autotitle.R
import com.khoben.autotitle.extension.getRect
import com.khoben.autotitle.ui.overlay.gesture.ControlType
import com.khoben.autotitle.ui.overlay.gesture.MultiTouchListener
import com.khoben.autotitle.ui.overlay.gesture.MultiTouchListener2

class OverlayDrawLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var selectedOverlay: OverlayObject? = null
    private var overlays: List<OverlayObject>? = null

    private val helpFramePaint = Paint().apply {
        color = Color.CYAN
        style = Paint.Style.STROKE
        isAntiAlias = true
        strokeWidth = 4f
        pathEffect = DashPathEffect(floatArrayOf(12F, 12F), 1F)
    }

    private val BITMAP_BTN_SCALE = 0.5F

    private lateinit var deleteBtn: Bitmap
    private var deleteBtnWidth: Int = 0
    private var deleteBtnHeight: Int = 0
    private val deleteRect = RectF()

    init {
        initBitmaps()
        initMultiTouchListener()
    }

    private fun initBitmaps() {
        deleteBtn = AppCompatResources.getDrawable(context, R.drawable.close_icon)!!.toBitmap()
        deleteBtnWidth = (deleteBtn.width * BITMAP_BTN_SCALE).toInt()
        deleteBtnHeight = (deleteBtn.height * BITMAP_BTN_SCALE).toInt()
    }

    private val gestureEventListener = object : MultiTouchListener2.OnGestureControl {
        override fun onClick(e: MotionEvent) {
            TODO("Not yet implemented")
        }

        override fun onLongClick(e: MotionEvent) {
            TODO("Not yet implemented")
        }

        override fun onDoubleTap(e: MotionEvent) {
            TODO("Not yet implemented")
        }

        override fun onMove(deltaX: Float, deltaY: Float) {
            TODO("Not yet implemented")
        }

        override fun onScale(delta: Float) {
            TODO("Not yet implemented")
        }

        override fun onRotate(delta: Float) {
            TODO("Not yet implemented")
        }

        override fun setSelectedAt(event: MotionEvent) {
            TODO("Not yet implemented")
        }

        override fun computeRenderOffset(pivotX: Float, pivotY: Float) {
            TODO("Not yet implemented")
        }
    }

    private fun initMultiTouchListener() {
        setOnTouchListener(
            MultiTouchListener2().apply {
                setOnGestureControl(gestureEventListener)
            }
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (selectedOverlay != null) {
            // draw frame border
            canvas.drawRect(
                0F + deleteBtnWidth / 2F,
                0F + deleteBtnHeight / 2F,
                width.toFloat() - deleteBtnHeight / 2F,
                height.toFloat() - deleteBtnHeight / 2F,
                helpFramePaint
            )

            // draw delete icon
            deleteRect.set(
                (width - deleteBtnWidth).toFloat(),
                0F,
                width.toFloat(),
                deleteBtnHeight.toFloat()
            )
            canvas.drawBitmap(deleteBtn, null, deleteRect, null)
        }
    }

    private fun adjustAngle(degrees: Float): Float {
        var deg = degrees
        if (deg > 180.0f) {
            deg -= 360.0f
        } else if (deg < -180.0f) {
            deg += 360.0f
        }
        return deg
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