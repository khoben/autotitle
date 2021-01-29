package com.khoben.autotitle.common

import android.app.Activity
import android.content.res.Resources
import android.os.Build
import android.util.TypedValue
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.FragmentActivity

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
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dip.toFloat(),
            Resources.getSystem().displayMetrics
        ).toInt()
    }

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
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
                }
            }
        }
    }

    private fun updateTheme(activity: Activity? = null, nightMode: Int): Boolean {
        AppCompatDelegate.setDefaultNightMode(nightMode)
        activity?.recreate()
        return true
    }
}