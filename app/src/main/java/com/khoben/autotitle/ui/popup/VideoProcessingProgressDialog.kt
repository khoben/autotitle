package com.khoben.autotitle.ui.popup

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.khoben.autotitle.R
import java.lang.ref.WeakReference

class VideoProcessingProgressDialog(val context: WeakReference<Context>) {
    private val inflater = LayoutInflater.from(context.get())
    private var dialog: AlertDialog? = null

    private var mainView: View? = null
    private var curPercentTextView: TextView? = null
    private var loadingTextView: TextView? = null
    private var confirmationLayout: LinearLayout? = null
    private var cancelButton: Button? = null

    private var savedHintText = ""

    var listener: ProgressDialogListener? = null
    interface ProgressDialogListener {
        fun cancelBtnClicked()
        fun confirmCancelBtnClicked()
        fun nopeCancelBtnClicked()
    }

    fun show(hint: String = "") {
        mainView = inflater.inflate(R.layout.popup_window_video_process_layout, null)
            .apply {
                loadingTextView = findViewById<TextView>(R.id.loading_text).apply {
                    text = hint
                }
                confirmationLayout = findViewById(R.id.confirmation)
                curPercentTextView = findViewById(R.id.percentage)

                cancelButton = findViewById<Button>(R.id.cancel_btn).apply {
                    setOnClickListener { onCancelBtnClicked() }
                }

                findViewById<Button>(R.id.confirm).
                    setOnClickListener {
                        onConfirmCancelBtnClicked()
                    }

                findViewById<Button>(R.id.nope).
                    setOnClickListener {
                        onNopeCancelBtnClicked()
                    }

            }
        dialog = MaterialAlertDialogBuilder(context.get()!!, R.style.CustomAlertDialog).apply {
            setCancelable(false)
            setView(mainView!!)
        }.create()
        dialog!!.show()
    }

    private fun onNopeCancelBtnClicked() {
        listener?.nopeCancelBtnClicked()

        loadingTextView?.text = savedHintText
        cancelButton?.visibility = View.VISIBLE
        confirmationLayout?.visibility = View.INVISIBLE
    }

    private fun onConfirmCancelBtnClicked() {
        listener?.confirmCancelBtnClicked()
    }

    private fun onCancelBtnClicked() {
        listener?.cancelBtnClicked()

        loadingTextView?.apply {
            savedHintText = text.toString()
            text = "Confirm cancellation"
        }

        cancelButton?.visibility = View.INVISIBLE
        confirmationLayout?.visibility = View.VISIBLE
    }

    fun isShowing(): Boolean {
        return dialog != null && dialog!!.isShowing
    }

    fun updatePercentage(percentage: String) {
        curPercentTextView?.text = percentage
    }

    fun dismiss() {
        dialog?.dismiss()
        dialog = null
    }
}