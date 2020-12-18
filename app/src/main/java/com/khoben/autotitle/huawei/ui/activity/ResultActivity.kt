package com.khoben.autotitle.huawei.ui.activity

import android.Manifest
import android.os.Bundle
import android.widget.Toast
import com.khoben.autotitle.huawei.R
import com.khoben.autotitle.huawei.databinding.ActivityPostVideoBinding
import com.khoben.autotitle.huawei.extension.activityresult.permission.permissionsDSL
import com.khoben.autotitle.huawei.mvp.presenter.ResultViewActivityPresenter
import com.khoben.autotitle.huawei.mvp.view.ResultActivityView
import com.khoben.autotitle.huawei.ui.activity.VideoEditActivity.Companion.VIDEO_OUTPUT_URI_INTENT
import moxy.MvpAppCompatActivity
import moxy.presenter.InjectPresenter

class ResultActivity : MvpAppCompatActivity(), ResultActivityView {

    @InjectPresenter
    lateinit var presenter: ResultViewActivityPresenter

    private var videoPath: String? = null

    private val saveResult = permissionsDSL {
        allGranted = {
            presenter.save(this@ResultActivity)
        }
        denied = {
            // denied
        }
        explained = {
            // explained
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityPostVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        videoPath = intent.getStringExtra(VIDEO_OUTPUT_URI_INTENT)
        binding.shareBtn.setOnClickListener {
            shareVideo()
        }
        binding.saveGalleryBtn.setOnClickListener {
            saveVideo()
        }
        binding.finishOkBtn.setOnClickListener {
            finish()
        }
        binding.epVideoView.player = presenter.getPlayer().getPlayerImpl(this)
        presenter.init(videoPath!!)
    }

    private fun saveVideo() {
        saveResult.launch(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE))
    }

    private fun shareVideo() {
        presenter.share(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.deactivate()
        presenter.setMediaSessionState(false)
    }

    override fun onPause() {
        presenter.pause()
        super.onPause()
    }

    override fun showVideoSavedToast(path: String?) {
        Toast.makeText(
            this,
            getString(R.string.saved_path_video, path),
            Toast.LENGTH_LONG
        ).show()
    }
}