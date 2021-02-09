package com.khoben.autotitle.ui.recyclerview.overlays

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.khoben.autotitle.R
import com.khoben.autotitle.common.StyledAttrUtils
import com.khoben.autotitle.databinding.RecyclerViewOverlayItemBinding
import com.khoben.autotitle.ui.overlay.OverlayDataMapper
import java.util.*

class OverlayViewListAdapter :
    ListAdapter<OverlayDataMapper,
            OverlayViewListAdapter.OverlayViewHolder>(OverlayViewDiffCallback()),
    RecyclerViewItemEventListener {

    private var selectListener: SelectionTouchListener? = null

    var listItemEventListener: RecyclerViewItemEventListener? = null
    override fun onMoveUp(id: Int, start: Int, end: Int, text: String?) {
        listItemEventListener?.onMoveUp(id, start, end, text)
    }

    override fun onMoveDown(id: Int, start: Int, end: Int, text: String?) {
        listItemEventListener?.onMoveDown(id, start, end, text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OverlayViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = RecyclerViewOverlayItemBinding.inflate(layoutInflater, parent, false)
        return OverlayViewHolder(binding)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: OverlayViewHolder, position: Int) {
        val item = getItem(position)
        holder.setSelected(item.isSelected)
        holder.bind(item)
    }

    fun addSelectListener(selectTouchListener: SelectionTouchListener) {
        this.selectListener = selectTouchListener
        this.selectListener!!.selectionController =
            object : SelectionTouchListener.SelectionControllerListener {
                override fun onMoveDown(uuid: UUID?, start: Int, end: Int, text: CharSequence) {

                }

                override fun onMoveUp(uuid: UUID?, start: Int, end: Int, text: CharSequence) {

                }

            }
    }

    inner class OverlayViewHolder(private val binding: RecyclerViewOverlayItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val itemSelectedColor by lazy {
            StyledAttrUtils.getColor(itemView.context, R.attr.colorControlHighlight)
        }

        private val surfaceColor by lazy {
            StyledAttrUtils.getColor(itemView.context, R.attr.colorSurface)
        }

        fun bind(overlay: OverlayDataMapper) {
            binding.itemContent.setOnLongClickListener { textView ->
                selectListener?.startLongPressSelect(textView as TextView, overlay.uuid)
                true
            }
            binding.overlay = overlay
            binding.executePendingBindings()
            selectListener?.applySelection(binding.itemContent, overlay.uuid)
        }

        fun setSelected(isSelected: Boolean) {
            if (isSelected)
                itemView.setBackgroundColor(itemSelectedColor)
            else
                itemView.setBackgroundColor(surfaceColor)
        }
    }
}