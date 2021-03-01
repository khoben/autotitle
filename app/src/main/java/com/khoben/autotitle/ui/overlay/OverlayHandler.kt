package com.khoben.autotitle.ui.overlay

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.core.animation.addListener
import com.khoben.autotitle.App.Companion.FRAME_TIME_MS
import com.khoben.autotitle.extension.getRect
import com.khoben.autotitle.model.MLCaption
import com.khoben.autotitle.model.OverlayTextSaveLightModel
import com.khoben.autotitle.ui.overlay.gesture.ControlType
import com.khoben.autotitle.ui.overlay.gesture.MultiTouchListener
import timber.log.Timber
import java.lang.ref.WeakReference
import java.util.*
import kotlin.math.min

class OverlayHandler private constructor(
    context: Context,
    private var parentView: WeakReference<ViewGroup>
) {
    private val overlayFactory = OverlayFactory(context)
    private val overlayViews = ArrayList<OverlayObject>()
    private var currentOverlayView: OverlayObject? = null
    private var lastDeletedOverlay: OverlayObject? = null

    var overlayObjectEventListener: OverlayObjectEventListener? = null

    interface OverlayObjectEventListener {
        /**
         * Editing for [overlay] has been started
         *
         * @param overlay OverlayObject
         */
        fun onEdit(overlay: OverlayObject)

        /**
         * Editing has been done
         *
         * @param overlay List<OverlayObject>
         */
        fun onEdited(overlay: List<OverlayObject>)

        /**
         * [overlay] has been deselected
         *
         * @param overlay Deselected overlay
         * @param overlays List of overlays
         */
        fun onClearOverlaySelection(overlay: OverlayObject?, overlays: List<OverlayObject>)

        /**
         * New [overlay] has been added
         *
         * @param overlay Newly added overlay
         * @param overlays List of overlays
         */
        fun onAdded(overlay: OverlayObject?, overlays: List<OverlayObject>)

        /**
         * List of overlays has been added
         *
         * @param overlays List<OverlayObject>
         */
        fun onAddedAll(overlays: List<OverlayObject>)

        /**
         * [overlay] has been selected
         *
         * @param overlay Selected overlay
         * @param overlays List of overlays
         * @param seekToOverlayStart Indicates if player should seek to [OverlayObject.startTime]
         */
        fun onSelect(
            overlay: OverlayObject?,
            overlays: List<OverlayObject>,
            seekToOverlayStart: Boolean
        )

        /**
         * Overlay with index equals to [idxRemoved]
         * has been removed
         *
         * @param idxRemoved Index of removed overlay
         * @param removedOverlay Removed overlay
         * @param overlays List of overlays
         */
        fun onRemoved(
            idxRemoved: Int,
            removedOverlay: OverlayObject,
            overlays: List<OverlayObject>
        )
    }

    /**
     * Sets parent layout for overlays
     *
     * @param parentView Parent layout
     */
    fun setLayout(parentView: WeakReference<ViewGroup>) {
        this.parentView = parentView
        // remove previous parent from overlays
        this.overlayViews.forEach {
            (it.parent as ViewGroup).removeView(it)
            addOverlayToParent(it)
        }
    }

    /**
     * Get all overlays
     *
     * @return ArrayList<OverlayObject>
     */
    fun getOverlays() = overlayViews

    /**
     * Get selected overlay
     *
     * @return OverlayObject?
     */
    fun getSelectedOverlay() = currentOverlayView

    /**
     * Get all overlays and selected overlay
     *
     * @return Pair<OverlayObject?, ArrayList<OverlayObject>>
     */
    fun getOverlaysWithSelected() = Pair(currentOverlayView, overlayViews)

    private val punctuationRegex = """[,.?!-=+:]""".toRegex()

    /**
     * Filter text from punctuation
     *
     * @param text Text
     * @return Result string
     */
    private fun filterPunctuation(text: String): String {
        return punctuationRegex.replace(text, "").trim()
    }

    /**
     * Creates new text overlay ([OverlayText] object)
     *
     * @param startTime Start timestamp, in ms
     * @param endTime End timestamp, in ms
     * @param text Text
     * @return OverlayText
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun createTextOverlay(startTime: Long, endTime: Long, text: String = ""): OverlayText {
        val newOverlay = overlayFactory.get(OverlayType.TEXT) as OverlayText
        newOverlay.apply {
            this.startTime = startTime
            this.endTime = endTime
            this.textView!!.text = text
            // gestures
            this.initMultiTouchListener(
                parentView.get()!!.getRect(),
                object : MultiTouchListener.OnGestureControl {
                    override fun onClick() {
                        selectOverlay(newOverlay, false)
                    }

                    override fun onLongClick() {
                    }

                    override fun onDoubleTap() {
                        editOverlay(newOverlay)
                    }

                    override fun onMove() {
                        selectOverlay(newOverlay, false)
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

    @SuppressLint("ClickableViewAccessibility")
    private fun createTextOverlayFromDiskModel(overlayDiskModel: OverlayTextSaveLightModel): OverlayText {
        val newOverlay = overlayFactory.get(OverlayType.TEXT) as OverlayText
        newOverlay.apply {
            this.startTime = overlayDiskModel.startTime
            this.endTime = overlayDiskModel.endTime
            this.textView!!.text = overlayDiskModel.text
            this.scaleX = overlayDiskModel.scale
            this.scaleY = overlayDiskModel.scale
            this.pivotX = overlayDiskModel.pivotX
            this.pivotY = overlayDiskModel.pivotY
            this.translationX = overlayDiskModel.translationX
            this.translationY = overlayDiskModel.translationY
            this.rotation = overlayDiskModel.rotation
            this.textView!!.setTextColor(overlayDiskModel.textColor)
            // gestures
            this.initMultiTouchListener(
                parentView.get()!!.getRect(),
                object : MultiTouchListener.OnGestureControl {
                    override fun onClick() {
                        selectOverlay(newOverlay, false)
                    }

                    override fun onLongClick() {
                    }

                    override fun onDoubleTap() {
                        editOverlay(newOverlay)
                    }

                    override fun onMove() {
                        selectOverlay(newOverlay, false)
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

    /**
     * Adds new [OverlayText]
     *
     * @param startTime Start timestamp, in ms
     * @param endTime End timestamp, in ms
     * @param text Text
     * @param batch Indicates if it adds many objects
     */
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
            overlayObjectEventListener?.onAdded(newOverlay, overlayViews)
        }
    }

    private fun addTextOverlaysFromDisk(
        overlayDiskModel: OverlayTextSaveLightModel,
        batch: Boolean = true
    ) {
        val newOverlay = createTextOverlayFromDiskModel(overlayDiskModel)
        if (!batch) setSelectedOverlay(newOverlay)
        addOverlayToParent(newOverlay)
        overlayViews.add(newOverlay)
        if (!batch) {
            overlayViews.sort()
            overlayObjectEventListener?.onAdded(newOverlay, overlayViews)
        }
    }


    /**
     * Adds new overlay with provided [type]
     *
     * @param startTime Start timestamp, in ms
     * @param videoDuration Video duration, in ms
     * @param type OverlayType
     */
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

    /**
     * Adds [OverlayText] after the specified index
     *
     * @param idx Overlay index
     * @return [OverlayObject.startTime]
     */
    fun addTextOverlayAfterSpecificPosition(idx: Int): Long {
        val newOverlay =
            createTextOverlay(
                overlayViews[idx].endTime,
                overlayViews[idx].endTime + FRAME_TIME_MS
            )

        addOverlayToParent(newOverlay)
        overlayViews.add(idx + 1, newOverlay)
        overlayViews.sort()

        setSelectedOverlay(newOverlay)

        overlayObjectEventListener?.onAdded(currentOverlayView!!, overlayViews)
        return newOverlay.startTime
    }

    /**
     * Adds [OverlayText] at the specified index
     *
     * @param idx Overlay index
     * @param item OverlayObject
     */
    fun addOverlayAtSpecificPosition(idx: Int, item: OverlayObject) {
        addOverlayToParent(item)
        overlayViews.add(idx, item)
        overlayViews.sort()
        if (lastDeletedOverlay == item) setSelectedOverlay(item)
        overlayObjectEventListener?.onAdded(currentOverlayView, overlayViews)
    }

    /**
     * Removes overlay with index equals to [idx]
     *
     * @param idx Overlay index
     */
    fun removeOverlay(idx: Int) {
        removeOverlay(overlayViews[idx])
    }

    /**
     * Clears selection of [currentOverlayView]
     */
    fun clearOverlaySelection() {
        currentOverlayView?.isInEdit = false
        currentOverlayView = null
        overlayObjectEventListener?.onClearOverlaySelection(currentOverlayView, overlayViews)
    }

    private fun setSelectedOverlay(overlay: OverlayObject) {
        currentOverlayView?.isInEdit = false
        currentOverlayView = overlay
        currentOverlayView!!.isInEdit = true
    }

    private fun removeOverlay(overlay: OverlayObject) {
        val idxRemoved = overlayViews.indexOf(overlay)
        overlayViews.remove(overlay)
        overlayObjectEventListener?.onRemoved(idxRemoved, overlay, overlayViews)

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
                }
            )
        }.start()
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

    /**
     * Edits overlay with index equals to [idx]
     *
     * @param idx Overlay index
     */
    fun editOverlay(idx: Int) {
        editOverlay(overlayViews[idx])
    }

    /**
     * Saves edited properties for [overlay]
     *
     * @param overlay Edited overlay
     * @param text Text
     * @param color Color code
     */
    fun editedOverlay(overlay: OverlayObject, text: String, @ColorInt color: Int) {
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

    /**
     * Makes overlay with index equals to [idx] selected
     *
     * @param idx Index of overlay
     */
    fun selectOverlayById(idx: Int) {
        selectOverlay(overlayViews[idx], true)
    }


    /**
     * Makes [overlay] selected
     *
     * @param overlay Selected overlay
     * @param seekToOverlayStart Indicates if should seek to [OverlayObject.startTime]
     */
    fun selectOverlay(overlay: OverlayObject, seekToOverlayStart: Boolean = false) {
        // do nothing if selected previously selected overlay
        if (overlay == currentOverlayView && currentOverlayView!!.isInEdit) return
        setSelectedOverlay(overlay)
        currentOverlayView!!.bringToFront()
        overlayObjectEventListener?.onSelect(overlay, overlayViews, seekToOverlayStart)
    }

    /**
     * Changes [OverlayObject.startTime] and [OverlayObject.endTime] for current selected overlay
     *
     * @param startTime Start timestamp, in ms
     * @param endTime End timestamp, in ms
     */
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

    /**
     * Changes overlay's visibility by [currentTime]
     *
     * @param currentTime Timestamp, in ms
     */
    fun changeVisibilityOverlayByTime(currentTime: Long) {
        overlayViews.forEach { overlay ->
            if (currentTime in overlay.startTime..overlay.endTime) {
                overlay.visibility = View.VISIBLE
            } else {
                overlay.visibility = View.GONE
            }
        }
    }

    /**
     * Hides all overlays, but not for overlays which have [OverlayObject.startTime]
     * equals to 0ms
     */
    fun hideAllOverlaysExceptAtStart() {
        overlayViews.forEach {
            if (it.startTime == 0L) {
                it.visibility = View.VISIBLE
            } else {
                it.visibility = View.INVISIBLE
            }
        }
        clearOverlaySelection()
    }

    /**
     * Sets [parentView]'s visibility to [View.GONE]
     */
    fun hideRootView() {
        parentView.get()?.visibility = View.GONE
    }

    /**
     * Makes [parentView] visible
     */
    fun showRootView() {
        parentView.get()?.visibility = View.VISIBLE
    }

    /**
     * Adds list of [overlays]
     *
     * @param overlays List<MLCaption>
     */
    fun addAllOverlay(overlays: List<MLCaption>) {
        overlayViews.clear()
        overlays.forEach { addTextOverlay(it.startTime, it.endTime, it.text, true) }
        overlayViews.sort()
        overlayObjectEventListener?.onAddedAll(overlayViews)
    }

    fun addAllOverlayObjects(overlays: List<OverlayObject>) {
        overlayViews.clear()
        overlayViews.addAll(overlays)
        overlayObjectEventListener?.onAddedAll(overlayViews)
    }

    fun addAllFromDisk(decoded: List<OverlayTextSaveLightModel>) {
        Timber.d("addAllFromDisk $decoded")
        parentView.get()?.post {
            overlayViews.clear()
            decoded.forEach {
                addTextOverlaysFromDisk(it, true)
            }
            overlayViews.sort()
            overlayObjectEventListener?.onAddedAll(overlayViews)
            hideAllOverlaysExceptAtStart()
        }
    }

    /**
     * Builder class for [OverlayHandler]
     *
     * @property context Application context
     * @property parentView Parent layout for overlays
     * @constructor
     */
    data class Builder(
        private var context: Context? = null,
        private var parentView: ViewGroup? = null
    ) {
        fun ctx(context: Context) = apply { this.context = context }
        fun parent(view: ViewGroup) = apply { this.parentView = view }
        fun build() = OverlayHandler(context!!, WeakReference(parentView!!))
    }
}