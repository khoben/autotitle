package com.minibugdev.sheetselection

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class EmptyRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    private var emptyView: View? = null

    fun setEmptyView(emptyView: View) {
        this.emptyView = emptyView
    }

    fun toggleSearchState(state: Boolean) {
        if (state) {
            this.visibility = View.VISIBLE
            emptyView?.visibility = View.GONE
        } else {
            this.visibility = View.GONE
            emptyView?.visibility = View.VISIBLE
        }
    }
}