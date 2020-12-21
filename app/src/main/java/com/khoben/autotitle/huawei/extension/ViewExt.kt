package com.khoben.autotitle.huawei.extension

import android.view.View
import android.view.ViewGroup
import com.khoben.autotitle.huawei.common.ViewUtils

fun View.getBitmap(parent: View, scaleX: Float = 1F, scaleY: Float = 1F) =
    ViewUtils.getBitmapFromView(this, parent, scaleX, scaleY)

fun View.getRealLocationOnScreen() = ViewUtils.getRealLocationOnScreen(this)

fun View.getPositionInParent(parent: View) =
    ViewUtils.getPositionInParent(parent as ViewGroup, this)

