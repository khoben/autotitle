package com.khoben.autotitle.ui.recyclerview

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import timber.log.Timber


class EmptyRecyclerView : RecyclerView {

    private var emptyView: View? = null

    private val observer: AdapterDataObserver = object : AdapterDataObserver() {
        override fun onChanged() {
            Timber.d("onChanged")
            checkIfEmpty()
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            Timber.d("onItemRangeInserted. Inserted: $itemCount items from $positionStart position")
            checkIfEmpty()
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            checkIfEmpty()
        }
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(
            context: Context, attrs: AttributeSet?,
            defStyle: Int
    ) : super(context, attrs, defStyle)

    fun checkIfEmpty() {
        if (adapter != null) {
            val emptyViewVisible = adapter!!.itemCount == 0
            emptyView?.isVisible = emptyViewVisible
            isVisible = !emptyViewVisible
        }
    }

    override fun setAdapter(adapter: Adapter<*>?) {
        getAdapter()?.unregisterAdapterDataObserver(observer)
        super.setAdapter(adapter)
        adapter?.registerAdapterDataObserver(observer)
        checkIfEmpty()
    }

    fun setEmptyView(emptyView: View) {
        this.emptyView = emptyView
        checkIfEmpty()
    }
}