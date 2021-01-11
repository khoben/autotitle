package com.khoben.autotitle.ui.overlay

import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import com.khoben.autotitle.R
import com.khoben.autotitle.ui.overlay.gesture.MultiTouchListener
import java.util.*


abstract class OverlayObject(context: Context, attrs: AttributeSet) :
    FrameLayout(context, attrs), Comparable<OverlayObject> {

    // generate unique id
    val uuid: UUID = UUID.randomUUID()

    var startTime: Long = 0
    var endTime: Long = 0

    /**
     * Init multitouch gesture listener with [rect] bounds
     * @param rect Rect bound
     * @param listener MultiTouch event listener
     */
    fun initMultiTouchListener(rect: Rect, listener: MultiTouchListener.OnGestureControl) {
        setOnTouchListener(
            MultiTouchListener(rect, true).apply { setOnGestureControl(listener) }
        )
    }

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