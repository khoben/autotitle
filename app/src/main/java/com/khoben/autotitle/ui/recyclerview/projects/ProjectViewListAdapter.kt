package com.khoben.autotitle.ui.recyclerview.projects

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.khoben.autotitle.database.entity.Project
import com.khoben.autotitle.databinding.RecyclerViewProjectItemBinding
import com.khoben.autotitle.model.project.ThumbProject
import com.khoben.autotitle.ui.popup.projectitem.ProjectItemOptionsDialog

class ProjectViewListAdapter :
    ListAdapter<Project,
            ProjectViewListAdapter.ProjectViewHolder>(ProjectViewDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectViewHolder {
        return ProjectViewHolder.from(parent)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ProjectViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    class ProjectViewHolder(private val binding: RecyclerViewProjectItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(project: Project) {
            initContextMenu()
            binding.project = project
            binding.executePendingBindings()
        }

        private fun initContextMenu() {
            binding.moreButton.setOnClickListener { showPopupMenu() }
        }

        private fun showPopupMenu() {
            (itemView.context as AppCompatActivity).supportFragmentManager.let {
                ProjectItemOptionsDialog.show(binding.project!!.id).apply {
                    show(it, "recycler_bottom_modal_sheet")
                }
            }
        }

        companion object {
            fun from(parent: ViewGroup): ProjectViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RecyclerViewProjectItemBinding.inflate(layoutInflater, parent, false)
                return ProjectViewHolder(binding)
            }
        }
    }
}