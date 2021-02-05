package com.khoben.autotitle.common

import android.content.Context
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.core.content.ContextCompat

object StyledAttrUtils {
    fun getColor(context: Context, @AttrRes attr: Int): Int {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(
            attr,
            typedValue,
            true
        )
        return ContextCompat.getColor(context, typedValue.resourceId)
    }
}