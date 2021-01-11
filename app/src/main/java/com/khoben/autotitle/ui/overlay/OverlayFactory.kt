package com.khoben.autotitle.ui.overlay

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.khoben.autotitle.R
import timber.log.Timber

class OverlayFactory(context: Context) {
    private val layoutInflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    fun get(overlayType: @OverlayType Int): View {
        var layout: View? = null
        when (overlayType) {
            TEXT -> {
                layout = layoutInflater.inflate(R.layout.text_overlay_layout, null)
                val txtText = layout.findViewById<TextView>(R.id.overlay_content)
                txtText.gravity = Gravity.CENTER
            }
            IMAGE -> {
                layout = layoutInflater.inflate(R.layout.custom_overlay_layout, null)
            }
            else -> {
                Timber.e("Unsupported type")
            }
        }
        layout!!.tag = overlayType
        return layout
    }
}