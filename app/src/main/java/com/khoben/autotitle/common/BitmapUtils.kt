package com.khoben.autotitle.common

import android.graphics.Bitmap
import android.graphics.Canvas
import android.media.ThumbnailUtils

object BitmapUtils {

    /**
     * Take thumbnail from [bitmap] with [w] x [h] dimensions
     *
     * @param bitmap Source bitmap
     * @param w Width of result bitmap
     * @param h Height of result bitmap
     * @return Result bitmap
     */
    fun cropCenter(bitmap: Bitmap, w: Int, h: Int): Bitmap {
        return ThumbnailUtils.extractThumbnail(bitmap, w, h)
    }

    /**
     * Combines [bitmaps] side by side
     *
     * @param bitmaps
     * @param w Width of single bitmap
     * @param h Height of single bitmap
     * @return Result bitmap
     */
    fun combine(bitmaps: List<Bitmap?>, w: Int, h: Int): Bitmap {
        val cropped = bitmaps.mapNotNull { b -> b?.let { cropCenter(b, w, h) } }
        val temp = Bitmap.createBitmap(w * cropped.size, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(temp)
        var left = 0F
        cropped.forEach { b ->
            canvas.drawBitmap(b, left, 0f, null)
            left += b.width
        }
        return temp
    }
}