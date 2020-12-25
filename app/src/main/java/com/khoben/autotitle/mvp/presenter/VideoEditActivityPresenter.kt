package com.khoben.autotitle.mvp.presenter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.widget.RelativeLayout
import com.khoben.autotitle.App
import com.khoben.autotitle.R
import com.khoben.autotitle.common.FileUtils
import com.khoben.autotitle.model.MLCaption
import com.khoben.autotitle.model.PlaybackEvent
import com.khoben.autotitle.model.PlaybackState
import com.khoben.autotitle.mvp.view.VideoEditActivityView
import com.khoben.autotitle.service.mediaplayer.MediaController
import com.khoben.autotitle.service.mediaplayer.VideoRender
import com.khoben.autotitle.service.videoloader.VideoLoader
import com.khoben.autotitle.service.videosaver.VideoProcessorBase
import com.khoben.autotitle.service.videosaver.VideoProcessorListener
import com.khoben.autotitle.ui.overlay.OverlayHandler
import com.khoben.autotitle.ui.overlay.OverlayText
import com.khoben.autotitle.ui.player.PlayPauseMaterialButton
import com.khoben.autotitle.ui.player.VideoControlsView
import com.khoben.autotitle.ui.player.seekbar.SeekBarListener
import moxy.InjectViewState
import moxy.MvpPresenter
import java.util.*
import javax.inject.Inject

@InjectViewState
class VideoEditActivityPresenter : MvpPresenter<VideoEditActivityView>(),
    OverlayHandler.OverlayObjectEventListener,
    MediaController.Callback,
    SeekBarListener,
    PlayPauseMaterialButton.OnClickListener {

    @Inject
    lateinit var videoProcessor: VideoProcessorBase

    @Inject
    lateinit var mediaController: MediaController

    @Inject
    lateinit var videoRenderer: VideoRender

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
        viewState.initVideoContainerLayoutParams(mediaController.mediaPlayer, videoRenderer)
        toggleMute(App.DEFAULT_MUTE_STATE, false)
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
        pausePlayback()
        overlayHandler!!.addOverlay(mediaController.currentPosition, mediaController.videoDuration)
    }

    /**
     * Adds new overlay view
     */
    fun addOverlayAfterSpecificPosition(pos: Int) {
        // pause playback before adding new overlay
        pausePlayback()
        val toTime = overlayHandler!!.addOverlayAfterSpecificPosition(pos)
        viewState.setControlsToTime(toTime)
    }

    fun addOverlayAtSpecificPosition(pos: Int, item: OverlayText) {
        pausePlayback()
        overlayHandler!!.addOverlayAtSpecificPosition(pos, item)
    }

    fun recyclerSelectOverlay(pos: Int) {
        pausePlayback()
        overlayHandler!!.selectedOverlayId(pos)
    }


    /**
     * Saves video with overlays
     */
    fun saveVideo(context: Context, parentViewSize: Pair<Int, Int>) {
        pausePlayback()
        // make visible all overlays
        overlayHandler!!.showRootView()
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
        pausePlayback()
        overlayHandler!!.unEditable()
    }

    override fun onEdited(overlay: List<OverlayText>) {
        viewState.onOverlaysChangedList(overlay)
    }

    override fun onUnEditable(overlay: OverlayText?, overlays: List<OverlayText>) {
        viewState.updatePlayback(overlays, overlay, isPlaying = false)
    }

    override fun onAdded(overlay: OverlayText?, overlays: List<OverlayText>, isEdit: Boolean) {
        viewState.updatePlayback(overlays, overlay, isPlaying = false)
        viewState.onOverlaysChangedList(overlays)
    }

    override fun onSelect(
        overlay: OverlayText?,
        overlays: List<OverlayText>,
        seekToOverlayStart: Boolean
    ) {
        if (seekToOverlayStart)
            viewState.setControlsToTime(overlay!!.startTime)
        viewState.updatePlayback(overlays, overlay, isPlaying = false)
        viewState.highlightListViewItem(overlays.indexOf(overlay))
    }

    override fun onRemoved(
        idxRemoved: Int,
        removedOverlay: OverlayText,
        overlays: ArrayList<OverlayText>
    ) {
        viewState.onRemovedOverlay(idxRemoved, removedOverlay, overlays)
        viewState.onOverlaysChangedList(overlays)
        viewState.updatePlayback(overlays, overlayHandler!!.getSelectedOverlay(), isPlaying = false)
    }

    override fun changeTimeRangeSelectedOverlay(startTime: Long, endTime: Long) {
        overlayHandler!!.changeTimeRangeSelectedOverlay(startTime, endTime)
    }

    override fun updateVideoPositionWithSeekBar(time: Long) {
        if (time >= mediaController.videoDuration) return
        overlayHandler!!.changeVisibilityOverlayByTime(time)
    }

    override fun seekBarRewind(currentTime: Long) {
        pausePlayback()
        mediaController.seekTo(currentTime)
        overlayHandler!!.changeVisibilityOverlayByTime(currentTime)
    }

    override fun seekBarCompletePlaying() {
        pausePlayback()
        mediaController.seekTo(0L)
        viewState.setControlsToTime(0L)
    }

    override fun seekBarOnTouch() {
        pausePlayback()
    }

    override fun seekBarOnDoubleTap() {
    }

    fun editItem(index: Int) {
        overlayHandler!!.editOverlay(index)
    }

    @SuppressLint("CheckResult")
    fun processVideo(context: Context) {
        val videoDuration = mediaController.videoDuration
        val amountFrames = videoDuration / App.FRAME_TIME_MS
        val frameTime = if (amountFrames > 0) videoDuration / amountFrames else App.FRAME_TIME_MS
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
                    // Empty result
                    audioTranscribeResult.caption == null ||
                            audioTranscribeResult.caption.isEmpty() -> {
                        Log.d(TAG, "No captions")
                        viewState.showPopupWindow(context.getString(R.string.error_no_captions))
                    }
                    //Success
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
        viewState.updatePlayback(overlayHandler!!.getOverlays(), null, false)
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
        result.forEach { addOverlay(it.startTime, it.endTime, it.text) }
        // TODO("View cant be drawn if it has never been displayed.
        //       On adding stage we will show all of these, but next we should hide it")
        overlayHandler!!.hideAllOverlaysExceptAtStart()
    }

    fun deleteOverlay(item: Int) {
        overlayHandler!!.removeOverlay(item)
    }

    fun getVideoDetails() = mediaController.videoDetails
    fun togglePlaybackState() = mediaController.toggle()
    fun pausePlayback() = mediaController.setPlayState(false)
    fun startPlayback() = mediaController.setPlayState(false)

    override fun handlePlaybackState(state: PlaybackEvent) {
        when (state.playState) {
            PlaybackState.PLAY -> {
                val overlays = overlayHandler!!.getOverlaysWithSelected()
                viewState.updatePlayback(
                    overlays.second, overlays.first,
                    isPlaying = true
                )
            }
            PlaybackState.PAUSED -> {
                val overlays = overlayHandler!!.getOverlaysWithSelected()
                viewState.updatePlayback(
                    overlays.second, overlays.first,
                    isPlaying = false
                )
            }
            PlaybackState.STOP -> {

            }
            PlaybackState.REWIND -> {

            }
        }
    }

    /**
     * Toggle mute on/off video playback
     * @param clicked Was fired by user click
     */
    fun toggleMute(clicked: Boolean = false) {
        toggleMute(!mediaController.isMuted, clicked)
    }

    private fun toggleMute(state: Boolean, clicked: Boolean) {
        mediaController.toggleMute(state)
        viewState.toggledMuteState(state, clicked)
    }

    companion object {
        private val TAG = VideoEditActivityPresenter::class.java.simpleName
    }

    override fun onPlayPauseButtonClicked() {
        togglePlaybackState()
    }
}