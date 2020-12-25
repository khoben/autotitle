package com.khoben.autotitle

import android.app.Application
import com.huawei.hms.mlsdk.common.MLApplication
import com.khoben.autotitle.common.APIKeyStore
import com.khoben.autotitle.common.DisplayUtils
import com.khoben.autotitle.di.components.ApplicationComponent
import com.khoben.autotitle.di.components.DaggerApplicationComponent
import com.khoben.autotitle.di.modules.ApplicationModule
import com.khoben.autotitle.di.modules.ContextModule

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        MLApplication.getInstance().apiKey = APIKeyStore.HuaweiMLKit()
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
        const val GUIDE_SHOW_DELAY: Long = 2000L
        const val DEFAULT_MUTE_STATE = false
    }
}