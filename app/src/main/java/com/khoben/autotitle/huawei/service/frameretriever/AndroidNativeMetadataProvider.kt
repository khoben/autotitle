package com.khoben.autotitle.huawei.service.frameretriever

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import kotlin.math.min

class AndroidNativeMetadataProvider(private val context: Context, private val uri: Uri) :
    VideoMetaDataProvider {

    private val mediaMetadataRetriever = MediaMetadataRetriever()
        .apply {
            setDataSource(context, uri)
        }

    override fun getFrameAt(frameInMicroseconds: Long): Bitmap? {
        return mediaMetadataRetriever.getFrameAtTime(frameInMicroseconds)
    }

    override fun getVideoDuration(): Long {
        return mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)!!
            .toLong()
    }

    override fun getCroppedFrameAt(frameInMicroseconds: Long, width: Int, height: Int): Bitmap? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            return mediaMetadataRetriever.getScaledFrameAtTime(
                frameInMicroseconds,
                MediaMetadataRetriever.OPTION_CLOSEST_SYNC, width, height
            )
        } else {
            val frameBitmap = mediaMetadataRetriever.getFrameAtTime(
                frameInMicroseconds,
                MediaMetadataRetriever.OPTION_CLOSEST_SYNC
            )
            val bitmapWidth = frameBitmap!!.width
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
    }

}