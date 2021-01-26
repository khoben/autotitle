package com.khoben.autotitle.ui.recyclerview

import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.khoben.autotitle.R
import com.khoben.autotitle.common.FileUtils
import com.khoben.autotitle.extension.formattedTime
import com.khoben.autotitle.model.project.ThumbProject
import com.khoben.autotitle.ui.overlay.OverlayDataMapper
import timber.log.Timber

@BindingAdapter("overlayFromString")
fun TextView.setOverlayFromString(item: OverlayDataMapper) {
    text = item.startTime.formattedTime()
}

@BindingAdapter("overlayToString")
fun TextView.setOverlayToString(item: OverlayDataMapper) {
    text = item.endTime.formattedTime()
}

@BindingAdapter("overlayContentString")
fun TextView.setOverlayContentString(item: OverlayDataMapper) {
    text = if (item.text.isEmpty()) {
        setTypeface(typeface, Typeface.ITALIC)
        context.getString(R.string.empty_overlay_recycler)
    } else {
        setTypeface(typeface, Typeface.NORMAL)
        item.text
    }
}

@BindingAdapter("badgeColor")
fun ImageView.setBadgeColor(item: OverlayDataMapper) {
    background.setTint(item.badgeColor)
}

@BindingAdapter("thumb")
fun ImageView.setThumb(item: ThumbProject) {
    // load bitmap
    val path = item.getThumbPath()
    if (FileUtils.checkIfExists(path)) {
        setImageBitmap(BitmapFactory.decodeFile(path))
    } else {
        setImageDrawable(ContextCompat.getDrawable(context, R.drawable.image_placeholder))
        Timber.e("Trying to load not existing thumb")
    }
}
