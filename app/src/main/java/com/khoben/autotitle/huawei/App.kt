package com.khoben.autotitle.huawei

import android.app.Application
import android.content.Context
import com.huawei.hms.mlsdk.common.MLApplication
import com.khoben.autotitle.huawei.di.components.ApplicationComponent
import com.khoben.autotitle.huawei.di.components.DaggerApplicationComponent
import com.khoben.autotitle.huawei.di.modules.ApplicationModule
import com.khoben.autotitle.huawei.di.modules.ContextModule

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        MLApplication.getInstance().apiKey =
            "CgB6e3x9/hsdbdBs4UtMv9w9yHOgLLpQWbfDNjtbW5685ZLdOW5HlpWzDmveL69IjxoeAg8TClmuNnbmvZ9xprrw"
        createApplicationComponent()
    }

    private fun createApplicationComponent() {
        applicationComponent = DaggerApplicationComponent.builder()
            .applicationModule(ApplicationModule(this))
            .contextModule(ContextModule(context = applicationContext))
            .build()
    }

    companion object {
        @JvmStatic
        lateinit var applicationComponent: ApplicationComponent
        private var context: Context? = null
        const val FRAMES_PER_SCREEN = 8
        const val FRAME_TIME_MS = 1000L
        const val LIMIT_DURATION_MS = 60 * 1000L
        const val VIDEO_EXTENSION = "mp4"
        const val AUDIO_EXTENSION = "aac"
        const val VIDEO_MIME_TYPE = "video/mp4"
        val THUMB_SIZE = Pair(150, 150)
        val VIDEO_VIEW_LAYOUT_SIZE = Pair(1120, 630)
    }
}