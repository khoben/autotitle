package com.khoben.autotitle.ui.recyclerview.projects

import androidx.recyclerview.widget.DiffUtil
import com.khoben.autotitle.model.project.ThumbProject

class ProjectViewDiffCallback : DiffUtil.ItemCallback<ThumbProject>() {
    override fun areItemsTheSame(oldItem: ThumbProject, newItem: ThumbProject): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: ThumbProject,
        newItem: ThumbProject
    ): Boolean {
        return oldItem.dateCreated == newItem.dateCreated &&
                oldItem.dateUpdated == newItem.dateUpdated &&
                oldItem.title == newItem.title &&
                oldItem.videoDuration == newItem.videoDuration &&
                oldItem.videoFileSizeBytes == newItem.videoFileSizeBytes
    }
}