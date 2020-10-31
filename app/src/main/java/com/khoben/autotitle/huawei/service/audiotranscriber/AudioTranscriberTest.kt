package com.khoben.autotitle.huawei.service.audiotranscriber

import android.content.Context
import android.net.Uri
import com.khoben.autotitle.huawei.model.MLCaption
import io.reactivex.rxjava3.core.Observable

class AudioTranscriberTest(context: Context) : AudioTranscriber {

    private fun mockService(): Observable<List<MLCaption>> {
        return Observable.create { emitter ->
            emitter.onNext(
                arrayListOf(
                    MLCaption("A", 0L, 1000L),
                    MLCaption("AB", 1000L, 2000L),
                    MLCaption("ABC", 4000L, 5000L),
                )
            )
            emitter.onComplete()
        }
    }

    override fun setLangCode(langCode: String) {
    }

    override fun start(uri: Uri): Observable<List<MLCaption>> {
        return mockService()
    }

    override fun start(path: String): Observable<List<MLCaption>> {
        return mockService()
    }
}