package com.khoben.autotitle.huawei.ui.recyclerview

import android.graphics.Typeface
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.khoben.autotitle.huawei.R
import com.khoben.autotitle.huawei.common.RandomColor
import com.khoben.autotitle.huawei.extension.toReadableTimeString
import com.khoben.autotitle.huawei.ui.overlay.OverlayDataMapper

@BindingAdapter("overlayFromString")
fun TextView.setOverlayFromString(item: OverlayDataMapper) {
    text = item.startTime.toReadableTimeString()
}

@BindingAdapter("overlayToString")
fun TextView.setOverlayToString(item: OverlayDataMapper) {
    text = item.endTime.toReadableTimeString()
}

@BindingAdapter("overlayContentString")
fun TextView.setOverlayContentString(item: OverlayDataMapper) {
    text = if (item.text.isEmpty()) {
        setTypeface(typeface, Typeface.ITALIC)
        context.getString(R.string.empty_overlay_recycler)
    } else {
        setTypeface(typeface, Typeface.BOLD)
        item.text
    }
}

@BindingAdapter("badgeColor")
fun ImageView.setBadgeColor(item: OverlayDataMapper) {
    background.setTint(item.badgeColor)
}