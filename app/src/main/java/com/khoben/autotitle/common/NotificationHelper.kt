package com.khoben.autotitle.common

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.khoben.autotitle.App
import com.khoben.autotitle.BuildConfig
import com.khoben.autotitle.R
import java.util.*

object NotificationHelper {

    /**
     * Fixed NotificationChannel ID
     */
    private const val CHANNEL_ID = BuildConfig.APPLICATION_ID + "_NOTIFICATION"

    /**
     * Creates single notification channel with predefined [CHANNEL_ID]
     *
     * @param context Application context
     * @param name Notification channel name
     * @param descriptionText Notification channel description
     */
    fun createNotificationChannel(context: Context, name: String, descriptionText: String = "") {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                name,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = descriptionText
            }
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }
    }

    /**
     * Create and show notification in channel with ID equals to [CHANNEL_ID]
     *
     * @param context Application context
     * @param text Notification message
     * @param title Notification title
     * @param largeIcon Notification large icon
     * @param notificationIntent Notification intent (click action)
     */
    fun show(
        context: Context,
        text: String,
        title: String = App.appName,
        largeIcon: Bitmap? = null,
        notificationIntent: PendingIntent? = null
    ) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_name)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .also { builder ->
                largeIcon?.let {
                    builder.setLargeIcon(it)
                }
                notificationIntent?.let {
                    builder.setContentIntent(it)
                }
            }
        with(NotificationManagerCompat.from(context)) {
            notify(UUID.randomUUID().hashCode(), builder.build())
        }
    }
}