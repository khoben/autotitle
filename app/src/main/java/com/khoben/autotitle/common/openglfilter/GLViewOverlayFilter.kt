package com.khoben.autotitle.common.openglfilter

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import com.daasuu.mp4compose.filter.GlOverlayFilter
import com.khoben.autotitle.extension.getBitmap
import com.khoben.autotitle.model.VideoInfo
import com.khoben.autotitle.service.videosaver.CurrentVideoProcessTimeListener
import com.khoben.autotitle.ui.overlay.OverlayText

/**
 * Draws bitmaps along theirs time ranges
 */
class GLViewOverlayFilter(
    private val list: List<OverlayText>,
    private val videoInfo: VideoInfo,
    private val parentViewSize: Pair<Int, Int>
) :
    GlOverlayFilter(),
    CurrentVideoProcessTimeListener {

    private var boundingRect = Rect()
    private var paint = Paint().apply {
        isAntiAlias = true
        isFilterBitmap = true
    }
    private val bitmaps = HashMap<OverlayText, Bitmap?>()

    private val EPS = 10

    private fun getViewBitmap(view: OverlayText, scaleX: Float, scaleY: Float): Bitmap? {
        return bitmaps.getOrPut(view, {
            return view.textView?.getBitmap(view, scaleX, scaleY)
        })
    }

    override fun drawCanvas(canvas: Canvas, currentTimeUs: Long) {
        val currentTimeMs = currentTimeUs / 1000L
        val scaleX = canvas.width.toFloat() / parentViewSize.first
        val scaleY = canvas.height.toFloat() / parentViewSize.second
        for (overlay in list) {
            // we have sorted sequence of overlay by start time
            if (overlay.startTime > currentTimeMs + EPS) break
            if (currentTimeMs > overlay.startTime - EPS &&
                currentTimeMs < overlay.endTime + EPS
            ) {
                val bitmap = getViewBitmap(overlay, scaleX, scaleY) ?: continue

                // we need put bitmap at center of overlay
                val bitmapCenterX = bitmap.width / 2F
                val bitmapCenterY = bitmap.height / 2F
                overlay.getHitRect(boundingRect)
                val centerX = boundingRect.exactCenterX()
                val centerY = boundingRect.exactCenterY()

                val x = centerX * scaleX - bitmapCenterX
                val y = centerY * scaleY - bitmapCenterY
                canvas.drawBitmap(bitmap, x, y, paint)
            }
        }
    }

    override fun release() {
        bitmaps.forEach {
            it.value?.recycle()
        }
        bitmaps.clear()
    }

    override fun onCurrentVideoTime(timeUs: Long) {
    }
}