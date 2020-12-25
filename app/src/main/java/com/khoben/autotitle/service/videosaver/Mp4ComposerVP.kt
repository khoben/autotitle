package com.khoben.autotitle.service.videosaver

import android.content.Context
import android.net.Uri
import com.daasuu.mp4compose.FillMode
import com.daasuu.mp4compose.composer.Mp4Composer
import com.khoben.autotitle.common.openglfilter.GLViewOverlayFilter
import com.khoben.autotitle.model.VideoInfo
import com.khoben.autotitle.ui.overlay.OverlayText

class Mp4ComposerVP : VideoProcessorBase() {

    companion object {
        private val TAG = Mp4ComposerVP::class.java.simpleName
    }

    private var instance: Mp4Composer? = null

    override fun setup(
        overlays: List<OverlayText>,
        sourceUri: Uri,
        outputPath: String,
        context: Context,
        videoInfo: VideoInfo,
        parentViewSize: Pair<Int, Int>
    ) {
        instance = Mp4Composer(sourceUri, outputPath, context).apply {
            init(this, overlays, outputPath, videoInfo, parentViewSize)
        }
    }

    override fun setup(
        overlays: List<OverlayText>,
        sourcePath: String,
        outputPath: String,
        videoInfo: VideoInfo,
        parentViewSize: Pair<Int, Int>
    ) {
        instance = Mp4Composer(sourcePath, outputPath).apply {
            init(this, overlays, outputPath, videoInfo, parentViewSize)
        }
    }

    private fun init(
        instance: Mp4Composer,
        overlays: List<OverlayText>,
        outputPath: String,
        videoInfo: VideoInfo,
        parentViewSize: Pair<Int, Int>
    ): Mp4Composer {
        val filter = GLViewOverlayFilter(overlays, videoInfo, parentViewSize)
        return instance.fillMode(FillMode.PRESERVE_ASPECT_FIT)
            .filter(filter)
            .listener(object : Mp4Composer.Listener {
                override fun onProgress(progress: Double) {
                    listener?.onProgress(progress)
                }

                override fun onCurrentVideoTime(timeUs: Long) {
                    filter.onCurrentVideoTime(timeUs)
                }

                override fun onCompleted() {
                    listener?.onComplete(outputPath)
                }

                override fun onCanceled() {
                    listener?.onCanceled()
                }

                override fun onFailed(exception: java.lang.Exception?) {
                    listener?.onError(exception.toString())
                }
            })
    }

    override fun start() {
        instance!!.start()
    }

    override fun cancel() {
        instance!!.cancel()
    }
}