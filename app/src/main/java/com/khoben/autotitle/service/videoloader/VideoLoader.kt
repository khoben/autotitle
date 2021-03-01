package com.khoben.autotitle.service.videoloader

import android.content.Context
import android.net.Uri
import com.khoben.autotitle.App
import com.khoben.autotitle.util.FileUtils
import com.khoben.autotitle.model.MLCaptionEnvelop
import com.khoben.autotitle.service.audioextractor.AudioExtractor
import com.khoben.autotitle.service.audiotranscriber.AudioTranscriber
import com.khoben.autotitle.service.frameretriever.ProviderType
import com.khoben.autotitle.service.frameretriever.VideoFrameRetriever
import com.khoben.autotitle.ui.player.seekbar.FramesHolder
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named

class VideoLoader : VideoLoaderContract() {

    private var disposable = CompositeDisposable()
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
        language: String?,
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
        this.tempAudioPath = FileUtils.getInternalRandomFilepath()
        this.audio = extractAudio(context, tempAudioPath!!, language?: "en-US")

        return this
    }

    private fun extractAudio(context: Context, outputPath: String, language: String): Observable<MLCaptionEnvelop> {
        return audioExtractor.extractAudio(context, uri!!, outputPath)
            .flatMap { path ->
                Timber.d("Audio has been extracted with path = $path")
                audioTranscriber.setLangCode(language)
                Timber.d("Set language to $language")
                audioTranscriber.start(path)
            }
    }

    override fun loadCaptions(
        callback: (MLCaptionEnvelop) -> Unit,
        onError: (Throwable) -> Unit
    ): VideoLoader {
        audio!!.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ result ->
                FileUtils.deleteFile(context!!, tempAudioPath!!)
                callback.invoke(result)
            }, { error ->
                FileUtils.deleteFile(context!!, tempAudioPath!!)
                onError.invoke(error)
            }).also {
                disposable.add(it)
            }
        return this
    }

    override fun loadFrames(
        callback: (FramesHolder) -> Unit,
        onError: (Throwable) -> Unit
    ): VideoLoader {
        frames!!.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ result ->
                callback.invoke(result)
            }, { error ->
                onError.invoke(error)
            }).also {
                disposable.add(it)
            }
        return this
    }

    override fun cancel() {
        disposable.clear()
        errorListener = null
    }
}