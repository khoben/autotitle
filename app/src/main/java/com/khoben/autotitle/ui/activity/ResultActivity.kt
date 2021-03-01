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
import com.khoben.autotitle.ui.popup.AlertDialogInfoMessage
import com.khoben.autotitle.ui.popup.CustomAlertDialog
import de.mustafagercek.materialloadingbutton.LoadingButton
import moxy.MvpAppCompatActivity
import moxy.presenter.InjectPresenter

class ResultActivity : MvpAppCompatActivity(), ResultActivityView,
    CustomAlertDialog.DialogClickListener {

    @InjectPresenter
    lateinit var presenter: ResultViewActivityPresenter

    private var videoPath: String? = null

    private lateinit var saveBtn: LoadingButton

    private lateinit var permissionManager: PermissionManager
    private val writeStoragePermissionToken = "storage"
    private val rationaleStorageDialogToken = "alert_rationale_storage"
    private val backPressedDialogToken = "backpressed_dialog"
    private val homebtnDialogToken = "homebtn_dialog"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityPostVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        videoPath = intent.getStringExtra(VIDEO_OUTPUT_URI_INTENT)
        binding.backBtn.setOnClickListener { onBackPressed() }
        binding.homeBtn.setOnClickListener { homeBtnClicked() }
        binding.shareBtn.setOnClickListener { shareVideo() }
        saveBtn = binding.saveGalleryBtn.apply { setButtonOnClickListener { saveVideo() } }
        binding.epVideoView.player = presenter.initNewPlayer()
        presenter.init(videoPath!!)

        permissionManager = PermissionManager(this)
        permissionManager.register(
            writeStoragePermissionToken,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE),
            {
                AlertDialogInfoMessage.new(
                    getString(R.string.app_needs_permission),
                    getString(R.string.permission_message_storage_alert)
                ).show(supportFragmentManager, AlertDialogInfoMessage.TAG)
            },
            {
                CustomAlertDialog.Builder()
                    .setPositive(getString(R.string.settings_caption))
                    .setNeutral(getString(R.string.cancel_caption))
                    .build(
                        getString(R.string.app_needs_permission),
                        getString(R.string.permission_message_storage_alert) +
                                "\n" +
                                getString(R.string.permission_message_explained),
                        rationaleStorageDialogToken
                    ).show(supportFragmentManager, CustomAlertDialog.TAG)
            })
    }

    private fun saveVideo() {
        permissionManager.runWithPermission(writeStoragePermissionToken) { presenter.save() }
    }

    private fun shareVideo() {
        presenter.share(this)
    }

    private fun homeBtnClicked() {
        CustomAlertDialog.Builder()
            .setPositive(getString(R.string.yes_caption))
            .setNegative(getString(R.string.cancel_caption))
            .build(
                getString(R.string.confirm_exit),
                getString(R.string.return_to_home_screen_question),
                homebtnDialogToken
            )
            .show(supportFragmentManager, CustomAlertDialog.TAG)
    }

    private fun toMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finishAffinity()
    }

    override fun onDestroy() {
        presenter.releasePlayer()
        permissionManager.release()
        super.onDestroy()
    }

    override fun onPause() {
        presenter.pause()
        super.onPause()
    }

    override fun onBackPressed() {
        CustomAlertDialog.Builder()
            .setPositive(getString(R.string.yes_caption))
            .setNegative(getString(R.string.cancel_caption))
            .build(
                getString(R.string.confirm_exit),
                getString(R.string.return_to_video_edit_question),
                backPressedDialogToken
            )
            .show(supportFragmentManager, CustomAlertDialog.TAG)
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

    override fun dialogOnNegative(token: String) {}

    override fun dialogOnPositive(token: String) {
        when (token) {
            rationaleStorageDialogToken -> {
                startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", packageName, null)
                })
            }
            backPressedDialogToken -> {
                finish()
            }
            homebtnDialogToken -> {
                toMainActivity()
            }
        }
    }

    override fun dialogOnNeutral(token: String) {}
}