package com.khoben.autotitle.huawei.service.audiotranscriber

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.huawei.hms.mlsdk.aft.cloud.MLRemoteAftEngine
import com.huawei.hms.mlsdk.aft.cloud.MLRemoteAftListener
import com.huawei.hms.mlsdk.aft.cloud.MLRemoteAftResult
import com.huawei.hms.mlsdk.aft.cloud.MLRemoteAftSetting
import com.khoben.autotitle.huawei.BuildConfig
import com.khoben.autotitle.huawei.model.MLCaption
import io.reactivex.rxjava3.core.Observable
import java.io.File


class AudioTranscriberImpl(var context: Context) : AudioTranscriber {
    private val engine = MLRemoteAftEngine.getInstance()
    private var setting: MLRemoteAftSetting? = null

    companion object {
        private var TAG = AudioTranscriberImpl::class.java.name
    }

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

    override fun start(uri: Uri): Observable<List<MLCaption>> {
        return Observable.create { emitter ->
            engine.setAftListener(object : MLRemoteAftListener {
                override fun onInitComplete(p0: String?, p1: Any?) {
                }

                override fun onUploadProgress(p0: String?, p1: Double, p2: Any?) {
                }

                override fun onEvent(p0: String?, p1: Int, p2: Any?) {
                }

                override fun onResult(p0: String?, result: MLRemoteAftResult?, p2: Any?) {
                    if (result!!.isComplete) {
                        // Process the transcription result.
                        emitter.onNext(result.sentences?.mapNotNull {
                            MLCaption(it.text, it.startTime.toLong(), it.endTime.toLong())
                        })
                        emitter.onComplete()
                    }
                }

                override fun onError(p0: String?, p1: Int, p2: String?) {
                    emitter.onError(Error(p2))
                }

            })

            engine.shortRecognize(uri, setting)
        }
    }

    override fun start(path: String): Observable<List<MLCaption>> {
        val fileUri =
            FileProvider.getUriForFile(
                context,
                "${BuildConfig.APPLICATION_ID}.fileprovider",
                File(path)
            )
        return start(fileUri)
    }

    private val aftListener: MLRemoteAftListener = object : MLRemoteAftListener {
        override fun onInitComplete(p0: String?, p1: Any?) {

        }

        override fun onUploadProgress(p0: String?, p1: Double, p2: Any?) {
        }

        override fun onEvent(p0: String?, p1: Int, p2: Any?) {
        }

        override fun onResult(p0: String?, result: MLRemoteAftResult?, p2: Any?) {
            if (result!!.isComplete) {
                // Process the transcription result.
                Log.d(TAG, "Success")
                Log.d(TAG, result.text)
            }
        }

        override fun onError(p0: String?, p1: Int, p2: String?) {
            Log.e(TAG, p2!!)
        }

    }
}