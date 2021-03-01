package com.khoben.autotitle.service.audiotranscriber

import android.content.Context
import android.net.Uri
import com.khoben.autotitle.model.MLCaption
import com.khoben.autotitle.model.MLCaptionEnvelop
import io.reactivex.rxjava3.core.Observable

class AudioTranscriberTest(context: Context) : AudioTranscriber {

    private fun mockService(): Observable<MLCaptionEnvelop> {
        Thread.sleep(2000L)
        return resultMockService()
    }

    private fun resultMockService(): Observable<MLCaptionEnvelop> {
        return Observable.create { emitter ->
            emitter.onNext(
                MLCaptionEnvelop(
                    arrayListOf(
                        MLCaption("First sentence", 0L, 1000L),
                        MLCaption("LOL", 1000L, 2000L),
                        MLCaption("Sample text sample text sample text", 4000L, 5000L),
                    )
                )
            )
            emitter.onComplete()
        }
    }

    private fun emptyMockService(): Observable<MLCaptionEnvelop> {
        return Observable.create { emitter ->
            emitter.onNext(
                MLCaptionEnvelop(
                    emptyList()
                )
            )
            emitter.onComplete()
        }
    }

    private fun errorMockService(): Observable<MLCaptionEnvelop> {
        return Observable.create {
            throw RuntimeException("Service error")
        }
    }

    private fun nullResultMockService(): Observable<MLCaptionEnvelop> {
        return Observable.create { emitter ->
            emitter.onNext(
                MLCaptionEnvelop(
                    null
                )
            )
            emitter.onComplete()
        }
    }

    override fun setLangCode(langCode: String) {
    }

    override fun start(uri: Uri): Observable<MLCaptionEnvelop> {
        return mockService()
    }

    override fun start(path: String): Observable<MLCaptionEnvelop> {
        return mockService()
    }
}