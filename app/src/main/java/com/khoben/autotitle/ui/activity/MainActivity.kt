package com.khoben.autotitle.ui.activity

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Toast
import com.khoben.autotitle.App.Companion.VIDEO_EXTENSION
import com.khoben.autotitle.R
import com.khoben.autotitle.common.*
import com.khoben.autotitle.databinding.ActivityMainBinding
import com.khoben.autotitle.extension.activityresult.permission.permissionsDSL
import com.khoben.autotitle.extension.activityresult.result.getContentDSL
import com.khoben.autotitle.extension.activityresult.result.takeVideoDSL
import com.khoben.autotitle.mvp.presenter.MainActivityPresenter
import com.khoben.autotitle.mvp.view.MainActivityView
import com.khoben.autotitle.ui.etc.OpenSourceLicensesDialog
import moxy.MvpAppCompatActivity
import moxy.presenter.InjectPresenter
import timber.log.Timber


class MainActivity : MvpAppCompatActivity(), MainActivityView {

    @InjectPresenter
    lateinit var presenter: MainActivityPresenter

    private val getContentActivityResult = getContentDSL {
        success = { outputPath ->
            onVideoSelected(outputPath as Uri)
        }
        error = { message ->
            Timber.e("$message")
        }
    }

    private var takenVideoUri: Uri? = null
    private val takeVideoActivityResult = takeVideoDSL {
        success = { _ ->
            onVideoSelected(takenVideoUri!!)
        }
        error = { message ->
            Timber.e("$message")
        }
    }

    private val takeVideoWithPermission = permissionsDSL {
        allGranted = {
            takenVideoUri = FileUtils.getRandomUri(
                this@MainActivity, VIDEO_EXTENSION, Environment.DIRECTORY_MOVIES
            )
            takeVideoActivityResult.launch(takenVideoUri)
        }
        denied = {
            // denied
        }
        explained = {
            // explained
        }
    }

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.settingsBtn.setOnClickListener {  settingsClick(it)  }
        binding.cameraCaptureButton.setOnClickListener { takeVideoClick(it) }
        binding.filestoreLoadButton.setOnClickListener { getContentClick(it) }
    }

    override fun hideRecentProject() {
//        binding.recentProjects.visibility = View.INVISIBLE
    }

    private fun settingsClick(view: View) {
        OpenSourceLicensesDialog().showLicenses(this)
    }

    private fun takeVideoClick(view: View) {
        takeVideoWithPermission.launch(arrayOf(Manifest.permission.CAMERA))
    }

    private fun getContentClick(view: View) {
        getContentActivityResult.launch(VIDEO_FILE_SELECT_TYPE)
    }

    override fun onVideoSelected(uri: Uri) {
        when (presenter.verifyMedia(uri)) {
            FAILED -> {
                Toast.makeText(
                    this,
                    getString(R.string.error_while_opening_file),
                    Toast.LENGTH_SHORT
                ).show()
            }
            LIMIT -> {
                Toast.makeText(this, getString(R.string.check_limit), Toast.LENGTH_SHORT).show()
            }
            SUCCESS -> {
                val intent = Intent(this, VideoEditActivity::class.java).apply {
                    putExtra(VIDEO_SOURCE_URI_INTENT, uri)
                }
                startActivity(intent)
            }
        }
    }

    companion object {
        private const val VIDEO_FILE_SELECT_TYPE = "video/*"
        const val VIDEO_SOURCE_URI_INTENT = "com.khoben.autotitle.VIDEO_SOURCE"
    }
}