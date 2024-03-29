package com.khoben.autotitle.ui.popup

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.khoben.autotitle.R
import com.khoben.autotitle.databinding.PopupWindowVideoProcessLayoutBinding

class VideoProcessingProgressDialog : DialogFragment() {

    private lateinit var binding: PopupWindowVideoProcessLayoutBinding

    private var curPercentTextView: TextView? = null
    private var loadingTextView: TextView? = null
    private var confirmationLayout: LinearLayout? = null
    private var cancelButton: Button? = null

    private var listener: ProgressDialogListener? = null

    private var savedHintText = ""

    private val EXTRA_BUTTON_STATE = "BUTTON_STATE"
    private val EXTRA_PERCENT_STATE = "EXTRA_PERCENT_STATE"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = PopupWindowVideoProcessLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.apply {
            setCancelable(false)
            setCanceledOnTouchOutside(false)
        }
        loadingTextView = binding.loadingText.apply { text = arguments?.getString(EXTRA_HINT) }
        confirmationLayout = binding.confirmation
        curPercentTextView = binding.percentage
        cancelButton = binding.cancelBtn.apply { setOnClickListener { onCancelBtnClicked() } }
        binding.confirm.setOnClickListener { onConfirmCancelBtnClicked() }
        binding.nope.setOnClickListener { onNopeCancelBtnClicked() }

        savedInstanceState?.let { bundle ->
            curPercentTextView?.text = bundle.getCharSequence(EXTRA_PERCENT_STATE, "")
            if (!bundle.getBoolean(EXTRA_BUTTON_STATE)) {
                onCancelBtnClicked()
            }
        }
    }

    interface ProgressDialogListener {
        fun cancelBtnClicked()
        fun confirmCancelBtnClicked()
        fun nopeCancelBtnClicked()
        fun onAttachBack(dialog: VideoProcessingProgressDialog)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is ProgressDialogListener) {
            listener = context
            listener?.onAttachBack(this)
        } else {
            throw RuntimeException("$context must implement ProgressDialogListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(EXTRA_BUTTON_STATE, confirmationLayout?.visibility == View.INVISIBLE)
        outState.putCharSequence(EXTRA_PERCENT_STATE, curPercentTextView?.text)
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
            text = context.getString(R.string.confirm_cancellation_title)
        }

        cancelButton?.visibility = View.INVISIBLE
        confirmationLayout?.visibility = View.VISIBLE
    }

    fun updatePercentage(percentage: String) {
        curPercentTextView?.text = percentage
    }

    companion object {
        const val TAG = "video_processing_dialog"
        private val EXTRA_HINT = "EXTRA_HINT"

        fun new(hint: String = ""): VideoProcessingProgressDialog {
            return VideoProcessingProgressDialog().apply {
                arguments = Bundle().apply {
                    putString(EXTRA_HINT, hint)
                }
            }
        }
    }
}