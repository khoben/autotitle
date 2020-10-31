package com.khoben.autotitle.huawei.extension

import android.view.View
import android.view.ViewGroup
import com.khoben.autotitle.huawei.common.DisplayUtils

fun View.getBitmap(parent: View, isWide: Boolean, scaleX: Float = 1F, scaleY: Float = 1F) =
    DisplayUtils.getBitmapFromView(this, parent, isWide, scaleX, scaleY)

fun View.getPositionInParent(parent: View) =
    DisplayUtils.getPositionInParent(parent as ViewGroup, this)

