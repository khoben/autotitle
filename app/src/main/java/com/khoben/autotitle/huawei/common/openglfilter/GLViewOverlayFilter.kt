package com.khoben.autotitle.huawei.common.openglfilter

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
    private var paint = Paint().apply {
        isAntiAlias = true
        isFilterBitmap = true
    }

    override fun drawCanvas(canvas: Canvas) {
        if (currentTimeMs < 0L) return
        for (overlay in list) {
            if (currentTimeMs in overlay.startTime..overlay.endTime) {
                var scaleX = 1F
                var scaleY = 1F
                val isWide = false
                scaleX = canvas.width.toFloat() / parentViewSize.first
                scaleY = canvas.height.toFloat() / parentViewSize.second
                // TODO("Pre-render view's bitmap")
                val b = overlay.textView!!.getBitmap(overlay, isWide, scaleX, scaleY) ?: continue
                // we need put bitmap at center of overlay
                val bitmapCenterX = b.width / 2
                val bitmapCenterY = b.height / 2
                overlay.getHitRect(boundingRect)
                val centerX = boundingRect.exactCenterX()
                val centerY = boundingRect.exactCenterY()
                val x = centerX * scaleX - bitmapCenterX
                val y = centerY * scaleY - bitmapCenterY
                canvas.drawBitmap(b, x, y, paint)
                b.recycle()
            }
        }
    }

    override fun release() {

    }

    override fun onCurrentVideoTime(timeUs: Long) {
        currentTimeMs = timeUs / 1000L
    }
}