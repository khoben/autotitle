package com.khoben.autotitle.ui.overlay

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import com.khoben.autotitle.R
import java.util.*


abstract class OverlayObject(context: Context, attrs: AttributeSet) :
    FrameLayout(context, attrs), Comparable<OverlayObject> {

    var startTime: Long = 0
    var endTime: Long = 0

    // TODO: Move uuid init in constructor
    var uuid: UUID? = null
    var isInEdit = false
        set(value) {
            field = value

            val drawable = background as GradientDrawable

            if (isInEdit) {
                drawable.setStroke(5, R.color.design_default_color_primary)
                closeButton!!.visibility = View.VISIBLE
            } else {
                drawable.setStroke(5, R.color.black)
                closeButton!!.visibility = View.GONE
            }
        }

    val closeButton: ImageView? by lazy {
        findViewById(R.id.overlay_delete_btn)
    }

    override fun compareTo(other: OverlayObject): Int {
        return (this.startTime - other.startTime).toInt()
    }
}