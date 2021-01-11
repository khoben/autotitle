package com.khoben.autotitle.ui.popup

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.khoben.autotitle.R


class TextEditorDialogFragment : DialogFragment() {
    private var mInputMethodManager: InputMethodManager? = null
    private var mTextEditorEvent: TextEditorEvent? = null

    var mAddTextEditText: EditText? = null
    var mColorCode = 0

    interface TextEditorEvent {
        fun onDone(inputText: String?, colorCode: Int)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        dialog?.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.add_text_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mAddTextEditText = view.findViewById<EditText>(R.id.add_text_edit_text).apply {
            setText(requireArguments().getString(EXTRA_INPUT_TEXT))
        }
        mColorCode = requireArguments().getInt(EXTRA_COLOR_CODE)
        mAddTextEditText!!.setTextColor(mColorCode)

        mInputMethodManager =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        view.findViewById<RecyclerView>(R.id.add_text_color_picker_recycler_view).apply {
            layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
            setHasFixedSize(true)
            adapter = activity?.let {
                ColorPickerAdapter(it).apply {
                    onColorPickerClickListener = object :
                        ColorPickerAdapter.OnColorPickerClickListener {
                        override fun onColorPickerClickListener(colorCode: Int) {
                            mColorCode = colorCode
                            mAddTextEditText!!.setTextColor(colorCode)
                        }
                    }
                }
            }
        }

        // show keyboard
        mInputMethodManager!!.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
        mAddTextEditText!!.requestFocus()

        //Make a callback on activity when user is done with text editing
        view.findViewById<Button>(R.id.add_text_done_tv).setOnClickListener { v ->
            mInputMethodManager!!.hideSoftInputFromWindow(v.windowToken, 0)
            dismiss()
            val inputText = mAddTextEditText!!.text.toString()
            if (!TextUtils.isEmpty(inputText)) {
                mTextEditorEvent?.onDone(inputText, mColorCode)
            }
        }
    }

    fun setOnTextEditorListener(textEditorEvent: TextEditorEvent?) {
        mTextEditorEvent = textEditorEvent
    }

    companion object {
        private val TAG: String = TextEditorDialogFragment::class.java.simpleName
        const val EXTRA_INPUT_TEXT = "extra_input_text"
        const val EXTRA_COLOR_CODE = "extra_color_code"

        @JvmOverloads
        fun show(
            appCompatActivity: AppCompatActivity,
            inputText: String? = "",
            @ColorInt colorCode: Int = ContextCompat.getColor(appCompatActivity, R.color.white)
        ): TextEditorDialogFragment {
            return TextEditorDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(EXTRA_INPUT_TEXT, inputText)
                    putInt(EXTRA_COLOR_CODE, colorCode)
                }
                show(appCompatActivity.supportFragmentManager, TAG)
            }
        }
    }
}