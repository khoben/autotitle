package com.khoben.autotitle.huawei.extension

import android.view.View
import android.view.ViewGroup
import com.khoben.autotitle.huawei.common.DisplayUtils

fun View.getBitmap(parent: View, scaleX: Float = 1F, scaleY: Float = 1F) =
    DisplayUtils.getBitmapFromView(this, parent, scaleX, scaleY)

fun View.getRealLocationOnScreen() = DisplayUtils.getRealLocationOnScreen(this)

fun View.getPositionInParent(parent: View) =
    DisplayUtils.getPositionInParent(parent as ViewGroup, this)

