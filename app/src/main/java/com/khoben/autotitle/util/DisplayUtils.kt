package com.khoben.autotitle.util

import android.app.Activity
import android.content.res.Resources
import android.os.Build
import android.util.TypedValue
import androidx.appcompat.app.AppCompatDelegate

object DisplayUtils {
    /**
     * Converts dp to pixels
     */
    fun dipToPx(dip: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dip,
            Resources.getSystem().displayMetrics
        )
    }

    /**
     * Converts dp to pixels
     */
    fun dipToPx(dip: Int): Int {
        return dipToPx(dip.toFloat()).toInt()
    }

    /**
     * Set UI mode (dark/light) for whole application
     *
     * @param activity Current activity
     * @param mode Mode: "dark", "light" or "system"
     */
    fun setAppUi(activity: Activity? = null, mode: String) {
        when (mode) {
            "dark" -> {
                updateTheme(activity, AppCompatDelegate.MODE_NIGHT_YES)
            }
            "light" -> {
                updateTheme(activity, AppCompatDelegate.MODE_NIGHT_NO)
            }
            else -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    updateTheme(activity, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                } else {
                    updateTheme(activity, AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
                }
            }
        }
    }

    private fun updateTheme(activity: Activity? = null, nightMode: Int) {
        AppCompatDelegate.setDefaultNightMode(nightMode)
        activity?.recreate()
    }
}