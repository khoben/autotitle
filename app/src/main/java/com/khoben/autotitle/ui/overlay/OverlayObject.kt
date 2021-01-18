package com.khoben.autotitle.ui.overlay

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.toBitmap
import com.khoben.autotitle.R
import com.khoben.autotitle.ui.overlay.gesture.ControlType
import com.khoben.autotitle.ui.overlay.gesture.MultiTouchListener
import java.util.*


abstract class OverlayObject(context: Context, attrs: AttributeSet) :
        FrameLayout(context, attrs), Comparable<OverlayObject> {

    val uuid: UUID = UUID.randomUUID()

    var isInEdit = false
        set(value) {
            field = value
            invalidate()
        }

    var startTime: Long = 0
    var endTime: Long = 0

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
        this.setWillNotDraw(false)
        this.initBitmaps()
    }

    private fun initBitmaps() {
        deleteBtn = AppCompatResources.getDrawable(context, R.drawable.close_icon)!!.toBitmap()
        deleteBtnWidth = (deleteBtn.width * BITMAP_BTN_SCALE).toInt()
        deleteBtnHeight = (deleteBtn.height * BITMAP_BTN_SCALE).toInt()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (isInEdit) {
            // draw frameborder
            canvas.drawRect(0F + deleteBtnWidth / 2F,
                    0F + deleteBtnHeight / 2F,
                    width.toFloat() - deleteBtnHeight / 2F,
                    height.toFloat() - deleteBtnHeight / 2F,
                    helpFramePaint)

            // draw delete icon
            deleteRect.set((width - deleteBtnWidth).toFloat(), 0F, width.toFloat(), deleteBtnHeight.toFloat())
            canvas.drawBitmap(deleteBtn, null, deleteRect, null)
        }
    }

    /**
     * Init multitouch gesture listener with [rect] bounds
     * @param rect Rect bound
     * @param listener MultiTouch event listener
     */
    fun initMultiTouchListener(rect: Rect, listener: MultiTouchListener.OnGestureControl) {
        setOnTouchListener(
                MultiTouchListener(rect,
                        listOf(Pair(ControlType.DELETE_BTN, deleteRect)))
                        .apply { setOnGestureControl(listener) }
        )
    }

    abstract fun getBitmap(scaleX: Float = 1F, scaleY: Float = 1F): Bitmap?

    override fun compareTo(other: OverlayObject): Int {
        return (this.startTime - other.startTime).toInt()
    }
}