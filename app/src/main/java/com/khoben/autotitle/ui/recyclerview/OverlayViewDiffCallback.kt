package com.khoben.autotitle.ui.recyclerview

import androidx.recyclerview.widget.DiffUtil
import com.khoben.autotitle.ui.overlay.OverlayDataMapper

class OverlayViewDiffCallback : DiffUtil.ItemCallback<OverlayDataMapper>() {
    override fun areItemsTheSame(oldItem: OverlayDataMapper, newItem: OverlayDataMapper): Boolean {
        return oldItem.uuid == newItem.uuid
    }

    override fun areContentsTheSame(
        oldItem: OverlayDataMapper,
        newItem: OverlayDataMapper
    ): Boolean {
        return oldItem.uuid == newItem.uuid &&
                oldItem.text == newItem.text &&
                oldItem.startTime == newItem.startTime &&
                oldItem.endTime == newItem.endTime
    }
}