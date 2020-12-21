package com.khoben.autotitle.huawei.ui.overlay

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import com.khoben.autotitle.huawei.R
import com.khoben.autotitle.huawei.common.RandomColor

class OverlayText(context: Context, attrs: AttributeSet) : OverlayObject(context, attrs) {

    val badgeColor: Int by lazy {
        RandomColor.color
    }

    val textView: TextView? by lazy {
        findViewById<TextView>(R.id.overlay_text)
    }
    var text: String?
        get() = textView!!.text.toString()
        set(value) {
            textView!!.text = value
        }
}