package com.khoben.autotitle.model.project

import com.khoben.autotitle.App
import com.khoben.autotitle.common.FileUtils
import kotlinx.serialization.ExperimentalSerializationApi


object RecentProjectsLoader {
    @ExperimentalSerializationApi
    private val listProjects = ListThumbProject()

    private val temp = mutableListOf<ThumbProject>()

    /**
     * Loads from filesystem (path = [App.PROJECTS_FOLDER]) projects info
     *
     * @return Has been read non empty list of projects
     */
    @ExperimentalSerializationApi
    fun load(): Boolean {
        return if (temp.isNotEmpty()) {
            true
        } else {
            FileUtils.createDirIfNotExists(App.PROJECTS_FOLDER)
            val status = listProjects.load()
            temp.clear()
            listProjects.list?.let { temp.addAll(it) }
            status
        }
    }

    /**
     * Adds new project to list
     *
     * Also it creates thumbnail
     *
     * @param item ThumbProject
     */
    fun new(item: ThumbProject) {
        item.createProjectFolderIfNotExists()
        item.createThumb()
        temp.add(item)
    }

    fun removeAt(idx: Int) {
        temp.removeAt(idx)
    }

    fun getCurrentProjects() = temp
    fun getCurrentProjectIdx(idx: Int) = temp[idx]
    fun setProjectTitle(idx: Int, title: String) {
        temp[idx] = temp[idx].copy(title = title)
    }
    fun getProjectTitle(idx: Int): String? {
        return temp[idx].title
    }

    /**
     * Saves all recent projects info to filesystem
     */
    @ExperimentalSerializationApi
    fun save() {
        listProjects.list = temp
        listProjects.save()
    }
}