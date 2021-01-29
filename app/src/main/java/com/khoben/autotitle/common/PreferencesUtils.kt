package com.khoben.autotitle.common

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

object PreferencesUtils {

    private var instance: SharedPreferences? = null

    fun init(context: Context) {
        instance = PreferenceManager.getDefaultSharedPreferences(context)
    }

    fun seekBarSmoothAnimation() = instance?.getBoolean("smooth_seek_animation", true)
    fun previewSeekAnimation() = instance?.getBoolean("preview_seek_animation", false)
    fun showNotificationOnSave() = instance?.getBoolean("show_notification_onsave", true)
    fun copySourceVideo() = instance?.getBoolean("copy_source_video", false)
    fun appTheme() = instance?.getString("app_theme", "system")
}