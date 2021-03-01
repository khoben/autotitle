package com.khoben.autotitle.extension

import com.khoben.autotitle.util.DisplayUtils

fun Int.dp() = DisplayUtils.dipToPx(this)
fun Float.dp() = DisplayUtils.dipToPx(this)