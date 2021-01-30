package com.khoben.autotitle.ui.activity

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.khoben.autotitle.R
import com.khoben.autotitle.common.PermissionManager
import com.khoben.autotitle.databinding.ActivityPostVideoBinding
import com.khoben.autotitle.mvp.presenter.ResultViewActivityPresenter
import com.khoben.autotitle.mvp.view.ResultActivityView
import com.khoben.autotitle.ui.activity.VideoEditActivity.Companion.VIDEO_OUTPUT_URI_INTENT
import com.khoben.autotitle.ui.popup.CustomAlertDialog
import de.mustafagercek.materialloadingbutton.LoadingButton
import moxy.MvpAppCompatActivity
import moxy.presenter.InjectPresenter
import java.lang.ref.WeakReference

class ResultActivity : MvpAppCompatActivity(), ResultActivityView {

    @InjectPresenter
    lateinit var presenter: ResultViewActivityPresenter

    private var videoPath: String? = null

    private lateinit var saveBtn: LoadingButton

    private val permissionManager = PermissionManager(WeakReference(this))
    private val writeStoragePermissionToken = "storage"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityPostVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        videoPath = intent.getStringExtra(VIDEO_OUTPUT_URI_INTENT)
        binding.backBtn.setOnClickListener { finish() }
        binding.homeBtn.setOnClickListener { toMainActivity() }
        binding.shareBtn.setOnClickListener { shareVideo() }
        saveBtn = binding.saveGalleryBtn.apply { setButtonOnClickListener { saveVideo() } }
        binding.epVideoView.player = presenter.initNewPlayer()
        presenter.init(videoPath!!)

        permissionManager.register(writeStoragePermissionToken, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), {
            CustomAlertDialog(
                context = this,
                layout = R.layout.alert_dialog_single_ok_btn,
                messageTextView = R.id.main_text,
                okButton = R.id.ok_btn,
                okButtonText = getString(R.string.edit_title_ok),
            ).show(getString(R.string.permission_message_storage_alert))
        }, {
            CustomAlertDialog(
                context = this,
                layout = R.layout.alert_dialog_ok_cancel_btn,
                messageTextView = R.id.main_text,
                okButton = R.id.ok_btn,
                okButtonText = getString(R.string.settings_caption),
                cancelButton = R.id.cancel_btn,
                cancelButtonText = getString(R.string.cancel_caption)
            ).show(
                getString(R.string.permission_message_storage_alert) +
                        "\n" +
                        getString(R.string.permission_message_explained),
                {
                    startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", packageName, null)
                    })
                })
        })
    }

    private fun saveVideo() {
        permissionManager.runWithPermission(writeStoragePermissionToken) { presenter.save() }
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
        runOnUiThread {
            Toast.makeText(
                this,
                getString(R.string.saved_path_video, path),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onSaveStarted() {
        runOnUiThread {
            saveBtn.onStartLoading()
        }
    }

    override fun onSavingEnd() {
        runOnUiThread {
            saveBtn.onStopLoading()
            saveBtn.setButtonColor(ContextCompat.getColor(this, R.color.green_color_picker))
            saveBtn.setButtonText(getString(R.string.result_screen_button_title_saved))
        }
    }
}