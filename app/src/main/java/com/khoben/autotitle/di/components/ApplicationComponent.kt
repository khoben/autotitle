package com.khoben.autotitle.di.components

import com.khoben.autotitle.di.modules.ApplicationModule
import com.khoben.autotitle.di.modules.ContextModule
import com.khoben.autotitle.mvp.presenter.VideoEditActivityPresenter
import com.khoben.autotitle.service.mediaplayer.MediaController
import com.khoben.autotitle.service.videoloader.VideoLoader
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [ApplicationModule::class, ContextModule::class])
interface ApplicationComponent {
    fun inject(videoEditActivityPresenter: VideoEditActivityPresenter)
    fun inject(videoLoader: VideoLoader)
    fun inject(mediaController: MediaController)
}