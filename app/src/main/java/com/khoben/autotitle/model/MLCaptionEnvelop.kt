package com.khoben.autotitle.model

/**
 * Wrapped [MLCaption] for RxJava passing null result on some error
 *
 * @property caption Captions
 * @property throwable An Error
 */
data class MLCaptionEnvelop(
    val caption: List<MLCaption>?,
    val throwable: Throwable? = null
)