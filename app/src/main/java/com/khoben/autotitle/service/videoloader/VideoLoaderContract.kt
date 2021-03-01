package com.khoben.autotitle.service.videoloader

import android.content.Context
import android.net.Uri
import com.khoben.autotitle.model.MLCaptionEnvelop
import com.khoben.autotitle.service.frameretriever.ProviderType
import com.khoben.autotitle.ui.player.seekbar.FramesHolder
import io.reactivex.rxjava3.core.Observable

abstract class VideoLoaderContract {
    protected var audio: Observable<MLCaptionEnvelop>? = null
    protected var frames: Observable<FramesHolder>? = null

    protected var context: Context? = null
    protected var uri: Uri? = null
    protected var frameTime = 0L
    protected var tempAudioPath: String? = null

    abstract fun init(
        language: String? = null,
        context: Context,
        uri: Uri,
        frameTime: Long,
        providerType: ProviderType = ProviderType.MEDIA_CODEC
    ): VideoLoaderContract

    abstract fun loadCaptions(
        callback: (MLCaptionEnvelop) -> Unit,
        onError: (Throwable) -> Unit
    ): VideoLoaderContract

    abstract fun loadFrames(
        callback: (FramesHolder) -> Unit,
        onError: (Throwable) -> Unit
    ): VideoLoaderContract

    abstract fun cancel()
}