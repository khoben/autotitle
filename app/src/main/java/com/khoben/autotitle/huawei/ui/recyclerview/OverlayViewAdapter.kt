package com.khoben.autotitle.huawei.ui.recyclerview

import android.annotation.SuppressLint
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.khoben.autotitle.huawei.databinding.RecyclerViewItemBinding
import com.khoben.autotitle.huawei.ui.overlay.OverlayDataMapper

interface ListItemEventListener {
    fun onClickedAddBelow(item: Int)
}

class OverlayViewAdapter : ListAdapter<OverlayDataMapper, OverlayViewAdapter.OverlayViewHolder>(
    OverlayViewCallback()
), ListItemEventListener {

    var listItemEventListener: ListItemEventListener? = null
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

        var listItemEventListener: ListItemEventListener? = null

        init {
            binding.addRecyclerItem.setOnClickListener {
                if (adapterPosition == RecyclerView.NO_POSITION) return@setOnClickListener
                listItemEventListener?.onClickedAddBelow(adapterPosition)
            }
        }

        fun bind(o: OverlayDataMapper) {
            binding.overlay = o
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

class OverlayViewCallback : DiffUtil.ItemCallback<OverlayDataMapper>() {
    override fun areItemsTheSame(oldItem: OverlayDataMapper, newItem: OverlayDataMapper): Boolean {
        return oldItem.timestamp == newItem.timestamp
    }

    override fun areContentsTheSame(
        oldItem: OverlayDataMapper,
        newItem: OverlayDataMapper
    ): Boolean {
        return oldItem.timestamp == newItem.timestamp &&
                TextUtils.equals(oldItem.text, newItem.text) &&
                oldItem.startTime == newItem.startTime &&
                oldItem.endTime == newItem.endTime
    }
}