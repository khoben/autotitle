package com.khoben.autotitle.mvp.presenter

import android.content.Context
import android.net.Uri
import com.khoben.autotitle.App
import com.khoben.autotitle.mvp.view.PreloadActivityView
import com.khoben.autotitle.service.mediaplayer.MediaPlayer
import moxy.MvpPresenter
import javax.inject.Inject

class PreloadActivityPresenter : MvpPresenter<PreloadActivityView>() {

    @Inject
    lateinit var mediaPlayer: MediaPlayer

    @Inject
    lateinit var appContext: Context

    init {
        App.applicationComponent.inject(this)
    }

    fun init(uri: Uri) {
        mediaPlayer.init(appContext, uri)
    }

    fun initNewPlayer() = mediaPlayer.initNewPlayer(appContext)
    fun releasePlayer() {
        mediaPlayer.releasePlayer()
    }

    fun setMediaSessionState(isActive: Boolean) = mediaPlayer.setMediaSessionState(isActive)
    fun deactivate() {}
    fun pause() = mediaPlayer.pause()
}