package com.khoben.autotitle.service.frameretriever

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import org.codec.media.FrameGrab
import kotlin.math.min


class AndroidMediaCodecMetadataProvider(context: Context, uri: Uri) : VideoMetaDataProvider {

    private val mediaMetadataRetriever = FrameGrab().apply {
        setSource(context, uri)
        init()
    }

    override fun getCroppedFrameAt(frameInMicroseconds: Long, width: Int, height: Int): Bitmap {
        val frameBitmap = getFrameAt(frameInMicroseconds)
        val bitmapWidth = frameBitmap.width
        val bitmapHeight = frameBitmap.height
        val min = min(width, height)
        val bitmapMin = min(bitmapWidth.toFloat(), bitmapHeight.toFloat())
        val scale = min / bitmapMin
        val scaledBitmap = Bitmap.createScaledBitmap(
                frameBitmap,
                (bitmapWidth * scale).toInt(),
                (bitmapHeight * scale).toInt(),
                true
        )
        frameBitmap.recycle()
        return scaledBitmap
    }

    override fun getFrameAt(frameInMicroseconds: Long): Bitmap {
        mediaMetadataRetriever.seekToTime(frameInMicroseconds / 1_000_000)
        mediaMetadataRetriever.getFrameAtTime(frameInMicroseconds / 1_000_000)
        return mediaMetadataRetriever.bitmap
    }

    override fun getVideoDuration() = mediaMetadataRetriever.durationMs
}