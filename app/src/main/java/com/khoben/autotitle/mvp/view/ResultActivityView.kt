package com.khoben.autotitle.mvp.view

import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(value = AddToEndSingleStrategy::class)
interface ResultActivityView : MvpView {
    fun showVideoSavedToast(path: String?)
}