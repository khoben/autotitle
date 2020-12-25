package com.khoben.autotitle.ui.snackbar

import android.content.Context
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import com.google.android.material.snackbar.Snackbar

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