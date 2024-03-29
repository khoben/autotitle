package com.khoben.autotitle.extension.activityresult.result

import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher

inline fun ComponentActivity.takeVideoDSL(
    builderResult: ResultBuilder.() -> Unit
): ActivityResultLauncher<Any?> {
    val builder = ResultBuilder()
    builder.builderResult()
    return takeVideo(
        onError = builder.error,
        onSuccess = builder.success
    )
}

inline fun ComponentActivity.getContentDSL(
    builderResult: ResultBuilder.() -> Unit
): ActivityResultLauncher<String> {
    val builder = ResultBuilder()
    builder.builderResult()
    return getContent(
        onError = builder.error,
        onSuccess = builder.success
    )
}