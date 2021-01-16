package com.khoben.autotitle.di.modules

import com.khoben.autotitle.App
import com.khoben.autotitle.service.audioextractor.AudioExtractor
import com.khoben.autotitle.service.audioextractor.AudioExtractorImpl
import com.khoben.autotitle.service.audiotranscriber.AudioTranscriber
import com.khoben.autotitle.service.audiotranscriber.AudioTranscriberTest
import com.khoben.autotitle.service.frameretriever.VideoFrameRetriever
import com.khoben.autotitle.service.frameretriever.VideoFrameRetrieverImpl
import com.khoben.autotitle.service.mediaplayer.*
import com.khoben.autotitle.service.videoloader.VideoLoader
import com.khoben.autotitle.service.videosaver.Mp4ComposerVP
import com.khoben.autotitle.service.videosaver.VideoProcessorBase
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ApplicationModule(private val app: App) {
    @Provides
    @Singleton
    internal fun provideVideoFrameRetriever(): VideoFrameRetriever {
        return VideoFrameRetrieverImpl(context = app.applicationContext)
    }

    @Provides
    @Singleton
    internal fun provideVideoProcessorBase(): VideoProcessorBase {
        return Mp4ComposerVP()
    }

    @Provides
    @Singleton
    internal fun provideSurfaceMediaPlayer(): MediaSurfacePlayer {
        return MediaExoPlayerSurfaceWrapper(context = app.applicationContext)
    }

    @Provides
    internal fun provideUIMediaPlayer(): MediaPlayer {
        return MediaExoPlayerUIWrapper()
    }

    @Provides
    @Singleton
    internal fun provideVideoRenderer(): VideoRender {
        return SimpleVideoRender()
    }

    @Provides
    @Singleton
    internal fun provideAudioExtractor(): AudioExtractor {
        return AudioExtractorImpl()
    }

    @Provides
    @Singleton
    internal fun provideAudioTranscriber(): AudioTranscriber {
        return AudioTranscriberTest(context = app.applicationContext)
    }

    @Provides
    @Singleton
    internal fun provideMediaController(): MediaController {
        return MediaController()
    }

    @Provides
    @Singleton
    internal fun provideVideoLoader(): VideoLoader {
        return VideoLoader()
    }
}