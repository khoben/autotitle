package com.khoben.autotitle.huawei.common.openglfilter

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import com.daasuu.mp4compose.filter.GlOverlayFilter
import com.khoben.autotitle.huawei.extension.getBitmap
import com.khoben.autotitle.huawei.model.VideoInfo
import com.khoben.autotitle.huawei.service.videosaver.CurrentVideoProcessTimeListener
import com.khoben.autotitle.huawei.ui.overlay.OverlayText

/**
 * Draws bitmaps along theirs time ranges
 */
class GLViewOverlayFilter(
    private val list: List<OverlayText>,
    private val videoInfo: VideoInfo,
    private val parentViewSize: Pair<Int, Int>
) :
    GlOverlayFilter(), CurrentVideoProcessTimeListener {

    private var currentTimeMs = 0L
    private var boundingRect = Rect()
    private var paint = Paint()
    private val bitmaps = mutableListOf<Bitmap?>()

    private fun getBitmap(idx: Int, scaleX: Float, scaleY: Float): Bitmap? {
        if (bitmaps.size <= idx) {
            list[idx].textView?.getBitmap(list[idx], scaleX, scaleY).let { bitmaps.add(idx, it) }
        }
        return bitmaps[idx]
    }

    override fun drawCanvas(canvas: Canvas) {
        if (currentTimeMs < 0L) return
        for ((index, overlay) in list.withIndex()) {
            if (currentTimeMs in overlay.startTime..overlay.endTime) {
                val scaleX = canvas.width.toFloat() / parentViewSize.first
                val scaleY = canvas.height.toFloat() / parentViewSize.second
                val bitmap = getBitmap(index, scaleX, scaleY) ?: continue
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
            it?.recycle()
        }
        bitmaps.clear()
    }

    override fun onCurrentVideoTime(timeUs: Long) {
        currentTimeMs = timeUs / 1000L
    }
}