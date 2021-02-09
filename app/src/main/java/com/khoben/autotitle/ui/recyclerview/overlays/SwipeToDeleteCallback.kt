package com.khoben.autotitle.ui.recyclerview.overlays

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.util.TypedValue
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.khoben.autotitle.R


abstract class SwipeToDeleteCallback internal constructor(context: Context) :
    ItemTouchHelper.Callback() {

    var swipeListener: SwipeListener? = null

    interface SwipeListener {
        fun swipeStarted(view: View)
        fun swipeEnded(view: View)
        fun swipeDeleted(view: View)
    }

    private val mBackground = ColorDrawable().apply {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(R.attr.colorError, typedValue, true)
        color = ContextCompat.getColor(context, typedValue.resourceId)
    }
    private val deleteDrawable = ContextCompat.getDrawable(context, R.drawable.delete_icon_24dp)!!

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ) = makeMovementFlags(0, ItemTouchHelper.LEFT)

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        viewHolder1: RecyclerView.ViewHolder
    ) = false

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        val itemView = viewHolder.itemView
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            if (isCurrentlyActive) swipeListener?.swipeStarted(viewHolder.itemView)
            else if (dX >= 0) swipeListener?.swipeEnded(viewHolder.itemView)
        }
        val iconMarginVertical = (viewHolder.itemView.height - deleteDrawable.intrinsicHeight) / 2

        when {
            dX <= -itemView.right -> {
                // disable background flickering on item create
                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
                return
            }
            dX > 0 -> {
                mBackground.setBounds(itemView.left, itemView.top, dX.toInt(), itemView.bottom)
                deleteDrawable.setBounds(
                    itemView.left + iconMarginVertical,
                    itemView.top + iconMarginVertical,
                    itemView.left + iconMarginVertical + deleteDrawable.intrinsicWidth,
                    itemView.bottom - iconMarginVertical
                )
            }
            else -> {
                mBackground.setBounds(
                    itemView.right + dX.toInt(),
                    itemView.top,
                    itemView.right,
                    itemView.bottom
                )
                deleteDrawable.setBounds(
                    itemView.right - iconMarginVertical - deleteDrawable.intrinsicWidth,
                    itemView.top + iconMarginVertical,
                    itemView.right - iconMarginVertical,
                    itemView.bottom - iconMarginVertical
                )
                deleteDrawable.level = 0
            }
        }

        mBackground.draw(c)

        c.save()

        if (dX > 0)
            c.clipRect(itemView.left, itemView.top, dX.toInt(), itemView.bottom)
        else
            c.clipRect(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)

        deleteDrawable.draw(c)

        c.restore()

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    abstract fun onSwipedDone(viewHolder: RecyclerView.ViewHolder, direction: Int)

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        swipeListener?.swipeDeleted(viewHolder.itemView)
        onSwipedDone(viewHolder, direction)
    }

    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder) = 0.5F
}
