package com.khoben.autotitle.ui.activity

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.*
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.BaseTransientBottomBar.ANIMATION_MODE_SLIDE
import com.google.android.material.snackbar.Snackbar
import com.khoben.autotitle.App
import com.khoben.autotitle.R
import com.khoben.autotitle.databinding.ActivityVideoBinding
import com.khoben.autotitle.mvp.presenter.VideoEditActivityPresenter
import com.khoben.autotitle.mvp.view.VideoEditActivityView
import com.khoben.autotitle.service.mediaplayer.MediaSurfacePlayer
import com.khoben.autotitle.service.mediaplayer.VideoRender
import com.khoben.autotitle.ui.activity.MainActivity.Companion.VIDEO_SOURCE_URI_INTENT
import com.khoben.autotitle.ui.overlay.OverlayDataMapper
import com.khoben.autotitle.ui.overlay.OverlayText
import com.khoben.autotitle.ui.player.VideoControlsView
import com.khoben.autotitle.ui.player.VideoSurfaceView
import com.khoben.autotitle.ui.popup.CustomAlertDialog
import com.khoben.autotitle.ui.popup.LoadingDialog
import com.khoben.autotitle.ui.recyclerview.*
import com.khoben.autotitle.ui.snackbar.SnackBarHelper
import me.toptas.fancyshowcase.FancyShowCaseView
import me.toptas.fancyshowcase.FocusShape
import moxy.MvpAppCompatActivity
import moxy.presenter.InjectPresenter
import java.util.*


class VideoEditActivity : MvpAppCompatActivity(),
    VideoEditActivityView,
    RecyclerViewItemEventListener {

    @InjectPresenter
    lateinit var presenter: VideoEditActivityPresenter

    private lateinit var videoSurfaceView: VideoSurfaceView
    private lateinit var overlayView: RelativeLayout
    private lateinit var videoLayer: FrameLayout
    private lateinit var videoControlsView: VideoControlsView
    private lateinit var recyclerView: EmptyRecyclerView

    private lateinit var loadingDialog: LoadingDialog
    private lateinit var alertDialog: CustomAlertDialog
    private lateinit var saveBtn: Button
    private lateinit var muteBtn: MaterialButton
    private lateinit var addItemBtn: Button

    private lateinit var lottieAnimationLoadingView: View
    private lateinit var binding: ActivityVideoBinding
    private val overlayViewAdapter: OverlayViewAdapter = OverlayViewAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        lottieAnimationLoadingView = binding.lottieLoadingView.root
        videoControlsView = binding.videoSeekbarView.apply {
            ppBtnListener = presenter
        }
        videoSurfaceView = binding.videolayer.videoPreview
        videoLayer = binding.videolayer.root
        overlayView = binding.videolayer.overlaysRoot
        muteBtn = binding.videolayer.muteBtn
        saveBtn = binding.saveVideoBtn
        addItemBtn = binding.addItem
        recyclerView = binding.recyclerview

        loadingDialog = LoadingDialog(this)
        alertDialog = CustomAlertDialog(this)

        val sourceVideoUri = intent.getParcelableExtra<Uri>(VIDEO_SOURCE_URI_INTENT)

        // video processing in presenter@onFirstViewAttach
        presenter.setUri(sourceVideoUri!!)
        presenter.initEditor(this, overlayView, videoControlsView)

        videoLayer.setOnClickListener { onViewClicked(it) }
        binding.backBtn.setOnClickListener { onViewClicked(it) }
        binding.emptyViewRecycler.addCaptionRecycler.setOnClickListener { onViewClicked(it) }
        saveBtn.setOnClickListener { onViewClicked(it) }
        addItemBtn.setOnClickListener { onViewClicked(it) }
        muteBtn.setOnClickListener { toggleMute() }

        setupRecyclerView(binding.emptyViewRecycler.root)
    }

    private fun setupRecyclerView(emptyRecyclerView: View) {
        recyclerView.also {
            it.addItemDecoration(
                DividerItemDecoration(
                    this,
                    DividerItemDecoration.VERTICAL
                ).also { divider ->
                    divider.setDrawable(
                        ContextCompat.getDrawable(
                            this,
                            R.drawable.recycler_horizontal_divider
                        )!!
                    )
                })
            it.layoutManager = LinearLayoutManager(this)
            it.adapter = overlayViewAdapter
            it.setEmptyView(emptyRecyclerView)
            (recyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        }

        recyclerView.addOnItemTouchListener(
            RecyclerViewClickListener(
                this,
                recyclerView,
                object : RecyclerViewClickListener.ClickListener {
                    override fun onClick(view: View?, position: Int) {
                        presenter.recyclerSelectOverlay(position)
                    }

                    override fun onLongClick(view: View?, position: Int) {
                    }

                    override fun onDoubleClick(view: View?, position: Int) {
                        presenter.editItem(position)
                    }
                })
        )
        overlayViewAdapter.listItemEventListener = this

        val swipeHandler = object : SwipeToDeleteCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // swipe to delete action
                presenter.deleteOverlay(viewHolder.adapterPosition)
            }
        }
        ItemTouchHelper(swipeHandler).attachToRecyclerView(recyclerView)
    }

    override fun initVideoContainerLayoutParams(
        mediaPlayer: MediaSurfacePlayer,
        videoRenderer: VideoRender
    ) {
        videoSurfaceView.init(videoRenderer, mediaPlayer)
        val videoDetails = presenter.getVideoDetails()!!
        Log.d(TAG, "Video details = $videoDetails")
        videoControlsView.setMediaDuration(videoDetails.duration)
        if (videoDetails.rotation == 0 &&
            videoDetails.width > videoDetails.height ||
            videoDetails.rotation == 180 &&
            videoDetails.width > videoDetails.height
        ) {
            videoSurfaceView.setVideoSize(videoDetails.width, videoDetails.height)
            Log.d(TAG, "Horizontal video")
        } else {
            // rotated
            if (videoDetails.rotation == 0) {
                videoSurfaceView.setVideoSize(videoDetails.width, videoDetails.height)
            } else {
                videoSurfaceView.setVideoSize(videoDetails.height, videoDetails.width)
            }
            Log.d(TAG, "Vertical video")
        }

        videoSurfaceView.post {
            val width: Int = videoSurfaceView.measuredWidth
            val height: Int = videoSurfaceView.measuredHeight

            val layoutParams: ViewGroup.LayoutParams = overlayView.layoutParams
            layoutParams.width = width
            layoutParams.height = height

            overlayView.layoutParams = layoutParams
        }
    }

    private var lastTimeOnViewClicked = 0L
    private fun onViewClicked(view: View) {
        val clickTime = System.currentTimeMillis()
        if (clickTime - lastTimeOnViewClicked < IGNORE_TIME_CLICK_EVENT
            || loadingDialog.isShowing()
        ) {
            return
        }
        lastTimeOnViewClicked = clickTime
        when (view.id) {
            R.id.videolayer -> {
                presenter.unEditable()
            }
            R.id.back_btn -> {
                onBackPressed()
            }
            R.id.add_caption_recycler -> {
                presenter.addOverlayAtCurrentPosition()
            }
            R.id.add_item -> {
                presenter.addOverlayAtCurrentPosition()
            }
            R.id.save_video_btn -> {
                presenter.saveVideo(this, Pair(videoSurfaceView.width, videoSurfaceView.height))
            }
        }
    }

    override fun onPause() {
        super.onPause()
        presenter.pausePlayback()
    }

    override fun onDestroy() {
        super.onDestroy()
        videoSurfaceView.onDestroy()
    }

    override fun onBackPressed() {
        // if loading screen is shown then ignore back press
        if (lottieAnimationLoadingView.isVisible) return

        val func: () -> Unit
        val text: String
        if (loadingDialog.isShowing()) {
            func = { presenter.cancelSavingVideo() }
            text = getString(R.string.cancel_video_saving_question)
        } else {
            func = { onSuperBackPressed() }
            text = getString(R.string.exit_question)
        }
        MaterialAlertDialogBuilder(this)
            .setMessage(text)
            .setCancelable(false)
            .setPositiveButton(getString(R.string.yes_caption)) { _, _ ->
                func()
            }
            .setNegativeButton(getString(R.string.no_caption)) { dialog, _ ->
                dialog.cancel()
            }
            .create()
            .show()
    }

    private fun onSuperBackPressed() {
        super.onBackPressed()
    }

    override fun stopLoadingView() {
        lottieAnimationLoadingView.visibility = View.GONE
    }

    override fun onErrorVideoProcessing(e: Throwable) {
        Log.e(TAG, e.toString())
        stopLoadingView()
    }

    override fun showPopupWindow(content: String) {
        alertDialog.show(content)
    }

    override fun onVideoProcessed(
        thumbnails: List<Bitmap>,
        frameTime: Long
    ) {
        runOnUiThread {
            Log.d(TAG, "Video processed")
            videoControlsView.addFramesToSeekBar(thumbnails, frameTime)

            val sharedPref: SharedPreferences =
                getSharedPreferences(PREF_NAME_GUIDE_SHOWN, PRIVATE_MODE)
            if (!sharedPref.getBoolean(PREF_NAME_GUIDE_SHOWN, false)) {
                val handler = Handler(Looper.getMainLooper())
                val runnable = object : Runnable {
                    override fun run() {
                        if (alertDialog.isShowing()) {
                            handler.postDelayed(this, App.GUIDE_SHOW_DELAY)
                        } else {
                            handler.removeCallbacks(this)
                            initGuide()
                            val editor = sharedPref.edit()
                            editor.putBoolean(PREF_NAME_GUIDE_SHOWN, true)
                            editor.apply()
                        }
                    }
                }
                handler.post(runnable)
            } else {
                Log.d(TAG, "Guide was shown")
            }
            stopLoadingView()
        }
    }

    private fun initGuide() {
        var dummy = false
        if (overlayViewAdapter.itemCount == 0) {
            dummy = true
            // add dummy item and delete it later
            overlayViewAdapter.submitList(
                mutableListOf(
                    OverlayDataMapper(
                        0L, 1000L, UUID.randomUUID(), "Test item for guide"
                    )
                )
            )
        }

        Handler(Looper.getMainLooper()).postDelayed({
            val addCase = FancyShowCaseView.Builder(this)
                .backgroundColor(R.color.white)
                .focusCircleRadiusFactor(2.0)
                .focusOn(addItemBtn)
                .enableAutoTextPosition()
                .title(getString(R.string.guide_add_case))
                .build()

                .titleTextColor(R.color.white) // Specify the color of the title text
                .descriptionTextSize(10) // Specify the size (in sp) of the description text
                .textTypeface(Typeface.SANS_SERIF) // Specify a typeface for the text
                .dimColor(R.color.black) // If set, will dim behind the view with 30% opacity of the given color
                .drawShadow(true) // Whether to draw a drop shadow or not
                .cancelable(false) // Whether tapping outside the outer circle dismisses the view
                .tintTarget(true) // Whether to tint the target view's color
                .transparentTarget(true) // Specify whether the target is transparent (displays the content underneath)
                .targetRadius(60),  // Specify the target radius (in dp)
            object : TapTargetView.Listener() {
            })

                }
            }.add(addCase)
                .add(recyclerItemCase)
                .add(recyclerItemEditCase)
                .add(recyclerItemDeleteCase)
                .add(doubleTapCase)
                .add(resizeCase)
                .add(saveCase)
                .show()
        }, 500)
    }

    override fun finishOnError() {
        stopLoadingView()
        Toast.makeText(this, getString(R.string.some_error), Toast.LENGTH_SHORT).show()
        finish()
    }

    override fun updatePlayback(
        overlays: List<OverlayText>,
        selectedOverlay: OverlayText?,
        isPlaying: Boolean
    ) {
        videoControlsView.updatePlayback(overlays, selectedOverlay, isPlaying)
    }

    override fun onRemovedOverlay(
        idxRemoved: Int,
        removedOverlay: OverlayText,
        overlays: ArrayList<OverlayText>
    ) {
        Snackbar.make(
            findViewById(android.R.id.content),
            getString(R.string.snack_bar_item_deleted),
            Snackbar.LENGTH_LONG
        ).setAction(getString(R.string.snack_bar_undo)) {
            presenter.addOverlayAtSpecificPosition(idxRemoved, removedOverlay)
        }.apply {
            animationMode = ANIMATION_MODE_SLIDE
            SnackBarHelper.addMargin(this)
        }.show()
    }

    private fun showGuideHowDeselectItemOnce() {
        val sharedPref = getSharedPreferences(PREF_NAME_GUIDE_DESELECT_ITEM, PRIVATE_MODE)
        if (!sharedPref.getBoolean(PREF_NAME_GUIDE_DESELECT_ITEM, false)) {
            FancyShowCaseView.Builder(this)
                .backgroundColor(R.color.white75)
                .focusOn(videoLayer)
                .focusShape(FocusShape.ROUNDED_RECTANGLE)
                .enableAutoTextPosition()
                .title("Click somewhere on video for deselect item")
                .build()
                .show()

            sharedPref.edit().apply {
                putBoolean(PREF_NAME_GUIDE_DESELECT_ITEM, true)
                apply()
            }
        }
    }

    override fun highlightListViewItem(index: Int) {
        recyclerView.layoutManager?.scrollToPosition(index)
        // TODO: Highlight selected item

        showGuideHowDeselectItemOnce()
    }

    override fun onOverlaysChangedList(overlays: List<OverlayText>) {
        // update recycler view
        runOnUiThread {
            overlayViewAdapter.submitList(overlays.map {
                OverlayDataMapper(
                    startTime = it.startTime,
                    endTime = it.endTime,
                    uuid = it.uuid!!,
                    text = it.text!!,
                    badgeColor = it.badgeColor
                )
            })
        }
    }

    override fun onVideoSavingStarted() {
        runOnUiThread {
            loadingDialog.show(getString(R.string.save_captions))
        }
    }

    override fun onVideoSavingCancelled() {
        runOnUiThread {
            loadingDialog.dismiss()
            Toast.makeText(
                this,
                getString(R.string.video_saving_cancelled_caption),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onVideoSavingError(msg: String) {
        Log.e(TAG, msg)
        runOnUiThread {
            loadingDialog.dismiss()
            Toast.makeText(
                this,
                getString(R.string.error_while_saving),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onVideoSavingProgress(progress: Double) {
        val percent = (progress * 100).toInt()
        runOnUiThread {
            loadingDialog.updatePercentage(getString(R.string.percent_text, percent.toString()))
        }
    }

    private fun runFinishVideoView(filepath: String) {
        val intent = Intent(this, ResultActivity::class.java).apply {
            putExtra(VIDEO_OUTPUT_URI_INTENT, filepath)
        }
        startActivity(intent)
    }

    override fun onVideoSavingComplete(filepath: String) {
        Log.d(TAG, "Saved video with path=$filepath")
        runOnUiThread {
            loadingDialog.updatePercentage(getString(R.string.percent_text, "0"))
            loadingDialog.dismiss()
            Toast.makeText(
                this,
                getString(R.string.saved_caption),
                Toast.LENGTH_SHORT
            ).show()
            Handler(Looper.getMainLooper()).postDelayed({
                runFinishVideoView(filepath)
            }, 1000)
        }
    }

    override fun setControlsToTime(time: Long) {
        videoControlsView.setControlsToTime(time)
    }

    override fun onClickedAddBelow(item: Int) {
        Log.d(TAG, "Clicked on $item")
        presenter.addOverlayAfterSpecificPosition(item)
    }

    private fun toggleMute() {
        presenter.toggleMute(true)
    }

    override fun toggledMuteState(state: Boolean, clicked: Boolean) {
        when(state) {
            true -> {
                muteBtn.setIconResource(R.drawable.volume_off_icon_24dp)
                if (clicked) Toast.makeText(this, "Muted", Toast.LENGTH_SHORT).show()
            }
            else -> {
                muteBtn.setIconResource(R.drawable.volume_up_icon_24dp)
                if (clicked) Toast.makeText(this, "Unmuted", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private val TAG = VideoEditActivity::class.java.simpleName
        private const val IGNORE_TIME_CLICK_EVENT = 500
        const val VIDEO_OUTPUT_URI_INTENT = "VIDEO_OUTPUT_URI"
        private var PRIVATE_MODE = 0
        private const val PREF_NAME_GUIDE_SHOWN = "GUIDED_TOUR_897er9XX"
        private const val PREF_NAME_GUIDE_DESELECT_ITEM = "GUIDE_DESELECT_ITEM_0934i23d"
    }
}