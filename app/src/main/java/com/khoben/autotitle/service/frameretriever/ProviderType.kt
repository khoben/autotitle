package com.khoben.autotitle.service.frameretriever

import androidx.annotation.IntDef

@Target(AnnotationTarget.TYPE)
@IntDef(value = [NATIVE_ANDROID, MEDIA_CODEC])
@Retention(AnnotationRetention.SOURCE)
annotation class ProviderType

const val NATIVE_ANDROID = 0
const val MEDIA_CODEC = 1