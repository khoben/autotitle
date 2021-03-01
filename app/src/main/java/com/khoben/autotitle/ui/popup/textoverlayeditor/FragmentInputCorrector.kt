package com.khoben.autotitle.ui.popup.textoverlayeditor

import android.view.WindowManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import javax.inject.Inject

/**
 * FragmentSoftInputCorrector changes a soft input type of activity
 * for a single fragment that requires SOFT_INPUT_ADJUST_RESIZE
 * during it is attached to this activity
 *
 * Usage inside a fragment:
 *
 *  override fun onAttach(context: Context) {
 *      super.onAttach(context)
 *      FragmentSoftInputCorrector(activity!!, this)
 *  }
 *  @author [featzima](https://gist.github.com/featzima/2567135cbaff227a69453d7bf1e5269a)
 */
class FragmentSoftInputCorrector @Inject constructor(
    private val activity: FragmentActivity,
    private val fragment: Fragment
) : LifecycleObserver {

    private var originalSoftInputMode: Int? = null

    init {
        fragment.lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun replaceSoftInput() {
        originalSoftInputMode = activity.window?.attributes?.softInputMode
        activity.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun restoreSoftInput() {
        originalSoftInputMode?.let { activity.window?.setSoftInputMode(it) }
        fragment.lifecycle.removeObserver(this)
    }
}