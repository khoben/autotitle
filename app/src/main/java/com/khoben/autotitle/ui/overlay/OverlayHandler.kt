package com.khoben.autotitle.ui.overlay

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import com.khoben.autotitle.App.Companion.FRAME_TIME_MS
import com.khoben.autotitle.ui.overlay.gesture.MultiTouchListener
import com.khoben.autotitle.ui.popup.TextEditorDialogFragment
import java.util.*
import kotlin.math.min

class OverlayHandler private constructor(
    private var context: Context,
    private var parentView: RelativeLayout
) {

    private var TAG: String = OverlayHandler::class.java.simpleName
    private val overlayFactory = OverlayFactory(context)
    private val overlayViews = ArrayList<OverlayText>()
    private var currentOverlayView: OverlayText? = null

    var overlayObjectEventListener: OverlayObjectEventListener? = null

    companion object {
        val regex = """[,.?!-=+:]""".toRegex()
    }

    interface OverlayObjectEventListener {
        fun onEdited(overlay: List<OverlayText>)
        fun onUnEditable(overlay: OverlayText?, overlays: List<OverlayText>)
        fun onAdded(overlay: OverlayText?, overlays: List<OverlayText>, isEdit: Boolean = true)
        fun onSelect(
            overlay: OverlayText?,
            overlays: List<OverlayText>,
            seekToOverlayStart: Boolean
        )

        fun onRemoved(
            idxRemoved: Int,
            removedOverlay: OverlayText,
            overlays: ArrayList<OverlayText>
        )
    }

    fun setLayout(
        context: Context,
        parentView: RelativeLayout
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
    private fun createOverlay(startTime: Long, endTime: Long, text: String = ""): OverlayText {
        val newOverlay = overlayFactory.get(OverlayType.TEXT) as OverlayText
        newOverlay.apply {
            this.startTime = startTime
            this.endTime = endTime
            this.textView!!.text = text
            // on delete
            this.closeButton!!.setOnClickListener {
                removeOverlay(this)
            }
            // gestures
            this.setOnTouchListener(getMultiTouchListener(this))
            // generate unique id
            this.uuid = UUID.randomUUID()
        }
        return newOverlay
    }

    private fun setSelectedOverlay(overlay: OverlayText) {
        currentOverlayView?.isInEdit = false
        currentOverlayView = overlay
        currentOverlayView!!.isInEdit = true
    }

    fun addOverlay(startTime: Long, endTime: Long, text: String) {
        val filteredText = filterPunctuation(text)
        if (filteredText.isEmpty()) {
            Log.e(TAG, "Skip adding empty overlay")
            return
        }

        val newOverlay = createOverlay(startTime, endTime, filteredText)

        setSelectedOverlay(newOverlay)

        addOverlayToParent(newOverlay)
        overlayViews.add(newOverlay)
        overlayViews.sort()

        overlayObjectEventListener?.onAdded(newOverlay, overlayViews, true)
    }

    fun addOverlay(startTime: Long, videoDuration: Long, type: OverlayType = OverlayType.TEXT) {
        val newOverlay = createOverlay(startTime, min(startTime + FRAME_TIME_MS, videoDuration))

        setSelectedOverlay(newOverlay)

        addOverlayToParent(newOverlay)
        overlayViews.add(newOverlay)
        overlayViews.sort()

        overlayObjectEventListener?.onAdded(newOverlay, overlayViews)
    }

    fun addOverlayAfterSpecificPosition(pos: Int): Long {
        val newOverlay =
            createOverlay(overlayViews[pos].endTime, overlayViews[pos].endTime + FRAME_TIME_MS)

        addOverlayToParent(newOverlay)
        overlayViews.add(pos + 1, newOverlay)
        overlayViews.sort()

        setSelectedOverlay(newOverlay)

        overlayObjectEventListener?.onAdded(currentOverlayView!!, overlayViews)
        return newOverlay.startTime
    }

    fun addOverlayAtSpecificPosition(pos: Int, item: OverlayText) {
        addOverlayToParent(item)
        overlayViews.add(pos, item)
        overlayViews.sort()
        overlayObjectEventListener?.onAdded(currentOverlayView, overlayViews)
    }

    fun removeOverlay(item: Int) {
        removeOverlay(overlayViews[item])
    }

    private fun removeOverlay(overlayObject: OverlayText) {
        val idxRemoved = overlayViews.indexOf(overlayObject)
        overlayViews.remove(overlayObject)
        parentView.removeView(overlayObject)
        // deselect
        if (overlayObject == currentOverlayView) {
            currentOverlayView?.isInEdit = false
            currentOverlayView = null
        }
        overlayObjectEventListener?.onRemoved(idxRemoved, overlayObject, overlayViews)
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
        parentView.addView(
            child,
            RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE)
            }
        )
    }

    private fun getMultiTouchListener(overlay: OverlayText): MultiTouchListener {
        return MultiTouchListener(
            parentView,
            true,
        ).apply {
            setOnGestureControl(object : MultiTouchListener.OnGestureControl {
                override fun onClick() {
                    selectedOverlay(overlay, false)
                }

                override fun onLongClick() {
                }

                override fun onDoubleTap() {
                    editOverlay(overlay)
                }

                override fun onMove() {
                    selectedOverlay(overlay, false)
                }
            })
        }
    }

    fun editOverlay(index: Int) {
        editOverlay(overlayViews[index])
    }

    private fun editOverlay(overlay: OverlayText) {
        TextEditorDialogFragment.show(
            context as AppCompatActivity,
            overlay.text,
            overlay.textView!!.currentTextColor
        ).setOnTextEditorListener(object : TextEditorDialogFragment.TextEditorEvent {
            override fun onDone(inputText: String?, colorCode: Int) {
                overlay.text = inputText
                overlay.textView!!.setTextColor(colorCode)
                overlayObjectEventListener?.onEdited(overlayViews)
            }
        })
    }

    fun selectedOverlayId(pos: Int) {
        selectedOverlay(overlayViews[pos], true)
    }

    fun selectedOverlay(overlay: OverlayText, seekToOverlayStart: Boolean = false) {
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
        parentView.visibility = View.GONE
    }

    fun showRootView() {
        parentView.visibility = View.VISIBLE
    }

    data class Builder(
        private var context: Context? = null,
        private var parentView: RelativeLayout? = null
    ) {
        fun ctx(context: Context) = apply { this.context = context }
        fun parent(view: RelativeLayout) = apply { this.parentView = view }
        fun build() = OverlayHandler(context!!, parentView!!)
    }
}