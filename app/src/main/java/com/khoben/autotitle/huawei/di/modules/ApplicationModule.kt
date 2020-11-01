package com.khoben.autotitle.huawei.di.modules

import android.app.Application
import com.khoben.autotitle.huawei.App
import com.khoben.autotitle.huawei.service.audioextractor.AudioExtractor
import com.khoben.autotitle.huawei.service.audioextractor.AudioExtractorImpl
import com.khoben.autotitle.huawei.service.audiotranscriber.AudioTranscriber
import com.khoben.autotitle.huawei.service.audiotranscriber.AudioTranscriberImpl
import com.khoben.autotitle.huawei.service.audiotranscriber.AudioTranscriberTest
import com.khoben.autotitle.huawei.service.frameretriever.VideoFrameRetriever
import com.khoben.autotitle.huawei.service.frameretriever.VideoFrameRetrieverImpl
import com.khoben.autotitle.huawei.service.mediaplayer.MediaExoPlayerSurfaceWrapper
import com.khoben.autotitle.huawei.service.mediaplayer.MediaSurfacePlayer
import com.khoben.autotitle.huawei.service.mediaplayer.SimpleVideoRender
import com.khoben.autotitle.huawei.service.mediaplayer.VideoRender
import com.khoben.autotitle.huawei.service.videosaver.Mp4ComposerVP
import com.khoben.autotitle.huawei.service.videosaver.VideoProcessorBase
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ApplicationModule(private var app: App) {
    @Provides
    @Singleton
    internal fun provideApplication(): Application {
        return app
    }

    @Provides
    internal fun provideVideoFrameRetriever(): VideoFrameRetriever {
        return VideoFrameRetrieverImpl(context = app.applicationContext)
    }

    @Provides
    internal fun provideVideoProcessorBase(): VideoProcessorBase {
        return Mp4ComposerVP()
    }

    @Provides
    @Singleton
    internal fun provideMediaPlayer(): MediaSurfacePlayer {
        return MediaExoPlayerSurfaceWrapper(context = app.applicationContext)
    }

    @Provides
    @Singleton
    internal fun provideVideoRenderer(): VideoRender {
        return SimpleVideoRender(context = app.applicationContext)
    }

    @Provides
    internal fun provideAudioExtractor(): AudioExtractor {
        return AudioExtractorImpl()
    }

    @Provides
    @Singleton
    internal fun provideAudioTranscriber(): AudioTranscriber {
        return AudioTranscriberImpl(context = app.applicationContext)
    }
}