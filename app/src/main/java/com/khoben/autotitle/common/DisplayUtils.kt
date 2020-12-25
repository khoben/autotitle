package com.khoben.autotitle.common

import android.content.Context
import android.content.res.Resources
import android.util.TypedValue

object DisplayUtils {
    fun dipToPx(ctx: Context, dip: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dip,
            ctx.resources.displayMetrics
        ).toInt()
    }

    fun dipToPx(dip: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dip.toFloat(),
            Resources.getSystem().displayMetrics
        ).toInt()
    }
}