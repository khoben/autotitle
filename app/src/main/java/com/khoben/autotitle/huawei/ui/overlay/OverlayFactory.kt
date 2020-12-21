package com.khoben.autotitle.huawei.ui.overlay

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.khoben.autotitle.huawei.R

class OverlayFactory(context: Context) {
    private val layoutInflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    fun get(overlayType: OverlayType): View {
        val layout: View?
        when (overlayType) {
            OverlayType.TEXT -> {
                layout = layoutInflater.inflate(R.layout.text_overlay_layout, null)
                val txtText: TextView = layout.findViewById<TextView>(R.id.overlay_text)
                txtText.gravity = Gravity.CENTER
            }
            OverlayType.IMAGE -> {
                layout = layoutInflater.inflate(R.layout.image_overlay_layout, null)
            }
        }
        layout.tag = overlayType
        return layout
    }
}