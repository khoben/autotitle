package com.khoben.autotitle.extension.activityresult.result

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts

inline fun ComponentActivity.getContent(
        crossinline onError: (error: Throwable) -> Unit = {},
        crossinline onSuccess: (uri: Uri) -> Unit = {}
): ActivityResultLauncher<String> {
    return registerForActivityResult(ActivityResultContracts.GetContent()) {
        when {
            it != null -> onSuccess.invoke(it)
            else -> onError.invoke(Error("Error while loading content"))
        }
    }
}

inline fun ComponentActivity.takeVideo(
        crossinline onError: (error: Throwable) -> Unit = {},
        crossinline onSuccess: (message: String) -> Unit = {}
): ActivityResultLauncher<Uri> {
    return registerForActivityResult(ActivityResultContracts.TakeVideo()) {
        onSuccess.invoke("Video was taken")
    }
}