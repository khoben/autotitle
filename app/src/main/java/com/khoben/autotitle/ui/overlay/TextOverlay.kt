package com.khoben.autotitle.ui.overlay

import android.graphics.*
import android.text.StaticLayout
import android.text.TextPaint
import com.khoben.autotitle.common.FontProvider
import timber.log.Timber
import kotlin.math.max

class TextOverlay(
    layout: TextLayout,
    canvasWidth: Int,
    canvasHeight: Int,
    private val fontProvider: FontProvider
) : OverlayEntity(layout, canvasWidth, canvasHeight) {

    private var bitmap: Bitmap? = null
    private val textPaint = TextPaint()

    init {
        update()
    }

    private fun update(moveToCenter: Boolean = false) {
        // save previous center
        val oldCenter = absoluteCenter()
        val newBmp = createBitmap(layout as TextLayout, bitmap)

        Timber.d("update newbmp: ${newBmp.width} x ${newBmp.height}")

        // recycle previous bitmap (if not reused) as soon as possible
        if (bitmap != null && bitmap != newBmp && !bitmap!!.isRecycled) {
            bitmap!!.recycle()
        }

        bitmap = newBmp

        val width = bitmap!!.width.toFloat()
        val height = bitmap!!.height.toFloat()
        val widthAspect = 1.0f * canvasWidth / width

        // for text we always match text width with parent width
        holyScale = 1F

        // initial position of the entity

        // initial position of the entity
        srcPoints[0] = 0F
        srcPoints[1] = 0F
        srcPoints[2] = width
        srcPoints[3] = 0F
        srcPoints[4] = width
        srcPoints[5] = height
        srcPoints[6] = 0F
        srcPoints[7] = height
        srcPoints[8] = 0F
        srcPoints[8] = 0F

        if (moveToCenter) {
            // move to previous center
            moveCenterTo(oldCenter)
        }
    }

    /**
     * If reuseBmp is not null, and size of the new bitmap matches the size of the reuseBmp,
     * new bitmap won't be created, reuseBmp it will be reused instead
     *
     * @param textLayout text to draw
     * @param reuseBmp  the bitmap that will be reused
     * @return bitmap with the text
     */
    private fun createBitmap(textLayout: TextLayout, reuseBmp: Bitmap?): Bitmap {

        // init params - size, color, typeface
        val font = textLayout.getFont()!!
        textPaint.style = Paint.Style.FILL
        textPaint.textSize = font.getSize() * canvasWidth
        textPaint.color = font.getColor()
        textPaint.typeface = fontProvider.getTypeface(font.getTypeface())

        val text = textLayout.getText()!!
        val boundsWidth = text.split("\n").maxOf {
            line -> textPaint.measureText(line).toInt()
        }

        // drawing text guide : http://ivankocijan.xyz/android-drawing-multiline-text-on-canvas/
        // Static layout which will be drawn on canvas

        val sl = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            StaticLayout.Builder
                .obtain(text, 0, text.length, textPaint, boundsWidth)
                .setAlignment(android.text.Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(1F, 1F)
                .setIncludePad(true)
                .build()
        } else {
            StaticLayout(
                textLayout.getText(),
                textPaint,
                boundsWidth,
                android.text.Layout.Alignment.ALIGN_NORMAL,
                1F,
                1F,
                true
            )
        }

        // calculate height for the entity, min - Limits.MIN_BITMAP_HEIGHT
        val boundsHeight = sl.height

        // create bitmap not smaller than TextLayer.Limits.MIN_BITMAP_HEIGHT
        val bmpHeight = (canvasHeight * max(
            TextLayout.MIN_BITMAP_HEIGHT,
            1.0f * boundsHeight / canvasHeight
        )).toInt()

        Timber.d("createBitmap: w = $boundsWidth h = $bmpHeight")

        // create bitmap where text will be drawn
        val bmp: Bitmap
        if (reuseBmp != null && reuseBmp.width == boundsWidth && reuseBmp.height == bmpHeight) {
            // if previous bitmap exists, and it's width/height is the same - reuse it
            bmp = reuseBmp
            bmp.eraseColor(Color.TRANSPARENT) // erase color when reusing
        } else {
            bmp = Bitmap.createBitmap(boundsWidth, bmpHeight, Bitmap.Config.ARGB_8888)
        }
        val canvas = Canvas(bmp)
        canvas.save()

        // move text to center if bitmap is bigger that text
        if (boundsHeight < bmpHeight) {
            //calculate Y coordinate - In this case we want to draw the text in the
            //center of the canvas so we move Y coordinate to center.
            val textYCoordinate = ((bmpHeight - boundsHeight) / 2).toFloat()
            canvas.translate(0f, textYCoordinate)
        }

        //draws static layout on canvas
        sl.draw(canvas)
        canvas.restore()
        return bmp
    }

    override fun drawContent(canvas: Canvas, paint: Paint?) {
        bitmap?.let { canvas.drawBitmap(it, matrix, paint) }
    }

    override fun getWidth(): Int {
        return if (bitmap != null) bitmap!!.width else 0
    }

    override fun getHeight(): Int {
        return if (bitmap != null) bitmap!!.height else 0
    }

    override fun release() {
        if (bitmap != null && !bitmap!!.isRecycled) {
            bitmap!!.recycle()
        }
    }

}