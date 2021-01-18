package com.khoben.autotitle.service.videoloader

import android.content.Context
import android.net.Uri
import com.khoben.autotitle.App
import com.khoben.autotitle.common.FileUtils
import com.khoben.autotitle.model.MLCaptionEnvelop
import com.khoben.autotitle.service.audioextractor.AudioExtractor
import com.khoben.autotitle.service.audiotranscriber.AudioTranscriber
import com.khoben.autotitle.service.frameretriever.ProviderType
import com.khoben.autotitle.service.frameretriever.VideoFrameRetriever
import com.khoben.autotitle.ui.player.seekbar.FramesHolder
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named

class VideoLoader : VideoLoaderContract() {

    private var disposable: Disposable? = null
    private var errorListener: ((Throwable) -> Unit)? = null

    @Inject
    @Named("AsyncFrame")
    lateinit var videoFrameRetriever: VideoFrameRetriever

    @Inject
    lateinit var audioExtractor: AudioExtractor

    @Inject
    lateinit var audioTranscriber: AudioTranscriber

    init {
        App.applicationComponent.inject(this)
    }

    override fun init(
        context: Context,
        uri: Uri,
        frameTime: Long,
        providerType: ProviderType
    ): VideoLoader {
        this.context = context
        this.uri = uri
        this.frameTime = frameTime

        this.frames = videoFrameRetriever.init(uri, providerType)
            .retrieveFrames(frameTime, App.THUMB_SIZE.first, App.THUMB_SIZE.second)
        this.tempAudioPath = FileUtils.getRandomFilepath(context, App.AUDIO_EXTENSION)
        this.audio = extractAudio(context, tempAudioPath!!)

        return this
    }

    private fun extractAudio(context: Context, outputPath: String): Observable<MLCaptionEnvelop> {
        return audioExtractor.extractAudio(context, uri!!, outputPath)
            .flatMap { path ->
                Timber.d("Audio has been extracted with path = $path")
                // TODO("LANG CODE")
                audioTranscriber.setLangCode("en-US")
                audioTranscriber.start(path)
            }
    }

    override fun loadCaptions(callback: (MLCaptionEnvelop) -> Unit, onError: (Throwable) -> Unit): VideoLoader {
        audio!!.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ result ->
                FileUtils.deleteFile(context!!, tempAudioPath!!)
                callback.invoke(result)
            }, { error ->
                FileUtils.deleteFile(context!!, tempAudioPath!!)
                onError.invoke(error)
            })
        return this
    }

    override fun loadFrames(callback: (FramesHolder) -> Unit, onError: (Throwable) -> Unit): VideoLoader {
        frames!!.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ result ->
                callback.invoke(result)
            }, { error ->
                onError.invoke(error)
            })
        return this
    }

    override fun cancel() {
        disposable?.let {
            if (it.isDisposed.not()) {
                it.dispose()
            }
        }
        errorListener = null
    }
}