package com.khoben.autotitle.service.frameretriever

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers

class VideoFrameRetrieverImpl(
        private val context: Context
) : VideoFrameRetriever {

    private var videoMetadataProvider: VideoMetaDataProvider? = null
    private var videoDuration: Long? = null

    private var frameRetrieverDisposable: Disposable? = null
    private var callback: ((List<Bitmap>) -> Unit)? = null
    private var errorListener: ((Throwable) -> Unit)? = null

    override fun onError(errorListener: (Throwable) -> Unit): VideoFrameRetrieverImpl {
        this.errorListener = errorListener
        return this
    }

    override fun load(frameTime: Long, callback: (List<Bitmap>) -> Unit) {
        this.callback = callback
        callObservable(retrieveFrames(frameTime))
    }

    override fun init(uri: Uri, providerType: ProviderType): VideoFrameRetrieverImpl {
        videoMetadataProvider = MetadataProviderFactory.get(
                providerType = providerType,
                context = context,
                uri = uri
        )
        videoDuration = videoMetadataProvider!!.getVideoDuration()
        return this
    }

    override fun load(frameTime: Long, w: Int, h: Int, callback: (List<Bitmap>) -> Unit) {
        this.callback = callback
        callObservable(retrieveFrames(frameTime, w, h))
    }

    private fun callObservable(o: Observable<List<Bitmap>>) {
        frameRetrieverDisposable = o.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { frames -> callback?.invoke(frames) },
                        { error -> errorListener?.invoke(error) })
    }

    override fun cancel() {
        frameRetrieverDisposable?.let {
            if (it.isDisposed.not()) {
                it.dispose()
            }
        }
        callback = null
        errorListener = null
    }

    override fun retrieveFrames(frameTime: Long): Observable<List<Bitmap>> {
        return Observable.create { emitter ->
            val returnList = arrayListOf<Bitmap>()
            for (i in 0..videoDuration!! step frameTime) {
                var bitmap: Bitmap? = null
                try {
                    bitmap = videoMetadataProvider!!.getFrameAt(1000L * i)
                } catch (e: Exception) {
                    emitter.onError(e)
                }
                bitmap?.let { returnList.add(it) }
            }
            emitter.onNext(returnList)
            emitter.onComplete()
        }
    }

    override fun retrieveFrames(frameTime: Long, w: Int, h: Int): Observable<List<Bitmap>> {
        return Observable.create { emitter ->
            val returnList = arrayListOf<Bitmap>()
            for (i in 0..videoDuration!! step frameTime) {
                var bitmap: Bitmap? = null
                try {
                    bitmap = videoMetadataProvider!!.getCroppedFrameAt(1000L * i, w, h)
                } catch (e: Exception) {
                    emitter.onError(e)
                }
                bitmap?.let { returnList.add(it) }
            }
            emitter.onNext(returnList)
            emitter.onComplete()
        }
    }
}