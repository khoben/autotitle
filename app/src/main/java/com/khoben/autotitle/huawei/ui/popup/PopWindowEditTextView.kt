package com.khoben.autotitle.huawei.ui.popup

import android.app.Activity
import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.PopupWindow
import com.khoben.autotitle.huawei.R
import com.khoben.autotitle.huawei.databinding.PopupWindowTextEditLayoutBinding

class PopWindowEditTextView(private val context: Context) {
    private lateinit var edContent: EditText
    private lateinit var tvConfirm: Button
    private var popupWindow: PopupWindow? = null
    private lateinit var popupWindowView: View
    private var initText: String? = null
    private lateinit var textWatcher: TextWatcher
    val isShowing: Boolean
        get() = popupWindow!!.isShowing

    init {
        initPopupWindow()
    }

    private fun initPopupWindow() {
        popupWindow?.dismiss()
        val binding = PopupWindowTextEditLayoutBinding.inflate(LayoutInflater.from(context))
        popupWindowView = binding.root
        edContent = binding.edContent
        edContent.setOnClickListener {
            onViewClicked(it)
        }
        tvConfirm = binding.tvConfirm
        tvConfirm.setOnClickListener {
            onViewClicked(it)
        }
        popupWindow = PopupWindow(
            popupWindowView,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )
        popupWindow!!.inputMethodMode = PopupWindow.INPUT_METHOD_NEEDED
        popupWindow!!.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        popupWindow!!.isOutsideTouchable = true
        popupWindow!!.setBackgroundDrawable(BitmapDrawable())
        popupWindow!!.setOnDismissListener(PopupDismissListener())
        popupWindowView.setOnTouchListener { _, _ -> false }
    }

    fun backgroundAlpha(bgAlpha: Float) {
        val lp = (context as Activity).window.attributes
        lp.alpha = bgAlpha
        context.window.attributes = lp
    }

    private fun onViewClicked(view: View) {
        when (view.id) {
            R.id.ed_content -> {
            }
            R.id.tv_confirm -> {
                if (onTextSendListener != null) {
                    onTextSendListener!!.onTextSend(edContent.text.toString())
                }
                edContent.setText("")
                dismiss()
            }
        }
    }

    internal inner class PopupDismissListener : PopupWindow.OnDismissListener {
        override fun onDismiss() {
            backgroundAlpha(1f)
            closeKeyboard()
        }
    }

    private fun dismiss() {
        popupWindow?.dismiss()
        closeKeyboard()
    }

    private fun showKeyboard() {
        val inputMethodManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }

    private fun closeKeyboard() {
        val inputMethodManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
    }

    fun show(initText: String) {
        if (popupWindow != null && !popupWindow!!.isShowing) {
            this.initText = initText
            edContent.setText(initText)
            edContent.setSelection(initText.length)
            showKeyboard()
            popupWindow!!.showAtLocation(
                LayoutInflater.from(context).inflate(R.layout.base_activity, null),
                Gravity.BOTTOM, 0, 0
            )
            edContent.requestFocus()
        }
    }

    var onTextSendListener: OnTextSendListener? = null

    interface OnTextSendListener {
        fun onTextSend(text: String?)
    }

    companion object {
        private val TAG = PopWindowEditTextView::class.java.simpleName
    }
}