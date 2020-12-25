package com.khoben.autotitle.service.frameretriever

import android.graphics.Bitmap

interface VideoMetaDataProvider {
    fun getCroppedFrameAt(frameInMicroseconds: Long, width: Int, height: Int): Bitmap?
    fun getFrameAt(frameInMicroseconds: Long): Bitmap?
    fun getVideoDuration(): Long
}