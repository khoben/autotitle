package com.khoben.autotitle.ui.overlay

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.khoben.autotitle.R
import com.khoben.autotitle.common.RandomColor

class OverlayText(context: Context, attrs: AttributeSet) : OverlayObject(context, attrs) {

    private val timeRangeBackgroundColor =
        ContextCompat.getColor(context, R.color.timelineBackgroundColor)

    val timeline = View(context).apply {
        setBackgroundColor(timeRangeBackgroundColor)
    }

    val badgeColor: Int by lazy {
        RandomColor.color
    }

    val textView: TextView? by lazy {
        findViewById(R.id.overlay_content)
    }
    var text: String?
        get() = textView!!.text.toString()
        set(value) {
            textView!!.text = value
        }
}