package com.khoben.autotitle.service.audiotranscriber

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.huawei.hms.mlsdk.aft.cloud.MLRemoteAftEngine
import com.huawei.hms.mlsdk.aft.cloud.MLRemoteAftListener
import com.huawei.hms.mlsdk.aft.cloud.MLRemoteAftResult
import com.huawei.hms.mlsdk.aft.cloud.MLRemoteAftSetting
import com.khoben.autotitle.BuildConfig
import com.khoben.autotitle.model.MLCaption
import com.khoben.autotitle.model.MLCaptionEnvelop
import io.reactivex.rxjava3.core.Observable
import java.io.File


class AudioTranscriberImpl(var context: Context) : AudioTranscriber {
    private val engine = MLRemoteAftEngine.getInstance()
    private var setting: MLRemoteAftSetting? = null

    init {
        engine.init(context)
    }

    override fun setLangCode(langCode: String) {
        setting =
            MLRemoteAftSetting.Factory()
                .setLanguageCode(langCode)
                .enableWordTimeOffset(true)
                .enableSentenceTimeOffset(true)
                .enablePunctuation(true)
                .create()
    }

    override fun start(uri: Uri): Observable<MLCaptionEnvelop> {
        return Observable.create { emitter ->
            engine.setAftListener(object : MLRemoteAftListener {
                override fun onInitComplete(taskId: String, ext: Any?) {
                }

                override fun onUploadProgress(taskId: String, progress: Double, ext: Any?) {
                }

                override fun onEvent(taskId: String, eventId: Int, ext: Any?) {
                }

                override fun onResult(taskId: String, result: MLRemoteAftResult?, ext: Any?) {
                    if (result?.isComplete == true) {
                        // Process the transcription result
                        val sentences = result.sentences?.mapNotNull {
                            MLCaption(it.text, it.startTime.toLong(), it.endTime.toLong())
                        }
                        emitter.onNext(MLCaptionEnvelop(sentences))
                        emitter.onComplete()
                    }
                }

                override fun onError(taskId: String, errorCode: Int, message: String) {
                    emitter.onError(Error(message))
                }

            })

            engine.shortRecognize(uri, setting)
        }
    }

    override fun start(path: String): Observable<MLCaptionEnvelop> {
        val fileUri =
            FileProvider.getUriForFile(
                context,
                "${BuildConfig.APPLICATION_ID}.fileprovider",
                File(path)
            )
        return start(fileUri)
    }
}