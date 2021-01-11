package com.khoben.autotitle.common

import android.content.res.Resources
import android.util.TypedValue

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
}