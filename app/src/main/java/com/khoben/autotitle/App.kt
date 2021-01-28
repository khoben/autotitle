package com.khoben.autotitle

import android.app.Application
import android.content.Context
import com.huawei.hms.mlsdk.common.MLApplication
import com.khoben.autotitle.common.NotificationUtils
import com.khoben.autotitle.di.components.ApplicationComponent
import com.khoben.autotitle.di.components.DaggerApplicationComponent
import com.khoben.autotitle.di.modules.ApplicationModule
import com.khoben.autotitle.di.modules.ContextModule
import com.khoben.autotitle.extension.dp
import timber.log.Timber

class App : Application() {

    private val appPref by lazy {
        getSharedPreferences(
            APP_PREFERENCES,
            Context.MODE_PRIVATE
        )
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        initFoldersPath()
        initPreferences()
        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
        MLApplication.getInstance().apiKey =
            "CgB6e3x9/hsdbdBs4UtMv9w9yHOgLLpQWbfDNjtbW5685ZLdOW5HlpWzDmveL69IjxoeAg8TClmuNnbmvZ9xprrw"
        createApplicationComponent()
        NotificationUtils.createNotificationChannel(applicationContext, appName)
    }

    private fun initPreferences() {
        SEEKBAR_FLING_SMOOTH_ANIMATION =
            appPref.getBoolean(SEEKBAR_FLING_SMOOTH_ANIMATION_PREF, SEEKBAR_FLING_SMOOTH_ANIMATION)
    }

    private fun initFoldersPath() {
        APP_MAIN_FOLDER = "${appContext.getExternalFilesDir(null)}"
        PROJECTS_FOLDER = "${APP_MAIN_FOLDER}/Projects"
        PROJECTS_FILE_SERIALIZED = "${APP_MAIN_FOLDER}/proj"
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

        @JvmStatic
        lateinit var appContext: Context
        const val appName = "AutoTitle"
        const val appFeedbackEmail = "autotitleapp@gmail.com"
        const val FRAMES_PER_SCREEN = 8
        val SEEKBAR_HEIGHT_DP_PIXELS = 60.dp()
        const val FRAME_TIME_MS = 1000L
        const val LIMIT_DURATION_MS = 3 * 60 * 1000L
        const val PLAYBACK_TIME_FORMAT_MS = "m:ss.S"
        const val DATETIME_TIME_FORMAT = "dd-MM-yyyy HH:mm"
        const val VIDEO_EXTENSION = "mp4"
        const val AUDIO_EXTENSION = "aac"
        const val VIDEO_MIME_TYPE = "video/mp4"
        const val VIDEO_SOURCE_URI_INTENT = "com.khoben.autotitle.VIDEO_SOURCE"
        const val VIDEO_LOAD_MODE = "com.khoben.autotitle.VIDEO_LOAD_MODE"
        val THUMB_SIZE = Pair(100, 100)
        const val GUIDE_SHOW_DELAY: Long = 2000L
        const val DEFAULT_MUTE_STATE = false
        lateinit var APP_MAIN_FOLDER: String
        lateinit var PROJECTS_FOLDER: String
        lateinit var PROJECTS_FILE_SERIALIZED: String

        // Preferences settings
        private val APP_PREFERENCES = "APP_PREFERENCES"
        private val SEEKBAR_FLING_SMOOTH_ANIMATION_PREF = "SEEKBAR_FLING_SMOOTH_ANIMATION"
        var SEEKBAR_FLING_SMOOTH_ANIMATION = true
    }
}