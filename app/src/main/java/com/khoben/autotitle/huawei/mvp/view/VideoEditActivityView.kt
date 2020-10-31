package com.khoben.autotitle.huawei.mvp.view

import android.graphics.Bitmap
import com.khoben.autotitle.huawei.ui.overlay.OverlayText
import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(value = AddToEndSingleStrategy::class)
interface VideoEditActivityView : MvpView {
    fun stopLoading()
    fun onErrorThumbnailsProcessing(e: Throwable)
    fun onThumbnailsProcessed(thumbnails: List<Bitmap>, frameTime: Long)
    fun initVideoContainerLayoutParams()
    fun finishOnError()
    fun videoPlay(baseImageViews: List<OverlayText>?, isVideoPlaying: Boolean)
    fun drawOverlayTimeRange(
        overlays: List<OverlayText>?,
        selectedOverlay: OverlayText?,
        isEdit: Boolean
    )

    fun recoverView()
    fun onOverlaysChangedListViewNotifier(overlays: List<OverlayText>)
    fun onVideoSavingStarted()
    fun onVideoSavingCancelled()
    fun onVideoSavingError(msg: String)
    fun onVideoSavingProgress(progress: Double)
    fun onVideoSavingComplete(filepath: String)
    fun setControlsToTime(time: Long)
}