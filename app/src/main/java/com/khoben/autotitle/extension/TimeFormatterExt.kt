package com.khoben.autotitle.extension

import android.annotation.SuppressLint
import com.khoben.autotitle.App
import java.text.SimpleDateFormat
import java.util.*

/**
 * Formatted output of milliseconds
 *
 * Format declared in [com.khoben.autotitle.App.TIME_FORMAT_MS]
 * @receiver Value in milliseconds
 * @return Formatted output of milliseconds
 */
@SuppressLint("SimpleDateFormat")
fun Long.toReadableTimeString(): String? = SimpleDateFormat(App.TIME_FORMAT_MS).format(Date(this))