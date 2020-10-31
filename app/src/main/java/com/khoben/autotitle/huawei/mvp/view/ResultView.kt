package com.khoben.autotitle.huawei.mvp.view

import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(value = AddToEndSingleStrategy::class)
interface ResultView : MvpView {
    fun showVideoSavedToast(path: String?)
}