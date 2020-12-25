package com.khoben.autotitle.ui.overlay

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.khoben.autotitle.App
import com.khoben.autotitle.R
import com.khoben.autotitle.common.RandomColor

class OverlayText(context: Context, attrs: AttributeSet) : OverlayObject(context, attrs) {

    val badgeColor: Int by lazy {
        RandomColor.color
    }

    val textView: TextView? by lazy {
        findViewById(R.id.overlay_text)
    }
    var text: String?
        get() = textView!!.text.toString()
        set(value) {
            textView!!.text = value
        }
}