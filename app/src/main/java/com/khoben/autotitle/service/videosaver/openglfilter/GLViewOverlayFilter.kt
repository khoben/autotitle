package com.khoben.autotitle.service.videosaver.openglfilter

import android.graphics.*
import android.view.View
import com.daasuu.mp4compose.filter.GlOverlayFilter
import com.khoben.autotitle.service.videosaver.CurrentVideoProcessTimeListener
import com.khoben.autotitle.ui.overlay.OverlayObject

/**
 * Draws [OverlayObject]'s along theirs time ranges
 */
class GLViewOverlayFilter(
    private val overlayList: List<OverlayObject>,
    private val parentViewSize: Pair<Int, Int>
) : GlOverlayFilter(),
    CurrentVideoProcessTimeListener {

    private val TIMESTAMP_ACCURACY_MS = 25L

    private val paint = Paint().apply {
        isAntiAlias = true
        isFilterBitmap = true
        isDither = true
    }

    /**
     *  Cached bitmaps and scaled center point of [OverlayObject]
     */
    private val bitmaps = HashMap<OverlayObject, Pair<Bitmap, PointF>?>()

    /**
     * Hit [Rect] of [View] in parent coordinates
     */
    private val hitRect = Rect()

    private fun getViewBitmap(
        view: OverlayObject,
        scaleX: Float,
        scaleY: Float
    ): Pair<Bitmap, PointF>? {
        return bitmaps.getOrPut(view, {
            // view bitmap
            val bitmap = view.getBitmap(scaleX, scaleY) ?: return null

            // getting center point
            view.getHitRect(hitRect)
            val centerX = hitRect.exactCenterX()
            val centerY = hitRect.exactCenterY()

            val x = centerX * scaleX
            val y = centerY * scaleY
            return Pair(bitmap, PointF(x, y))

        })
    }

    override fun drawCanvas(canvas: Canvas, currentTimeUs: Long) {
        val currentTimeMs = currentTimeUs / 1000L
        val scaleX = canvas.width.toFloat() / parentViewSize.first
        val scaleY = canvas.height.toFloat() / parentViewSize.second
        for (overlay in overlayList) {
            // we have sorted sequence of overlay by start time
            if (overlay.startTime > currentTimeMs + TIMESTAMP_ACCURACY_MS) break
            if (
                currentTimeMs in
                overlay.startTime - TIMESTAMP_ACCURACY_MS
                ..
                overlay.endTime + TIMESTAMP_ACCURACY_MS
            ) {
                val bitmap = getViewBitmap(overlay, scaleX, scaleY) ?: continue
                canvas.save()
                // Apply rotation of [overlay]
                canvas.rotate(overlay.rotation, bitmap.second.x, bitmap.second.y)
                // We need put bitmap at center of [overlay]
                canvas.drawBitmap(
                    bitmap.first,
                    bitmap.second.x - bitmap.first.width / 2F,
                    bitmap.second.y - bitmap.first.height / 2F,
                    paint
                )
                canvas.restore()
            }
        }
    }

    override fun release() {
        bitmaps.forEach {
            it.value?.first?.recycle()
        }
        bitmaps.clear()
    }

    override fun onCurrentVideoTime(timeUs: Long) {}
}