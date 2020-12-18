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
import kotlin.math.max


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
        _scaleX: Float,
        _scaleY: Float
    ): Bitmap? {

        val displayedWidth = parentView.measuredWidth
        val displayedHeight = parentView.measuredHeight
        val scaleX = parentView.scaleX * _scaleX
        val scaleY = parentView.scaleY * _scaleY

        var newSizeX = displayedWidth * scaleX
        var newSizeY = displayedHeight * scaleY

        if (newSizeX < 1 || newSizeY < 1) return null

        val maxSize = max(newSizeX, newSizeY)
        newSizeX = maxSize
        newSizeY = maxSize

        val pivotX = maxSize / 2
        val pivotY = maxSize / 2
        val rotation = parentView.rotation

        val returnedBitmap =
            Bitmap.createBitmap(
                (maxSize).toInt(),
                (maxSize).toInt(),
                Bitmap.Config.ARGB_8888
            )
        val canvas = Canvas(returnedBitmap)
        canvas.rotate(rotation, pivotX, pivotY)
        canvas.scale(scaleX, scaleY, pivotX, pivotY)
        if (view is TextView) {
            // text metrics
            val txtWidth = view.width
            val txtHeight = view.height
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

    fun getRealLocationOnScreen(view: View): Pair<Int, Int> {
        val point = IntArray(2)
        view.getLocationOnScreen(point)
        return Pair(point[0], point[1])
    }
}