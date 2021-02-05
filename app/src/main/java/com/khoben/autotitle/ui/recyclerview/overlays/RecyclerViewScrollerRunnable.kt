package com.khoben.autotitle.ui.recyclerview.overlays

import android.view.animation.AnimationUtils
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.khoben.autotitle.extension.dp

class RecyclerViewScrollerRunnable(
    private val mRecyclerView: RecyclerView,
    private val mScrolledListener: OnScrolledListener?
) :
    Runnable {
    private var mScrollDir = 0
    private val mAutoscrollAmount = 500.dp()
    private var mPrevTime: Long = 0
    fun setScrollDir(dir: Int) {
        if (mScrollDir == dir) return
        if (dir != 0 && mScrollDir == 0) ViewCompat.postOnAnimation(mRecyclerView, this)
        mScrollDir = dir
        mPrevTime = AnimationUtils.currentAnimationTimeMillis()
    }

    override fun run() {
        if (mScrollDir == 0) return
        val now = AnimationUtils.currentAnimationTimeMillis()
        val delta = (now - mPrevTime) * 0.001f
        mPrevTime = now
        mRecyclerView.scrollBy(0, (delta * mAutoscrollAmount * mScrollDir).toInt())
        mScrolledListener?.onScrolled(mScrollDir)
        ViewCompat.postOnAnimation(mRecyclerView, this)
    }

    interface OnScrolledListener {
        fun onScrolled(scrollDir: Int)
    }
}