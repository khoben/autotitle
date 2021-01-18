package com.khoben.autotitle.ui.overlay

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.khoben.autotitle.R

class OverlayFactory(context: Context) {
    private val layoutInflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    fun get(overlayType: OverlayType): View {
        val layout = when (overlayType) {
            OverlayType.TEXT -> {
                layoutInflater.inflate(R.layout.text_overlay_layout, null)
            }
            OverlayType.IMAGE -> {
                layoutInflater.inflate(R.layout.image_overlay_layout, null)
            }
        }
        return layout.apply { tag = overlayType }
    }
}