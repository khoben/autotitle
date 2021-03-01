package com.khoben.autotitle.ui.overlay

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.khoben.autotitle.R
import com.khoben.autotitle.common.PseudoRandomColorGenerator
import com.khoben.autotitle.extension.getBitmap

class OverlayText(context: Context, attrs: AttributeSet) : OverlayObject(context, attrs) {

    private val timeRangeBackgroundColor =
        ContextCompat.getColor(context, R.color.timelineBackgroundColor)

    val timeline = View(context).apply {
        setBackgroundColor(timeRangeBackgroundColor)
    }

    val badgeColor: Int by lazy {
        PseudoRandomColorGenerator.color
    }

    val textView: TextView? by lazy {
        findViewById(R.id.overlay_content)
    }

    var text: String?
        get() = textView!!.text.toString()
        set(value) {
            textView!!.text = value
        }

    private val backgroundPaint = Paint().apply {
        color = Color.BLACK
    }

    override fun onDraw(canvas: Canvas) {
        // TODO: Make rounded background
//        canvas.drawRoundRect(0F, 0F, width.toFloat(), height.toFloat(), 10F, 10F, backgroundPaint)
        super.onDraw(canvas)
    }

    override fun getBitmap(scaleX: Float, scaleY: Float): Bitmap? {
        return textView?.getBitmap(this, scaleX, scaleY)
    }
}