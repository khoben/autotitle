package com.khoben.autotitle.ui.popup

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.khoben.autotitle.R

class CustomAlertDialog(
    private val context: Context,
    @LayoutRes private val layout: Int,
    @IdRes private val messageTextView: Int,
    private var content: String? = null,
    @IdRes private var okButton: Int? = null,
    @IdRes private var cancelButton: Int? = null,
    private var okButtonText: String? = null,
    private var cancelButtonText: String? = null
) {
    private val inflater = LayoutInflater.from(context)
    private var view: View? = null
    private var dialog: AlertDialog? = null

    fun show(
        message: String = "",
        onOkClicked: (() -> Unit)? = null,
        onCancelClicked: (() -> Unit)? = null
    ) {
        view = inflater.inflate(layout, null)
        view!!.findViewById<TextView>(messageTextView).text = content ?: message
        okButton?.let {
            view!!.findViewById<TextView>(it).apply {
                text = okButtonText ?: text
            }.setOnClickListener {
                dismiss()
                onOkClicked?.invoke()
            }
        }
        cancelButton?.let {
            view!!.findViewById<TextView>(it).apply {
                text = cancelButtonText ?: text
            }.setOnClickListener {
                dismiss()
                onCancelClicked?.invoke()
            }
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