package com.khoben.autotitle.mvp.presenter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.view.ViewGroup
import androidx.annotation.ColorInt
import com.khoben.autotitle.App
import com.khoben.autotitle.R
import com.khoben.autotitle.common.BitmapUtils
import com.khoben.autotitle.common.FileUtils
import com.khoben.autotitle.common.FileUtils.getFileName
import com.khoben.autotitle.database.entity.Project
import com.khoben.autotitle.model.MLCaption
import com.khoben.autotitle.model.PlaybackEvent
import com.khoben.autotitle.model.PlaybackState
import com.khoben.autotitle.model.VideoLoadMode
import com.khoben.autotitle.mvp.view.VideoEditActivityView
import com.khoben.autotitle.service.audioextractor.AudioExtractorNoAudioException
import com.khoben.autotitle.service.frameretriever.AndroidNativeMetadataProvider
import com.khoben.autotitle.service.mediaplayer.MediaController
import com.khoben.autotitle.service.mediaplayer.VideoRender
import com.khoben.autotitle.service.videoloader.VideoLoaderContract
import com.khoben.autotitle.service.videosaver.VideoProcessorBase
import com.khoben.autotitle.service.videosaver.VideoProcessorListener
import com.khoben.autotitle.ui.overlay.OverlayHandler
import com.khoben.autotitle.ui.overlay.OverlayObject
import com.khoben.autotitle.ui.player.VideoControlsView
import com.khoben.autotitle.ui.player.seekbar.SeekBarListener
import moxy.InjectViewState
import moxy.MvpPresenter
import timber.log.Timber
import java.lang.ref.WeakReference
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
    lateinit var videoRenderer: VideoRender

    @Inject
    lateinit var appContext: Context

    @Inject
    lateinit var videoLoader: VideoLoaderContract

    private var overlayHandler: OverlayHandler? = null
    private var sourceUri: Uri? = null
    private var videoLoadMode: VideoLoadMode? = null

    init {
        App.applicationComponent.inject(this)
        mediaController.addSubscription(this)
    }

    fun initVideoSource(
        sourceVideoUri: Uri,
        videoLoadingMode: VideoLoadMode,
        existProject: Project?
    ) {
        if (sourceUri != null && videoLoadMode != null) return

        sourceUri = sourceVideoUri
        videoLoadMode = videoLoadingMode

        mediaController.setVideoSource(sourceUri!!)

        viewState.initVideoContainerLayoutParams(
            mediaController.mediaPlayer,
            videoRenderer,
            getVideoDetails()!!
        )

        if (videoLoadingMode != VideoLoadMode.LOAD_RECENT) {
            viewState.createNewProject(
                Project(
                    title = sourceUri?.getFileName(appContext)!!,
                    videoDuration = mediaController.videoDuration,
                    videoFileSizeBytes = FileUtils.getSizeBytes(appContext, sourceUri!!),
                    sourceFileUri = FileUtils.getRealPathFromURI(appContext, sourceUri!!)!!
                )
            )
        } else {
            existProject?.let { viewState.updateProject(it) }
        }

        processVideo()
    }

    /**
     * Run video processing with frames extraction and speech-to-text conversion
     */
    @SuppressLint("CheckResult")
    fun processVideo() {
        val videoDuration = mediaController.videoDuration
        val amountFrames = videoDuration / App.FRAME_TIME_MS
        val frameTime = if (amountFrames > 0) videoDuration / amountFrames else App.FRAME_TIME_MS
        runVideoLoader(frameTime)
    }

    private fun runVideoLoader(frameTime: Long) {
        val videoLoaderInstance = videoLoader.init(appContext, sourceUri!!, frameTime)

        // Load frame-line
        videoLoaderInstance.loadFrames({ frameResult ->
            viewState.loadFrames(frameResult)
        }, { frameError ->
            Timber.e("Error while loading video $frameError")
            errorProcessVideo(frameError)
        })

        // Load captions
        if (videoLoadMode == VideoLoadMode.AUTO_DETECT) {
            viewState.setLoadingViewVisibility(true)
            videoLoaderInstance.loadCaptions({ captionResult ->
                // if caption loaded then success
                successProcessedVideo()
                when {
                    // Empty result
                    captionResult.caption == null ||
                            captionResult.caption.isEmpty() -> {
                        Timber.d("No captions")
                        viewState.showInfoMessage( appContext.getString(R.string.error_no_captions), appContext.getString(R.string.attention))
                    }
                    //Success
                    else -> {
                        Timber.d("Text = $captionResult")
                        processTranscribe(captionResult.caption)
                    }
                }
            }, { captionError ->
                viewState.setLoadingViewVisibility(false)
                // Service transcription error
                when (captionError) {
                    is AudioExtractorNoAudioException -> {
                        Timber.e("No captions. Source video doesn't have audio track")
                        viewState.showInfoMessage(
                            appContext.getString(R.string.error_no_captions)
                                    + ".\n"
                                    + appContext.getString(R.string.error_no_captions_no_audio), appContext.getString(R.string.attention)
                        )
                    }
                    else -> {
                        Timber.e("No captions. Network service error")
                        viewState.showInfoMessage(
                            appContext.getString(R.string.error_no_captions)
                                    + "\n"
                                    + appContext.getString(R.string.error_no_caption_net_error), appContext.getString(R.string.attention)
                        )
                    }

                }
                Timber.e("Error while loading video $captionError")
            })
        }
    }

    private fun successProcessedVideo() {
        Timber.d("Success video load")
        viewState.onVideoProcessed()
        viewState.updatePlayback(overlayHandler!!.getOverlays(), null, false)
    }

    private fun errorProcessVideo(e: Throwable) {
        Timber.e("Error video processing")
        viewState.onErrorVideoProcessing(e)
    }

    /**
     * Init object that handles add/edit/delete operations
     * with overlays
     * @param parentView Parent view to attach to this overlays
     */
    fun initOverlayHandler(
        parentView: ViewGroup,
        videoControlsView: VideoControlsView
    ) {
        if (overlayHandler == null) {
            overlayHandler = OverlayHandler.Builder()
                .ctx(appContext)
                .parent(parentView)
                .build()
            overlayHandler?.overlayObjectEventListener = this
        } else {
            overlayHandler?.setLayout(WeakReference(parentView))
        }
        videoControlsView.seekBarListener = this
    }

    /**
     * Restores current playback position from media player
     */
    fun restoreCurrentPlaybackPosition() {
        viewState.setControlsToTime(mediaController.currentPosition)
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
     * Adds new overlay at current playback position
     */
    fun addOverlayAtCurrentPosition() {
        // pause playback before adding new overlay
        pausePlayback()
        overlayHandler!!.addOverlay(mediaController.currentPosition, mediaController.videoDuration)
    }

    /**
     * Adds new overlay view right after overlay with index
     * equals to [pos]
     *
     * @param pos Index of overlay
     */
    fun addOverlayAfterSpecificPosition(pos: Int) {
        // pause playback before adding new overlay
        pausePlayback()
        val toTime = overlayHandler!!.addTextOverlayAfterSpecificPosition(pos)
        viewState.setControlsToTime(toTime)
    }

    /**
     * Adds new overlay at index equals to [idx]
     *
     * @param idx Index of overlay
     * @param item OverlayObject
     */
    fun addOverlayAtSpecificPosition(idx: Int, item: OverlayObject) {
        pausePlayback()
        overlayHandler!!.addOverlayAtSpecificPosition(idx, item)
    }

    /**
     * Highlights overlay with index equals to [idx]
     *
     * @param idx Index of overlay
     */
    fun selectOverlayByIdx(idx: Int) {
        pausePlayback()
        overlayHandler!!.selectOverlayById(idx)
    }


    /**
     * Saves video with overlays
     *
     * @param parentViewSize Overlays parent size
     */
    fun saveVideo(parentViewSize: Pair<Int, Int>) {
        pausePlayback()
        // make visible all overlays
        overlayHandler!!.showRootView()
        val outputPath = FileUtils.getOutputVideoFilePath(appContext)
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

    /**
     * Resume video saving process
     */
    fun resumeSavingVideo() {
        videoProcessor.resume()
    }

    /**
     * Reset overlays selection
     */
    fun clearOverlaySelection() {
        pausePlayback()
        overlayHandler!!.clearOverlaySelection()
    }

    override fun onEdit(overlay: OverlayObject) {
        viewState.showOverlayEditor(overlay)
    }

    override fun onEdited(overlay: List<OverlayObject>) {
        viewState.onOverlaysChangedList(overlay)
    }

    override fun onClearOverlaySelection(overlay: OverlayObject?, overlays: List<OverlayObject>) {
        viewState.updatePlayback(overlays, overlay, isPlaying = false)
        viewState.onOverlaysChangedList(overlays)
    }

    override fun onAdded(overlay: OverlayObject?, overlays: List<OverlayObject>) {
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
        overlays: List<OverlayObject>
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

    /**
     * Edits overlay with index equals to [idx]
     *
     * @param idx Index of overlay
     */
    fun editOverlayItem(idx: Int) {
        overlayHandler!!.editOverlay(idx)
    }

    /**
     * Saves overlay with [text] and [colorCode] properties
     *
     * @param overlay OverlayObject
     * @param text String?
     * @param colorCode Int
     */
    fun saveEditedOverlay(overlay: OverlayObject, text: String?, @ColorInt colorCode: Int) {
        if (text == null) return
        overlayHandler!!.editedOverlay(overlay, text, colorCode)
    }

    /**
     * Add text overlay
     *
     * @param start Start timestamp
     * @param end End timestamp
     * @param text Caption text
     */
    private fun addOverlay(start: Long, end: Long, text: String) {
        overlayHandler!!.addTextOverlay(start, end, text)
    }

    /**
     * Adds [overlays] list
     *
     * @param overlays List<MLCaption>
     */
    private fun addAllOverlay(overlays: List<MLCaption>) {
        overlayHandler!!.addAllOverlay(overlays)
    }

    /**
     * Adds detected result to overlay
     *
     * @param result List<MLCaption>
     */
    private fun processTranscribe(result: List<MLCaption>) {
        addAllOverlay(result)
        // TODO("View cant be drawn if it has never been displayed.
        //       On adding stage we will show all of these, but next we should hide it")
        overlayHandler!!.hideAllOverlaysExceptAtStart()
    }

    /**
     * Deletes overlay with index equals to [idx]
     *
     * @param idx Int
     */
    fun deleteOverlay(idx: Int) {
        overlayHandler!!.removeOverlay(idx)
    }

    /**
     * Gets current loaded video's info
     * @return VideoInfo?
     */
    fun getVideoDetails() = mediaController.videoDetails

    /**
     * Toggles playback state to paused or playing
     */
    fun togglePlaybackState() = mediaController.toggle()

    /**
     * Pauses playback
     */
    fun pausePlayback() = mediaController.setPlayState(false)

    /**
     * Starts playback
     */
    fun startPlayback() = mediaController.setPlayState(false)

    /**
     * Seeking to [time] timestamp
     *
     * @param time Timestamp
     * @param withPause Should be playback paused
     */
    private fun seekTo(time: Long, withPause: Boolean = true) {
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

    fun createProjectThumbnail(id: Long): String {
        val projectFolder = "${App.PROJECTS_FOLDER}/${id}"
        FileUtils.createDirIfNotExists(projectFolder)
        return "${projectFolder}/thumb".also { thumbPath ->
            AndroidNativeMetadataProvider(appContext, sourceUri!!)
                .getFrameAt(0L)?.let { BitmapUtils.cropCenter(it, 512, 384) }
                ?.let {
                    FileUtils.writeBitmap(thumbPath, it, Bitmap.CompressFormat.WEBP, 75)
                }
        }
    }

    fun releaseResources() {
        videoLoader.cancel()
    }
}