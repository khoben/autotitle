package com.khoben.autotitle.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.media.ThumbnailUtils
import android.net.Uri
import com.khoben.autotitle.service.frameretriever.AndroidNativeMetadataProvider

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

    /**
     * Retrieve cropped thumbnail from video
     *
     * @param context Application context
     * @param sourceUri Video uri
     * @param width Width of thumbnail, px
     * @param height Height of thumbnail, px
     * @return Thumbnail
     */
    fun getVideoThumbnail(context: Context, sourceUri: Uri, width: Int = 512, height: Int = 384): Bitmap? {
        return AndroidNativeMetadataProvider(context, sourceUri)
            .getFrameAt(0L)?.let { cropCenter(it, width, height) }
    }
}