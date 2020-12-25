package com.khoben.autotitle.service.frameretriever

import android.graphics.Bitmap
import android.net.Uri
import io.reactivex.rxjava3.core.Observable

interface VideoFrameRetriever {
    fun init(
        uri: Uri,
        providerType: ProviderType = ProviderType.NATIVE_ANDROID
    ): VideoFrameRetriever

    fun load(frameTime: Long, w: Int, h: Int, callback: (List<Bitmap>) -> Unit)
    fun load(frameTime: Long, callback: (List<Bitmap>) -> Unit)
    fun onError(errorListener: (Throwable) -> Unit): VideoFrameRetriever
    fun cancel()

    fun retrieveFrames(frameTime: Long): Observable<List<Bitmap>>
    fun retrieveFrames(frameTime: Long, w: Int, h: Int): Observable<List<Bitmap>>
}