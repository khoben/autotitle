package com.khoben.autotitle.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.widget.TextView

object ViewUtils {

    /**
     * https://developer.squareup.com/blog/showing-the-android-keyboard-reliably/
     * @receiver View
     */
    fun View.focusAndShowKeyboard() {
        /**
         * This is to be called when the window already has focus.
         */
        fun View.showTheKeyboardNow() {
            if (isFocused) {
                post {
                    // We still post the call, just in case we are being notified of the windows focus
                    // but InputMethodManager didn't get properly setup yet.
                    val imm =
                        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
                }
            }
        }

        requestFocus()
        if (hasWindowFocus()) {
            // No need to wait for the window to get focus.
            showTheKeyboardNow()
        } else {
            // We need to wait until the window gets focus.
            viewTreeObserver.addOnWindowFocusChangeListener(
                object : ViewTreeObserver.OnWindowFocusChangeListener {
                    override fun onWindowFocusChanged(hasFocus: Boolean) {
                        // This notification will arrive just before the InputMethodManager gets set up.
                        if (hasFocus) {
                            this@focusAndShowKeyboard.showTheKeyboardNow()
                            // It’s very important to remove this listener once we are done.
                            viewTreeObserver.removeOnWindowFocusChangeListener(this)
                        }
                    }
                })
        }
    }

    /**
     * Get bitmap from [view] with [parentView] scale properties
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

        val newSizeX = displayedWidth * scaleX
        val newSizeY = displayedHeight * scaleY

        if (newSizeX < 1 || newSizeY < 1) return null

        val pivotX = newSizeX / 2F
        val pivotY = newSizeY / 2F

        val returnedBitmap =
            Bitmap.createBitmap(
                (newSizeX).toInt(),
                (newSizeY).toInt(),
                Bitmap.Config.ARGB_8888
            )
        val canvas = Canvas(returnedBitmap)
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
     * @return Pair<Int, Int>
     */
    fun getPositionInParent(parent: ViewGroup, view: View): Pair<Int, Int> {
        val relativePosition = intArrayOf(view.left, view.top)
        var currentParent = view.parent as ViewGroup
        while (currentParent !== parent) {
            relativePosition[0] += currentParent.left
            relativePosition[1] += currentParent.top
            currentParent = currentParent.parent as ViewGroup
        }
        return Pair(relativePosition[0], relativePosition[1])
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