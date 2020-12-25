package com.khoben.autotitle.ui.popup

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.khoben.autotitle.R

class LoadingDialog(val context: Context) {
    private val inflater = LayoutInflater.from(context)
    private var view: View? = null
    private var dialog: AlertDialog? = null
    private var curPercentTextView: TextView? = null

    fun show(hint: String = "") {
        view = inflater.inflate(R.layout.popup_window_video_process_layout, null)
            .apply {
                findViewById<TextView>(R.id.tv_hint).text = hint
            }
        curPercentTextView = view!!.findViewById(R.id.pop_video_percent_tv)
        dialog = MaterialAlertDialogBuilder(context, R.style.CustomAlertDialog).apply {
            setCancelable(false)
            setView(view)
        }.create()
        dialog!!.show()
    }

    fun isShowing(): Boolean {
        return dialog != null && dialog!!.isShowing
    }

    fun updatePercentage(percentage: String) {
        curPercentTextView?.text = percentage
    }

    fun dismiss() = dialog?.dismiss()
}