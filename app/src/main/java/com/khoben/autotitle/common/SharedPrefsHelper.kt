package com.khoben.autotitle.common

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

object SharedPrefsHelper {

    private var instance: SharedPreferences? = null
    private var preferenceChangeListener: SharedPreferences.OnSharedPreferenceChangeListener? = null

    private const val seekBarSmoothAnimationKey = "smooth_seek_animation"
    private const val previewSeekAnimationKey = "preview_seek_animation"
    private const val showNotificationOnSaveKey = "show_notification_onsave"
    private const val copySourceVideoKey = "copy_source_video"
    private const val appThemeKey = "app_theme"

    var seekBarSmoothAnimation: Boolean? = null
    var previewSeekAnimation: Boolean? = null
    var showNotificationOnSave: Boolean? = null
    var copySourceVideo: Boolean? = null
    var appTheme: String? = null

    /**
     * Fills initial values and registers shared preferences change value listener
     *
     * @param context Application context
     */
    fun register(context: Context) {
        instance = PreferenceManager.getDefaultSharedPreferences(context)

        seekBarSmoothAnimation = instance?.getBoolean(seekBarSmoothAnimationKey, true)
        previewSeekAnimation = instance?.getBoolean(previewSeekAnimationKey, false)
        showNotificationOnSave = instance?.getBoolean(showNotificationOnSaveKey, true)
        copySourceVideo = instance?.getBoolean(copySourceVideoKey, false)
        appTheme = instance?.getString(appThemeKey, "system")

        preferenceChangeListener =
            SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
                when (key) {
                    seekBarSmoothAnimationKey -> {
                        seekBarSmoothAnimation = prefs.getBoolean(seekBarSmoothAnimationKey, true)
                    }
                    previewSeekAnimationKey -> {
                        previewSeekAnimation = prefs.getBoolean(previewSeekAnimationKey, false)
                    }
                    showNotificationOnSaveKey -> {
                        showNotificationOnSave = prefs.getBoolean(showNotificationOnSaveKey, true)
                    }
                    copySourceVideoKey -> {
                        copySourceVideo = prefs.getBoolean(copySourceVideoKey, false)
                    }
                    appThemeKey -> {
                        appTheme = prefs.getString(appThemeKey, "system")
                    }
                }
            }
        instance?.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
    }

    /**
     * Unregister shared preferences change listener
     */
    fun unregister() {
        instance?.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)
    }

}