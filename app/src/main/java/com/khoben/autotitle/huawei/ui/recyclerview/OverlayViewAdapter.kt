package com.khoben.autotitle.huawei.ui.recyclerview

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.khoben.autotitle.huawei.databinding.RecyclerViewItemBinding
import com.khoben.autotitle.huawei.ui.overlay.OverlayDataMapper

class OverlayViewAdapter : ListAdapter<OverlayDataMapper, OverlayViewAdapter.OverlayViewHolder>(
    OverlayViewDiffCallback()
), RecyclerViewItemEventListener {

    var listItemEventListener: RecyclerViewItemEventListener? = null
    override fun onClickedAddBelow(item: Int) {
        listItemEventListener?.onClickedAddBelow(item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OverlayViewHolder {
        return OverlayViewHolder.from(parent)
            .apply { listItemEventListener = this@OverlayViewAdapter }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: OverlayViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class OverlayViewHolder(private val binding: RecyclerViewItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        var listItemEventListener: RecyclerViewItemEventListener? = null

        fun bind(overlay: OverlayDataMapper) {
            binding.overlay = overlay
            binding.executePendingBindings()
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