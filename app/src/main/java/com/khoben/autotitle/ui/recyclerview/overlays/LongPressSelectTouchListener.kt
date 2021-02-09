package com.khoben.autotitle.ui.recyclerview.overlays

import android.graphics.Rect
import android.view.MotionEvent
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.max
import kotlin.math.min

class LongPressSelectTouchListener(private val mRecyclerView: RecyclerView?) :
    RecyclerView.OnItemTouchListener {
    private var mSelectMode = false
    private var mListener: Listener? = null
    private val mTempRect = Rect()
    private var mStartElementId: Long = -1
    private var mEndElementId: Long = -1
    private var mScroller: RecyclerViewScrollerRunnable? = null


    init {
        mScroller = RecyclerViewScrollerRunnable(
            mRecyclerView!!,
            object : RecyclerViewScrollerRunnable.OnScrolledListener {
                override fun onScrolled(scrollDir: Int) {
                    val llm = mRecyclerView.layoutManager as LinearLayoutManager?
                    updateHighlightedElements(
                        mRecyclerView, mRecyclerView.adapter!!.getItemId(
                            if (scrollDir > 0) llm!!.findLastCompletelyVisibleItemPosition() else llm!!.findFirstCompletelyVisibleItemPosition()
                        )
                    )
                }

            })
    }

    fun startSelectMode(startPos: Long) {
        mSelectMode = true
        mStartElementId = startPos
        mEndElementId = -1
        mListener!!.onElementSelected(mRecyclerView, startPos)
    }

    fun setListener(listener: Listener?) {
        mListener = listener
    }

    fun isElementHighlighted(id: Long): Boolean {
        return id == mStartElementId ||
                id in mStartElementId..mEndElementId ||
                id in mEndElementId..mStartElementId && mEndElementId != -1L
    }

    private fun updateHighlightedElements(recyclerView: RecyclerView?, endId: Long) {
        if (mStartElementId == -1L) {
            mStartElementId = endId
            mListener!!.onElementHighlighted(recyclerView, mStartElementId, true)
            return
        }
        for (i in max(
            mEndElementId,
            mStartElementId
        ) + 1..endId) mListener!!.onElementHighlighted(recyclerView, i, true)
        for (i in min(
            if (mEndElementId == -1L) mStartElementId else mEndElementId,
            mStartElementId
        ) - 1 downTo endId) mListener!!.onElementHighlighted(recyclerView, i, true)
        if (mEndElementId != -1L) {
            for (i in max(
                endId,
                mStartElementId
            ) + 1..mEndElementId) mListener!!.onElementHighlighted(recyclerView, i, false)
            for (i in min(
                endId,
                mStartElementId
            ) - 1 downTo mEndElementId) mListener!!.onElementHighlighted(recyclerView, i, false)
        }
        mEndElementId = endId
    }

    override fun onInterceptTouchEvent(
        recyclerView: RecyclerView,
        motionEvent: MotionEvent
    ): Boolean {
        if (mSelectMode) {
            if (motionEvent.action == MotionEvent.ACTION_UP ||
                motionEvent.action == MotionEvent.ACTION_CANCEL
            ) {
                if (mListener != null && mStartElementId != -1L) {
                    var start = mStartElementId
                    var end = if (mEndElementId == -1L) mStartElementId else mEndElementId
                    if (start > end) {
                        start = mEndElementId
                        end = mStartElementId
                    }
                    for (i in start..end) mListener!!.onElementSelected(recyclerView, i)
                }
                mStartElementId = -1
                mEndElementId = -1
                mSelectMode = false
                mScroller!!.setScrollDir(0)
                return true
            }
            if (mListener == null) return false
            var x = motionEvent.x.toInt()
            var y = motionEvent.y.toInt()
            if (y < 0) {
                mScroller!!.setScrollDir(-1)
            } else if (y > mRecyclerView!!.height) {
                mScroller!!.setScrollDir(1)
            } else {
                mScroller!!.setScrollDir(0)
            }
            x = max(min(x, mRecyclerView!!.width), 0)
            y = max(min(y, mRecyclerView.height), 0)
            var id = Long.MIN_VALUE
            val childCount = recyclerView.childCount
            for (i in 0 until childCount) {
                val view = recyclerView.getChildAt(i)
                view.getHitRect(mTempRect)
                if (mTempRect.contains(x, y)) id = recyclerView.getChildItemId(view)
            }
            if (id != Long.MIN_VALUE) updateHighlightedElements(recyclerView, id)
            return true
        }
        return false
    }

    override fun onTouchEvent(recyclerView: RecyclerView, motionEvent: MotionEvent) {
        onInterceptTouchEvent(recyclerView, motionEvent)
    }

    override fun onRequestDisallowInterceptTouchEvent(b: Boolean) {}

    interface Listener {
        fun onElementSelected(recyclerView: RecyclerView?, adapterPos: Long)
        fun onElementHighlighted(recyclerView: RecyclerView?, adapterId: Long, highlight: Boolean)
    }

}