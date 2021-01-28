package com.khoben.autotitle.mvp.presenter

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import com.khoben.autotitle.App
import com.khoben.autotitle.App.Companion.LIMIT_DURATION_MS
import com.khoben.autotitle.common.FileUtils
import com.khoben.autotitle.common.OpeningVideoFileState
import com.khoben.autotitle.database.AppDatabase
import com.khoben.autotitle.model.project.RecentProjectsLoader
import com.khoben.autotitle.mvp.view.MainActivityView
import kotlinx.serialization.ExperimentalSerializationApi
import moxy.InjectViewState
import moxy.MvpPresenter
import timber.log.Timber
import javax.inject.Inject


@InjectViewState
class MainActivityPresenter : MvpPresenter<MainActivityView>() {

    @Inject
    lateinit var appContext: Context

    init {
        App.applicationComponent.inject(this)
    }

    fun verifyMedia(uri: Uri): OpeningVideoFileState {
        val mediaMetadataRetriever = MediaMetadataRetriever()
        try {
            mediaMetadataRetriever.setDataSource(appContext, uri)
        } catch (e: Exception) {
            mediaMetadataRetriever.release()
            return OpeningVideoFileState.FAILED
        }
        try {
            val duration =
                mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)!!
                    .toLong()
            if (duration >= LIMIT_DURATION_MS) {
                mediaMetadataRetriever.release()
                return OpeningVideoFileState.LIMIT
            }
        } catch (e: Exception) {
            mediaMetadataRetriever.release()
            return OpeningVideoFileState.FAILED
        }
        mediaMetadataRetriever.release()
        return OpeningVideoFileState.SUCCESS
    }
}