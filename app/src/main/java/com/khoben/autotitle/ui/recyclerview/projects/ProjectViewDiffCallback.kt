package com.khoben.autotitle.ui.recyclerview.projects

import androidx.recyclerview.widget.DiffUtil
import com.khoben.autotitle.database.entity.Project

class ProjectViewDiffCallback : DiffUtil.ItemCallback<Project>() {
    override fun areItemsTheSame(oldItem: Project, newItem: Project): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: Project,
        newItem: Project
    ): Boolean {
        return oldItem.createdAt == newItem.createdAt &&
                oldItem.updatedAt == newItem.updatedAt &&
                oldItem.title == newItem.title &&
                oldItem.videoDuration == newItem.videoDuration &&
                oldItem.videoFileSizeBytes == newItem.videoFileSizeBytes
    }
}