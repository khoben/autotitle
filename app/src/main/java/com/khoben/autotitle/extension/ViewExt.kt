package com.khoben.autotitle.extension

import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import com.khoben.autotitle.common.ViewUtils

/**
 * Get bounding rectangle of [View]
 */
fun View.getRect() = Rect(left, top, right, bottom)

/**
 * Get bitmap from [parent] with applied scale factors
 * ([scaleX] and [scaleY]), also it take attention of
 * origin scale factor of view
 *
 * @receiver View
 * @param parent View
 * @param scaleX Float
 * @param scaleY Float
 * @return Bitmap?
 */
fun View.getBitmap(parent: View, scaleX: Float = 1F, scaleY: Float = 1F) =
    ViewUtils.getBitmapFromView(this, parent, scaleX, scaleY)

/**
 * Get absolute position on screen of [View]
 * @receiver View
 * @return Pair<Int, Int> (x, y)
 */
fun View.getRealLocationOnScreen() = ViewUtils.getRealLocationOnScreen(this)

/**
 * Get relative position of [View] in [parent]
 *
 * @receiver View
 * @param parent View
 * @return IntArray [x, y]
 */
fun View.getPositionInParent(parent: View) =
    ViewUtils.getPositionInParent(parent as ViewGroup, this)

