package com.khoben.autotitle.ui.player.seekbar

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView

class FrameImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {
    private val placeHolderColor = Color.LTGRAY
    private val transparentColor = Color.TRANSPARENT

    override fun onDraw(canvas: Canvas?) {
        if (drawable == null) {
            setBackgroundColor(placeHolderColor)
        } else {
            setBackgroundColor(transparentColor)
        }
        super.onDraw(canvas)
    }
}