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
    private var drawPlaceHolder = true

    override fun onDraw(canvas: Canvas?) {
        if (drawable == null) {
            setBackgroundColor(placeHolderColor)
            return
        }
        // check if bitmap is null or recycled then draw placeholder
        if (drawable is BitmapDrawable) {
            if ((drawable as BitmapDrawable).bitmap == null || (drawable as BitmapDrawable).bitmap.isRecycled) {
                if (!drawPlaceHolder) {
                    setBackgroundColor(placeHolderColor)
                    drawPlaceHolder = true
                }
                return
            }
        }
        super.onDraw(canvas)
    }
}