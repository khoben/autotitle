package com.khoben.autotitle.service.frameretriever

import android.content.Context
import android.graphics.Bitmap
import com.khoben.autotitle.ui.player.seekbar.FrameStatus
import com.khoben.autotitle.ui.player.seekbar.FramesHolder
import io.reactivex.rxjava3.core.Observable
import timber.log.Timber

class AsyncFrameRetrieverImpl(context: Context): VideoFrameRetrieverImpl(context) {

    override fun retrieveFrames(frameTime: Long): Observable<FramesHolder> {
        return Observable.create { emitter ->
            val returnList = arrayListOf<Bitmap>()
            emitter.onNext(FramesHolder(status = FrameStatus.PRELOAD, frameTime = frameTime, emptyFramesCount = videoDuration!! / frameTime))
            for (i in 0..videoDuration!! step frameTime) {
                var bitmap: Bitmap? = null
                try {
                    bitmap = videoMetadataProvider!!.getFrameAt(1000L * i)
                } catch (e: Exception) {
                    emitter.onError(e)
                }
                bitmap?.let {
                    returnList.add(it)
                    emitter.onNext(FramesHolder(status = FrameStatus.LOAD_SINGLE, singleFrame = it))
                }
            }
            emitter.onNext(FramesHolder(status = FrameStatus.COMPLETED, listFrames =  returnList))
            emitter.onComplete()
        }
    }

    override fun retrieveFrames(frameTime: Long, w: Int, h: Int): Observable<FramesHolder> {
        return Observable.create { emitter ->
            val returnList = arrayListOf<Bitmap>()
            emitter.onNext(FramesHolder(status = FrameStatus.PRELOAD, frameTime = frameTime, emptyFramesCount = videoDuration!! / frameTime))
            for (i in 0..videoDuration!! step frameTime) {
                var bitmap: Bitmap? = null
                try {
                    bitmap = videoMetadataProvider!!.getCroppedFrameAt(1000L * i, w, h)
                } catch (e: Exception) {
                    emitter.onError(e)
                }
                bitmap?.let {
                    returnList.add(it)
                    emitter.onNext(FramesHolder(status = FrameStatus.LOAD_SINGLE, singleFrame = it))
                }
            }
            emitter.onNext(FramesHolder(status = FrameStatus.COMPLETED, listFrames = returnList))
            emitter.onComplete()
        }
    }
}