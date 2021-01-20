package com.khoben.autotitle.extension

import android.annotation.SuppressLint
import com.khoben.autotitle.App
import java.text.SimpleDateFormat
import java.util.*

/**
 * Formatted output of milliseconds
 *
 * @receiver Value in milliseconds
 * @return Formatted output of milliseconds
 */
@SuppressLint("SimpleDateFormat")
fun Long.formattedTime(): String? = SimpleDateFormat(App.PLAYBACK_TIME_FORMAT_MS).format(Date(this))
//fun Long.formattedTime(): String {
//    val minutes = "${(this / (1000L * 60)) % 60}".padStart(2, '0')
//    val seconds = "${(this / 1000L) % 60}".padStart(2, '0')
//    val milliseconds = "${this % 1000L}".take(1)
//    return "$minutes:$seconds.$milliseconds"
//}

/**
 * DateTime string
 * @receiver Value in milliseconds
 * @return Datetime string
 */
@SuppressLint("SimpleDateFormat")
fun Long.dateTime(): String? = SimpleDateFormat(App.DATETIME_TIME_FORMAT).format(Date(this))