package com.khoben.autotitle.mvp.view

import com.khoben.autotitle.database.entity.Project
import com.khoben.autotitle.model.VideoInfo
import com.khoben.autotitle.service.mediaplayer.MediaSurfacePlayer
import com.khoben.autotitle.service.mediaplayer.VideoRender
import com.khoben.autotitle.ui.overlay.OverlayObject
import com.khoben.autotitle.ui.player.seekbar.FramesHolder
import moxy.MvpView
import moxy.viewstate.strategy.alias.*
import java.util.*

interface VideoEditActivityView : MvpView {

    @Skip
    fun setLoadingViewVisibility(visible: Boolean)

    @AddToEndSingle
    fun onVideoProcessed()

    @AddToEndSingle
    fun loadFrames(frameResult: FramesHolder)

    @SingleState
    fun onErrorVideoProcessing(e: Throwable)

    @Skip
    fun showPopupWindow(content: String)

    @OneExecution
    fun setControlsToTime(time: Long)

    @AddToEndSingle
    fun initVideoContainerLayoutParams(
        mediaPlayer: MediaSurfacePlayer,
        videoRenderer: VideoRender,
        videoDetails: VideoInfo
    )

    @AddToEndSingle
    fun onOverlaysChangedList(overlays: List<OverlayObject>)

    @Skip
    fun finishOnError()

    @Skip
    fun onVideoSavingStarted()

    @Skip
    fun onVideoSavingCancelled()

    @Skip
    fun onVideoSavingError(msg: String)

    @Skip
    fun onVideoSavingProgress(progress: Double)

    @OneExecution
    fun onVideoSavingComplete(filepath: String)

    @AddToEnd
    fun updatePlayback(
        overlays: List<OverlayObject>,
        selectedOverlay: OverlayObject?,
        isPlaying: Boolean
    )

    @Skip
    fun onRemovedOverlay(
        idxRemoved: Int,
        removedOverlay: OverlayObject,
        overlays: List<OverlayObject>
    )

    @AddToEnd
    fun highlightListViewItem(index: Int, uuid: UUID? = null)

    @OneExecution
    fun toggledMuteState(state: Boolean, clicked: Boolean)

    @OneExecution
    fun showOverlayEditor(overlay: OverlayObject)

    @OneExecution
    fun createNewProject(project: Project)

    @OneExecution
    fun updateProject(project: Project)
}