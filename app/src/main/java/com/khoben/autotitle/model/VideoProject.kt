package com.khoben.autotitle.model

import android.graphics.Bitmap
import com.khoben.autotitle.database.entity.Project

class VideoProject(private val project: Project) {
    private val captionsList = mutableListOf<Any>()
    private val seekBarFramesList = mutableListOf<Bitmap>()
}