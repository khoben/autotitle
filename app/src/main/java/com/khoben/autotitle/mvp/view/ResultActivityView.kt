package com.khoben.autotitle.mvp.view

import moxy.MvpView
import moxy.viewstate.strategy.alias.AddToEndSingle
import moxy.viewstate.strategy.alias.Skip

interface ResultActivityView : MvpView {
    @Skip
    fun showVideoSavedToast(path: String?)

    @AddToEndSingle
    fun alreadySaved()
}