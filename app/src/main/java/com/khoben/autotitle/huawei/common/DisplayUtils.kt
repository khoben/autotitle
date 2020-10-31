package com.khoben.autotitle.huawei.common

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.TextView


object DisplayUtils {
    fun dp2px(context: Context, dipValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dipValue * scale + 0.5f).toInt()
    }

    fun dipToPx(ctx: Context, dip: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dip,
            ctx.resources.displayMetrics
        )
            .toInt()
    }

    fun dipToPx(dip: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dip.toFloat(),
            Resources.getSystem().displayMetrics
        )
            .toInt()
    }

    private fun addBorder(bmp: Bitmap, borderSize: Int): Bitmap {
        val bmpWithBorder =
            Bitmap.createBitmap(bmp.width + borderSize * 2, bmp.height + borderSize * 2, bmp.config)
        val canvas = Canvas(bmpWithBorder)
        canvas.drawColor(Color.BLACK)
        canvas.drawBitmap(bmp, borderSize.toFloat(), borderSize.toFloat(), null)
        return bmpWithBorder
    }

    /**
     * Get bitmap from view with parent's scale/rotation properties
     * @param view
     * @param parentView
     * @return Bitmap?
     */
    fun getBitmapFromView(
        view: View,
        parentView: View,
        isWide: Boolean,
        _scaleX: Float,
        _scaleY: Float
    ): Bitmap? {

        val displayedWidth = parentView.measuredWidth
        val displayedHeight = parentView.measuredHeight
        val scaleX = parentView.scaleX * _scaleX
        val scaleY = parentView.scaleY * _scaleY

        val newSizeX = displayedWidth * scaleX
        val newSizeY = displayedHeight * scaleY

//        if (newSizeX < 1 || newSizeY < 1) return null

        val pivotX = newSizeX / 2
        val pivotY = newSizeY / 2
        val rotation = parentView.rotation

        val returnedBitmap =
            Bitmap.createBitmap(
                (newSizeX).toInt(),
                (newSizeY).toInt(),
                Bitmap.Config.ARGB_8888
            )
        val canvas = Canvas(returnedBitmap)
//        val bgDrawable: Drawable? = view.background
//        if (bgDrawable == null) {
//            canvas.drawColor(Color.WHITE)
//        } else {
//            bgDrawable.draw(canvas)
//        }
        canvas.rotate(rotation, pivotX, pivotY)
        canvas.scale(scaleX, scaleY, pivotX, pivotY)
        if (view is TextView) {
            // text metrics
            view.measure(0, 0)
            val txtWidth = view.measuredWidth
            val txtHeight = view.measuredHeight
            // apply transformation
            canvas.translate((newSizeX - txtWidth) / 2F, (newSizeY - txtHeight) / 2F)
        }
        view.draw(canvas)
        return returnedBitmap
    }

    fun getPositionInParent(parent: ViewGroup, view: View): IntArray {
        val relativePosition = intArrayOf(view.left, view.top)
        var currentParent = view.parent as ViewGroup
        while (currentParent !== parent) {
            relativePosition[0] += currentParent.left
            relativePosition[1] += currentParent.top
            currentParent = currentParent.parent as ViewGroup
        }
        return relativePosition
    }
}