package com.khoben.autotitle.ui.recyclerview

import android.annotation.SuppressLint
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.khoben.autotitle.R
import com.khoben.autotitle.databinding.RecyclerViewOverlayItemBinding
import com.khoben.autotitle.ui.overlay.OverlayDataMapper
import java.util.*

class OverlayViewListAdapter :
    ListAdapter<OverlayDataMapper,
            OverlayViewListAdapter.OverlayViewHolder>(OverlayViewDiffCallback()),
    RecyclerViewItemEventListener {

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
        holder.setSelected(item.isSelected)
        holder.bind(item)
    }

    class OverlayViewHolder(private val binding: RecyclerViewOverlayItemBinding) :
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
                val binding = RecyclerViewOverlayItemBinding.inflate(layoutInflater, parent, false)
                return OverlayViewHolder(binding)
            }
        }
    }
}