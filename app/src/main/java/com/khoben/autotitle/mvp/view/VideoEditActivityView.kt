package com.khoben.autotitle.mvp.view

import android.graphics.Bitmap
import com.khoben.autotitle.service.mediaplayer.MediaSurfacePlayer
import com.khoben.autotitle.service.mediaplayer.VideoRender
import com.khoben.autotitle.ui.overlay.OverlayObject
import moxy.MvpView
import moxy.viewstate.strategy.alias.AddToEnd
import moxy.viewstate.strategy.alias.OneExecution
import moxy.viewstate.strategy.alias.SingleState
import moxy.viewstate.strategy.alias.Skip
import java.util.*

interface VideoEditActivityView : MvpView {

    @Skip
    fun setLoadingViewVisibility(visible: Boolean)

    @AddToEnd
    fun onVideoProcessed(thumbnails: List<Bitmap>, frameTime: Long)

    @SingleState
    fun onErrorVideoProcessing(e: Throwable)

    @Skip
    fun showPopupWindow(content: String)

    @OneExecution
    fun setControlsToTime(time: Long)

    @AddToEnd
    fun initVideoContainerLayoutParams(mediaPlayer: MediaSurfacePlayer, videoRenderer: VideoRender)

    @AddToEnd
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

    @Skip
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
        overlays: ArrayList<OverlayObject>
    )

    @AddToEnd
    fun highlightListViewItem(index: Int, uuid: UUID? = null)

    @AddToEnd
    fun unSelectRecycler()

    @OneExecution
    fun toggledMuteState(state: Boolean, clicked: Boolean)

    @OneExecution
    fun showOverlayEditor(overlay: OverlayObject)
}