package com.khoben.autotitle.ui.popup

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.khoben.autotitle.R

class AlertDialogInfoMessage : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val title = arguments?.getCharSequence(EXTRA_TITLE, "")
        val body = arguments?.getCharSequence(EXTRA_BODY, "")

        return MaterialAlertDialogBuilder(requireContext(), R.style.CustomAlertDialog)
            .setTitle(title)
            .setCancelable(false)
            .setMessage(body)
            .setPositiveButton(getString(R.string.edit_title_ok)) { _, _ ->
                dismissAllowingStateLoss()
            }.create()
    }


    companion object {
        const val TAG = "alert_dialog_one_button"
        private const val EXTRA_TITLE = "extra_title"
        private const val EXTRA_BODY = "extra_body"

        @JvmStatic
        fun new(title: CharSequence, body: CharSequence): AlertDialogInfoMessage {
            return AlertDialogInfoMessage().apply {
                arguments = Bundle().apply {
                    putCharSequence(EXTRA_TITLE, title)
                    putCharSequence(EXTRA_BODY, body)
                }
            }
        }
    }
}