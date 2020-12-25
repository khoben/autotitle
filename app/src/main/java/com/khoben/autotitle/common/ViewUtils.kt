package com.khoben.autotitle.common

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlin.math.max

object ViewUtils {

    /**
     * Get bitmap from [view] with [parentView] scale/rotation properties
     * @param view
     * @param parentView
     * @param _scaleX
     * @param _scaleY
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
        returnedBitmap.setHasAlpha(true)
        returnedBitmap.eraseColor(Color.TRANSPARENT)
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

    /**
     * Gets relative position of [view] in the [parent]
     * @param parent ViewGroup
     * @param view View
     * @return IntArray
     */
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

    /**
     * Gets absolute position of [view] on screen
     * @param view View
     * @return Pair<Int, Int>
     */
    fun getRealLocationOnScreen(view: View): Pair<Int, Int> {
        val point = IntArray(2)
        view.getLocationOnScreen(point)
        return Pair(point[0], point[1])
    }
}