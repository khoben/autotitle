package com.khoben.autotitle.mvp.view

import android.net.Uri
import moxy.MvpView
import moxy.viewstate.strategy.alias.AddToEndSingle

interface MainActivityView : MvpView {
    @AddToEndSingle
    fun onVideoSelected(uri: Uri)

    @AddToEndSingle
    fun hideRecentProject()
}