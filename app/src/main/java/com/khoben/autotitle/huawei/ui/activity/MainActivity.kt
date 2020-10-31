package com.khoben.autotitle.huawei.ui.activity

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Toast
import com.khoben.autotitle.huawei.App.Companion.VIDEO_EXTENSION
import com.khoben.autotitle.huawei.R
import com.khoben.autotitle.huawei.common.FileState
import com.khoben.autotitle.huawei.common.FileUtils
import com.khoben.autotitle.huawei.databinding.ActivityMainBinding
import com.khoben.autotitle.huawei.extension.activityresult.permission.permissionsDSL
import com.khoben.autotitle.huawei.extension.activityresult.result.getContentDSL
import com.khoben.autotitle.huawei.extension.activityresult.result.takeVideoDSL
import com.khoben.autotitle.huawei.mvp.presenter.MainActivityPresenter
import com.khoben.autotitle.huawei.mvp.view.MainActivityView
import moxy.MvpAppCompatActivity
import moxy.presenter.InjectPresenter

class MainActivity : MvpAppCompatActivity(), MainActivityView {

    @InjectPresenter
    lateinit var presenter: MainActivityPresenter

    private val getContentActivityResult = getContentDSL {
        success = { outputPath ->
            onVideoSelected(outputPath as Uri)
        }
        error = { message ->
            Log.e(TAG, "$message")
        }
    }

    private var takenVideoUri: Uri? = null
    private val takeVideoActivityResult = takeVideoDSL {
        success = { _ ->
            onVideoSelected(takenVideoUri!!)
        }
        error = { message ->
            Log.e(TAG, "$message")
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(ActivityMainBinding.inflate(layoutInflater).root)
    }

    fun takeVideoClick(view: View) {
        takeVideoWithPermission.launch(arrayOf(Manifest.permission.CAMERA))
    }

    fun getContentClick(view: View) {
        getContentActivityResult.launch(VIDEO_FILE_SELECT_TYPE)
    }

    override fun onVideoSelected(uri: Uri) {
        when (presenter.verifyMedia(this, uri)) {
            FileState.FAILED -> {
                Toast.makeText(
                    this,
                    getString(R.string.error_while_opening_file),
                    Toast.LENGTH_SHORT
                ).show()
            }
            FileState.LIMIT -> {
                Toast.makeText(this, getString(R.string.check_limit), Toast.LENGTH_SHORT).show()
            }
            FileState.SUCCESS -> {
                val intent = Intent(this, VideoEditActivity::class.java).apply {
                    putExtra(VIDEO_SOURCE_URI_INTENT, uri)
                }
                startActivity(intent)
            }
        }
    }

    companion object {
        private var TAG = MainActivity::class.java.simpleName
        private const val VIDEO_FILE_SELECT_TYPE = "video/*"
        const val VIDEO_SOURCE_URI_INTENT = "com.khoben.autotitle.VIDEO_SOURCE"
    }
}