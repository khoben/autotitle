package com.khoben.autotitle.huawei.ui.player

import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.khoben.autotitle.huawei.App
import com.khoben.autotitle.huawei.R
import com.khoben.autotitle.huawei.common.DisplayUtils
import com.khoben.autotitle.huawei.extension.toReadableTimeString
import com.khoben.autotitle.huawei.model.PlaybackEvent
import com.khoben.autotitle.huawei.model.PlaybackState
import com.khoben.autotitle.huawei.service.mediaplayer.MediaController
import com.khoben.autotitle.huawei.ui.overlay.OverlayText
import com.khoben.autotitle.huawei.ui.player.seekbar.SeekBarListener
import com.khoben.autotitle.huawei.ui.player.seekbar.VideoSeekBarView
import javax.inject.Inject

class VideoControlsView(context: Context, attrs: AttributeSet) :
    RelativeLayout(context, attrs),
    SeekBarListener,
    MediaController.Callback {

    private lateinit var videoSeekBarView: VideoSeekBarView
    private lateinit var playPauseButton: PlayPauseMaterialButton
    private lateinit var videoSeekBarCenterLine: ImageView

    private lateinit var totalAndCurrentTimeLayout: RelativeLayout
    private lateinit var totalVideoTime: TextView
    private lateinit var currentVideoTime: TextView

    private var viewWidth = 0
    private var viewHeight = 0
    private var screenWidth = 0

    @Inject
    lateinit var mediaController: MediaController

    init {
        App.applicationComponent.inject(this)
        initView(context, attrs)
    }

    companion object {
        private val TAG = VideoControlsView::class.java.simpleName
    }

    private fun initView(context: Context, attrs: AttributeSet) {
        screenWidth = context.resources.displayMetrics.widthPixels

        /*********SeekBar view*********/
        videoSeekBarView = VideoSeekBarView(context, null).apply {
            layoutParams = LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            seekBarListener = this@VideoControlsView
        }

        addView(
            videoSeekBarView,
            LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT)
                .apply {
                    addRule(CENTER_VERTICAL, TRUE)
                    addRule(ALIGN_PARENT_LEFT, TRUE)
                }
        )
        /*************************************/

        /****Current and total video time****/
        totalAndCurrentTimeLayout = LayoutInflater.from(context)
            .inflate(R.layout.current_total_time_layout, null) as RelativeLayout

        currentVideoTime = totalAndCurrentTimeLayout.findViewById(R.id.tv_currentTime)
        currentVideoTime.text =
            context.getString(R.string.time_second_string, 0L.toReadableTimeString())
        totalVideoTime = totalAndCurrentTimeLayout.findViewById(R.id.tv_totalTime)

        addView(
            totalAndCurrentTimeLayout,
            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        )
        /************************************/

        /*********Vertical center line********/
        videoSeekBarCenterLine = ImageView(context).apply {
            setImageResource(R.drawable.vertical_line_rounded)
        }

        addView(
            videoSeekBarCenterLine,
            LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT)
                .apply {
                    addRule(CENTER_IN_PARENT, TRUE)
                }
        )
        /**************************************/

        /**********Play/pause button**************/
        playPauseButton = PlayPauseMaterialButton(context, null).apply {
            setOnClickListener {
                mediaController.toggle()
            }
        }
        addView(
            playPauseButton,
            LayoutParams(
                DisplayUtils.dipToPx(context, App.SEEKBAR_HEIGHT_DP),
                DisplayUtils.dipToPx(context, App.SEEKBAR_HEIGHT_DP)
            )
                .apply {
                    addRule(CENTER_VERTICAL, TRUE)
                    addRule(ALIGN_PARENT_LEFT, TRUE)
                }
        )
        mediaController.addSubscription(this)
        /************************************/
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        viewWidth = videoSeekBarView.measuredWidth
        viewHeight = measuredHeight
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        videoSeekBarView.layout(screenWidth / 2, 0, screenWidth / 2 + viewWidth, viewHeight)
    }

    fun addFramesToSeekBar(bitmaps: List<Bitmap>, frameTime: Long) {
        val fullWidthFrameLine = bitmaps.size * (screenWidth.toFloat() / App.FRAMES_PER_SCREEN)
        videoSeekBarView.layoutParams =
            LayoutParams(fullWidthFrameLine.toInt(), ViewGroup.LayoutParams.MATCH_PARENT)
        videoSeekBarView.addFramesToSeekBar(bitmaps, frameTime)
    }

    /**
     * Set video duration time
     *
     * @param totalTime Video duration time in milliseconds
     */
    fun setMediaDuration(totalTime: Long) {
        totalVideoTime.text =
            context.getString(R.string.time_second_string, totalTime.toReadableTimeString())
        videoSeekBarView.setMediaDuration(totalTime)
    }

    /**
     * Update playback state
     *
     * @param overlays List of [OverlayText]
     * @param selectedOverlay Currently selected [OverlayText]
     * @param isEdit Is in edit mode
     * @param isPlaying Is playing
     */
    fun updatePlayback(
        overlays: List<OverlayText>?,
        selectedOverlay: OverlayText?,
        isEdit: Boolean,
        isPlaying: Boolean
    ) {
        videoSeekBarView.updatePlayback(overlays, selectedOverlay, isEdit, isPlaying)
    }

    /**
     * Sets UI controls to desired timestamp
     *
     * @param pos Timestamp
     */
    fun setControlsToTime(pos: Long) {
        currentVideoTime.text = pos.toReadableTimeString()
        playPauseButton.toggle(false)
        videoSeekBarView.setToState(pos)
    }

    var seekBarListener: SeekBarListener? = null
    override fun changeTimeRangeSelectedOverlay(startTime: Long, endTime: Long) {
        seekBarListener?.changeTimeRangeSelectedOverlay(startTime, endTime)
    }

    override fun seekBarRewind(currentTime: Long) {
        currentVideoTime.text = currentTime.toReadableTimeString()
        playPauseButton.toggle(false)
        seekBarListener?.seekBarRewind(currentTime)
    }

    override fun seekBarOnTouch() {
        seekBarListener?.seekBarOnTouch()
    }

    override fun seekBarOnDoubleTap() {
        seekBarListener?.seekBarOnDoubleTap()
    }

    override fun updateVideoPositionWithSeekBar(time: Long) {
        currentVideoTime.text =
            context.getString(R.string.time_second_string, time.toReadableTimeString())
        seekBarListener?.updateVideoPositionWithSeekBar(time)
    }

    override fun handlePlaybackState(state: PlaybackEvent) {
        when (state.playState) {
            PlaybackState.PLAY -> {
                playPauseButton.toggle(true)
            }
            PlaybackState.PAUSED -> {
                playPauseButton.toggle(false)
            }
            PlaybackState.STOP -> {
            }
            PlaybackState.REWIND -> {
            }
        }
    }
}
