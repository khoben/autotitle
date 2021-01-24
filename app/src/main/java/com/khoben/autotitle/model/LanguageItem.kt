package com.khoben.autotitle.model

import androidx.annotation.DrawableRes

data class LanguageItem(
    val key: String,
    val value: String,
    @DrawableRes val icon: Int? = null
)
