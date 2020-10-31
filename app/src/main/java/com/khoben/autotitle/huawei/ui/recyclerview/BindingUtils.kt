package com.khoben.autotitle.huawei.ui.recyclerview

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.khoben.autotitle.huawei.R
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
    if (item.text.isEmpty()) {
        text = context.getString(R.string.empty_overlay_recycler)
    } else {
        text = item.text
    }
}