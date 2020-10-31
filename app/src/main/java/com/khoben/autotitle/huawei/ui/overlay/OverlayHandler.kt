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
import com.khoben.autotitle.huawei.ui.player.VideoSeekBarView
import com.khoben.autotitle.huawei.ui.popup.TextEditorDialogFragment
import java.util.*

class OverlayHandler private constructor(
    private val context: Context,
    private val parentView: RelativeLayout
) : VideoSeekBarView.SeekBarListener {

    private var TAG: String = OverlayHandler::class.java.simpleName
    private val overlayFactory = OverlayFactory(context)
    private val overlayViews = ArrayList<OverlayText>()
    private var currentOverlayView: OverlayText? = null

    var overlayObjectEventListener: OverlayObjectEventListener? = null

    companion object {
        val regex = """[,.?!-=+:]""".toRegex()
    }

    interface OverlayObjectEventListener {
        fun onRemoved(overlay: OverlayText)
        fun onRemoved(overlay: List<OverlayText>)
        fun onEdit()
        fun onEdited(overlay: OverlayText)
        fun onEdited(overlay: List<OverlayText>)
        fun onUnEditable(overlay: OverlayText?, overlays: List<OverlayText>)
        fun onAdd()
        fun onAdded(overlay: OverlayText)
        fun onAdded(overlay: OverlayText?, overlays: List<OverlayText>, isEdit: Boolean = true)
        fun onSelect(overlay: OverlayText?, overlays: List<OverlayText>)
    }

    @SuppressLint("ClickableViewAccessibility")
    fun addOverlay(startTime: Long, endTime: Long, text: String) {
        // filter text from punctuation
        val filtered = regex.replace(text, "").trim()
        if (filtered.isEmpty()) {
            Log.e(TAG, "Adding empty overlay")
            return
        }
        overlayObjectEventListener?.onAdd()
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

        overlayObjectEventListener?.onAdded(currentOverlayView!!)
        overlayObjectEventListener?.onAdded(currentOverlayView, overlayViews, true)
    }

    @SuppressLint("ClickableViewAccessibility")
    fun addOverlay(startTime: Long, type: OverlayType = OverlayType.TEXT) {
        overlayObjectEventListener?.onAdd()
        val newOverlay = overlayFactory.get(type) as OverlayText
        newOverlay.apply {
            this.startTime = startTime
            this.endTime = startTime + FRAME_TIME_MS
            // on delete
            this.closeButton!!.setOnClickListener {
                removeOverlay(this)
            }
//            seekBarRewind(0L)
        }
        newOverlay.setOnTouchListener(getMultiTouchListener(newOverlay))
        newOverlay.timestamp = System.currentTimeMillis()
        addOverlayToParent(newOverlay)
        overlayViews.add(newOverlay)
        overlayViews.sort()
        currentOverlayView?.isInEdit = false
        currentOverlayView = newOverlay
        currentOverlayView!!.isInEdit = true
        overlayObjectEventListener?.onAdded(currentOverlayView!!)
        overlayObjectEventListener?.onAdded(currentOverlayView!!, overlayViews)
    }

    private fun removeOverlay(overlayObject: OverlayText) {
        overlayViews.remove(overlayObject)
        parentView.removeView(overlayObject)
        overlayObjectEventListener?.onRemoved(overlayObject)
        overlayObjectEventListener?.onRemoved(overlayViews)
    }

    fun getOverlays(): List<OverlayText> {
        return overlayViews
    }

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
        overlayObjectEventListener?.onEdit()
        TextEditorDialogFragment.show(
            context as AppCompatActivity,
            overlay.text,
            overlay.textView!!.currentTextColor
        )
            .setOnTextEditorListener(object : TextEditorDialogFragment.TextEditorEvent {
                override fun onDone(inputText: String?, colorCode: Int) {
                    overlay.text = inputText
                    overlay.textView!!.setTextColor(colorCode)
                    val i = overlayViews.indexOf(overlay)
                    if (i > -1) overlayViews[i] = overlay
                    // edited
                    overlayObjectEventListener?.onEdited(overlay)
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
            overlayObjectEventListener?.onEdited(currentOverlayView!!)
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
    fun addOverlayAtSpecificPosition(pos: Int): Long {
        overlayObjectEventListener?.onAdd()
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
//        overlayViews.sort()
        currentOverlayView?.isInEdit = false
        currentOverlayView = newOverlay
        currentOverlayView!!.isInEdit = true
        overlayObjectEventListener?.onAdded(currentOverlayView!!)
        overlayObjectEventListener?.onAdded(currentOverlayView!!, overlayViews)
        return newOverlay.startTime
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