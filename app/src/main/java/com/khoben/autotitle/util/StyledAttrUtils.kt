package com.khoben.autotitle.util

import android.content.Context
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.core.content.ContextCompat


/**
 * Retrieves the value from theme attributes
 */
object StyledAttrUtils {

    private fun getTypedValue(context: Context, @AttrRes attr: Int): TypedValue {
        return TypedValue().also { typedValue ->
            context.theme.resolveAttribute(
                attr,
                typedValue,
                true
            )
        }
    }

    /**
     * Get color from attribute resource
     *
     * @param context Application context
     * @param attr Attribute resource ID
     * @return Color
     */
    fun getColor(context: Context, @AttrRes attr: Int): Int {
        val typedValue = getTypedValue(context, attr)
        return ContextCompat.getColor(context, typedValue.resourceId)
    }
}