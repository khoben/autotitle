package com.khoben.autotitle.mvp.view

import android.net.Uri
import com.khoben.autotitle.model.project.ThumbProject
import moxy.MvpView
import moxy.viewstate.strategy.alias.AddToEnd
import moxy.viewstate.strategy.alias.AddToEndSingle

interface MainActivityView : MvpView {
    @AddToEndSingle
    fun onVideoSelected(uri: Uri)

    @AddToEndSingle
    fun hideRecentProject()

    @AddToEnd
    fun submitList(list: List<ThumbProject>)

    @AddToEndSingle
    fun showEditTitleFragment(idx: Int, title: String)
}