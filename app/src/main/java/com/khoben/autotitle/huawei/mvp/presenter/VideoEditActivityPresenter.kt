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
import com.khoben.autotitle.huawei.model.VideoInfo
import com.khoben.autotitle.huawei.mvp.view.VideoEditActivityView
import com.khoben.autotitle.huawei.service.mediaplayer.MediaPlayerSurfaceCallback
import com.khoben.autotitle.huawei.service.mediaplayer.MediaSurfacePlayer
import com.khoben.autotitle.huawei.service.videoloader.VideoLoader
import com.khoben.autotitle.huawei.service.videosaver.VideoProcessorBase
import com.khoben.autotitle.huawei.service.videosaver.VideoProcessorListener
import com.khoben.autotitle.huawei.ui.overlay.OverlayHandler
import com.khoben.autotitle.huawei.ui.overlay.OverlayText
import com.khoben.autotitle.huawei.ui.player.VideoControlsView
import com.khoben.autotitle.huawei.ui.player.VideoSeekBarView
import moxy.InjectViewState
import moxy.MvpPresenter
import javax.inject.Inject

@InjectViewState
class VideoEditActivityPresenter : MvpPresenter<VideoEditActivityView>(),
    OverlayHandler.OverlayObjectEventListener,
    MediaPlayerSurfaceCallback,
    VideoSeekBarView.SeekBarListener {

    @Inject
    lateinit var videoProcessor: VideoProcessorBase

    @Inject
    lateinit var mediaPlayer: MediaSurfacePlayer


    @Inject
    lateinit var appContext: Context

    private var overlayHandler: OverlayHandler? = null
    private var sourceUri: Uri? = null
    private lateinit var videoLoader: VideoLoader

    init {
        App.applicationComponent.inject(this)
        mediaPlayer.setMediaCallbackListener(this)
        videoLoader = VideoLoader()
    }

    fun setUri(uri: Uri) {
        sourceUri = uri
    }

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        initVideoDetails(sourceUri!!)
        processVideo(appContext)
        viewState.initVideoContainerLayoutParams()
    }

    fun setLayoutToEditor(
        context: Context,
        parentView: RelativeLayout, videoView: VideoControlsView
    ) {
        overlayHandler?.setLayout(context, parentView)
        videoView.seekBarListener = this
    }

    /**
     * Init editor that handles add/edit/delete operations
     * on overlays
     * @param parentView Parent view to attach to this overlays
     */
    fun initEditor(context: Context, parentView: RelativeLayout, videoView: VideoControlsView) {
        overlayHandler = OverlayHandler.Builder()
            .ctx(context)
            .parent(parentView)
            .build()
        overlayHandler?.overlayObjectEventListener = this
        videoView.seekBarListener = this
    }

    /**
     * Adds new overlay view
     */
    fun addOverlayAtCurrentPosition() {
        // pause playback before adding new overlay
        setPlayState(false)
        overlayHandler!!.addOverlay(getCurrentPosition())
    }

    /**
     * Adds new overlay view
     */
    fun addOverlayAtSpecificPosition(pos: Int) {
        // pause playback before adding new overlay
        setPlayState(false)
        val toTime = overlayHandler!!.addOverlayAtSpecificPosition(pos)
        viewState.setControlsToTime(toTime)
    }

    fun recyclerSelectOverlay(pos: Int) {
        setPlayState(false)
        overlayHandler!!.selectedOverlayId(pos)
        viewState.setControlsToTime(overlayHandler!!.getOverlays()[pos].startTime)
    }


    /**
     * Saves video with overlays
     */
    fun saveVideo(context: Context, parentViewSize: Pair<Int, Int>) {
        overlayHandler!!.showRootView()
        // pause playing
        setPlayState(false)
        viewState.videoPlay(overlayHandler!!.getOverlays(), false)

        val outputPath = FileUtils.getRandomFilepath(context, App.VIDEO_EXTENSION)
        videoProcessor.setup(
            overlayHandler!!.getOverlays(),
            sourceUri!!,
            outputPath,
            context,
            getVideoDetails()!!,
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

    /**
     * Toggles playback state
     */
    fun togglePlayState() {
        val isPlaying = !mediaPlayer.isPlaying()
        mediaPlayer.toggle()
        viewState.videoPlay(overlayHandler!!.getOverlays(), isPlaying)
    }

    /**
     * Sets playback state
     * @param playState True -- Play; False -- Pause
     */
    fun setPlayState(playState: Boolean) {
        when (playState) {
            true -> {
                if (!mediaPlayer.isPlaying()) {
                    mediaPlayer.play()
                }
            }
            false -> {
                viewState.videoPlay(overlayHandler!!.getOverlays(), false)
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause()
                }
            }
        }
    }

    /**
     * Fetch video details from video file with provided uri
     * @param uri Uri
     */
    fun initVideoDetails(uri: Uri) {
        sourceUri = uri
        mediaPlayer.setDataSourceUri(uri)
//        viewState.setTotalTime(getVideoDuration())
    }

    fun getVideoDuration(): Long {
        return mediaPlayer.getVideoDuration()
    }

    fun getVideoDetails(): VideoInfo? {
        return mediaPlayer.getVideoInfo()
    }

    fun getCurrentPosition(): Long {
        return mediaPlayer.getCurrentPosition()
    }

    fun unEditable() {
        overlayHandler!!.unEditable()
    }

    override fun onRemoved(overlay: OverlayText) {
    }

    override fun onRemoved(overlay: List<OverlayText>) {
        viewState.onOverlaysChangedListViewNotifier(overlay)
        viewState.drawOverlayTimeRange(overlay, null, false)
    }

    override fun onEdit() {
    }

    override fun onEdited(overlay: OverlayText) {
    }

    override fun onEdited(overlay: List<OverlayText>) {
        viewState.onOverlaysChangedListViewNotifier(overlay)
    }

    override fun onUnEditable(overlay: OverlayText?, overlays: List<OverlayText>) {
        viewState.drawOverlayTimeRange(overlays, overlay, false)
    }

    override fun onAdd() {
    }

    override fun onAdded(overlay: OverlayText) {
    }


    override fun onAdded(overlay: OverlayText?, overlays: List<OverlayText>, isEdit: Boolean) {
        viewState.drawOverlayTimeRange(overlays, overlay, isEdit)
        viewState.onOverlaysChangedListViewNotifier(overlays)
    }

    override fun onSelect(overlay: OverlayText?, overlays: List<OverlayText>) {
        viewState.drawOverlayTimeRange(overlays, overlay, true)
    }


    override fun onMediaPlayerStarted() {
    }

    override fun onMediaPlayerPaused() {
    }

    override fun onMediaPlayerPrepared() {
    }

    override fun onMediaPlayerCompletion() {
    }

    override fun updateVideoPositionWithSeekBar(time: Long) {
        if (time >= getVideoDuration()) {
            viewState.recoverView()
            overlayHandler!!.updateVideoPositionWithSeekBar(0L)
            return
        }
        overlayHandler!!.updateVideoPositionWithSeekBar(time)
    }

    override fun changeTimeRangeSelectedOverlay(startTime: Long, endTime: Long) {
        overlayHandler!!.changeTimeRangeSelectedOverlay(startTime, endTime)
    }

    override fun seekBarRewind(currentTime: Long) {
        if (mediaPlayer.isPlaying()) {
            setPlayState(false)
        }
        mediaPlayer.seekTo(currentTime)
        overlayHandler!!.seekBarRewind(currentTime)
    }

    override fun seekBarOnTouch() {
        setPlayState(false)
        viewState.videoPlay(overlayHandler!!.getOverlays(), false)
    }

    override fun seekBarOnDoubleTap() {
        addOverlayAtCurrentPosition()
    }

    fun editItem(index: Int) {
        overlayHandler!!.editOverlay(index)
    }

    @SuppressLint("CheckResult")
    fun processVideo(context: Context) {
        val videoDuration = mediaPlayer.getVideoDuration()
        val frame = videoDuration / App.FRAME_TIME_MS
        val frameTime = if (frame > 0) videoDuration / frame else App.FRAME_TIME_MS
        videoLoader.init(context, sourceUri!!, frameTime)
            .onError { error ->
                Log.d(TAG, "Error while loading video $error")
                errorProcessVideo(error)
            }
            .load {
                val audioR = it.second
                val framesR = it.first
                when {
                    audioR.isNotEmpty() -> {
                        Log.d(TAG, "Text = $audioR")
                        processTranscribe(audioR)
                    }
                    else -> {
                        Log.d(TAG, "No captions")
                        viewState.showPopupWindow("", context.getString(R.string.error_no_captions))
                    }
                }
                successProcessedVideo(framesR, frameTime)
            }
    }

    private fun successProcessedVideo(frames: List<Bitmap>, frameTime: Long) {
        Log.d(TAG, "Success processed")
        // TODO("Moxy tells that no views were attached then calling from Rx")
//        VideoEditActivity.temporaryFixViewStateAccess!!.onThumbnailsProcessed(frames, frameTime)
        viewState.onVideoProcessed(frames, frameTime)
    }

    private fun errorProcessVideo(e: Throwable) {
        viewState.onErrorVideoProcessing(e)
        // TODO("Moxy tells that no views were attached then calling from Rx")
//        VideoEditActivity.temporaryFixViewStateAccess!!.onErrorThumbnailsProcessing(e)
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

    companion object {
        private val TAG = VideoEditActivityPresenter::class.java.simpleName
    }

}