package com.khoben.autotitle.mvp.presenter

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import com.khoben.autotitle.App
import com.khoben.autotitle.App.Companion.LIMIT_DURATION_MS
import com.khoben.autotitle.common.FAILED
import com.khoben.autotitle.common.LIMIT
import com.khoben.autotitle.common.OpeningVideoFileState
import com.khoben.autotitle.common.SUCCESS
import com.khoben.autotitle.model.project.RecentProjectsLoader
import com.khoben.autotitle.mvp.view.MainActivityView
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

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        loadRecentProjects()
    }

    fun verifyMedia(uri: Uri): @OpeningVideoFileState Int {
        val mediaMetadataRetriever = MediaMetadataRetriever()
        try {
            mediaMetadataRetriever.setDataSource(appContext, uri)
        } catch (e: Exception) {
            mediaMetadataRetriever.release()
            return FAILED
        }
        try {
            val duration =
                mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)!!.toLong()
            if (duration >= LIMIT_DURATION_MS) {
                mediaMetadataRetriever.release()
                return LIMIT
            }
        } catch (e: Exception) {
            mediaMetadataRetriever.release()
            return FAILED
        }
        mediaMetadataRetriever.release()
        return SUCCESS
    }

    private fun loadRecentProjects() {
        if (!RecentProjectsLoader.load()) viewState.hideRecentProject()
        else {
            Timber.d("Recent projects = ${RecentProjectsLoader.listProjects}")
        }
    }
}