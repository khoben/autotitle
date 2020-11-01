package com.khoben.autotitle.huawei.mvp.view

import android.graphics.Bitmap
import com.khoben.autotitle.huawei.ui.overlay.OverlayText
import moxy.MvpView
import moxy.viewstate.strategy.*

@StateStrategyType(value = AddToEndSingleStrategy::class)
interface VideoEditActivityView : MvpView {

    @StateStrategyType(value = SkipStrategy::class)
    fun stopLoadingView()

    @StateStrategyType(value = AddToEndStrategy::class)
    fun onThumbnailsProcessed(thumbnails: List<Bitmap>, frameTime: Long)

    @StateStrategyType(value = SingleStateStrategy::class)
    fun onErrorThumbnailsProcessing(e: Throwable)

    @StateStrategyType(value = OneExecutionStateStrategy::class)
    fun setControlsToTime(time: Long)

    @StateStrategyType(value = AddToEndStrategy::class)
    fun initVideoContainerLayoutParams()

    fun onOverlaysChangedListViewNotifier(overlays: List<OverlayText>)

    @StateStrategyType(value = SkipStrategy::class)
    fun finishOnError()

    @StateStrategyType(value = AddToEndStrategy::class)
    fun recoverView()

    @StateStrategyType(value = SkipStrategy::class)
    fun onVideoSavingStarted()

    @StateStrategyType(value = SkipStrategy::class)
    fun onVideoSavingCancelled()

    @StateStrategyType(value = SkipStrategy::class)
    fun onVideoSavingError(msg: String)

    @StateStrategyType(value = SkipStrategy::class)
    fun onVideoSavingProgress(progress: Double)

    @StateStrategyType(value = SkipStrategy::class)
    fun onVideoSavingComplete(filepath: String)

    @StateStrategyType(value = AddToEndStrategy::class)
    fun videoPlay(baseImageViews: List<OverlayText>?, isVideoPlaying: Boolean)

    @StateStrategyType(value = AddToEndStrategy::class)
    fun drawOverlayTimeRange(
        overlays: List<OverlayText>?,
        selectedOverlay: OverlayText?,
        isEdit: Boolean
    )
}