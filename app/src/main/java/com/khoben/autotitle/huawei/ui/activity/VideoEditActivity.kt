package com.khoben.autotitle.huawei.ui.activity

import android.app.AlertDialog
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
import android.widget.*
import android.widget.Toast.LENGTH_SHORT
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.khoben.autotitle.huawei.R
import com.khoben.autotitle.huawei.databinding.ActivityVideoBinding
import com.khoben.autotitle.huawei.mvp.presenter.VideoEditActivityPresenter
import com.khoben.autotitle.huawei.mvp.view.VideoEditActivityView
import com.khoben.autotitle.huawei.ui.activity.MainActivity.Companion.VIDEO_SOURCE_URI_INTENT
import com.khoben.autotitle.huawei.ui.overlay.OverlayDataMapper
import com.khoben.autotitle.huawei.ui.overlay.OverlayText
import com.khoben.autotitle.huawei.ui.player.VideoControlsView
import com.khoben.autotitle.huawei.ui.player.VideoSurfaceView
import com.khoben.autotitle.huawei.ui.recyclerview.ListItemEventListener
import com.khoben.autotitle.huawei.ui.recyclerview.OverlayViewAdapter
import com.khoben.autotitle.huawei.ui.recyclerview.RecyclerViewClickListener
import kotlinx.android.synthetic.main.activity_video.*
import me.toptas.fancyshowcase.FancyShowCaseQueue
import me.toptas.fancyshowcase.FancyShowCaseView
import moxy.MvpAppCompatActivity
import moxy.presenter.InjectPresenter


class VideoEditActivity : MvpAppCompatActivity(),
    VideoEditActivityView,
    ListItemEventListener {

    @InjectPresenter
    lateinit var presenter: VideoEditActivityPresenter

    private var sourceVideoUri: Uri? = null

    private var videoSurfaceView: VideoSurfaceView? = null
    private var videoControlsView: VideoControlsView? = null

    private lateinit var saveBtn: Button
    private lateinit var overlayView: RelativeLayout
    private lateinit var popUpLoadingContainer: FrameLayout
    private lateinit var popUpLoadingContainerText: TextView

    private lateinit var loadingView: View
    private lateinit var binding: ActivityVideoBinding
    private var adapter: OverlayViewAdapter = OverlayViewAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadingView = binding.loadingView.root

        popUpLoadingContainer = binding.popuploader.root
        popUpLoadingContainerText = binding.popuploader.popVideoPercentTv
        videoControlsView = binding.llEditSeekbar
        videoSurfaceView = binding.videolayer.videoPreview
        overlayView = binding.videolayer.overlaysRoot

        sourceVideoUri = intent.getParcelableExtra(VIDEO_SOURCE_URI_INTENT)

        presenter.initEditor(this, overlayView, videoControlsView!!)

        overlayView.setOnClickListener { onViewClicked(it) }
        binding.ivBack.setOnClickListener { onViewClicked(it) }
        binding.idEmptyView.addCaptionRecycler.setOnClickListener { onViewClicked(it) }
        videoControlsView!!.llPlayVideoView!!.setOnClickListener { onViewClicked(it) }
        saveBtn = binding.editVideoNextTv
        saveBtn.setOnClickListener { onViewClicked(it) }

        initRecycler(binding.idEmptyView.root)

        // video init in presenter@onFirstViewAttach
        presenter.setUri(sourceVideoUri!!)
    }

    private fun initGuide() {

        val addCase = FancyShowCaseView.Builder(this)
            .backgroundColor(R.color.exo_white_opacity_70)
            .focusOn(videoControlsView!!.videoSeekBarView!!.imageList as View)
            .enableAutoTextPosition()
            .title(getString(R.string.guide_add_case))
            .build()

        val recyclerItemCase = FancyShowCaseView.Builder(this)
            .backgroundColor(R.color.exo_white_opacity_70)
            .focusOn(recyclerview)
            .enableAutoTextPosition()
            .title(getString(R.string.guide_recycler_item))
            .build()

        val recyclerItemEditCase = FancyShowCaseView.Builder(this)
            .backgroundColor(R.color.exo_white_opacity_70)
            .focusOn(recyclerview)
            .enableAutoTextPosition()
            .title(getString(R.string.guide_recycler_item_edit))
            .build()

        val doubleTapCase = FancyShowCaseView.Builder(this)
            .backgroundColor(R.color.exo_white_opacity_70)
            .focusOn(overlayView)
            .enableAutoTextPosition()
            .title(getString(R.string.guide_doubletap))
            .build()

        val resizeCase = FancyShowCaseView.Builder(this)
            .backgroundColor(R.color.exo_white_opacity_70)
            .focusOn(overlayView)
            .enableAutoTextPosition()
            .title(getString(R.string.guide_resize))
            .build()

        val saveCase = FancyShowCaseView.Builder(this)
            .backgroundColor(R.color.exo_white_opacity_70)
            .focusOn(saveBtn)
            .enableAutoTextPosition()
            .title(getString(R.string.guide_save))
            .build()

        FancyShowCaseQueue()
            .add(addCase)
            .add(recyclerItemCase)
            .add(recyclerItemEditCase)
            .add(doubleTapCase)
            .add(resizeCase)
            .add(saveCase)
            .show()
    }

    private fun initRecycler(emptyView: View) {
        recyclerview.also {
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
            it.adapter = adapter
            it.setEmptyView(emptyView)
        }

        recyclerview.addOnItemTouchListener(
            RecyclerViewClickListener(
                this,
                recyclerview,
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
        );
        adapter.listItemEventListener = this
    }

    override fun initVideoContainerLayoutParams() {
        val videoDetails = presenter.getVideoDetails()!!
        Log.d(TAG, "Video details = $videoDetails")
        videoControlsView!!.setMediaDuration(videoDetails.duration)
        if (videoDetails.rotation == 0
            && videoDetails.width > videoDetails.height
            || videoDetails.rotation == 180 && videoDetails.width > videoDetails.height

        ) {

            videoSurfaceView!!.setVideoSize(videoDetails.width, videoDetails.height)
            Log.d(TAG, "Horizontal video")
        } else {
            // rotated
            if (videoDetails.rotation == 0) {
                videoSurfaceView!!.setVideoSize(videoDetails.width, videoDetails.height)
            } else {
                videoSurfaceView!!.setVideoSize(videoDetails.height, videoDetails.width)
            }
            Log.d(TAG, "Vertical video")
        }

        videoSurfaceView!!.post {
            val width: Int = videoSurfaceView!!.measuredWidth
            val height: Int = videoSurfaceView!!.measuredHeight

            val layoutParams: ViewGroup.LayoutParams = overlayView.layoutParams
            layoutParams.width = width
            layoutParams.height = height

            overlayView.layoutParams = layoutParams
        }
    }

    private var lastTimeOnViewClicked: Long = 0
    private fun onViewClicked(view: View) {
        val clickTime = System.currentTimeMillis()
        if (clickTime - lastTimeOnViewClicked < IGNORE_TIME_CLICK_EVENT
            || popUpLoadingContainer.visibility == View.VISIBLE
        ) {
            return
        }
        lastTimeOnViewClicked = clickTime
        when (view.id) {
            R.id.overlays_root -> {
                presenter.unEditable()
            }
            R.id.iv_back -> {
                onBackPressed()
            }
            R.id.add_caption_recycler -> {
                presenter.addOverlayAtCurrentPosition()
            }
            R.id.edit_video_next_tv -> {
                presenter.saveVideo(this, Pair(videoSurfaceView!!.width, videoSurfaceView!!.height))
            }
            R.id.ll_play_video -> {
                presenter.togglePlayState()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        presenter.setPlayState(false)
    }

    override fun onDestroy() {
        super.onDestroy()
        videoSurfaceView?.onDestroy()
        System.gc()
    }

    override fun onBackPressed() {
        val func: () -> Unit
        val text: String
        if (popUpLoadingContainer.visibility == View.VISIBLE) {
            func = { presenter.cancelSavingVideo() }
            text = getString(R.string.cancel_video_saving_question)
        } else {
            func = { onSuperBackPressed() }
            text = getString(R.string.exit_question)
        }
        AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_DARK).setMessage(text)
            .setCancelable(false)
            .setPositiveButton(getString(R.string.yes_caption)) { dialog, id ->
                func()
            }
            .setNegativeButton(getString(R.string.no_caption)) { dialog, id ->
                dialog.cancel()
            }
            .create()
            .show()
    }

    private fun onSuperBackPressed() {
        super.onBackPressed()
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
    }

    override fun stopLoading() {
        loadingView.visibility = View.GONE
    }

    override fun onErrorThumbnailsProcessing(e: Throwable) {
        Log.e(TAG, e.toString())
        // TODO("HIDE ANIMATION")
        stopLoading()
    }

    override fun onThumbnailsProcessed(thumbnails: List<Bitmap>, frameTime: Long) {
        runOnUiThread {
            Log.d(TAG, "Video processed")
            videoControlsView?.addFramesToSeekBar(thumbnails, frameTime)
            val sharedPref: SharedPreferences =
                getSharedPreferences(PREF_NAME_GUIDE_SHOWN, PRIVATE_MODE)
            if (!sharedPref.getBoolean(PREF_NAME_GUIDE_SHOWN, false)) {
                // run guide
                Handler(Looper.getMainLooper()).postDelayed({
                    initGuide()
                }, 500)
                val editor = sharedPref.edit()
                editor.putBoolean(PREF_NAME_GUIDE_SHOWN, true)
                editor.apply()
            } else {
                Log.d(TAG, "Was shown")
            }
            // TODO("HIDE ANIMATION")
            stopLoading()
        }
    }

    override fun finishOnError() {
        // TODO("HIDE ANIMATION")
        stopLoading()
        Toast.makeText(this, getString(R.string.some_error), LENGTH_SHORT).show()
        finish()
    }

    override fun videoPlay(baseImageViews: List<OverlayText>?, isVideoPlaying: Boolean) {
        videoControlsView!!.videoPlay(baseImageViews, isVideoPlaying)
    }

    override fun drawOverlayTimeRange(
        overlays: List<OverlayText>?,
        selectedOverlay: OverlayText?,
        isEdit: Boolean
    ) {
        videoControlsView!!.drawOverlayTimeRange(overlays, selectedOverlay, isEdit)
    }

    override fun recoverView() {
        videoControlsView!!.setToDefaultState()
    }

    override fun onOverlaysChangedListViewNotifier(overlays: List<OverlayText>) {
        // update recycler view
        adapter.submitList(overlays.map {
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
            popUpLoadingContainer.visibility = View.VISIBLE
        }
    }

    override fun onVideoSavingCancelled() {
        runOnUiThread {
            popUpLoadingContainer.visibility = View.GONE
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
            popUpLoadingContainer.visibility = View.GONE
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
            popUpLoadingContainerText.text = getString(R.string.percent_text, percent.toString())
        }
    }

    private fun runFinishVideoView(filepath: String) {
        val intent = Intent(this, ResultViewActivity::class.java).apply {
            putExtra(VIDEO_OUTPUT_URI_INTENT, filepath)
        }
        startActivity(intent)
    }

    override fun onVideoSavingComplete(filepath: String) {
        Log.d(TAG, "Saved video with path=$filepath")
        runOnUiThread {
            popUpLoadingContainerText.text = getString(R.string.percent_text, "0")
            popUpLoadingContainer.visibility = View.GONE
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
        videoControlsView!!.setToState(time)
    }

    override fun onClickedAddBelow(item: Int) {
        Log.d(TAG, "Clicked on $item")
        presenter.addOverlayAtSpecificPosition(item)

    }

    companion object {
        private val TAG = VideoEditActivity::class.java.simpleName
        private const val IGNORE_TIME_CLICK_EVENT = 500
        const val VIDEO_OUTPUT_URI_INTENT = "VIDEO_OUTPUT_URI"
        private var PRIVATE_MODE = 0
        private const val PREF_NAME_GUIDE_SHOWN = "GUIDED_TOUR_897er9XX"
        var temporaryFixViewStateAccess: VideoEditActivity? = null
    }
}