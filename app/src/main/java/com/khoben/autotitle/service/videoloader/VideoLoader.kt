package com.khoben.autotitle.service.videoloader

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.khoben.autotitle.App
import com.khoben.autotitle.common.FileUtils
import com.khoben.autotitle.model.MLCaptionEnvelop
import com.khoben.autotitle.service.audioextractor.AudioExtractor
import com.khoben.autotitle.service.audiotranscriber.AudioTranscriber
import com.khoben.autotitle.service.frameretriever.ProviderType
import com.khoben.autotitle.service.frameretriever.VideoFrameRetriever
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class VideoLoader {

    @Inject
    lateinit var videoFrameRetriever: VideoFrameRetriever

    @Inject
    lateinit var audioExtractor: AudioExtractor

    @Inject
    lateinit var audioTranscriber: AudioTranscriber

    init {
        App.applicationComponent.inject(this)
    }

    private var disposable: Disposable? = null
    private var callback: ((Pair<List<Bitmap>, MLCaptionEnvelop>) -> Unit)? = null
    private var errorListener: ((Throwable) -> Unit)? = null

    private var audio: Observable<MLCaptionEnvelop>? = null
    private var frames: Observable<List<Bitmap>>? = null

    private var context: Context? = null
    private var uri: Uri? = null
    private var frameTime = 0L
    private var tempAudioPath: String? = null

    fun init(context: Context, uri: Uri, frameTime: Long): VideoLoader {
        this.context = context
        this.uri = uri
        this.frameTime = frameTime

        this.frames = videoFrameRetriever.init(uri, ProviderType.MEDIA_CODEC)
            .retrieveFrames(frameTime, App.THUMB_SIZE.first, App.THUMB_SIZE.second)
        this.tempAudioPath = FileUtils.getRandomFilepath(context, App.AUDIO_EXTENSION)
        this.audio = extractAudio(context, tempAudioPath!!)

        return this
    }

    fun onError(errorListener: (Throwable) -> Unit): VideoLoader {
        this.errorListener = errorListener
        return this
    }

    fun load(callback: (Pair<List<Bitmap>, MLCaptionEnvelop>) -> Unit) {
        this.callback = callback
        Observable.zip(
            frames,
            audio!!.onErrorReturn { error ->
                Timber.e("Audio Transcription error: $error")
                MLCaptionEnvelop(null, error)
            },
            { f, a -> Pair<List<Bitmap>, MLCaptionEnvelop>(f, a) }
        ).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ result ->
                FileUtils.deleteFile(context!!, tempAudioPath!!)
                callback.invoke(result)
            }, { error ->
                FileUtils.deleteFile(context!!, tempAudioPath!!)
                errorListener?.invoke(error)
            })

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

    fun cancel() {
        disposable?.let {
            if (it.isDisposed.not()) {
                it.dispose()
            }
        }
        callback = null
        errorListener = null
    }
}