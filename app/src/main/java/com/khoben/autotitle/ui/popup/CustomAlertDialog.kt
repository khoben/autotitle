package com.khoben.autotitle.ui.popup

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.khoben.autotitle.R

class CustomAlertDialog(val context: Context) {
    private val inflater = LayoutInflater.from(context)
    private var view: View? = null
    private var dialog: AlertDialog? = null

    fun show(message: String = "") {
        view = inflater.inflate(R.layout.popup_view, null)
        view!!.findViewById<TextView>(R.id.titleText).text = message
        view!!.findViewById<TextView>(R.id.messageButton).setOnClickListener {
            dismiss()
        }
        dialog = MaterialAlertDialogBuilder(context, R.style.CustomAlertDialog).apply {
            setCancelable(false)
            setView(view)
        }.create()
        dialog!!.show()
    }

    private fun dismiss() = dialog?.dismiss()
    fun isShowing() = dialog != null && dialog!!.isShowing
}