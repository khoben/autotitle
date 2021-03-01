package com.khoben.autotitle.model

import androidx.annotation.ColorInt
import kotlinx.serialization.Serializable

@Serializable
data class OverlayTextSaveLightModel(
    val scale: Float,
    val rotation: Float,
    val translationX: Float,
    val translationY: Float,
    val pivotX: Float,
    val pivotY: Float,
    val startTime: Long,
    val endTime: Long,
    val x: Float,
    val y: Float,
    val text: String,
    @ColorInt val textColor: Int,
    val fontType: String
)