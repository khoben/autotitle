package com.khoben.autotitle.ui.recyclerview.overlays

import android.annotation.TargetApi
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.res.Resources
import android.graphics.Rect
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.Spannable
import android.view.*
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.khoben.autotitle.R
import timber.log.Timber
import java.util.*
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

class SelectionTouchListener(private val mRecyclerView: RecyclerView) :
    RecyclerView.OnItemTouchListener, View.OnAttachStateChangeListener,
    SwipeToDeleteCallback.SwipeListener {
    private var mCurTextUUID: UUID? = null
    private var mCurTextView: TextView? = null
    private val mActionModeCallback: BaseActionModeCallback
    private var mActionModeCallback2: ActionModeCallback2? = null
    private var mScroller: RecyclerViewScrollerRunnable
    private var mSelectionStartOffset = -1
    private var mSelectionEndOffset = -1
    private var mSelectionLongPressMode = false
    private var mSelectionLongPressStart = -1
    private var mSelectionLongPressEnd = -1
    private var mLastTouchInText = false
    private var mLastTouchTextOffset = 0
    private var mTouchDownX = 0f
    private var mTouchDownY = 0f
    private var mLeftHandle: TextSelectionHandlePopup? = null
    private var mRightHandle: TextSelectionHandlePopup? = null
    private var mMultiSelectListener: LongPressSelectTouchListener? = null
    private val mTmpLocation = IntArray(2)
    private val mTmpLocation2 = IntArray(2)

    fun setMultiSelectListener(selectListener: LongPressSelectTouchListener?) {
        mMultiSelectListener = selectListener
    }

    override fun swipeStarted(view: View) {
        Timber.d("swipeStarted")
        if (mCurTextView != null && mCurTextView == findTextViewIn(view)) {
            hideActionModeForSelection()
            hideHandles()
        }
    }

    override fun swipeEnded(view: View) {
        Timber.d("swipeEnded")
        if (mCurTextView != null) {
            hideActionModeForSelection()
            showActionMode()
            Handler(Looper.getMainLooper()).postDelayed(
                {
                    showHandles()
                }, 200
            )
        }
    }

    override fun swipeDeleted(view: View) {
        Timber.d("swipeDeleted")
        if (mCurTextView != null) {
            clearSelection()
        }
    }

    override fun onViewAttachedToWindow(v: View) {}
    override fun onViewDetachedFromWindow(v: View) {
        Timber.d("onViewDetachedFromWindow")
        hideHandles()
        hideActionModeForSelection()
    }

    private fun createHandles() {
        mLeftHandle = TextSelectionHandlePopup(mRecyclerView.context, false)
        mRightHandle = TextSelectionHandlePopup(mRecyclerView.context, true)
        mLeftHandle?.setOnMoveListener(HandleMoveListener(false))
        mRightHandle?.setOnMoveListener(HandleMoveListener(true))
    }

    private fun showHandle(handle: TextSelectionHandlePopup?, offset: Int) {
        if (mCurTextView != null) {
            val line = mCurTextView!!.layout.getLineForOffset(offset)
            val y = mCurTextView!!.layout.getLineBottom(line)
            val x = mCurTextView!!.layout.getPrimaryHorizontal(offset)
            handle?.show(mCurTextView!!, x.toInt(), y)
        } else {
            handle?.hide()
        }
    }

    private fun showHandles() {
        if (mLeftHandle == null) createHandles()
        if (mSelectionLongPressMode) return
        showHandle(mLeftHandle, mSelectionStartOffset)
        showHandle(mRightHandle, mSelectionEndOffset)
        if (!mLeftHandle!!.isVisible() && !mRightHandle!!.isVisible() && mCurTextView != null) clearSelection()
    }

    private fun hideHandles() {
        mLeftHandle?.hide()
        mRightHandle?.hide()
    }

    private fun showActionMode() {
        Timber.d("showActionMode")
        if (mActionModeCallback.mCurrentActionMode != null) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mRecyclerView.startActionMode(mActionModeCallback2, ActionMode.TYPE_FLOATING)
        } else {
            mRecyclerView.startActionMode(mActionModeCallback)
        }
    }

    private fun hideActionModeForSelection() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && mActionModeCallback.mCurrentActionMode != null)
            mActionModeCallback.mCurrentActionMode!!.finish()
    }

    private fun handleSelection(x: Float, y: Float, rawX: Float, rawY: Float): Boolean {
        val textView = mCurTextView ?: return mSelectionLongPressMode
        textView.getLocationOnScreen(mTmpLocation)
        val viewX = rawX - mTmpLocation[0]
        val viewY = rawY - mTmpLocation[1]
        val tViewY = min(
            max(viewY, 0f),
            (textView.height - textView.compoundPaddingBottom).toFloat()
        ) - textView.compoundPaddingTop
        val tViewX = min(
            max(viewX, 0f),
            (textView.width - textView.compoundPaddingRight).toFloat()
        ) - textView.compoundPaddingLeft
        val line = textView.layout.getLineForVertical(tViewY.toInt())
        mLastTouchTextOffset = textView.layout.getOffsetForHorizontal(line, tViewX)
        mLastTouchInText =
            viewX >= textView.compoundPaddingLeft && viewX <= textView.width - textView.compoundPaddingEnd && viewY >= textView.compoundPaddingTop && viewY <= textView.height - textView.compoundPaddingBottom && tViewX <= textView.layout.getLineWidth(
                line
            )
        if (mSelectionLongPressMode) {
            val sel: Long = TextSelectionHelper.getWordAt(
                textView.text, mLastTouchTextOffset,
                mLastTouchTextOffset + 1
            )
            val selStart: Int = TextSelectionHelper.unpackTextRangeStart(sel)
            val selEnd: Int = TextSelectionHelper.unpackTextRangeEnd(sel)
            if (selEnd >= mSelectionLongPressStart
            ) {
                setSelection(mSelectionLongPressStart, selEnd)
            } else {
                setSelection(selStart, mSelectionLongPressEnd)
            }
            return true
        }
        return false
    }

    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
        if (e.actionMasked == MotionEvent.ACTION_DOWN) {
            mTouchDownX = e.x
            mTouchDownY = e.y
            hideActionModeForSelection()
        }
        if (e.actionMasked == MotionEvent.ACTION_UP ||
            e.actionMasked == MotionEvent.ACTION_CANCEL
        ) {
            if (!mSelectionLongPressMode && e.eventTime - e.downTime < MAX_CLICK_DURATION &&
                sqrt(
                    (e.x - mTouchDownX).toDouble().pow(2.0) +
                            (e.y - mTouchDownY).toDouble().pow(2.0)
                )
                < MAX_CLICK_DISTANCE * Resources.getSystem().displayMetrics.density
            ) {
                clearSelection()
            }
            mRecyclerView.parent.requestDisallowInterceptTouchEvent(false)
            mSelectionLongPressMode = false
            if (mCurTextView != null) {
                showHandles()
                showActionMode()
            }
        }
        if (mSelectionLongPressMode) {
            when {
                e.actionMasked == MotionEvent.ACTION_UP -> mScroller.setScrollDir(0)
                e.y < 0 -> mScroller.setScrollDir(-1)
                e.y > mRecyclerView.height -> mScroller.setScrollDir(1)
                else -> mScroller.setScrollDir(0)
            }
        }
        return handleSelection(e.x, e.y, e.rawX, e.rawY)
    }

    override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
        onInterceptTouchEvent(rv, e)
    }

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}

    fun startLongPressSelect(textView: TextView, uuid: UUID) {
        clearSelection()
        mCurTextView = textView
        mCurTextUUID = uuid
        mSelectionLongPressMode = true
        val sel: Long = TextSelectionHelper.getWordAt(
            textView.text, mLastTouchTextOffset,
            mLastTouchTextOffset + 1
        )
        mSelectionLongPressStart = TextSelectionHelper.unpackTextRangeStart(sel)
        mSelectionLongPressEnd = TextSelectionHelper.unpackTextRangeEnd(sel)
        setSelection(mSelectionLongPressStart, mSelectionLongPressEnd)
        mRecyclerView.parent.requestDisallowInterceptTouchEvent(true)
    }

    val selectedText: CharSequence
        get() {
            return if (mCurTextView == null) ""
            else mCurTextView!!.text.subSequence(mSelectionStartOffset, mSelectionEndOffset)
        }

    fun clearSelection() {
        hideActionModeForSelection()
        mCurTextView?.let {
            TextSelectionHelper.removeSelection(it.text as Spannable)
        }
        mCurTextView = null
        mCurTextUUID = null
        mSelectionStartOffset = -1
        mSelectionEndOffset = -1
        showHandles()
    }

    fun setSelection(startOffset: Int, endOffset: Int) {
        Timber.d("setSelection $startOffset $endOffset")
        mSelectionStartOffset = startOffset
        mSelectionEndOffset = endOffset
        if (mCurTextView != null) {
            TextSelectionHelper.setSelection(
                mCurTextView!!.context,
                mCurTextView!!.text as Spannable, startOffset, endOffset
            )
        }
    }


    private fun findTextViewIn(view: View): TextView {
        return view.findViewById(R.id.item_content)
    }

    private inner class HandleMoveListener(private val mRightHandle: Boolean) :
        TextSelectionHandleView.MoveListener {
        private var mCurrentlyRightHandle = false
        private val mScroller: RecyclerViewScrollerRunnable
        override fun onMoveStarted() {
            mCurrentlyRightHandle = mRightHandle
        }

        override fun onMoveFinished() {
            showActionMode()
            mScroller.setScrollDir(0)
        }

        override fun onMoved(x: Float, y: Float) {
            hideActionModeForSelection()
            mRecyclerView.getLocationOnScreen(mTmpLocation)
            when {
                (y - mTmpLocation[1] < 0) -> mScroller.setScrollDir(-1)
                (y - mTmpLocation[1] > mRecyclerView.height) -> mScroller.setScrollDir(1)
                else -> mScroller.setScrollDir(0)
            }
            val view = mCurTextView?.parent ?: return
            val textView = mCurTextView ?: return
            (view as View).getLocationOnScreen(mTmpLocation)
            // textView left position
            val x = x - textView.left
            val offset = textView.getOffsetForPosition(
                x - mTmpLocation[0],
                y - mTmpLocation[1]
            )

            if (mCurrentlyRightHandle) {
                if (offset < mSelectionStartOffset) {
                    setSelection(offset, mSelectionStartOffset)
                    mCurrentlyRightHandle = false
                } else {
                    setSelection(mSelectionStartOffset, offset)
                }
            } else {
                if (offset > mSelectionEndOffset) {
                    setSelection(mSelectionEndOffset, offset)
                    mCurrentlyRightHandle = true
                } else {
                    setSelection(offset, mSelectionEndOffset)
                }
            }
            showHandles()
        }

        init {
            mScroller = RecyclerViewScrollerRunnable(
                mRecyclerView,
                object : RecyclerViewScrollerRunnable.OnScrolledListener {
                    override fun onScrolled(scrollDir: Int) {
                        mRecyclerView.getLocationOnScreen(mTmpLocation)
                        if (scrollDir < 0)
                            onMoved(mTmpLocation[0].toFloat(), (mTmpLocation[1] - 1).toFloat())
                        else
                            if (scrollDir > 0)
                                onMoved(
                                    (mTmpLocation[0] + mRecyclerView.width).toFloat(),
                                    (mTmpLocation[1] + mRecyclerView.height + 1).toFloat()
                                )
                    }
                })
        }
    }

    inner class BaseActionModeCallback : ActionMode.Callback {
        var mCurrentActionMode: ActionMode? = null
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            Timber.d("onCreateActionMode")
            mCurrentActionMode = mode
            mode.menuInflater.inflate(R.menu.menu_context_selection, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            if (mCurTextView != null) {
                val textLength = mCurTextView!!.text.length
                menu.findItem(R.id.action_select_all).isVisible =
                    !(mSelectionStartOffset == 0 && mSelectionEndOffset == textLength)
            }
            return true
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            return when (item.itemId) {
                R.id.action_copy -> {
                    val clipboard = mRecyclerView.context
                        .getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(
                        ClipData.newPlainText("AutoTitle text", selectedText)
                    )
                    clearSelection()
                    hideActionModeForSelection()
                    true
                }
                R.id.action_select_all -> {
                    doSelectAll()
                    true
                }
                R.id.action_move_up -> {
                    doMoveUp()
                    clearSelection()
                    hideActionModeForSelection()
                    true
                }
                R.id.action_move_down -> {
                    doMoveDown()
                    clearSelection()
                    hideActionModeForSelection()
                    true
                }
                else -> false
            }
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            Timber.d("onDestroyActionMode")
            mCurrentActionMode = null
        }
    }

    var selectionController: SelectionControllerListener? = null

    interface SelectionControllerListener {
        fun onMoveDown(uuid: UUID?, start: Int, end: Int, text: CharSequence)
        fun onMoveUp(uuid: UUID?, start: Int, end: Int, text: CharSequence)
    }

    private fun doMoveDown() {
        selectionController?.onMoveDown(
            mCurTextUUID,
            mSelectionStartOffset,
            mSelectionEndOffset,
            selectedText
        )
    }

    private fun doMoveUp() {
        selectionController?.onMoveUp(
            mCurTextUUID,
            mSelectionStartOffset,
            mSelectionEndOffset,
            selectedText
        )
    }

    private fun doSelectAll() {
        mCurTextView?.let {
            setSelection(0, it.text.length)
            showHandles()
            hideActionModeForSelection()
            Handler(Looper.getMainLooper()).postDelayed({ showActionMode() }, 200)
        }
    }

    fun applySelection(itemContent: TextView, uuid: UUID) {
        if (mCurTextUUID == uuid) {
            Timber.d("applySelection")
            mCurTextView = itemContent
            setSelection(mSelectionStartOffset, mSelectionEndOffset)
        }
    }

    private val recyclerViewGlobalRect = Rect()
    fun checkIfShouldClearSelection(ev: MotionEvent?) {
        mRecyclerView.getGlobalVisibleRect(recyclerViewGlobalRect)
        if (!recyclerViewGlobalRect.contains(ev!!.rawX.toInt(), ev.rawY.toInt())) {
            clearSelection()
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    inner class ActionModeCallback2 internal constructor(private val mActionMode: BaseActionModeCallback) :
        ActionMode.Callback2() {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            return mActionMode.onCreateActionMode(mode, menu)
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            return mActionMode.onPrepareActionMode(mode, menu)
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            return mActionMode.onActionItemClicked(mode, item)
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            mActionMode.onDestroyActionMode(mode)
        }

        override fun onGetContentRect(mode: ActionMode, view: View, outRect: Rect) {
            val textView = mCurTextView
            val lineStart = textView?.layout?.getLineForOffset(mSelectionStartOffset) ?: -1
            val lineEnd = textView?.layout?.getLineForOffset(mSelectionEndOffset) ?: -1
            view.getLocationOnScreen(mTmpLocation)
            if (textView == null) return
            textView.getLocationOnScreen(mTmpLocation2)
            outRect.top = mTmpLocation2[1] - mTmpLocation[1] + textView.layout.getLineTop(lineStart)
            outRect.bottom =
                mTmpLocation2[1] - mTmpLocation[1] + textView.layout.getLineBottom(lineEnd)
            outRect.left =
                mTmpLocation2[0] - mTmpLocation[0] + textView.layout.getPrimaryHorizontal(
                    mSelectionStartOffset
                ).toInt()
            outRect.right =
                mTmpLocation2[0] - mTmpLocation[0] + textView.layout.getPrimaryHorizontal(
                    mSelectionEndOffset
                ).toInt()
        }
    }

    companion object {
        private const val MAX_CLICK_DURATION = 200
        private const val MAX_CLICK_DISTANCE = 30
    }

    init {
        mScroller = RecyclerViewScrollerRunnable(mRecyclerView, object :
            RecyclerViewScrollerRunnable.OnScrolledListener {
            override fun onScrolled(scrollDir: Int) {
                if (scrollDir < 0) {
                    handleSelection(0f, 0f, 0f, 0f)
                } else if (scrollDir > 0) {
                    mRecyclerView.getLocationOnScreen(mTmpLocation)
                    handleSelection(
                        mRecyclerView.width.toFloat(), mRecyclerView.height.toFloat(), (
                                mTmpLocation[0] + mRecyclerView.width).toFloat(), (
                                mTmpLocation[1] + mRecyclerView.height).toFloat()
                    )
                }
            }
        })
        mActionModeCallback = BaseActionModeCallback()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) mActionModeCallback2 =
            ActionModeCallback2(mActionModeCallback)
        mRecyclerView.viewTreeObserver.addOnScrollChangedListener { showHandles() }
        mRecyclerView.viewTreeObserver.addOnGlobalLayoutListener { showHandles() }
        mRecyclerView.addOnAttachStateChangeListener(this)
    }
}
