package com.khoben.autotitle.huawei.mvp.view

import android.graphics.Bitmap
import com.khoben.autotitle.huawei.ui.overlay.OverlayText
import moxy.MvpView
import moxy.viewstate.strategy.*
import java.util.ArrayList

@StateStrategyType(value = AddToEndSingleStrategy::class)
interface VideoEditActivityView : MvpView {

    @StateStrategyType(value = SkipStrategy::class)
    fun stopLoadingView()

    @StateStrategyType(value = AddToEndStrategy::class)
    fun onVideoProcessed(thumbnails: List<Bitmap>, frameTime: Long)

    @StateStrategyType(value = SingleStateStrategy::class)
    fun onErrorVideoProcessing(e: Throwable)

    @StateStrategyType(value = SkipStrategy::class)
    fun showPopupWindow(content: String)

    @StateStrategyType(value = OneExecutionStateStrategy::class)
    fun setControlsToTime(time: Long)

    @StateStrategyType(value = AddToEndStrategy::class)
    fun initVideoContainerLayoutParams()

    fun onOverlaysChangedList(overlays: List<OverlayText>)

    @StateStrategyType(value = SkipStrategy::class)
    fun finishOnError()

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

//    @StateStrategyType(value = AddToEndStrategy::class)
//    fun videoPlay(baseImageViews: List<OverlayText>?, isVideoPlaying: Boolean)

    @StateStrategyType(value = AddToEndStrategy::class)
    fun updatePlayback(
        overlays: List<OverlayText>?,
        selectedOverlay: OverlayText?,
        isEdit: Boolean,
        isPlaying: Boolean
    )

    @StateStrategyType(value = SkipStrategy::class)
    fun onRemovedOverlay(
        idxRemoved: Int,
        removedOverlay: OverlayText,
        overlays: ArrayList<OverlayText>
    )

    @StateStrategyType(value = AddToEndStrategy::class)
    fun highlightListViewItem(index: Int)
}