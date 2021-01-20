package com.khoben.autotitle.extension.activityresult

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContract
import androidx.annotation.CallSuper

class CustomTakeVideo : ActivityResultContract<Any?, Uri?>() {
    @CallSuper
    override fun createIntent(context: Context, input: Any?): Intent {
        return Intent(MediaStore.ACTION_VIDEO_CAPTURE)
    }

    override fun getSynchronousResult(
        context: Context,
        input: Any?
    ): SynchronousResult<Uri?>? {
        return null
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        return if (intent == null || resultCode != Activity.RESULT_OK)
            null
        else
            intent.data
    }
}