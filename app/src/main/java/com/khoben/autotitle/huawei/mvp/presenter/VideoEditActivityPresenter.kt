package com.khoben.autotitle.huawei.mvp.presenter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.RelativeLayout
import com.khoben.autotitle.huawei.App
import com.khoben.autotitle.huawei.R
import com.khoben.autotitle.huawei.common.FileUtils
import com.khoben.autotitle.huawei.model.MLCaption
import com.khoben.autotitle.huawei.model.PlaybackEvent
import com.khoben.autotitle.huawei.model.PlaybackState
import com.khoben.autotitle.huawei.mvp.view.VideoEditActivityView
import com.khoben.autotitle.huawei.service.mediaplayer.MediaController
import com.khoben.autotitle.huawei.service.videoloader.VideoLoader
import com.khoben.autotitle.huawei.service.videosaver.VideoProcessorBase
import com.khoben.autotitle.huawei.service.videosaver.VideoProcessorListener
import com.khoben.autotitle.huawei.ui.overlay.OverlayHandler
import com.khoben.autotitle.huawei.ui.overlay.OverlayText
import com.khoben.autotitle.huawei.ui.player.VideoControlsView
import com.khoben.autotitle.huawei.ui.player.seekbar.SeekBarListener
import moxy.InjectViewState
import moxy.MvpPresenter
import java.util.*
import javax.inject.Inject

@InjectViewState
class VideoEditActivityPresenter : MvpPresenter<VideoEditActivityView>(),
    OverlayHandler.OverlayObjectEventListener,
    MediaController.Callback,
    SeekBarListener {

    @Inject
    lateinit var videoProcessor: VideoProcessorBase

    @Inject
    lateinit var mediaController: MediaController

    @Inject
    lateinit var appContext: Context

    private var videoLoader: VideoLoader = VideoLoader()
    private var overlayHandler: OverlayHandler? = null
    private var sourceUri: Uri? = null

    init {
        App.applicationComponent.inject(this)
        mediaController.addSubscription(this)
    }

    fun setUri(uri: Uri) {
        sourceUri = uri
    }

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()

        mediaController.setVideoSource(sourceUri!!)
        processVideo(appContext)
        viewState.initVideoContainerLayoutParams()
    }

    /**
     * Init editor that handles add/edit/delete operations
     * on overlays
     * @param parentView Parent view to attach to this overlays
     */
    fun initEditor(
        context: Context,
        parentView: RelativeLayout,
        videoView: VideoControlsView
    ) {
        if (overlayHandler == null) {
            overlayHandler = OverlayHandler.Builder()
                .ctx(context)
                .parent(parentView)
                .build()
            overlayHandler?.overlayObjectEventListener = this
        } else {
            overlayHandler?.setLayout(context, parentView)
        }
        videoView.seekBarListener = this
    }

    /**
     * Adds new overlay view
     */
    fun addOverlayAtCurrentPosition() {
        // pause playback before adding new overlay
        setPlayState(false)
        overlayHandler!!.addOverlay(mediaController.currentPosition, mediaController.videoDuration)
    }

    /**
     * Adds new overlay view
     */
    fun addOverlayAfterSpecificPosition(pos: Int) {
        // pause playback before adding new overlay
        setPlayState(false)
        val toTime = overlayHandler!!.addOverlayAfterSpecificPosition(pos)
        viewState.setControlsToTime(toTime)
    }

    fun addOverlayAtSpecificPosition(pos: Int, item: OverlayText) {
        setPlayState(false)
        overlayHandler!!.addOverlayAtSpecificPosition(pos, item)
    }

    fun recyclerSelectOverlay(pos: Int) {
        setPlayState(false)
        overlayHandler!!.selectedOverlayId(pos)
    }


    /**
     * Saves video with overlays
     */
    fun saveVideo(context: Context, parentViewSize: Pair<Int, Int>) {
        overlayHandler!!.showRootView()
        setPlayState(false)
        val outputPath = FileUtils.getRandomFilepath(context, App.VIDEO_EXTENSION)
        videoProcessor.setup(
            overlayHandler!!.getOverlays(),
            sourceUri!!,
            outputPath,
            context,
            mediaController.videoDetails!!,
            parentViewSize
        )
        viewState.onVideoSavingStarted()
        videoProcessor.listener = object : VideoProcessorListener {
            override fun onProgress(progress: Double) {
                viewState.onVideoSavingProgress(progress)
            }

            override fun onComplete(filepath: String) {
                viewState.onVideoSavingComplete(outputPath)
            }

            override fun onCanceled() {
                viewState.onVideoSavingCancelled()
                FileUtils.deleteFile(context, outputPath)
            }

            override fun onError(message: String) {
                viewState.onVideoSavingError(message)
                FileUtils.deleteFile(context, outputPath)
            }

        }
        videoProcessor.start()
    }

    /**
     * Cancels video saving process
     */
    fun cancelSavingVideo() {
        videoProcessor.cancel()
    }

    fun unEditable() {
        mediaController.setPlayState(false)
        overlayHandler!!.unEditable()
    }

    override fun onEdited(overlay: List<OverlayText>) {
        viewState.onOverlaysChangedList(overlay)
    }

    override fun onUnEditable(overlay: OverlayText?, overlays: List<OverlayText>) {
        viewState.updatePlayback(overlays, overlay, isEdit = false, isPlaying = false)
    }

    override fun onAdded(overlay: OverlayText?, overlays: List<OverlayText>, isEdit: Boolean) {
        viewState.updatePlayback(overlays, overlay, isEdit, false)
        viewState.onOverlaysChangedList(overlays)
    }

    override fun onSelect(overlay: OverlayText?, overlays: List<OverlayText>) {
        viewState.setControlsToTime(overlay!!.startTime)
        viewState.updatePlayback(overlays, overlay, isEdit = true, isPlaying = false)
        viewState.highlightListViewItem(overlays.indexOf(overlay))
    }

    override fun onRemoved(
        idxRemoved: Int,
        removedOverlay: OverlayText,
        overlays: ArrayList<OverlayText>
    ) {
        viewState.onRemovedOverlay(idxRemoved, removedOverlay, overlays)
        viewState.onOverlaysChangedList(overlays)
        viewState.updatePlayback(overlays, null, isEdit = false, isPlaying = false)
    }

    override fun changeTimeRangeSelectedOverlay(startTime: Long, endTime: Long) {
        overlayHandler!!.changeTimeRangeSelectedOverlay(startTime, endTime)
    }

    override fun updateVideoPositionWithSeekBar(time: Long) {
        if (time >= mediaController.videoDuration) return
        overlayHandler!!.changeVisibilityOverlayByTime(time)
    }

    override fun seekBarRewind(currentTime: Long) {
        setPlayState(false)
        mediaController.seekTo(currentTime)
        overlayHandler!!.changeVisibilityOverlayByTime(currentTime)
    }

    override fun seekBarOnTouch() {
        setPlayState(false)
    }

    override fun seekBarOnDoubleTap() {
//        addOverlayAtCurrentPosition()
    }

    fun editItem(index: Int) {
        overlayHandler!!.editOverlay(index)
    }

    @SuppressLint("CheckResult")
    fun processVideo(context: Context) {
        val videoDuration = mediaController.videoDuration
        val frame = videoDuration / App.FRAME_TIME_MS
        val frameTime = if (frame > 0) videoDuration / frame else App.FRAME_TIME_MS
        videoLoader.init(context, sourceUri!!, frameTime)
            .onError { error ->
                Log.d(TAG, "Error while loading video $error")
                errorProcessVideo(error)
            }
            .load {
                val audioTranscribeResult = it.second
                val framesResult = it.first
                when {
                    // Service transcription is not available
                    audioTranscribeResult.throwable != null -> {
                        Log.e(TAG, "No captions. Network service error")
                        viewState.showPopupWindow(
                            context.getString(R.string.error_no_captions)
                                    + "\n"
                                    + context.getString(R.string.error_no_caption_net_error)
                        )
                    }
                    audioTranscribeResult.caption!!.isEmpty() -> {
                        Log.d(TAG, "No captions")
                        viewState.showPopupWindow(context.getString(R.string.error_no_captions))
                    }
                    else -> {
                        Log.d(TAG, "Text = $audioTranscribeResult")
                        processTranscribe(audioTranscribeResult.caption)
                    }
                }
                successProcessedVideo(framesResult, frameTime)
            }
    }

    private fun successProcessedVideo(frames: List<Bitmap>, frameTime: Long) {
        Log.d(TAG, "Success video load")
        viewState.onVideoProcessed(frames, frameTime)
    }

    private fun errorProcessVideo(e: Throwable) {
        Log.e(TAG, "Error video processing")
        viewState.onErrorVideoProcessing(e)
    }

    /**
     * Add auto captions
     * @param start Long
     * @param end Long
     * @param text String
     */
    private fun addOverlay(start: Long, end: Long, text: String) {
        overlayHandler!!.addOverlay(start, end, text)
    }

    /**
     * Adds detected result to overlay
     * @param result List<MLCaption>
     */
    private fun processTranscribe(result: List<MLCaption>) {
        result.forEach {
            addOverlay(it.startTime, it.endTime, it.text)
        }
        // TODO("View cant be drawn if it has never been displayed.
        //       On adding stage we will show all of these, but next we should hide it")
        unEditable()
        overlayHandler!!.getOverlays().forEach {
            if (0L in it.startTime..it.endTime) {
                it.visibility = VISIBLE
            } else {
                it.visibility = INVISIBLE
            }
        }
    }

    fun deleteOverlay(item: Int) {
        overlayHandler!!.removeOverlay(item)
    }

    fun getVideoDetails() = mediaController.videoDetails
    fun setPlayState(state: Boolean) = mediaController.setPlayState(state)

    override fun handlePlaybackState(state: PlaybackEvent) {
        when (state.playState) {
            PlaybackState.PLAY -> {
                val overlays = overlayHandler!!.getOverlaysWithSelected()
                viewState.updatePlayback(
                    overlays.second, overlays.first,
                    isEdit = false,
                    isPlaying = true
                )
            }
            PlaybackState.PAUSED -> {
                val overlays = overlayHandler!!.getOverlaysWithSelected()
                viewState.updatePlayback(
                    overlays.second, overlays.first,
                    isEdit = false,
                    isPlaying = false
                )
            }
            PlaybackState.STOP -> {

            }
            PlaybackState.REWIND -> {

            }
        }
    }

    companion object {
        private val TAG = VideoEditActivityPresenter::class.java.simpleName
    }
}