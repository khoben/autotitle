package com.khoben.autotitle.huawei.ui.overlay

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import com.khoben.autotitle.huawei.R

abstract class OverlayObject(context: Context, attrs: AttributeSet) :
    FrameLayout(context, attrs), Comparable<OverlayObject> {

    var startTime: Long = 0
    var endTime: Long = 0
    var timestamp: Long = 0
    var isInEdit = false
        set(value) {
            field = value
            if (isInEdit) {
                border!!.setBackgroundResource(R.drawable.rounded_border_tv)
                closeButton!!.visibility = View.VISIBLE
                border!!.tag = true
            } else {
                border!!.setBackgroundResource(R.drawable.rounded_border_unselect_tv)
                closeButton!!.visibility = View.GONE
                border!!.tag = false
            }
        }

    val closeButton: ImageView? by lazy {
        findViewById<ImageView>(R.id.imgPhotoEditorClose)
    }

    val border: FrameLayout? by lazy {
        findViewById<FrameLayout>(R.id.frmBorder)
    }

    override fun compareTo(other: OverlayObject): Int {
        Log.d("FAF", "compare")
        return (this.startTime - other.startTime).toInt()
    }
}