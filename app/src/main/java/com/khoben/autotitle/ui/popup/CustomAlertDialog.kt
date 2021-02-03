package com.khoben.autotitle.ui.popup

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.khoben.autotitle.R

class CustomAlertDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val title = arguments?.getCharSequence(EXTRA_TITLE, "")
        val body = arguments?.getCharSequence(EXTRA_BODY, "")
        val token = arguments?.getString(EXTRA_TOKEN, "")

        val positiveText = arguments?.getString(EXTRA_POSITIVE_TEXT, null)
        val negativeText = arguments?.getString(EXTRA_NEGATIVE_TEXT, null)
        val neutralText = arguments?.getString(EXTRA_NEUTRAL_TEXT, null)

        return MaterialAlertDialogBuilder(requireContext(), R.style.CustomAlertDialog)
            .setTitle(title)
            .setMessage(body)
            .setCancelable(false)
            .also { builder ->
                positiveText?.let { text ->
                    builder.setPositiveButton(text) { _, _ ->
                        dismissAllowingStateLoss()
                        listener?.dialogOnPositive(token!!)
                    }
                }
                negativeText?.let { text ->
                    builder.setNegativeButton(text) { _, _ ->
                        dismissAllowingStateLoss()
                        listener?.dialogOnNegative(token!!)
                    }
                }
                neutralText?.let { text ->
                    builder.setNeutralButton(text) { _, _ ->
                        dismissAllowingStateLoss()
                        listener?.dialogOnNeutral(token!!)
                    }
                }
            }
            .create()
    }

    interface DialogClickListener {
        fun dialogOnNegative(token: String)
        fun dialogOnPositive(token: String)
        fun dialogOnNeutral(token: String)
    }

    private var listener: DialogClickListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is DialogClickListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement DialogClickListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    class Builder {
        private var positiveText: String? = null
        private var negativeText: String? = null
        private var neutralText: String? = null

        fun setPositive(text: String): Builder {
            positiveText = text
            return this
        }

        fun setNegative(text: String): Builder {
            negativeText = text
            return this
        }

        fun setNeutral(text: String): Builder {
            neutralText = text
            return this
        }

        fun build(title: CharSequence, body: CharSequence, token: String): CustomAlertDialog {
            return CustomAlertDialog().apply {
                arguments = Bundle().apply {
                    putCharSequence(EXTRA_TITLE, title)
                    putCharSequence(EXTRA_BODY, body)
                    putString(EXTRA_TOKEN, token)

                    putString(EXTRA_POSITIVE_TEXT, positiveText)
                    putString(EXTRA_NEGATIVE_TEXT, negativeText)
                    putString(EXTRA_NEUTRAL_TEXT, neutralText)
                }
            }
        }
    }

    companion object {
        const val TAG = "alert_dialog_builder"
        private const val EXTRA_TOKEN = "extra_token"
        private const val EXTRA_TITLE = "extra_title"
        private const val EXTRA_BODY = "extra_body"

        private const val EXTRA_POSITIVE_TEXT = "extra_positive_text"
        private const val EXTRA_NEGATIVE_TEXT = "extra_negative_text"
        private const val EXTRA_NEUTRAL_TEXT = "extra_neutral_text"
    }
}