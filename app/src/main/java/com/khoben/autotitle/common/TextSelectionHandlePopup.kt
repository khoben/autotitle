package com.khoben.autotitle.common

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import com.khoben.autotitle.ui.recyclerview.overlays.TextSelectionHandleView

class TextSelectionHandlePopup(ctx: Context?, rightHandle: Boolean) {
    private var mView: TextSelectionHandleView? = null
    private var mWindow: PopupWindow? = null
    private val mTempLocation = IntArray(2)

    init {
        mView = TextSelectionHandleView(ctx!!, rightHandle)
        mWindow = PopupWindow(mView?.context, null, android.R.attr.textSelectHandleWindowStyle)
        mWindow!!.isSplitTouchEnabled = true
        mWindow!!.isClippingEnabled = false
        mWindow!!.contentView = mView
        mWindow!!.width = ViewGroup.LayoutParams.WRAP_CONTENT
        mWindow!!.height = ViewGroup.LayoutParams.WRAP_CONTENT
        mView?.measure(0, 0)
    }

    @SuppressLint("RtlHardcoded")
    fun show(parent: View, x: Int, y: Int) {
        var x = x
        var y = y
        parent.getLocationOnScreen(mTempLocation)
        x = x - mView!!.hotspotX + mTempLocation[0] + parent.paddingLeft
        y += mTempLocation[1] + parent.paddingTop
        if (mWindow!!.isShowing)
            mWindow!!.update(
                x,
                y,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        else
            mWindow!!.showAtLocation(parent, Gravity.START or Gravity.TOP, x, y)
    }

    fun hide() {
        mWindow?.dismiss()
    }

    fun isVisible(): Boolean {
        return mWindow!!.isShowing
    }

    fun setOnMoveListener(listener: TextSelectionHandleView.MoveListener?) {
        mView?.setOnMoveListener(listener)
    }

}