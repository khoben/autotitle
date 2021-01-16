package com.khoben.autotitle.mvp.presenter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.view.ViewGroup
import com.khoben.autotitle.App
import com.khoben.autotitle.R
import com.khoben.autotitle.common.FileUtils
import com.khoben.autotitle.common.FileUtils.getFileName
import com.khoben.autotitle.model.MLCaption
import com.khoben.autotitle.model.PlaybackEvent
import com.khoben.autotitle.model.PlaybackState
import com.khoben.autotitle.model.project.RecentProjectsLoader
import com.khoben.autotitle.model.project.ThumbProject
import com.khoben.autotitle.mvp.view.VideoEditActivityView
import com.khoben.autotitle.service.audioextractor.AudioExtractorNoAudioException
import com.khoben.autotitle.service.mediaplayer.MediaController
import com.khoben.autotitle.service.mediaplayer.VideoRender
import com.khoben.autotitle.service.videoloader.VideoLoader
import com.khoben.autotitle.service.videosaver.VideoProcessorBase
import com.khoben.autotitle.service.videosaver.VideoProcessorListener
import com.khoben.autotitle.ui.overlay.OverlayHandler
import com.khoben.autotitle.ui.overlay.OverlayObject
import com.khoben.autotitle.ui.player.PlayPauseMaterialButton
import com.khoben.autotitle.ui.player.VideoControlsView
import com.khoben.autotitle.ui.player.seekbar.SeekBarListener
import moxy.InjectViewState
import moxy.MvpPresenter
import timber.log.Timber
import java.lang.ref.WeakReference
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

    fun setDataSourceUri(uri: Uri) {
        if (sourceUri != null) return
        sourceUri = uri
    }

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()

        mediaController.setVideoSource(sourceUri!!)

        RecentProjectsLoader.new(
            ThumbProject(
                id = UUID.randomUUID().toString(),
                title = sourceUri?.getFileName(appContext),
                dateCreated = System.currentTimeMillis(),
                dateUpdated = System.currentTimeMillis(),
                videoDuration = mediaController.videoDuration,
                videoFileSizeBytes = 10500L,
                videoSourceFilePath = FileUtils.getRealPathFromURI(appContext, sourceUri!!)
            )
        )

        processVideo(appContext)
        viewState.initVideoContainerLayoutParams(mediaController.mediaPlayer, videoRenderer)
    }

    /**
     * Init object that handles add/edit/delete operations
     * with overlays
     * @param parentView Parent view to attach to this overlays
     */
    fun initOverlayHandler(
            parentView: ViewGroup,
            videoView: VideoControlsView
    ) {
        if (overlayHandler == null) {
            overlayHandler = OverlayHandler.Builder()
                    .ctx(appContext)
                    .parent(parentView)
                    .build()
            overlayHandler?.overlayObjectEventListener = this
        } else {
            overlayHandler?.setLayout(appContext, WeakReference(parentView))
            // TODO: need to move to better place
            restoreCurrentPlaybackTime()
        }
        videoView.seekBarListener = this
    }

    /**
     * @return True if muted, otherwise false
     */
    fun getMuteState() = mediaController.isMuted

    /**
     * Sets media volume to 0% (muted) or to 100% (unmuted)
     * @param mute True if muted, otherwise false
     */
    fun setMuteState(mute: Boolean) {
        viewState.toggledMuteState(mute, false)
        mediaController.toggleMute(mute)
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
        val toTime = overlayHandler!!.addTextOverlayAfterSpecificPosition(pos)
        viewState.setControlsToTime(toTime)
    }

    fun addOverlayAtSpecificPosition(pos: Int, item: OverlayObject) {
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
    fun saveVideo(parentViewSize: Pair<Int, Int>) {
        pausePlayback()
        // make visible all overlays
        overlayHandler!!.showRootView()
        val outputPath = FileUtils.getRandomFilepath(appContext, App.VIDEO_EXTENSION)
        viewState.onVideoSavingStarted()
        videoProcessor.apply {
            setup(
                    overlayHandler!!.getOverlays(),
                    sourceUri!!,
                    outputPath,
                    appContext,
                    mediaController.videoDetails!!,
                    parentViewSize
            )
            listener = object : VideoProcessorListener {
                override fun onProgress(progress: Double) {
                    viewState.onVideoSavingProgress(progress)
                }

                override fun onComplete(filepath: String) {
                    viewState.onVideoSavingComplete(outputPath)
                }

                override fun onCanceled() {
                    viewState.onVideoSavingCancelled()
                    FileUtils.deleteFile(appContext, outputPath)
                }

                override fun onError(message: String) {
                    viewState.onVideoSavingError(message)
                    FileUtils.deleteFile(appContext, outputPath)
                }
            }
        }.start()
    }

    /**
     * Cancel video saving process
     */
    fun cancelSavingVideo() {
        videoProcessor.cancel()
    }

    /**
     * Pause video saving process
     */
    fun pauseSavingVideo() {
        videoProcessor.pause()
    }

    fun resumeSavingVideo() {
        videoProcessor.resume()
    }

    fun unEditable() {
        pausePlayback()
        overlayHandler!!.unEditable()
    }

    override fun onEdit(overlay: OverlayObject) {
        viewState.showOverlayEditor(overlay)
    }

    override fun onEdited(overlay: List<OverlayObject>) {
        viewState.onOverlaysChangedList(overlay)
    }

    override fun onUnEditable(overlay: OverlayObject?, overlays: List<OverlayObject>) {
        viewState.updatePlayback(overlays, overlay, isPlaying = false)
        viewState.onOverlaysChangedList(overlays)
    }

    override fun onAdded(overlay: OverlayObject?, overlays: List<OverlayObject>, isEdit: Boolean) {
        viewState.updatePlayback(overlays, overlay, isPlaying = false)
        viewState.onOverlaysChangedList(overlays)
        val index = overlays.indexOf(overlay)
        if (index != -1) {
            Timber.d("onAdded $index")
            viewState.highlightListViewItem(index, overlay!!.uuid)
        }
    }

    override fun onAddedAll(overlays: List<OverlayObject>) {
        viewState.updatePlayback(overlays, null, isPlaying = false)
        viewState.onOverlaysChangedList(overlays)
    }

    override fun onSelect(
            overlay: OverlayObject?,
            overlays: List<OverlayObject>,
            seekToOverlayStart: Boolean
    ) {
        if (seekToOverlayStart)
            viewState.setControlsToTime(overlay!!.startTime)
        viewState.updatePlayback(overlays, overlay, isPlaying = false)
        viewState.onOverlaysChangedList(overlays)
        viewState.highlightListViewItem(overlays.indexOf(overlay), overlay!!.uuid)
    }

    override fun onRemoved(
            idxRemoved: Int,
            removedOverlay: OverlayObject,
            overlays: ArrayList<OverlayObject>
    ) {
        viewState.onRemovedOverlay(idxRemoved, removedOverlay, overlays)
        viewState.onOverlaysChangedList(overlays)
        viewState.updatePlayback(overlays, null, isPlaying = false)
    }

    override fun changeTimeRangeSelectedOverlay(startTime: Long, endTime: Long) {
        overlayHandler!!.changeTimeRangeSelectedOverlay(startTime, endTime)
    }

    override fun syncCurrentPlaybackTimeWithSeekBar(time: Long, isSeeking: Boolean) {
        if (time >= mediaController.videoDuration) return
        if (isSeeking) seekTo(time)
        overlayHandler!!.changeVisibilityOverlayByTime(time)
    }

    override fun seekBarCompletePlaying() {
        seekTo(0L)
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

    fun onEditedOverlayFragment(overlay: OverlayObject, text: String?, colorCode: Int) {
        if (text == null) return
        overlayHandler!!.editedOverlay(overlay, text, colorCode)
    }

    @SuppressLint("CheckResult")
    fun processVideo(context: Context) {
        viewState.setLoadingViewVisibility(true)
        val videoDuration = mediaController.videoDuration
        val amountFrames = videoDuration / App.FRAME_TIME_MS
        val frameTime = if (amountFrames > 0) videoDuration / amountFrames else App.FRAME_TIME_MS
        videoLoader.init(context, sourceUri!!, frameTime)
                .onError { error ->
                    Timber.d("Error while loading video $error")
                    errorProcessVideo(error)
                }
                .load {
                    val audioTranscribeResult = it.second
                    val framesResult = it.first
                    when {
                        // Service transcription error
                        audioTranscribeResult.throwable != null -> {
                            when(audioTranscribeResult.throwable) {
                                is AudioExtractorNoAudioException -> {
                                    Timber.e("No captions. Source video doesn't have audio track")
                                    viewState.showPopupWindow(
                                        context.getString(R.string.error_no_captions)
                                                + ".\n"
                                                + context.getString(R.string.error_no_captions_no_audio)
                                    )
                                }
                                else -> {
                                    Timber.e("No captions. Network service error")
                                    viewState.showPopupWindow(
                                        context.getString(R.string.error_no_captions)
                                                + "\n"
                                                + context.getString(R.string.error_no_caption_net_error)
                                    )
                                }
                            }
                        }
                        // Empty result
                        audioTranscribeResult.caption == null ||
                                audioTranscribeResult.caption.isEmpty() -> {
                            Timber.d("No captions")
                            viewState.showPopupWindow(context.getString(R.string.error_no_captions))
                        }
                        //Success
                        else -> {
                            Timber.d("Text = $audioTranscribeResult")
                            processTranscribe(audioTranscribeResult.caption)
                        }
                    }
                    successProcessedVideo(framesResult, frameTime)
                }
    }

    private fun successProcessedVideo(frames: List<Bitmap>, frameTime: Long) {
        Timber.d("Success video load")
        viewState.onVideoProcessed(frames, frameTime)
        viewState.updatePlayback(overlayHandler!!.getOverlays(), null, false)
    }

    private fun errorProcessVideo(e: Throwable) {
        Timber.e("Error video processing")
        viewState.onErrorVideoProcessing(e)
    }

    /**
     * Add caption
     * @param start Long
     * @param end Long
     * @param text String
     */
    private fun addOverlay(start: Long, end: Long, text: String) {
        overlayHandler!!.addTextOverlay(start, end, text)
    }

    private fun addAllOverlay(overlays: List<MLCaption>) {
        overlayHandler!!.addAllOverlay(overlays)
    }

    /**
     * Adds detected result to overlay
     * @param result List<MLCaption>
     */
    private fun processTranscribe(result: List<MLCaption>) {
        addAllOverlay(result)
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
    fun seekTo(time: Long, withPause: Boolean = true) {
        if (withPause) pausePlayback()
        mediaController.seekTo(time)
    }

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
            PlaybackState.SEEK -> {

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

    override fun onPlayPauseButtonClicked() {
        togglePlaybackState()
    }

    private fun restoreCurrentPlaybackTime() {
        viewState.setControlsToTime(mediaController.currentPosition)
    }
}