package com.khoben.autotitle.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.khoben.autotitle.App
import com.khoben.autotitle.common.FileUtils
import com.khoben.autotitle.database.AppDatabase
import com.khoben.autotitle.database.entity.Project

class ProjectViewModel(context: Context) : ViewModel() {
    private val projectDao = AppDatabase.getInstance(context).projectDao()

    /**
     * Current selected project within activity
     */
    var currentProject: Project? = null

    val projectList: LiveData<List<Project>> = projectDao.all

    suspend fun getById(id: Long) = projectDao.getById(id)

    suspend fun insert(project: Project) = projectDao.insert(project)

    suspend fun insertWithTimestamp(project: Project) = projectDao.insertWithTimestamp(project)

    suspend fun update(project: Project) = projectDao.update(project)

    suspend fun updateWithTimestamp(project: Project) = projectDao.updateWithTimestamp(project)

    suspend fun updateTitle(id: Long, title: String) = projectDao.updateTitle(id, title)

    suspend fun delete(project: Project) = projectDao.delete(project)

    suspend fun deleteById(id: Long) {
        projectDao.deleteById(id)
        FileUtils.removeFileFolderRecursive("${App.PROJECTS_FOLDER}/$id")
    }
}