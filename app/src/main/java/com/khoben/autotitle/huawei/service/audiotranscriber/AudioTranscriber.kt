package com.khoben.autotitle.huawei.service.audiotranscriber

import android.net.Uri
import com.khoben.autotitle.huawei.model.MLCaptionEnvelop
import io.reactivex.rxjava3.core.Observable

interface AudioTranscriber {
    fun setLangCode(langCode: String)
    fun start(uri: Uri): Observable<MLCaptionEnvelop>
    fun start(path: String): Observable<MLCaptionEnvelop>
}