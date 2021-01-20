package com.khoben.autotitle.service.frameretriever

import android.net.Uri
import com.khoben.autotitle.ui.player.seekbar.FramesHolder
import io.reactivex.rxjava3.core.Observable

interface VideoFrameRetriever {
    fun init(
        uri: Uri,
        providerType: ProviderType = ProviderType.NATIVE_ANDROID
    ): VideoFrameRetriever

    fun load(frameTime: Long, w: Int, h: Int, callback: (FramesHolder) -> Unit)
    fun load(frameTime: Long, callback: (FramesHolder) -> Unit)
    fun onError(errorListener: (Throwable) -> Unit): VideoFrameRetriever
    fun cancel()

    fun retrieveFrames(frameTime: Long): Observable<FramesHolder>
    fun retrieveFrames(frameTime: Long, w: Int, h: Int): Observable<FramesHolder>
}