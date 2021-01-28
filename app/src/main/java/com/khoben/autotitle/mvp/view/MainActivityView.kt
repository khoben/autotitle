package com.khoben.autotitle.mvp.view

import android.net.Uri
import com.khoben.autotitle.database.entity.Project
import moxy.MvpView
import moxy.viewstate.strategy.alias.AddToEnd
import moxy.viewstate.strategy.alias.AddToEndSingle

interface MainActivityView : MvpView {
    @AddToEndSingle
    fun onVideoSelected(uri: Uri)

    @AddToEndSingle
    fun hideRecentProject()

    @AddToEnd
    fun submitList(list: List<Project>)

    @AddToEndSingle
    fun showEditTitleFragment(id: Long, title: String)

    @AddToEndSingle
    fun showRecentProject()
}