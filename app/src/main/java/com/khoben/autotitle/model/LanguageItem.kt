package com.khoben.autotitle.model

import androidx.annotation.DrawableRes
import com.google.gson.annotations.SerializedName

data class LanguageItem(
    @SerializedName("code")
    val key: String,
    @SerializedName("title")
    val value: String,
    @DrawableRes val icon: Int? = null
)
