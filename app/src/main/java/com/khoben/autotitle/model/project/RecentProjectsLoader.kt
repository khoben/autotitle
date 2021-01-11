package com.khoben.autotitle.model.project

import com.khoben.autotitle.App
import com.khoben.autotitle.common.FileUtils

object RecentProjectsLoader {
    val listProjects = ListThumbProject()

    private val temp = mutableListOf<ThumbProject>()

    fun load(): Boolean {
        FileUtils.createDirIfNotExists(App.PROJECTS_FOLDER)
        return listProjects.load()
    }

    fun store(item: ThumbProject) {
        temp.add(item)
    }

    fun save() {
        listProjects.list = temp
        listProjects.store()
    }
}