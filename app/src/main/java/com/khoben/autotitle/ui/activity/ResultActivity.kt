package com.khoben.autotitle.ui.activity

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.khoben.autotitle.R
import com.khoben.autotitle.databinding.ActivityPostVideoBinding
import com.khoben.autotitle.extension.activityresult.permission.permissionsDSL
import com.khoben.autotitle.mvp.presenter.ResultViewActivityPresenter
import com.khoben.autotitle.mvp.view.ResultActivityView
import com.khoben.autotitle.ui.activity.VideoEditActivity.Companion.VIDEO_OUTPUT_URI_INTENT
import moxy.MvpAppCompatActivity
import moxy.presenter.InjectPresenter

class ResultActivity : MvpAppCompatActivity(), ResultActivityView {

    @InjectPresenter
    lateinit var presenter: ResultViewActivityPresenter

    private var videoPath: String? = null

    private lateinit var saveBtn: MaterialButton

    private val saveResult = permissionsDSL {
        allGranted = {
            presenter.save()
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
        binding.backBtn.setOnClickListener {
            finish()
        }
        binding.homeBtn.setOnClickListener {
            toMainActivity()
        }
        binding.shareBtn.setOnClickListener {
            shareVideo()
        }
        saveBtn = binding.saveGalleryBtn
        saveBtn.setOnClickListener {
            saveVideo()
        }
        binding.epVideoView.player = presenter.initNewPlayer()
        presenter.init(videoPath!!)
    }

    private fun saveVideo() {
        saveResult.launch(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE))
    }

    private fun shareVideo() {
        presenter.share(this)
    }

    private fun toMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finishAffinity()
    }

    override fun onDestroy() {
        presenter.releasePlayer()
        super.onDestroy()
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

    override fun alreadySaved() {
        saveBtn.text = getString(R.string.result_screen_button_title_saved)
        saveBtn.setBackgroundColor(ContextCompat.getColor(this, R.color.green_color_picker))
        saveBtn.icon = ContextCompat.getDrawable(this, R.drawable.check_circle_icon_24dp)
    }
}