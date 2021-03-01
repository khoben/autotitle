package com.khoben.autotitle.ui.activity

import android.Manifest
import android.animation.ValueAnimator
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.Html
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Toast
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.huawei.agconnect.crash.AGConnectCrash
import com.khoben.autotitle.App.Companion.VIDEO_EXIST_PROJECT
import com.khoben.autotitle.App.Companion.VIDEO_LOAD_MODE
import com.khoben.autotitle.App.Companion.VIDEO_SOURCE_URI_INTENT
import com.khoben.autotitle.R
import com.khoben.autotitle.model.OpeningVideoFileState
import com.khoben.autotitle.common.PermissionManager
import com.khoben.autotitle.database.entity.Project
import com.khoben.autotitle.databinding.ActivityMainBinding
import com.khoben.autotitle.extension.activityresult.result.getContentDSL
import com.khoben.autotitle.extension.activityresult.result.takeVideoDSL
import com.khoben.autotitle.model.VideoLoadMode
import com.khoben.autotitle.mvp.presenter.MainActivityPresenter
import com.khoben.autotitle.mvp.view.MainActivityView
import com.khoben.autotitle.ui.popup.AlertDialogInfoMessage
import com.khoben.autotitle.ui.popup.CustomAlertDialog
import com.khoben.autotitle.ui.popup.projectitem.ProjectItemOptionsDialog
import com.khoben.autotitle.ui.popup.projectitem.ProjectTitleEditDialog
import com.khoben.autotitle.ui.recyclerview.projects.ProjectViewListAdapter
import com.khoben.autotitle.viewmodel.ProjectViewModel
import com.khoben.autotitle.viewmodel.ProjectViewModelFactory
import kotlinx.coroutines.launch
import moxy.MvpAppCompatActivity
import moxy.presenter.InjectPresenter
import timber.log.Timber
import java.io.File


class MainActivity : MvpAppCompatActivity(), MainActivityView,
    ProjectItemOptionsDialog.ItemClickListener,
    ProjectTitleEditDialog.ItemTitleEditListener,
    ProjectViewListAdapter.OnItemClickListener,
    CustomAlertDialog.DialogClickListener {

    @InjectPresenter
    lateinit var presenter: MainActivityPresenter

    private val getContentActivityResult = getContentDSL {
        success = { uri ->
            onVideoSelected(uri as Uri)
        }
        error = { message ->
            Timber.e("$message")
        }
    }

    private val takeVideoActivityResult = takeVideoDSL {
        success = { uri ->
            onVideoSelected(uri as Uri)
        }
        error = { message ->
            Timber.e("$message")
        }
    }

    private lateinit var binding: ActivityMainBinding
    private val recyclerViewAdapter = ProjectViewListAdapter().apply {
        clickListener = this@MainActivity
    }

    private val projectViewModel: ProjectViewModel by viewModels {
        ProjectViewModelFactory(applicationContext)
    }

    private lateinit var permissionManager: PermissionManager
    private val readStoragePermissionToken = "storage"
    private val takeVideoPermissionToken = "take_video"

    private val storageRationaleDialogToken = "storage_rationale_dialog"
    private val cameraRationaleDialogToken = "camera_rationale_dialog"
    private val deleteProjectDialogToken = "delete_project_dialog"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        projectViewModel.projectList.observe(this, { projects ->
            submitList(projects)
        })

        binding.recentRecycler.adapter = recyclerViewAdapter.apply {
            registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
                override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                    super.onItemRangeRemoved(positionStart, itemCount)
                    if (recyclerViewAdapter.itemCount < 1) {
                        hideRecentProjectAnim()
                    }
                }

                override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                    super.onItemRangeInserted(positionStart, itemCount)
                    // emits nothing from LiveData
                    if (positionStart == 0 && itemCount == 0) {
                        hideRecentProject()
                    } else {
                        showRecentProject()
                    }
                }
            })
        }

        binding.settingsBtn.setOnClickListener { settingsClick(it) }
        binding.cameraCaptureButton.setOnClickListener { takeVideoClick(it) }
        binding.filestoreLoadButton.setOnClickListener { getContentClick(it) }

        guidelineInitialPercent =
            (binding.guideline1.layoutParams as ConstraintLayout.LayoutParams).guidePercent

        permissionManager = PermissionManager(this)
        permissionManager.register(takeVideoPermissionToken, arrayOf(Manifest.permission.CAMERA), {
            AlertDialogInfoMessage.new(
                getString(R.string.app_needs_permission),
                getString(R.string.permission_message_camera_alert)
            ).show(supportFragmentManager, AlertDialogInfoMessage.TAG)
        }, {
            CustomAlertDialog.Builder()
                .setPositive(getString(R.string.settings_caption))
                .setNeutral(getString(R.string.cancel_caption))
                .build(
                    getString(R.string.app_needs_permission),
                    getString(R.string.permission_message_camera_alert) +
                            "\n" +
                            getString(R.string.permission_message_explained),
                    cameraRationaleDialogToken
                ).show(supportFragmentManager, CustomAlertDialog.TAG)
        })

        permissionManager.register(
            readStoragePermissionToken,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
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
                        storageRationaleDialogToken
                    ).show(supportFragmentManager, CustomAlertDialog.TAG)
            })
    }

    override fun dialogOnNegative(token: String) {}

    override fun dialogOnNeutral(token: String) {}

    override fun dialogOnPositive(token: String) {
        when (token) {
            cameraRationaleDialogToken, storageRationaleDialogToken -> {
                startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", packageName, null)
                })
            }
            deleteProjectDialogToken -> {
                lifecycleScope.launch {
                    projectViewModel.deleteById(projectViewModel.currentProject!!.id)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        permissionManager.release()
    }

    /**
     * Initial top horizontal guideline of 'recent project' block
     */
    private var guidelineInitialPercent = -1F

    private fun hideRecentProjectAnim() {
        binding.recentTitle.isVisible = false
//        binding.recentBtnEdit.isVisible = false
        ValueAnimator.ofFloat(guidelineInitialPercent, 1F).apply {
            duration = 1000
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { valueAnimator ->
                binding.guideline1.setGuidelinePercent(valueAnimator.animatedValue as Float)
            }
        }.start()
    }

    override fun hideRecentProject() {
        binding.recentTitle.isVisible = false
//        binding.recentBtnEdit.isVisible = false
        binding.guideline1.setGuidelinePercent(1F)
    }

    override fun showRecentProject() {
        binding.recentTitle.isVisible = true
//        binding.recentBtnEdit.isVisible = true
        binding.guideline1.setGuidelinePercent(guidelineInitialPercent)
    }

    override fun submitList(list: List<Project>) {
        recyclerViewAdapter.submitList(ArrayList(list))
    }

    override fun showEditTitleFragment(id: Long, title: String) {
        supportFragmentManager.let {
            ProjectTitleEditDialog.show(id, title).apply {
                show(it, ProjectTitleEditDialog.TAG)
            }
        }
    }

    private fun settingsClick(view: View) {
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    private fun takeVideoClick(view: View) {
        permissionManager.runWithPermission(takeVideoPermissionToken) {
            takeVideoActivityResult.launch(null)
        }
    }

    private fun getContentClick(view: View) {
        permissionManager.runWithPermission(readStoragePermissionToken) {
            getContentActivityResult.launch(VIDEO_FILE_SELECT_TYPE)
        }
    }

    override fun onVideoSelected(uri: Uri) {
        when (presenter.verifyMedia(uri)) {
            OpeningVideoFileState.FAILED -> {
                Toast.makeText(
                    this,
                    getString(R.string.error_while_opening_file),
                    Toast.LENGTH_SHORT
                ).show()
            }
            OpeningVideoFileState.LIMIT -> {
                Toast.makeText(this, getString(R.string.check_limit), Toast.LENGTH_SHORT).show()
            }
            OpeningVideoFileState.SUCCESS -> {
                val intent = Intent(this, PreloadActivity::class.java).apply {
                    putExtra(VIDEO_SOURCE_URI_INTENT, uri)
                }
                startActivity(intent)
            }
        }
    }

    override fun onEditTitleClick(id: Long) {
        lifecycleScope.launch {
            projectViewModel.getById(id).title.let { title ->
                showEditTitleFragment(id, title)
            }
        }
    }

    private fun fromHtml(html: String): CharSequence {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            Html.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(html)
        }
    }

    override fun onRemoveClick(id: Long) {
        lifecycleScope.launch {
            projectViewModel.getById(id).let { project ->
                projectViewModel.currentProject = project
                CustomAlertDialog.Builder()
                    .setPositive(getString(R.string.yes_caption))
                    .setNegative(getString(R.string.no_caption))
                    .build(
                        getString(R.string.delete_title),
                        fromHtml(
                            getString(
                                R.string.delete_project_dialog_text,
                                project.title
                            )
                        ), deleteProjectDialogToken
                    ).show(supportFragmentManager, CustomAlertDialog.TAG)
            }
        }
    }

    override fun onEditedItem(id: Long, title: String) {
        lifecycleScope.launch {
            projectViewModel.updateTitle(id, title)
        }
    }

    override fun onItemClicked(project: Project) {
        projectViewModel.currentProject = project
        permissionManager.runWithPermission(readStoragePermissionToken) { loadProject() }
    }

    private fun loadProject() {
        val project = projectViewModel.currentProject!!
        val uri = Uri.fromFile(File(project.sourceFileUri))
        when (presenter.verifyMedia(uri)) {
            OpeningVideoFileState.FAILED -> {
                CustomAlertDialog.Builder()
                    .setPositive(getString(R.string.yes_caption))
                    .setNeutral(getString(R.string.cancel_caption))
                    .build(
                        getString(R.string.error_while_opening_file), fromHtml(
                            getString(
                                R.string.delete_project_dialog_text,
                                project.title
                            )
                        ), deleteProjectDialogToken
                    )
                    .show(supportFragmentManager, CustomAlertDialog.TAG)
            }
            OpeningVideoFileState.LIMIT -> {
                AlertDialogInfoMessage.new(
                    getString(R.string.error_while_opening_file),
                    getString(R.string.check_limit)
                ).show(supportFragmentManager, AlertDialogInfoMessage.TAG)
            }
            OpeningVideoFileState.SUCCESS -> {
                val intent = Intent(this, VideoEditActivity::class.java).apply {
                    putExtra(VIDEO_SOURCE_URI_INTENT, uri)
                    putExtra(VIDEO_LOAD_MODE, VideoLoadMode.LOAD_RECENT)
                    putExtra(VIDEO_EXIST_PROJECT, project)
                }
                startActivity(intent)
            }
        }
    }

    companion object {
        private const val VIDEO_FILE_SELECT_TYPE = "video/*"
    }
}