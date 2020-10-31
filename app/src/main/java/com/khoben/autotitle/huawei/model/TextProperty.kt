package com.khoben.autotitle.huawei.model

data class TextProperty(
    var id: Long = 0, var text: String? = null, var xLocation: Float = 0f,
    var yLocation: Float = 0f, var degree: Float = 0f, var scaling: Float = 0f,
    var order: Int = 0
)