package com.khoben.autotitle.ui.recyclerview.overlays

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.khoben.autotitle.R


class SimpleDividerItemDecoration(context: Context) : ItemDecoration() {
    private val mDivider: Drawable =
        ContextCompat.getDrawable(context, R.drawable.recycler_horizontal_divider)!!

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val left = parent.paddingLeft
        val right = parent.width - parent.paddingRight
        val childCount = parent.childCount
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            val params = child.layoutParams as RecyclerView.LayoutParams
            val top: Int = child.bottom + params.bottomMargin
            val bottom: Int = top + mDivider.intrinsicHeight
            mDivider.setBounds(left, top, right, bottom)
            mDivider.draw(c)
            if (i == 0) {
                mDivider.setBounds(
                    left,
                    top - child.bottom,
                    right,
                    top - child.bottom + mDivider.intrinsicHeight
                )
                mDivider.draw(c)
            }
        }
    }

}