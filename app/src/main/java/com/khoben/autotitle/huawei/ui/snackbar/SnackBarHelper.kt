package com.khoben.autotitle.huawei.ui.snackbar

import android.content.Context
import android.os.Build
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.google.android.material.snackbar.Snackbar
import com.khoben.autotitle.huawei.R

class SnackBarHelper {
    companion object {

        fun config(context: Context, snack: Snackbar) {
            setElevation(snack)
            addMargin(snack)
        }

        fun setElevation(snack: Snackbar, elevationValue: Float = 12F) {
            ViewCompat.setElevation(snack.view, elevationValue)
        }

        fun addMargin(snack: Snackbar, marginValue: Int = 12) {
            val params = snack.view.layoutParams as ViewGroup.MarginLayoutParams
            params.setMargins(marginValue, marginValue, marginValue, marginValue)
            snack.view.layoutParams = params
        }
    }
}