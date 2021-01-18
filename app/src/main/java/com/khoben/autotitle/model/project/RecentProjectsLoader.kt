package com.khoben.autotitle.model.project

import android.content.Context
import android.net.Uri
import com.khoben.autotitle.App
import com.khoben.autotitle.common.FileUtils
import kotlinx.serialization.ExperimentalSerializationApi


object RecentProjectsLoader {
    @ExperimentalSerializationApi
    private val listProjects = ListThumbProject()

    /**
     * Runtime projects storage
     */
    private val temp = mutableListOf<ThumbProject>()

    /**
     * Loads from filesystem (path = [App.PROJECTS_FOLDER]) projects info
     *
     * @return Has been read non-empty list of projects
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
     * Creates new project and its thumbnail
     *
     * @param item ThumbProject
     * @param uri Video source uri
     * @param context App context
     */
    fun new(item: ThumbProject, uri: Uri, context: Context) {
        item.createProjectFolderIfNotExists()
        item.createThumb(uri, context)
        temp.add(item)
    }

    /**
     * Removes project from filesystem
     *
     * @param idx Index of project
     */
    fun removeAt(idx: Int) {
        FileUtils.removeFileFolderRecursive("${App.PROJECTS_FOLDER}/${temp[idx].id}")
        temp.removeAt(idx)
    }

    /**
     * Get runtime list of projects
     *
     * @return List
     */
    fun getRecentProjects() = temp

    /**
     * Get project by [idx]
     *
     * @param idx Project's index
     * @return ThumbProject
     */
    fun getRecentProject(idx: Int) = temp[idx]

    /**
     * Set project title with [idx]
     * @param idx Project's index
     * @param title Title
     */
    fun setProjectTitle(idx: Int, title: String) {
        temp[idx] = temp[idx].copy(title = title)
    }
    fun getProjectTitle(idx: Int): String? {
        return temp[idx].title
    }

    /**
     * Saves all projects to filesystem
     */
    @ExperimentalSerializationApi
    fun save() {
        listProjects.list = temp
        listProjects.save()
    }
}