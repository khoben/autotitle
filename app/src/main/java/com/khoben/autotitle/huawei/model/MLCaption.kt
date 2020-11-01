package com.khoben.autotitle.huawei.model

data class MLCaptionEnvelop(val caption: MLCaption?)

data class MLCaption(
    var text: String,
    var startTime: Long,
    var endTime: Long
)