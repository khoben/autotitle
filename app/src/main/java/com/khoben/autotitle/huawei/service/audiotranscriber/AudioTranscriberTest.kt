package com.khoben.autotitle.huawei.service.audiotranscriber

import android.content.Context
import android.net.Uri
import com.khoben.autotitle.huawei.model.MLCaption
import com.khoben.autotitle.huawei.model.MLCaptionEnvelop
import io.reactivex.rxjava3.core.Observable

class AudioTranscriberTest(context: Context) : AudioTranscriber {

    private fun mockService(): Observable<MLCaptionEnvelop> {
        return resultMockService()
    }

    private fun resultMockService(): Observable<MLCaptionEnvelop> {
        return Observable.create { emitter ->
            emitter.onNext(
                MLCaptionEnvelop(
                    arrayListOf(
                        MLCaption("A", 0L, 1000L),
                        MLCaption("AB", 1000L, 2000L),
                        MLCaption("ABC", 4000L, 5000L),
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

    override fun setLangCode(langCode: String) {
    }

    override fun start(uri: Uri): Observable<MLCaptionEnvelop> {
        return mockService()
    }

    override fun start(path: String): Observable<MLCaptionEnvelop> {
        return mockService()
    }
}