package com.khoben.autotitle.ui.activity

import android.Manifest
import android.animation.ValueAnimator
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Toast
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.khoben.autotitle.App.Companion.VIDEO_SOURCE_URI_INTENT
import com.khoben.autotitle.R
import com.khoben.autotitle.common.OpeningVideoFileState
import com.khoben.autotitle.database.entity.Project
import com.khoben.autotitle.databinding.ActivityMainBinding
import com.khoben.autotitle.extension.activityresult.permission.permissionsDSL
import com.khoben.autotitle.extension.activityresult.result.getContentDSL
import com.khoben.autotitle.extension.activityresult.result.takeVideoDSL
import com.khoben.autotitle.mvp.presenter.MainActivityPresenter
import com.khoben.autotitle.mvp.view.MainActivityView
import com.khoben.autotitle.ui.popup.projectitem.ProjectItemOptionsDialog
import com.khoben.autotitle.ui.popup.projectitem.ProjectTitleEditDialog
import com.khoben.autotitle.ui.recyclerview.projects.ProjectViewListAdapter
import com.khoben.autotitle.viewmodel.ProjectViewModel
import com.khoben.autotitle.viewmodel.ProjectViewModelFactory
import kotlinx.coroutines.launch
import moxy.MvpAppCompatActivity
import moxy.presenter.InjectPresenter
import timber.log.Timber


class MainActivity : MvpAppCompatActivity(), MainActivityView,
    ProjectItemOptionsDialog.ItemClickListener,
    ProjectTitleEditDialog.ItemTitleEditListener {

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

    private val takeVideoWithPermission = permissionsDSL {
        allGranted = {
            // unused input but required
            takeVideoActivityResult.launch(null)
        }
        denied = {
            // denied
        }
        explained = {
            // explained
        }
    }

    private lateinit var binding: ActivityMainBinding
    private val recyclerViewAdapter = ProjectViewListAdapter()

    private val projectViewModel: ProjectViewModel by viewModels {
        ProjectViewModelFactory(applicationContext)
    }

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
    }

    private var guidelineInitialPercent = -1F
    private fun hideRecentProjectAnim() {
        binding.recentTitle.isVisible = false
        binding.recentBtnEdit.isVisible = false
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
        binding.recentBtnEdit.isVisible = false
        binding.guideline1.setGuidelinePercent(1F)
    }

    override fun showRecentProject() {
        binding.recentTitle.isVisible = true
        binding.recentBtnEdit.isVisible = true
        binding.guideline1.setGuidelinePercent(guidelineInitialPercent)
    }

    override fun submitList(list: List<Project>) {
        recyclerViewAdapter.submitList(ArrayList(list))
    }

    override fun showEditTitleFragment(id: Long, title: String) {
        supportFragmentManager.let {
            ProjectTitleEditDialog.show(id, title).apply {
                show(it, "edit_title_fragment")
            }
        }
    }

    private fun settingsClick(view: View) {
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    private fun takeVideoClick(view: View) {
        takeVideoWithPermission.launch(arrayOf(Manifest.permission.CAMERA))
    }

    private fun getContentClick(view: View) {
        getContentActivityResult.launch(VIDEO_FILE_SELECT_TYPE)
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

    override fun onRemoveClick(id: Long) {
        lifecycleScope.launch {
            projectViewModel.deleteById(id)
        }
    }

    override fun onEditedItem(id: Long, title: String) {
        lifecycleScope.launch {
            projectViewModel.updateTitle(id, title)
        }
    }

    companion object {
        private const val VIDEO_FILE_SELECT_TYPE = "video/*"
    }
}