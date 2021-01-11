package com.khoben.autotitle.common

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.khoben.autotitle.App
import com.khoben.autotitle.BuildConfig
import com.khoben.autotitle.R
import java.util.*

object NotificationUtils {
    fun createNotificationChannel(context: Context, name: String, descriptionText: String = "") {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
        }
    }

    fun show(context: Context, text: String, title: String = App.appName) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        with(NotificationManagerCompat.from(context)) {
            // notificationId is a unique int for each notification that you must define
            notify(UUID.randomUUID().hashCode(), builder.build())
        }
    }

    private const val CHANNEL_ID = BuildConfig.APPLICATION_ID + "_NOTIFICATION"
}