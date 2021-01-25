package com.khoben.autotitle.ui.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.recyclerview.widget.*
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.BaseTransientBottomBar.ANIMATION_MODE_SLIDE
import com.google.android.material.snackbar.Snackbar
import com.khoben.autotitle.App
import com.khoben.autotitle.App.Companion.VIDEO_LOAD_MODE
import com.khoben.autotitle.App.Companion.VIDEO_SOURCE_URI_INTENT
import com.khoben.autotitle.R
import com.khoben.autotitle.databinding.ActivityVideoBinding
import com.khoben.autotitle.model.VideoInfo
import com.khoben.autotitle.model.VideoLoadMode
import com.khoben.autotitle.model.project.RecentProjectsLoader
import com.khoben.autotitle.mvp.presenter.VideoEditActivityPresenter
import com.khoben.autotitle.mvp.view.VideoEditActivityView
import com.khoben.autotitle.service.mediaplayer.MediaSurfacePlayer
import com.khoben.autotitle.service.mediaplayer.VideoRender
import com.khoben.autotitle.ui.overlay.OverlayDataMapper
import com.khoben.autotitle.ui.overlay.OverlayObject
import com.khoben.autotitle.ui.overlay.OverlayText
import com.khoben.autotitle.ui.player.VideoControlsView
import com.khoben.autotitle.ui.player.VideoSurfaceView
import com.khoben.autotitle.ui.player.seekbar.FramesHolder
import com.khoben.autotitle.ui.popup.CustomAlertDialog
import com.khoben.autotitle.ui.popup.VideoProcessingProgressDialog
import com.khoben.autotitle.ui.popup.textoverlayeditor.TextEditorDialogFragment
import com.khoben.autotitle.ui.recyclerview.*
import com.khoben.autotitle.ui.recyclerview.overlays.OverlayViewListAdapter
import com.khoben.autotitle.ui.recyclerview.overlays.RecyclerViewItemEventListener
import com.khoben.autotitle.ui.recyclerview.overlays.SwipeToDeleteCallback
import com.khoben.autotitle.ui.snackbar.SnackBarHelper
import me.toptas.fancyshowcase.FancyShowCaseQueue
import me.toptas.fancyshowcase.FancyShowCaseView
import me.toptas.fancyshowcase.FocusShape
import me.toptas.fancyshowcase.listener.OnCompleteListener
import moxy.MvpAppCompatActivity
import moxy.presenter.InjectPresenter
import timber.log.Timber
import java.lang.ref.WeakReference
import java.util.*


class VideoEditActivity : MvpAppCompatActivity(),
    VideoEditActivityView,
    RecyclerViewItemEventListener,
    VideoProcessingProgressDialog.ProgressDialogListener {

    @InjectPresenter
    lateinit var presenter: VideoEditActivityPresenter

    private lateinit var videoSurfaceView: VideoSurfaceView
    private lateinit var overlayView: FrameLayout
    private lateinit var videoLayer: FrameLayout
    private lateinit var videoControlsView: VideoControlsView
    private lateinit var recyclerView: EmptyRecyclerView

    private lateinit var videoProcessingProgressDialog: VideoProcessingProgressDialog
    private lateinit var alertDialog: CustomAlertDialog
    private lateinit var saveBtn: Button
    private lateinit var muteBtn: MaterialButton
    private lateinit var addItemBtn: Button

    private lateinit var lottieAnimationLoadingView: View
    private lateinit var binding: ActivityVideoBinding
    private lateinit var overlayViewListAdapter: OverlayViewListAdapter

    private val userSettings by lazy {
        getSharedPreferences(
            USER_SETTINGS_PREF,
            Context.MODE_PRIVATE
        )
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        lottieAnimationLoadingView = binding.lottieLoadingView.root.apply {
            // ignore all touches on loading screen
            setOnTouchListener { _, _ -> true }
        }
        videoControlsView = binding.videoSeekbarLayout.videoSeekbarView
        videoSurfaceView = binding.videolayer.videoPreview
        videoLayer = binding.videolayer.root
        overlayView = binding.videolayer.overlaysRoot
        muteBtn = binding.videoSeekbarLayout.muteBtn
        saveBtn = binding.saveVideoBtn
        addItemBtn = binding.videoSeekbarLayout.addItem
        recyclerView = binding.recyclerview

        videoProcessingProgressDialog = VideoProcessingProgressDialog(WeakReference(this))
            .apply {
                listener = this@VideoEditActivity
            }
        alertDialog = CustomAlertDialog(WeakReference(this))
        overlayViewListAdapter = OverlayViewListAdapter()

        videoLayer.setOnClickListener { onViewClicked(it) }
        binding.backBtn.setOnClickListener { onViewClicked(it) }
        binding.emptyRecyclerView.addCaptionRecycler.setOnClickListener { onViewClicked(it) }
        saveBtn.setOnClickListener { onViewClicked(it) }
        addItemBtn.setOnClickListener { onViewClicked(it) }
        muteBtn.setOnClickListener { toggleMute() }

        setupRecyclerView(emptyRecyclerView = binding.emptyRecyclerView.root)

        val sourceVideoUri = intent.getParcelableExtra<Uri>(VIDEO_SOURCE_URI_INTENT)
        val videoLoadingMode = intent.getSerializableExtra(VIDEO_LOAD_MODE) as VideoLoadMode

        /**
         * video processing starts in [VideoEditActivityPresenter.onFirstViewAttach]
         */
        presenter.initVideoSource(sourceVideoUri!!, videoLoadingMode)
        presenter.initOverlayHandler(overlayView, videoControlsView)
        presenter.setMuteState(
            userSettings.getBoolean(
                USER_SETTINGS_ITEM_MUTED,
                App.DEFAULT_MUTE_STATE
            )
        )
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
            it.adapter = overlayViewListAdapter
            it.setEmptyView(emptyRecyclerView)
            (recyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        }

        recyclerView.addOnItemTouchListener(
            RecyclerViewClickListener(
                this,
                recyclerView,
                object : RecyclerViewClickListener.ClickListener {
                    override fun onClick(view: View?, position: Int) {
                        presenter.selectOverlayByIdx(position)
                    }

                    override fun onLongClick(view: View?, position: Int) {
                    }

                    override fun onDoubleClick(view: View?, position: Int) {
                        presenter.editOverlayItem(position)
                    }
                })
        )
        overlayViewListAdapter.listItemEventListener = this

        val swipeHandler = object : SwipeToDeleteCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // swipe to delete action
                presenter.deleteOverlay(viewHolder.bindingAdapterPosition)
            }
        }
        ItemTouchHelper(swipeHandler).attachToRecyclerView(recyclerView)
    }

    override fun initVideoContainerLayoutParams(
        mediaPlayer: MediaSurfacePlayer,
        videoRenderer: VideoRender,
        videoDetails: VideoInfo
    ) {
        Timber.d("Video details = $videoDetails")
        videoControlsView.setMediaDuration(videoDetails.duration)
        videoSurfaceView.init(videoRenderer, mediaPlayer)
        if (videoDetails.rotation == 0 &&
            videoDetails.width > videoDetails.height ||
            videoDetails.rotation == 180 &&
            videoDetails.width > videoDetails.height
        ) {
            videoSurfaceView.setVideoSize(videoDetails.width, videoDetails.height)
            Timber.d("Horizontal video")
        } else {
            // rotated
            if (videoDetails.rotation == 0) {
                videoSurfaceView.setVideoSize(videoDetails.width, videoDetails.height)
            } else {
                videoSurfaceView.setVideoSize(videoDetails.height, videoDetails.width)
            }
            Timber.d("Vertical video")
        }

        // adjust overlay size by video surface size
        // and restores playback position
        videoSurfaceView.post {
            val layoutParams = overlayView.layoutParams
            layoutParams.width = videoSurfaceView.measuredWidth
            layoutParams.height = videoSurfaceView.measuredHeight
            overlayView.layoutParams = layoutParams
            // restores current playback position
            presenter.restoreCurrentPlaybackPosition()
        }
    }

    private fun onViewClicked(view: View) {
        when (view.id) {
            R.id.videolayer -> {
                presenter.clearOverlaySelection()
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
                presenter.saveVideo(Pair(videoSurfaceView.width, videoSurfaceView.height))
            }
        }
    }

    override fun onPause() {
        super.onPause()
        presenter.pausePlayback()
        userSettings.edit().putBoolean(USER_SETTINGS_ITEM_MUTED, presenter.getMuteState()).apply()
        RecentProjectsLoader.save()
    }

    override fun onDestroy() {
        super.onDestroy()
        videoSurfaceView.onDestroy()
    }

    override fun onBackPressed() {
        // if loading screen is showing then ignore back press event
        if (lottieAnimationLoadingView.isVisible) return

        val func: () -> Unit = { onSuperBackPressed() }
        val text: String = getString(R.string.exit_question)

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
        if (isTaskRoot && supportFragmentManager.backStackEntryCount == 0) {
            finishAfterTransition()
        } else {
            super.onBackPressed()
        }
    }

    override fun setLoadingViewVisibility(visible: Boolean) {
        lottieAnimationLoadingView.isVisible = visible
    }

    override fun onErrorVideoProcessing(e: Throwable) {
        Timber.e(e.toString())
        setLoadingViewVisibility(false)
    }

    override fun showPopupWindow(content: String) {
        alertDialog.show(content)
    }

    override fun onVideoProcessed() {
        runOnUiThread {
            Timber.d("Video processed")

            if (!userSettings.getBoolean(USER_SETTINGS_ITEM_GUIDE_SHOWN, false)) {
                val handler = Handler(Looper.getMainLooper())
                val runnable = object : Runnable {
                    override fun run() {
                        if (alertDialog.isShowing()) {
                            handler.postDelayed(this, App.GUIDE_SHOW_DELAY)
                        } else {
                            handler.removeCallbacks(this)
                            initGuide()
                            userSettings.edit().putBoolean(USER_SETTINGS_ITEM_GUIDE_SHOWN, true)
                                .apply()
                        }
                    }
                }
                handler.post(runnable)
            } else {
                Timber.d("Guide was shown")
            }
            setLoadingViewVisibility(false)
        }
    }

    override fun loadFrames(frameResult: FramesHolder) {
        videoControlsView.loadFrames(frameResult)
    }

    private fun initGuide() {
        var dummy = false
        if (overlayViewListAdapter.itemCount == 0) {
            dummy = true
            // add dummy item and delete it later
            overlayViewListAdapter.submitList(
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

            val recyclerItemCase = FancyShowCaseView.Builder(this)
                .backgroundColor(R.color.white)
                .focusShape(FocusShape.ROUNDED_RECTANGLE)
                .focusOn(recyclerView[0])
                .enableAutoTextPosition()
                .title(getString(R.string.guide_recycler_item))
                .build()

            val recyclerItemEditCase = FancyShowCaseView.Builder(this)
                .backgroundColor(R.color.white)
                .focusShape(FocusShape.ROUNDED_RECTANGLE)
                .focusOn(recyclerView[0])
                .enableAutoTextPosition()
                .title(getString(R.string.guide_recycler_item_edit))
                .build()

            val recyclerItemDeleteCase = FancyShowCaseView.Builder(this)
                .backgroundColor(R.color.white)
                .focusShape(FocusShape.ROUNDED_RECTANGLE)
                .focusOn(recyclerView[0])
                .enableAutoTextPosition()
                .title(getString(R.string.guide_recycler_delete))
                .build()

            val doubleTapCase = FancyShowCaseView.Builder(this)
                .backgroundColor(R.color.white)
                .focusShape(FocusShape.ROUNDED_RECTANGLE)
                .focusOn(overlayView)
                .enableAutoTextPosition()
                .title(getString(R.string.guide_doubletap))
                .build()

            val resizeCase = FancyShowCaseView.Builder(this)
                .backgroundColor(R.color.white)
                .focusShape(FocusShape.ROUNDED_RECTANGLE)
                .focusOn(overlayView)
                .enableAutoTextPosition()
                .title(getString(R.string.guide_resize))
                .build()

            val saveCase = FancyShowCaseView.Builder(this)
                .backgroundColor(R.color.white)
                .focusOn(saveBtn)
                .enableAutoTextPosition()
                .title(getString(R.string.guide_save))
                .build()

            FancyShowCaseQueue().apply {
                completeListener = object : OnCompleteListener {
                    override fun onComplete() {
                        if (dummy) {
                            dummy = false
                            overlayViewListAdapter.submitList(emptyList())
                        }
                    }

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
        setLoadingViewVisibility(false)
        Toast.makeText(this, getString(R.string.some_error), Toast.LENGTH_SHORT).show()
        finish()
    }

    override fun updatePlayback(
        overlays: List<OverlayObject>,
        selectedOverlay: OverlayObject?,
        isPlaying: Boolean
    ) {
        videoControlsView.updatePlayback(overlays, selectedOverlay, isPlaying)
    }

    override fun onRemovedOverlay(
        idxRemoved: Int,
        removedOverlay: OverlayObject,
        overlays: List<OverlayObject>
    ) {
        Snackbar.make(
            findViewById(android.R.id.content),
            getString(R.string.snack_bar_item_deleted),
            Snackbar.LENGTH_LONG
        ).apply {
            animationMode = ANIMATION_MODE_SLIDE
            SnackBarHelper.addMargin(this)
        }.setAction(getString(R.string.snack_bar_undo)) {
            presenter.addOverlayAtSpecificPosition(idxRemoved, removedOverlay)
        }.show()
    }

    private fun showGuideHowDeselectItemOnce() {
        if (!userSettings.getBoolean(USER_SETTINGS_ITEM_GUIDE_DESELECT_ITEM, false)) {
            FancyShowCaseView.Builder(this)
                .backgroundColor(R.color.white)
                .focusOn(videoLayer)
                .focusShape(FocusShape.ROUNDED_RECTANGLE)
                .enableAutoTextPosition()
                .title("Click somewhere on video for deselect item")
                .build()
                .show()
            userSettings.edit().putBoolean(USER_SETTINGS_ITEM_GUIDE_DESELECT_ITEM, true).apply()
        }
    }

    override fun highlightListViewItem(index: Int, uuid: UUID?) {
        recyclerView.layoutManager?.scrollToPosition(index)
        showGuideHowDeselectItemOnce()
    }

    override fun onOverlaysChangedList(overlays: List<OverlayObject>) {
        // update recycler view
        runOnUiThread {
            overlayViewListAdapter.submitList(overlays.map {
                if (it is OverlayText) {
                    OverlayDataMapper(
                        startTime = it.startTime,
                        endTime = it.endTime,
                        uuid = it.uuid,
                        text = it.text!!,
                        badgeColor = it.badgeColor,
                        isSelected = it.isInEdit
                    )
                } else {
                    OverlayDataMapper(
                        startTime = it.startTime,
                        endTime = it.endTime,
                        uuid = it.uuid,
                        isSelected = it.isInEdit
                    )
                }
            })
        }
    }

    override fun onVideoSavingStarted() {
        runOnUiThread {
            videoProcessingProgressDialog.show(getString(R.string.save_captions))
        }
    }

    override fun onVideoSavingCancelled() {
        runOnUiThread {
            videoProcessingProgressDialog.dismiss()
            Toast.makeText(
                this,
                getString(R.string.video_saving_cancelled_caption),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onVideoSavingError(msg: String) {
        Timber.e(msg)
        runOnUiThread {
            videoProcessingProgressDialog.dismiss()
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
            videoProcessingProgressDialog.updatePercentage(
                getString(
                    R.string.percent_text,
                    percent.toString()
                )
            )
        }
    }

    private fun runFinishVideoView(filepath: String) {
        val intent = Intent(this, ResultActivity::class.java).apply {
            putExtra(VIDEO_OUTPUT_URI_INTENT, filepath)
        }
        startActivity(intent)
    }

    override fun onVideoSavingComplete(filepath: String) {
        Timber.d("Saved video with path=$filepath")
        runOnUiThread {
            videoProcessingProgressDialog.updatePercentage(getString(R.string.percent_text, "0"))
            videoProcessingProgressDialog.dismiss()
            Toast.makeText(
                this,
                getString(R.string.saved_caption),
                Toast.LENGTH_SHORT
            ).show()
            Handler(Looper.getMainLooper()).postDelayed({
                runFinishVideoView(filepath)
            }, 2000)
        }
    }

    override fun setControlsToTime(time: Long) {
        videoControlsView.setControlsToTime(time)
    }

    override fun onClickedAddBelow(item: Int) {
        Timber.d("Clicked on $item")
        presenter.addOverlayAfterSpecificPosition(item)
    }

    private fun toggleMute() {
        presenter.toggleMute(true)
    }

    override fun toggledMuteState(state: Boolean, clicked: Boolean) {
        when (state) {
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

    override fun showOverlayEditor(overlay: OverlayObject) {
        if (overlay is OverlayText) {
            TextEditorDialogFragment.show(
                this,
                overlay.text,
                overlay.textView!!.currentTextColor
            ).setOnTextEditorListener(object : TextEditorDialogFragment.TextEditorEvent {
                override fun onDone(inputText: String?, colorCode: Int) {
                    presenter.saveEditedOverlay(overlay, inputText, colorCode)
                }
            })
        }
    }

    companion object {
        const val VIDEO_OUTPUT_URI_INTENT = "VIDEO_OUTPUT_URI"
        private const val USER_SETTINGS_PREF = "USER_SETTINGS_PREF"
        private const val USER_SETTINGS_ITEM_GUIDE_SHOWN = "GUIDED_TOUR"
        private const val USER_SETTINGS_ITEM_GUIDE_DESELECT_ITEM = "GUIDE_DESELECT"
        private const val USER_SETTINGS_ITEM_MUTED = "PLAYER_MUTED"
    }

    override fun cancelBtnClicked() {
        presenter.pauseSavingVideo()
    }

    override fun confirmCancelBtnClicked() {
        presenter.cancelSavingVideo()
    }

    override fun nopeCancelBtnClicked() {
        presenter.resumeSavingVideo()
    }
}