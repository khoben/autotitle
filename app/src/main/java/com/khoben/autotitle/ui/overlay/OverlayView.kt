package com.khoben.autotitle.ui.overlay

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View.OnTouchListener
import android.widget.FrameLayout
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.GestureDetectorCompat
import com.almeros.android.multitouch.MoveGestureDetector
import com.almeros.android.multitouch.RotateGestureDetector
import com.khoben.autotitle.R
import com.khoben.autotitle.ui.overlay.gesture.MultiTouchGestureDetector
import timber.log.Timber


class OverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

    private val overlays = mutableListOf<OverlayEntity>()
    private var selectedOverlay: OverlayEntity? = null

    private lateinit var deleteControl: OverlayControl

    private lateinit var gestureDetectorCompat: GestureDetectorCompat

    private lateinit var mMultiTouchGestureDetector: MultiTouchGestureDetector

    private val overlayPaint = Paint().apply {
        isAntiAlias = true
        isDither = true
    }

    private val strokePaint = Paint().apply {
        color = Color.LTGRAY
        style = Paint.Style.STROKE
        isAntiAlias = true
        strokeWidth = 4f
    }

    private val anchorPaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        isAntiAlias = true
        strokeWidth = 2f
    }

    private val centerLinesPaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        isAntiAlias = true
        strokeWidth = 2f
        pathEffect = DashPathEffect(floatArrayOf(12F, 12F), 1F)
    }

    interface OverlayCallback {
        fun onDoubleClick(overlay: OverlayEntity?)
        fun onLongClick(overlay: OverlayEntity?)
        fun onSingleClick(overlay: OverlayEntity?)
    }

    private var overlayCallback: OverlayCallback? = null

    fun setOverlayCallback(overlayCallback: OverlayCallback) {
        this.overlayCallback = overlayCallback
    }

    @SuppressLint("ClickableViewAccessibility")
    private val onTouchListener = OnTouchListener { _, event ->
        mMultiTouchGestureDetector.onTouchEvent(event)
        gestureDetectorCompat.onTouchEvent(event)
        true
    }

    init {
        setWillNotDraw(false)

        mMultiTouchGestureDetector =
            MultiTouchGestureDetector(context, MultiTouchGestureDetectorListener())
        gestureDetectorCompat = GestureDetectorCompat(context, TapsListener())
        setOnTouchListener(onTouchListener)

        initControls()

        update()
    }

    private fun initControls() {
        val deleteBitmap =
            AppCompatResources.getDrawable(context, R.drawable.close_icon)!!.toBitmap()
        deleteControl = OverlayControl(
            deleteBitmap,
            deleteBitmap.width * BITMAP_BTN_SCALE,
            deleteBitmap.height * BITMAP_BTN_SCALE,
            RectF(),
            Position.TOP_RIGHT
        )
    }

    fun add(entity: OverlayEntity) {
        entity.setControls(listOf(deleteControl))
        entity.moveToCanvasCenter()
        entity.initScale()
        overlays.add(entity)
        selectOverlay(entity)
    }

    fun remove(entity: OverlayEntity) {
        if (overlays.remove(entity)) {
            if (entity == selectedOverlay) {
                selectedOverlay = null
            }
            entity.release()
            invalidate()
        }
    }

    fun getSelected() = selectedOverlay
    fun setOverlays(data: List<OverlayEntity>) {
        overlays.clear()
        overlays.addAll(overlays)
    }

    private val clearAnchorHandler = Handler(Looper.getMainLooper())

    private var clearCenterStarted = false
    private val clearCenterRunnable = Runnable {
        clearCenterStarted = false
        update()
    }

    private var clearVerticalStarted = false
    private val clearVerticalRunnable = Runnable {
        clearVerticalStarted = false
        update()
    }

    private var clearHorizontalStarted = false
    private val clearHorizontalRunnable = Runnable {
        clearHorizontalStarted = false
        update()
    }

    override fun onDraw(canvas: Canvas) {
        drawAll(canvas, overlayPaint)
        drawHelpLines(canvas)
        super.onDraw(canvas)
    }

    private fun drawHelpLines(canvas: Canvas) {
        if (drawAnchors == 0) return

        if (drawAnchors and ShowAnchor.CENTER.flag != 0) {
            drawAnchors = drawAnchors and ShowAnchor.CENTER.flag.inv()
            if (!clearCenterStarted) {
                clearAnchorHandler.postDelayed(clearCenterRunnable, SHOW_HELP_LINE_TIME_MS)
                clearCenterStarted = true
            } else {
                clearAnchorHandler.removeCallbacks(clearCenterRunnable)
                clearAnchorHandler.postDelayed(clearCenterRunnable, SHOW_HELP_LINE_TIME_MS)
            }
        }

        if (clearCenterStarted) selectedOverlay?.drawCenterLines(canvas, centerLinesPaint)

        if (drawAnchors and ShowAnchor.VERTICAL.flag != 0) {
            drawAnchors = drawAnchors and ShowAnchor.VERTICAL.flag.inv()
            if (!clearVerticalStarted) {
                clearAnchorHandler.postDelayed(clearVerticalRunnable, SHOW_HELP_LINE_TIME_MS)
                clearVerticalStarted = true
            } else {
                clearAnchorHandler.removeCallbacks(clearVerticalRunnable)
                clearAnchorHandler.postDelayed(clearVerticalRunnable, SHOW_HELP_LINE_TIME_MS)
            }
        }

        if (clearVerticalStarted) {
            canvas.drawLine(0F, height / 2F, width / 15F, height / 2F, anchorPaint)
            canvas.drawLine(
                width - width / 15F,
                height / 2F,
                width.toFloat(),
                height / 2F,
                anchorPaint
            )
        }

        if (drawAnchors and ShowAnchor.HORIZONTAL.flag != 0) {
            drawAnchors = drawAnchors and ShowAnchor.HORIZONTAL.flag.inv()
            if (!clearHorizontalStarted) {
                clearAnchorHandler.postDelayed(clearHorizontalRunnable, SHOW_HELP_LINE_TIME_MS)
                clearHorizontalStarted = true
            } else {
                clearAnchorHandler.removeCallbacks(clearHorizontalRunnable)
                clearAnchorHandler.postDelayed(clearHorizontalRunnable, SHOW_HELP_LINE_TIME_MS)
            }
        }

        if (clearHorizontalStarted) {
            canvas.drawLine(width / 2F, 0F, width / 2F, height / 15F, anchorPaint)
            canvas.drawLine(
                width / 2F,
                height - height / 15F,
                width / 2F,
                height.toFloat(),
                anchorPaint
            )
        }
    }

    private fun drawAll(canvas: Canvas, paint: Paint? = null) {
        overlays.forEach {
            it.draw(canvas, paint)
            if (it == selectedOverlay) {
                it.drawSelected(canvas, strokePaint)
            }
        }
    }

    private fun update() = invalidate()

    private fun handleTranslate(deltaX: Float, deltaY: Float): Boolean {
        if (selectedOverlay == null) return false
        val newCenterX = selectedOverlay!!.absoluteCenterX() + deltaX
        val newCenterY = selectedOverlay!!.absoluteCenterY() + deltaY

        /** limit center point by [OverlayView] bounds **/
        if (newCenterX >= 0 && newCenterX <= width && newCenterY >= 0 && newCenterY <= height) {
            drawAnchors =
                drawAnchors or selectedOverlay!!.postTranslate(deltaX / width, deltaY / height)
            update()
        }

        return true
    }

    private fun findAtPoint(x: Float, y: Float): OverlayEntity? {
        val p = PointF(x, y)
        for (i in overlays.indices.reversed()) {
            if (overlays[i].pointInLayerRect(p)) {
                return overlays[i]
            }
        }
        return null
    }

    private fun bringToFront(overlay: OverlayEntity) {
        if (overlays.remove(overlay)) {
            overlays.add(overlay)
        }
    }

    private fun selectOverlay(overlay: OverlayEntity?) {
        if (overlay == null) { // deselect
            selectedOverlay = null
            update()
            return
        }
        if (overlay == selectedOverlay) return
        selectedOverlay = overlay
        bringToFront(selectedOverlay!!)
        update()
        overlayCallback?.onSingleClick(selectedOverlay)
    }

    private fun RectF.containsWithExtra(x: Float, y: Float, extra: Float = 20F): Boolean {
        return this.left - extra < x && this.right + extra > x && this.top - extra < y && this.bottom + extra > y
    }

    private fun handleTapEvent(e: MotionEvent) {
        // check if control was tapped
        if (selectedOverlay != null) {
            when {
                deleteControl.rect.containsWithExtra(e.x, e.y) -> {
                    Timber.d("Delete clicked")
                    return
                }
            }
        }
        selectOverlay(findAtPoint(e.x, e.y))
    }

    private var drawAnchors = ShowAnchor.NONE.flag

    private inner class TapsListener : SimpleOnGestureListener() {
        override fun onDoubleTap(e: MotionEvent): Boolean {
            if (selectedOverlay != null) {
                overlayCallback?.onDoubleClick(selectedOverlay)
            }
            return true
        }

        override fun onLongPress(e: MotionEvent) {
            if (selectedOverlay != null) {
                overlayCallback?.onLongClick(selectedOverlay)
            }
        }

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            handleTapEvent(e)
            return true
        }
    }

    private inner class MultiTouchGestureDetectorListener :
        MultiTouchGestureDetector.SimpleOnMultiTouchGestureListener() {
        override fun onScale(detector: MultiTouchGestureDetector?) {
            if (selectedOverlay != null) {
                selectedOverlay!!.postScale(detector!!.getScale())
                update()
            }
        }

        override fun onMove(detector: MultiTouchGestureDetector?) {
            if (selectedOverlay != null) {
                handleTranslate(detector!!.getMoveX(), detector.getMoveY())
                update()
            }
        }

        override fun onRotate(detector: MultiTouchGestureDetector?) {
            if (selectedOverlay != null) {
                drawAnchors =
                    drawAnchors or selectedOverlay!!.postRotate(detector!!.getRotation())
                update()
            }
        }
    }

    fun release() {
        overlays.forEach { it.release() }
        deleteControl.bitmap.recycle()
    }

    companion object {
        private const val BITMAP_BTN_SCALE = 0.5F
        private const val SHOW_HELP_LINE_TIME_MS = 700L
    }
}