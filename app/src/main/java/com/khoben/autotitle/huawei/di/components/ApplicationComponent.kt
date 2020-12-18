package com.khoben.autotitle.huawei.di.components

import com.khoben.autotitle.huawei.di.modules.ApplicationModule
import com.khoben.autotitle.huawei.di.modules.ContextModule
import com.khoben.autotitle.huawei.mvp.presenter.VideoEditActivityPresenter
import com.khoben.autotitle.huawei.service.mediaplayer.MediaController
import com.khoben.autotitle.huawei.service.videoloader.VideoLoader
import com.khoben.autotitle.huawei.ui.player.VideoControlsView
import com.khoben.autotitle.huawei.ui.player.VideoSurfaceView
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [ApplicationModule::class, ContextModule::class])
interface ApplicationComponent {
    fun inject(videoEditActivityPresenter: VideoEditActivityPresenter)
    fun inject(videoSurfaceView: VideoSurfaceView)
    fun inject(videoLoader: VideoLoader)
    fun inject(mediaController: MediaController)
    fun inject(videoControlsView: VideoControlsView)
}