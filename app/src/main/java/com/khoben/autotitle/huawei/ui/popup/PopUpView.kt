package com.khoben.autotitle.huawei.ui.popup

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import com.khoben.autotitle.huawei.R


class PopUpClass {
    @SuppressLint("ClickableViewAccessibility")
    fun showPopupWindow(context: Context, content: String, dimPercent: Float = 0.5f) {
        val inflater = LayoutInflater.from(context)
        val popupView: View = inflater.inflate(R.layout.popup_view, null)

        val width = LinearLayout.LayoutParams.MATCH_PARENT
        val height = LinearLayout.LayoutParams.WRAP_CONTENT

        val focusable = true
        val popupWindow = PopupWindow(popupView, width, height, focusable)
        popupWindow.showAtLocation(
            LayoutInflater.from(context).inflate(
                R.layout.base_activity,
                null
            ), Gravity.CENTER, 0, 0
        )
        val textView: TextView = popupView.findViewById(R.id.titleText)
        textView.text = content
        val okButton: Button = popupView.findViewById(R.id.messageButton)
        okButton.setOnClickListener {
            popupWindow.dismiss()
        }
        popupView.setOnTouchListener { v, event -> false }
        popupWindow.dimBehind(dimPercent)
    }

    private fun PopupWindow.dimBehind(dimPercent: Float) {
        val container = contentView.rootView
        val context = contentView.context
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val p = container.layoutParams as WindowManager.LayoutParams
        p.flags = p.flags or WindowManager.LayoutParams.FLAG_DIM_BEHIND
        p.dimAmount = dimPercent
        wm.updateViewLayout(container, p)
    }
}