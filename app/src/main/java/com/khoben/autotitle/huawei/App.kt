package com.khoben.autotitle.huawei

import android.app.Application
import com.huawei.hms.mlsdk.common.MLApplication
import com.khoben.autotitle.huawei.common.DisplayUtils
import com.khoben.autotitle.huawei.di.components.ApplicationComponent
import com.khoben.autotitle.huawei.di.components.DaggerApplicationComponent
import com.khoben.autotitle.huawei.di.modules.ApplicationModule
import com.khoben.autotitle.huawei.di.modules.ContextModule

class App : Application() {

    override fun onCreate() {
        super.onCreate()
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
        const val FRAMES_PER_SCREEN = 8
        const val SEEKBAR_HEIGHT_DP = 60F
        val SEEKBAR_HEIGHT_DP_PIXELS = DisplayUtils.dipToPx(60)
        const val FRAME_TIME_MS = 1000L
        const val LIMIT_DURATION_MS = 60 * 1000L
        const val TIME_FORMAT_MS = "m:ss.S"
        const val VIDEO_EXTENSION = "mp4"
        const val AUDIO_EXTENSION = "aac"
        const val VIDEO_MIME_TYPE = "video/mp4"
        val THUMB_SIZE = Pair(150, 150)
        const val DOUBLE_TAP_TIME = 300L
        const val GUIDE_SHOW_DELAY: Long = 2000L
    }
}