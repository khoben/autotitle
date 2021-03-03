package com.khoben.autotitle.model

import androidx.annotation.DrawableRes
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class LanguageItem(
    @SerializedName("code")
    val key: String,
    @SerializedName("title")
    val value: String,
    @SerializedName("hms_code")
    val hms_code: String,
    @DrawableRes val icon: Int? = null
)
