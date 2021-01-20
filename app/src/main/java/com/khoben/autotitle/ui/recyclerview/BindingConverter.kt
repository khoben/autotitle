package com.khoben.autotitle.ui.recyclerview

import com.khoben.autotitle.extension.dateTime
import com.khoben.autotitle.extension.formattedTime

/**
 * @see [Long.dateTime]
 * @param time Long
 * @return String
 */
fun toDate(time: Long) = time.dateTime()

/**
 * @see [Long.formattedTime]
 * @param time Long
 * @return String?
 */
fun toShortTime(time: Long) = time.formattedTime()

/**
 * Converts [sizeInBytes] to readable string representation
 * @param sizeInBytes Long
 * @return String
 */
fun toFileSize(sizeInBytes: Long): String {
    if (sizeInBytes < 1024) return "%.2f B".format(sizeInBytes.toDouble() / 1024)
    val z = (63 - java.lang.Long.numberOfLeadingZeros(sizeInBytes)) / 10
    return String.format("%.1f %sB", sizeInBytes.toDouble() / (1L shl z * 10), " KMGTPE"[z])
}