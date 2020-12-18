package com.khoben.autotitle.huawei.ui.overlay

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import com.khoben.autotitle.huawei.App.Companion.FRAME_TIME_MS
import com.khoben.autotitle.huawei.ui.overlay.gesture.MultiTouchListener
import com.khoben.autotitle.huawei.ui.player.seekbar.SeekBarListener
import com.khoben.autotitle.huawei.ui.popup.TextEditorDialogFragment
import java.util.*
import kotlin.math.min

class OverlayHandler private constructor(
    private var context: Context,
    private var parentView: RelativeLayout
) : SeekBarListener {

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
        fun onSelect(overlay: OverlayText?, overlays: List<OverlayText>)
        fun onRemoved(idxRemoved: Int, removedOverlay: OverlayText, overlays: ArrayList<OverlayText>)
    }

    fun setLayout(
        context: Context,
        parentView: RelativeLayout
    ) {
        this.context = context
        this.parentView = parentView
        this.overlayViews.forEach {
            (it.parent as ViewGroup).removeView(it)
            addOverlayToParent(it)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    fun addOverlay(startTime: Long, endTime: Long, text: String) {
        // filter text from punctuation
        val filtered = regex.replace(text, "").trim()
        if (filtered.isEmpty()) {
            Log.e(TAG, "Adding empty overlay")
            return
        }
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
            this.timestamp = System.currentTimeMillis()
        }
        currentOverlayView?.isInEdit = false
        currentOverlayView = newOverlay
        currentOverlayView!!.isInEdit = true

        addOverlayToParent(currentOverlayView!!)
        overlayViews.add(currentOverlayView!!)
        overlayViews.sort()

        overlayObjectEventListener?.onAdded(currentOverlayView, overlayViews, true)
    }

    @SuppressLint("ClickableViewAccessibility")
    fun addOverlay(startTime: Long, videoDuration: Long, type: OverlayType = OverlayType.TEXT) {
        val newOverlay = overlayFactory.get(type) as OverlayText
        newOverlay.apply {
            this.startTime = startTime
            this.endTime = min(startTime + FRAME_TIME_MS, videoDuration)
            // on delete
            this.closeButton!!.setOnClickListener {
                removeOverlay(this)
            }
        }
        newOverlay.setOnTouchListener(getMultiTouchListener(newOverlay))
        newOverlay.timestamp = System.currentTimeMillis()
        addOverlayToParent(newOverlay)
        overlayViews.add(newOverlay)
        overlayViews.sort()
        currentOverlayView?.isInEdit = false
        currentOverlayView = newOverlay
        currentOverlayView!!.isInEdit = true
        overlayObjectEventListener?.onAdded(currentOverlayView!!, overlayViews)
    }

    fun removeOverlay(item: Int) {
        removeOverlay(overlayViews[item])
    }

    private fun removeOverlay(overlayObject: OverlayText) {
        val idxRemoved = overlayViews.indexOf(overlayObject)
        overlayViews.remove(overlayObject)
        parentView.removeView(overlayObject)
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
        val params = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE)
        parentView.addView(child, params)
    }

    private fun getMultiTouchListener(overlay: OverlayText): MultiTouchListener? {
        return MultiTouchListener(
            null,
            parentView,
            parentView,
            true,
            null
        ).apply {
            setOnGestureControl(object : MultiTouchListener.OnGestureControl {
                override fun onClick() {
                    selectedOverlay(overlay)
                }

                override fun onLongClick() {
                }

                override fun onDoubleTap() {
                    editOverlay(overlay)
                }

                override fun onMove() {
                    selectedOverlay(overlay)
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
        )
            .setOnTextEditorListener(object : TextEditorDialogFragment.TextEditorEvent {
                override fun onDone(inputText: String?, colorCode: Int) {
                    overlay.text = inputText
                    Log.d(TAG, colorCode.toString())
                    overlay.textView!!.setTextColor(colorCode)
                    val i = overlayViews.indexOf(overlay)
                    if (i > -1) overlayViews[i] = overlay
                    // edited
                    overlayObjectEventListener?.onEdited(overlayViews)
                }
            })
    }

    fun selectedOverlayId(pos: Int) {
        selectedOverlay(overlayViews[pos])
    }

    fun selectedOverlay(overlay: OverlayText) {
        currentOverlayView?.isInEdit = false
        currentOverlayView = overlay
        currentOverlayView!!.isInEdit = true
        overlayObjectEventListener?.onSelect(overlay, overlayViews)
    }

    override fun reset() {
    }

    override fun updateVideoPositionWithSeekBar(time: Long) {
        seekBarRewind(time)
    }

    override fun changeTimeRangeSelectedOverlay(startTime: Long, endTime: Long) {
        // change time range for current overlay
        if (overlayViews.isEmpty()) {
            return
        }
        val position = overlayViews.indexOf(currentOverlayView)
        if (position != -1) {
            currentOverlayView!!.startTime = startTime
            currentOverlayView!!.endTime = endTime
            overlayViews[position] = currentOverlayView!!
            overlayViews.sort()
            overlayObjectEventListener?.onEdited(overlayViews)
        }
    }

    override fun seekBarRewind(currentTime: Long) {
        for (overlay in overlayViews) {
            if (currentTime in overlay.startTime..overlay.endTime) {
                overlay.visibility = View.VISIBLE
            } else {
                overlay.visibility = View.GONE
            }
        }
    }

    override fun seekBarOnTouch() {
        //
    }

    override fun seekBarOnDoubleTap() {
        //
    }

    fun hideRootView() {
        parentView.visibility = View.GONE
    }

    fun showRootView() {
        parentView.visibility = View.VISIBLE
    }

    @SuppressLint("ClickableViewAccessibility")
    fun addOverlayAfterSpecificPosition(pos: Int): Long {
        val newOverlay = overlayFactory.get(OverlayType.TEXT) as OverlayText
        newOverlay.apply {
            val startTime = overlayViews[pos].endTime
            this.startTime = startTime
            this.endTime = startTime + FRAME_TIME_MS
            // on delete
            this.closeButton!!.setOnClickListener {
                removeOverlay(this)
            }
        }
        newOverlay.setOnTouchListener(getMultiTouchListener(newOverlay))
        newOverlay.timestamp = System.currentTimeMillis()
        addOverlayToParent(newOverlay)
        overlayViews.add(pos + 1, newOverlay)
        overlayViews.sort()
        currentOverlayView?.isInEdit = false
        currentOverlayView = newOverlay
        currentOverlayView!!.isInEdit = true
        overlayObjectEventListener?.onAdded(currentOverlayView!!, overlayViews)
        return newOverlay.startTime
    }

    fun addOverlayAtSpecificPosition(pos: Int, item: OverlayText) {
        addOverlayToParent(item)
        overlayViews.add(pos, item)
        overlayViews.sort()
        overlayObjectEventListener?.onAdded(currentOverlayView, overlayViews)
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