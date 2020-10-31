package com.khoben.autotitle.huawei.mvp.view

import android.net.Uri
import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(value = AddToEndSingleStrategy::class)
interface MainActivityView : MvpView {
    fun onVideoSelected(uri: Uri)
}