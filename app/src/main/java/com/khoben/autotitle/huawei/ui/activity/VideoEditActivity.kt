package com.khoben.autotitle.huawei.ui.activity

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.BaseTransientBottomBar.ANIMATION_MODE_SLIDE
import com.google.android.material.snackbar.Snackbar
import com.khoben.autotitle.huawei.App
import com.khoben.autotitle.huawei.R
import com.khoben.autotitle.huawei.databinding.ActivityVideoBinding
import com.khoben.autotitle.huawei.mvp.presenter.VideoEditActivityPresenter
import com.khoben.autotitle.huawei.mvp.view.VideoEditActivityView
import com.khoben.autotitle.huawei.ui.activity.MainActivity.Companion.VIDEO_SOURCE_URI_INTENT
import com.khoben.autotitle.huawei.ui.overlay.OverlayDataMapper
import com.khoben.autotitle.huawei.ui.overlay.OverlayText
import com.khoben.autotitle.huawei.ui.player.VideoControlsView
import com.khoben.autotitle.huawei.ui.player.VideoSurfaceView
import com.khoben.autotitle.huawei.ui.popup.CustomAlertDialog
import com.khoben.autotitle.huawei.ui.popup.LoadingDialog
import com.khoben.autotitle.huawei.ui.recyclerview.*
import com.khoben.autotitle.huawei.ui.snackbar.SnackBarHelper
import me.toptas.fancyshowcase.FancyShowCaseQueue
import me.toptas.fancyshowcase.FancyShowCaseView
import me.toptas.fancyshowcase.FocusShape
import me.toptas.fancyshowcase.listener.OnCompleteListener
import moxy.MvpAppCompatActivity
import moxy.presenter.InjectPresenter
import java.util.*


class VideoEditActivity : MvpAppCompatActivity(),
    VideoEditActivityView,
    ListItemEventListener {

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
    private lateinit var addItemBtn: Button

    private lateinit var lottieAnimationLoadingView: View
    private lateinit var binding: ActivityVideoBinding
    private val overlayViewAdapter: OverlayViewAdapter = OverlayViewAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lottieAnimationLoadingView = binding.lottieLoadingView.root
        videoControlsView = binding.videoSeekbarView
        videoSurfaceView = binding.videolayer.videoPreview
        videoLayer = binding.videolayer.root
        overlayView = binding.videolayer.overlaysRoot
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

    override fun initVideoContainerLayoutParams() {
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
        presenter.setPlayState(false)
    }

    override fun onDestroy() {
        super.onDestroy()
        videoSurfaceView.onDestroy()
    }

    override fun onBackPressed() {
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
                        0L, 1000L, 0L, "Test item for guide"
                    )
                )
            )
        }

        Handler(Looper.getMainLooper()).postDelayed({
            val addCase = FancyShowCaseView.Builder(this)
                .backgroundColor(R.color.white75)
                .focusCircleRadiusFactor(2.0)
                .focusOn(addItemBtn)
                .enableAutoTextPosition()
                .title(getString(R.string.guide_add_case))
                .build()

            val recyclerItemCase = FancyShowCaseView.Builder(this)
                .backgroundColor(R.color.white75)
                .focusShape(FocusShape.ROUNDED_RECTANGLE)
                .focusOn(recyclerView[0])
                .enableAutoTextPosition()
                .title(getString(R.string.guide_recycler_item))
                .build()

            val recyclerItemEditCase = FancyShowCaseView.Builder(this)
                .backgroundColor(R.color.white75)
                .focusShape(FocusShape.ROUNDED_RECTANGLE)
                .focusOn(recyclerView[0])
                .enableAutoTextPosition()
                .title(getString(R.string.guide_recycler_item_edit))
                .build()

            val recyclerItemDeleteCase = FancyShowCaseView.Builder(this)
                .backgroundColor(R.color.white75)
                .focusShape(FocusShape.ROUNDED_RECTANGLE)
                .focusOn(recyclerView[0])
                .enableAutoTextPosition()
                .title(getString(R.string.guide_recycler_delete))
                .build()

            val doubleTapCase = FancyShowCaseView.Builder(this)
                .backgroundColor(R.color.white75)
                .focusShape(FocusShape.ROUNDED_RECTANGLE)
                .focusOn(overlayView)
                .enableAutoTextPosition()
                .title(getString(R.string.guide_doubletap))
                .build()

            val resizeCase = FancyShowCaseView.Builder(this)
                .backgroundColor(R.color.white75)
                .focusShape(FocusShape.ROUNDED_RECTANGLE)
                .focusOn(overlayView)
                .enableAutoTextPosition()
                .title(getString(R.string.guide_resize))
                .build()

            val saveCase = FancyShowCaseView.Builder(this)
                .backgroundColor(R.color.white75)
                .focusOn(saveBtn)
                .enableAutoTextPosition()
                .title(getString(R.string.guide_save))
                .build()

            FancyShowCaseQueue().apply {
                completeListener = object : OnCompleteListener {
                    override fun onComplete() {
                        if (dummy) {
                            dummy = false
                            overlayViewAdapter.submitList(emptyList())
                        }
                    }

                }
            }
                .add(addCase)
                .add(recyclerItemCase)
                .add(recyclerItemEditCase)
                .add(recyclerItemDeleteCase)
                .add(doubleTapCase)
                .add(resizeCase)
                .add(saveCase)
                .show()
        }, App.GUIDE_SHOW_DELAY)
    }

    override fun finishOnError() {
        stopLoadingView()
        Toast.makeText(this, getString(R.string.some_error), LENGTH_SHORT).show()
        finish()
    }

//    override fun videoPlay(baseImageViews: List<OverlayText>?, isVideoPlaying: Boolean) {
//        videoControlsView.videoPlay(baseImageViews, isVideoPlaying)
//    }

    override fun updatePlayback(
        overlays: List<OverlayText>?,
        selectedOverlay: OverlayText?,
        isEdit: Boolean,
        isPlaying: Boolean
    ) {
        videoControlsView.updatePlayback(overlays, selectedOverlay, isEdit, isPlaying)
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

    override fun recoverView() {
        videoControlsView.reset()
    }

    override fun onOverlaysChangedListViewNotifier(overlays: List<OverlayText>) {
        // update recycler view
        overlayViewAdapter.submitList(overlays.map {
            OverlayDataMapper(
                it.startTime,
                it.endTime,
                it.timestamp,
                it.text!!
            )
        })
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

    companion object {
        private val TAG = VideoEditActivity::class.java.simpleName
        private const val IGNORE_TIME_CLICK_EVENT = 500
        const val VIDEO_OUTPUT_URI_INTENT = "VIDEO_OUTPUT_URI"
        private var PRIVATE_MODE = 0
        private const val PREF_NAME_GUIDE_SHOWN = "GUIDED_TOUR_897er9XX"
        private const val PREF_NAME_GUIDE_DESELECT_ITEM = "GUIDE_DESELECT_ITEM_0934i23d"
    }
}