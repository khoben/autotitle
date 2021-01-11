package com.khoben.autotitle.ui.overlay

import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.khoben.autotitle.R
import com.khoben.autotitle.common.RandomColor
import com.khoben.autotitle.extension.getBitmap

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

    override fun getBitmap(scaleX: Float, scaleY: Float): Bitmap? {
        return textView?.getBitmap(this, scaleX, scaleY)
    }
}