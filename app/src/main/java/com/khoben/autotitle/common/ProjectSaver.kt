package com.khoben.autotitle.common

import android.content.Context
import androidx.work.*
import com.khoben.autotitle.model.VideoProject

class ProjectSaver {

    private var curProject: VideoProject? = null

    fun save(context: Context, project: VideoProject) {
        curProject = project
        val work = OneTimeWorkRequest.Builder(SaverWorker::class.java)
        WorkManager.getInstance(context).enqueue(work.build())
    }

    inner class SaverWorker(context: Context, workerParams: WorkerParameters) : Worker(context,
        workerParams
    ) {
        override fun doWork(): Result {

            return Result.success()
        }

    }
}