package com.khoben.autotitle.huawei.common

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlin.math.max


object DisplayUtils {
    fun dipToPx(ctx: Context, dip: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dip,
            ctx.resources.displayMetrics
        )
            .toInt()
    }

    fun dipToPx(dip: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dip.toFloat(),
            Resources.getSystem().displayMetrics
        )
            .toInt()
    }
}