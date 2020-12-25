package com.khoben.autotitle.ui.player

import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.google.android.material.card.MaterialCardView
import com.khoben.autotitle.App
import com.khoben.autotitle.R
import com.khoben.autotitle.common.DisplayUtils
import com.khoben.autotitle.extension.toReadableTimeString
import com.khoben.autotitle.ui.overlay.OverlayText
import com.khoben.autotitle.ui.player.seekbar.SeekBarListener
import com.khoben.autotitle.ui.player.seekbar.VideoSeekBarView

class VideoControlsView(context: Context, attrs: AttributeSet) :
    RelativeLayout(context, attrs),
    SeekBarListener {

    private lateinit var videoSeekBarView: VideoSeekBarView
    private lateinit var playPauseButton: PlayPauseMaterialButton
    private lateinit var videoSeekBarCenterLine: ImageView

    private lateinit var totalAndCurrentTimeLayout: RelativeLayout
    private lateinit var totalVideoTime: TextView
    private lateinit var currentVideoTime: TextView

    private var viewWidth = 0
    private var viewHeight = 0
    private var screenWidth = 0

    var ppBtnListener: PlayPauseMaterialButton.OnClickListener? = null

    init {
        initView(context, attrs)
    }

    companion object {
        private val TAG = VideoControlsView::class.java.simpleName
    }

    private fun initView(context: Context, attrs: AttributeSet) {
        screenWidth = context.resources.displayMetrics.widthPixels

        /*********SeekBar view*********/
        videoSeekBarView = VideoSeekBarView(context, null)
            .apply {
                seekBarListener = this@VideoControlsView
            }

        addView(
            videoSeekBarView,
            LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT)
                .apply {
                    addRule(ALIGN_PARENT_LEFT)
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
            LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                addRule(ALIGN_PARENT_TOP, TRUE)
                setMargins(0, DisplayUtils.dipToPx(2), 0, 0)
            }
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
        /**
         * [ppbContainer] as background container for [playPauseButton] to hiding seekbar on
         * left side under [playPauseButton]
         *
         * Also [MaterialCardView] used for correct elevation background color
         */
        val ppbContainer = MaterialCardView(context, attrs).apply {
            cardElevation = 0f
            /**
             * [playPauseButton] has 8dp corner radius
             */
            shapeAppearanceModel = shapeAppearanceModel
                .toBuilder()
                .setBottomRightCornerSize(DisplayUtils.dipToPx(8).toFloat())
                .setTopRightCornerSize(DisplayUtils.dipToPx(8).toFloat())
                .setBottomLeftCornerSize(0F)
                .setTopLeftCornerSize(0F)
                .build()

            this@VideoControlsView.addView(this, LayoutParams(
                DisplayUtils.dipToPx(context, App.SEEKBAR_HEIGHT_DP),
                DisplayUtils.dipToPx(context, App.SEEKBAR_HEIGHT_DP)
            ).apply {
                    addRule(CENTER_VERTICAL, TRUE)
                    addRule(ALIGN_PARENT_LEFT, TRUE)
                })
        }
        playPauseButton = PlayPauseMaterialButton(context, null).apply {
            setOnClickListener {
                ppBtnListener?.onPlayPauseButtonClicked()
            }
        }
        ppbContainer.addView(
            playPauseButton,
            LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                addRule(CENTER_VERTICAL, TRUE)
                addRule(ALIGN_PARENT_LEFT, TRUE)
            }
        )
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
     * @param isPlaying Is playing
     */
    fun updatePlayback(
        overlays: List<OverlayText>,
        selectedOverlay: OverlayText?,
        isPlaying: Boolean
    ) {
        playPauseButton.toggle(isPlaying)
        videoSeekBarView.updatePlayback(overlays, selectedOverlay, isPlaying)
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
        seekBarListener?.seekBarRewind(currentTime)
    }

    override fun seekBarCompletePlaying() {
        seekBarListener?.seekBarCompletePlaying()
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
}
