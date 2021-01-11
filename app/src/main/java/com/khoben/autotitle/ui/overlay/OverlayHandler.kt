package com.khoben.autotitle.ui.overlay

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.animation.addListener
import com.khoben.autotitle.App.Companion.FRAME_TIME_MS
import com.khoben.autotitle.extension.getRect
import com.khoben.autotitle.model.MLCaption
import com.khoben.autotitle.ui.overlay.gesture.ControlType
import com.khoben.autotitle.ui.overlay.gesture.MultiTouchListener
import timber.log.Timber
import java.lang.ref.WeakReference
import java.util.*
import kotlin.math.min

class OverlayHandler private constructor(
        private var context: Context,
        private var parentView: WeakReference<ViewGroup>
) {
    private val overlayFactory = OverlayFactory(context)
    private val overlayViews = ArrayList<OverlayObject>()
    private var currentOverlayView: OverlayObject? = null
    private var lastDeletedOverlay: OverlayObject? = null

    var overlayObjectEventListener: OverlayObjectEventListener? = null

    companion object {
        val regex = """[,.?!-=+:]""".toRegex()
    }

    interface OverlayObjectEventListener {
        fun onEdit(overlay: OverlayObject)
        fun onEdited(overlay: List<OverlayObject>)
        fun onUnEditable(overlay: OverlayObject?, overlays: List<OverlayObject>)
        fun onAdded(overlay: OverlayObject?, overlays: List<OverlayObject>, isEdit: Boolean = true)
        fun onAddedAll(overlays: List<OverlayObject>)
        fun onSelect(
                overlay: OverlayObject?,
                overlays: List<OverlayObject>,
                seekToOverlayStart: Boolean
        )

        fun onRemoved(
                idxRemoved: Int,
                removedOverlay: OverlayObject,
                overlays: ArrayList<OverlayObject>
        )
    }

    fun setLayout(
            context: Context,
            parentView: WeakReference<ViewGroup>
    ) {
        this.context = context
        this.parentView = parentView
        // remove previous parent from overlays
        this.overlayViews.forEach {
            (it.parent as ViewGroup).removeView(it)
            addOverlayToParent(it)
        }
    }

    /**
     * Filter text from punctuation
     * @param text Text
     * @return Result string
     */
    private fun filterPunctuation(text: String): String {
        return regex.replace(text, "").trim()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun createTextOverlay(startTime: Long, endTime: Long, text: String = ""): OverlayText {
        val newOverlay = overlayFactory.get(OverlayType.TEXT) as OverlayText
        newOverlay.apply {
            this.startTime = startTime
            this.endTime = endTime
            this.textView!!.text = text
            // gestures
            this.initMultiTouchListener(parentView.get()!!.getRect(), object : MultiTouchListener.OnGestureControl {
                override fun onClick() {
                    selectedOverlay(newOverlay, false)
                }

                override fun onLongClick() {
                }

                override fun onDoubleTap() {
                    editOverlay(newOverlay)
                }

                override fun onMove() {
                    selectedOverlay(newOverlay, false)
                }

                override fun onControlClicked(which: ControlType) {
                    if (which == ControlType.DELETE_BTN) {
                        removeOverlay(newOverlay)
                    }
                }

            })
        }
        return newOverlay
    }

    private fun setSelectedOverlay(overlay: OverlayObject) {
        currentOverlayView?.isInEdit = false
        currentOverlayView = overlay
        currentOverlayView!!.isInEdit = true
    }

    fun addTextOverlay(startTime: Long, endTime: Long, text: String, batch: Boolean = false) {
        val filteredText = filterPunctuation(text)
        if (filteredText.isEmpty()) {
            Timber.e("Skip adding empty overlay")
            return
        }

        val newOverlay = createTextOverlay(startTime, endTime, filteredText)

        if (!batch) setSelectedOverlay(newOverlay)

        addOverlayToParent(newOverlay)
        overlayViews.add(newOverlay)
        if (!batch) {
            overlayViews.sort()
            overlayObjectEventListener?.onAdded(newOverlay, overlayViews, true)
        }
    }

    fun addOverlay(startTime: Long, videoDuration: Long, type: OverlayType = OverlayType.TEXT) {
        val newOverlay: OverlayObject?
        if (type == OverlayType.TEXT) {
            newOverlay = createTextOverlay(startTime, min(startTime + FRAME_TIME_MS, videoDuration))
        } else {
            newOverlay = null
        }

        newOverlay?.let {
            setSelectedOverlay(it)
            addOverlayToParent(it)
            overlayViews.add(it)
            overlayViews.sort()
            overlayObjectEventListener?.onAdded(it, overlayViews)
        }
    }

    fun addTextOverlayAfterSpecificPosition(pos: Int): Long {
        val newOverlay =
                createTextOverlay(overlayViews[pos].endTime,
                        overlayViews[pos].endTime + FRAME_TIME_MS)

        addOverlayToParent(newOverlay)
        overlayViews.add(pos + 1, newOverlay)
        overlayViews.sort()

        setSelectedOverlay(newOverlay)

        overlayObjectEventListener?.onAdded(currentOverlayView!!, overlayViews)
        return newOverlay.startTime
    }

    fun addOverlayAtSpecificPosition(pos: Int, item: OverlayObject) {
        addOverlayToParent(item)
        overlayViews.add(pos, item)
        overlayViews.sort()
        if (lastDeletedOverlay == item) setSelectedOverlay(item)
        overlayObjectEventListener?.onAdded(currentOverlayView, overlayViews)
    }

    fun removeOverlay(item: Int) {
        removeOverlay(overlayViews[item])
    }

    private fun removeOverlay(overlay: OverlayObject) {
        val idxRemoved = overlayViews.indexOf(overlay)
        overlayViews.remove(overlay)

        // save origin values before animation
        val oldAlpha = overlay.alpha
        val oldScaleX = overlay.scaleX
        val oldScaleY = overlay.scaleY

        AnimatorSet().apply {
            playTogether(
                    ObjectAnimator.ofFloat(overlay, "alpha", oldAlpha, 0f),
                    ObjectAnimator.ofFloat(overlay, "scaleX", oldScaleX, 0f),
                    ObjectAnimator.ofFloat(overlay, "scaleY", oldScaleY, 0f)
            )
            addListener(
                    onEnd = {
                        parentView.get()!!.removeView(overlay)
                        // deselect
                        if (overlay == currentOverlayView) {
                            lastDeletedOverlay = currentOverlayView
                            currentOverlayView?.isInEdit = false
                            currentOverlayView = null
                        }
                        // restore properties after animation
                        overlay.alpha = oldAlpha
                        overlay.scaleX = oldScaleX
                        overlay.scaleY = oldScaleY

                        overlayObjectEventListener?.onRemoved(idxRemoved, overlay, overlayViews)
                    }
            )
        }.start()
    }

    fun getOverlays() = overlayViews
    fun getSelectedOverlay() = currentOverlayView
    fun getOverlaysWithSelected() = Pair(currentOverlayView, overlayViews)

    fun unEditable() {
        currentOverlayView?.isInEdit = false
        currentOverlayView = null
        overlayObjectEventListener?.onUnEditable(currentOverlayView, overlayViews)
    }

    private fun addOverlayToParent(child: View) {
        parentView.get()!!.addView(
                child,
                FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = Gravity.CENTER
                }
        )
    }

    fun editOverlay(index: Int) {
        editOverlay(overlayViews[index])
    }

    fun editedOverlay(overlay: OverlayObject, text: String, color: Int) {
        if (overlay is OverlayText) {
            overlay.text = text
            overlay.textView!!.setTextColor(color)
            overlayObjectEventListener?.onEdited(overlayViews)
        }
    }

    private fun editOverlay(overlay: OverlayObject) {
        if (overlay is OverlayText) {
            overlayObjectEventListener?.onEdit(overlay)
        }
    }

    fun selectedOverlayId(pos: Int) {
        selectedOverlay(overlayViews[pos], true)
    }

    fun selectedOverlay(overlay: OverlayObject, seekToOverlayStart: Boolean = false) {
        // do nothing if selected previously selected overlay
        if (overlay == currentOverlayView && currentOverlayView!!.isInEdit) return
        setSelectedOverlay(overlay)
        currentOverlayView!!.bringToFront()
        overlayObjectEventListener?.onSelect(overlay, overlayViews, seekToOverlayStart)
    }

    fun changeTimeRangeSelectedOverlay(startTime: Long, endTime: Long) {
        // change time range for current overlay
        if (overlayViews.isEmpty() || currentOverlayView == null) {
            return
        }
        currentOverlayView!!.startTime = startTime
        currentOverlayView!!.endTime = endTime
        overlayViews.sort()
        overlayObjectEventListener?.onEdited(overlayViews)
    }

    fun changeVisibilityOverlayByTime(currentTime: Long) {
        overlayViews.forEach { overlay ->
            if (currentTime in overlay.startTime..overlay.endTime) {
                overlay.visibility = View.VISIBLE
            } else {
                overlay.visibility = View.GONE
            }
        }
    }

    fun hideAllOverlaysExceptAtStart() {
        overlayViews.forEach {
            if (it.startTime == 0L) {
                it.visibility = View.VISIBLE
            } else {
                it.visibility = View.INVISIBLE
            }
        }
        unEditable()
    }

    fun hideRootView() {
        parentView.get()?.visibility = View.GONE
    }

    fun showRootView() {
        parentView.get()?.visibility = View.VISIBLE
    }

    fun addAllOverlay(overlays: List<MLCaption>) {
        overlayViews.clear()
        overlays.forEach { addTextOverlay(it.startTime, it.endTime, it.text, true) }
        overlayViews.sort()
        overlayObjectEventListener?.onAddedAll(overlayViews)
    }

    data class Builder(
            private var context: Context? = null,
            private var parentView: ViewGroup? = null
    ) {
        fun ctx(context: Context) = apply { this.context = context }
        fun parent(view: ViewGroup) = apply { this.parentView = view }
        fun build() = OverlayHandler(context!!, WeakReference(parentView!!))
    }
}