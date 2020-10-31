package com.khoben.autotitle.huawei.extension

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("SimpleDateFormat")
fun Long.toReadableTimeString(): String? = SimpleDateFormat("m:ss.S").format(Date(this))