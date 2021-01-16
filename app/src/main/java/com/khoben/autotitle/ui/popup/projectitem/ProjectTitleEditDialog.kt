package com.khoben.autotitle.ui.popup.projectitem

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.khoben.autotitle.common.ViewUtils.focusAndShowKeyboard
import com.khoben.autotitle.databinding.RecyclerEditTitleBinding
import com.khoben.autotitle.extension.dp

class ProjectTitleEditDialog: DialogFragment() {
    private var binding: RecyclerEditTitleBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = RecyclerEditTitleBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setCanceledOnTouchOutside(false)
        val idx = requireArguments().getInt(EXTRA_IDX_ITEM)
        val title = requireArguments().getString(EXTRA_TITLE_ITEM)
        binding!!.editText.setText(title)
        binding!!.editText.focusAndShowKeyboard()
        binding!!.editTitleOkBtn.setOnClickListener {
            dismissAllowingStateLoss()
            listener?.onEditedItem(idx, binding!!.editText.text.toString())
        }
        binding!!.editTitleCancelBtn.setOnClickListener {
            dismissAllowingStateLoss()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window!!.setLayout(
            350.dp(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private var listener: ItemTitleEditListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is ItemTitleEditListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement ItemTitleEditListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface ItemTitleEditListener {
        fun onEditedItem(idx: Int, title: String)
    }

    companion object {
        const val EXTRA_IDX_ITEM = "extra_idx_item"
        const val EXTRA_TITLE_ITEM = "extra_title_item"

        @JvmStatic
        fun show(idx: Int, title: String): ProjectTitleEditDialog {
            return ProjectTitleEditDialog().apply {
                arguments = Bundle().apply {
                    putInt(EXTRA_IDX_ITEM, idx)
                    putString(EXTRA_TITLE_ITEM, title)
                }
            }
        }
    }
}