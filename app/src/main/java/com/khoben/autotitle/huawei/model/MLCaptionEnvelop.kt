package com.khoben.autotitle.huawei.model

data class MLCaptionEnvelop(
    val caption: List<MLCaption>?,
    val throwable: Throwable? = null
)