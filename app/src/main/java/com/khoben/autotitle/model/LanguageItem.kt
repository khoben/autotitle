package com.khoben.autotitle.model

import androidx.annotation.DrawableRes
import com.google.gson.annotations.SerializedName

data class LanguageItem(
    @SerializedName("code")
    val key: String,
    @SerializedName("title")
    val value: String,
    @SerializedName("hms_code")
    val hms_code: String,
    @DrawableRes val icon: Int? = null
)
