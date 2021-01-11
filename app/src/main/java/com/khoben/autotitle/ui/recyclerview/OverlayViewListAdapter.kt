package com.khoben.autotitle.ui.recyclerview

import android.annotation.SuppressLint
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.khoben.autotitle.R
import com.khoben.autotitle.databinding.RecyclerViewItemBinding
import com.khoben.autotitle.ui.overlay.OverlayDataMapper
import java.util.*

class OverlayViewListAdapter :
        ListAdapter<OverlayDataMapper, OverlayViewListAdapter.OverlayViewHolder>(
                OverlayViewDiffCallback()
        ), RecyclerViewItemEventListener {

    private var selectedItemUUID: UUID? = null
    private var lastSelectedItemUUID: UUID? = null
    private var selectedItemPos = -1
    private var lastItemSelectedPos = -1

    /**
     * Remove item selection
     */
    fun unSelect() {
        notifyItemChanged(selectedItemPos)
        lastItemSelectedPos = -1
        selectedItemUUID = null
        lastSelectedItemUUID = null
        selectedItemPos = -1
    }

    /**
     * Select the recyclerview item
     *
     * If [uuid] is not null then use it as ID, therefore use ID
     * from [getItem]
     *
     * @param pos Position
     * @param uuid ID of selected item
     */
    fun setSelected(pos: Int, uuid: UUID? = null) {
        selectedItemUUID = uuid ?: getItem(pos).uuid
        selectedItemPos = pos
        lastItemSelectedPos = if (lastItemSelectedPos == -1) {
            lastSelectedItemUUID = selectedItemUUID
            selectedItemPos
        } else {
            // deselect last selected item
            notifyItemChanged(currentList.indexOfFirst { it.uuid == lastSelectedItemUUID })
            // save for future deselection
            lastSelectedItemUUID = selectedItemUUID
            selectedItemPos
        }
        // select
        notifyItemChanged(selectedItemPos)
    }

    var listItemEventListener: RecyclerViewItemEventListener? = null
    override fun onClickedAddBelow(item: Int) {
        listItemEventListener?.onClickedAddBelow(item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OverlayViewHolder {
        return OverlayViewHolder.from(parent)
                .apply { listItemEventListener = this@OverlayViewListAdapter }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: OverlayViewHolder, position: Int) {
        val item = getItem(position)
        if (item.uuid == selectedItemUUID) {
            selectedItemPos = position
        }
        holder.setSelected(item.uuid == selectedItemUUID)
        holder.bind(item)
    }

    class OverlayViewHolder(private val binding: RecyclerViewItemBinding) :
            RecyclerView.ViewHolder(binding.root) {

        var listItemEventListener: RecyclerViewItemEventListener? = null

        private val itemSelectedColor by lazy {
            val typedValue = TypedValue()
            itemView.context.theme.resolveAttribute(
                    R.attr.colorControlHighlight,
                    typedValue,
                    true
            )
            ContextCompat.getColor(itemView.context, typedValue.resourceId)
        }
        private val surfaceColor by lazy {
            val typedValue = TypedValue()
            itemView.context.theme.resolveAttribute(
                    R.attr.colorSurface,
                    typedValue,
                    true
            )
            ContextCompat.getColor(itemView.context, typedValue.resourceId)
        }

        fun bind(overlay: OverlayDataMapper) {
            binding.overlay = overlay
            binding.executePendingBindings()
        }

        fun setSelected(isSelected: Boolean) {
            if (isSelected)
                itemView.setBackgroundColor(itemSelectedColor)
            else
                itemView.setBackgroundColor(surfaceColor)
        }

        companion object {
            fun from(parent: ViewGroup): OverlayViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RecyclerViewItemBinding.inflate(layoutInflater, parent, false)
                return OverlayViewHolder(binding)
            }
        }
    }
}