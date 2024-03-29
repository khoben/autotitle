package com.khoben.autotitle.ui.popup.projectitem

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.khoben.autotitle.databinding.RecyclerBottomSheetBinding

class ProjectItemOptionsDialog : BottomSheetDialogFragment() {

    private var binding: RecyclerBottomSheetBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = RecyclerBottomSheetBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val idx = requireArguments().getLong(EXTRA_IDX_ITEM)
        binding?.projectItemEditBtn?.setOnClickListener {
            dismissAllowingStateLoss()
            listener?.onEditTitleClick(idx)
        }
        binding?.projectItemDeleteBtn?.setOnClickListener {
            dismissAllowingStateLoss()
            listener?.onRemoveClick(idx)
        }
    }

    private var listener: ItemClickListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is ItemClickListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement ItemClickListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface ItemClickListener {
        fun onEditTitleClick(id: Long)
        fun onRemoveClick(id: Long)
    }

    companion object {
        const val EXTRA_IDX_ITEM = "extra_idx_item"

        @JvmStatic
        fun show(id: Long): ProjectItemOptionsDialog {
            return ProjectItemOptionsDialog().apply {
                arguments = Bundle().apply {
                    putLong(EXTRA_IDX_ITEM, id)
                }
            }
        }
    }

}