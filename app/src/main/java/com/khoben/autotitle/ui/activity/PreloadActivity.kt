package com.khoben.autotitle.ui.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.edit
import com.khoben.autotitle.App.Companion.VIDEO_LOAD_MODE
import com.khoben.autotitle.App.Companion.VIDEO_SOURCE_URI_INTENT
import com.khoben.autotitle.R
import com.khoben.autotitle.databinding.ActivityPreloadVideoBinding
import com.khoben.autotitle.model.LanguageItem
import com.khoben.autotitle.model.VideoLoadMode
import com.khoben.autotitle.mvp.presenter.PreloadActivityPresenter
import com.khoben.autotitle.mvp.view.PreloadActivityView
import com.khoben.autotitle.repository.LocalAssetLanguageRepository
import com.khoben.autotitle.viewmodel.LanguageRepositoryViewModelFactory
import com.khoben.autotitle.viewmodel.LanguageSelectorViewModel
import com.minibugdev.sheetselection.SheetSelection
import com.minibugdev.sheetselection.SheetSelectionItem
import moxy.MvpAppCompatActivity
import moxy.presenter.InjectPresenter


class PreloadActivity : MvpAppCompatActivity(), PreloadActivityView {
    @InjectPresenter
    lateinit var presenter: PreloadActivityPresenter

    private val languageSelectionModel: LanguageSelectorViewModel by viewModels {
        LanguageRepositoryViewModelFactory(
            LocalAssetLanguageRepository.getInstance()
        )
    }

    private var selectedIndex: Int? = null
    private var languageItems: List<LanguageItem>? = null

    private lateinit var binding: ActivityPreloadVideoBinding

    private val userLastSelection by lazy {
        getSharedPreferences(
            USER_LAST_SELECTION_PREF,
            Context.MODE_PRIVATE
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPreloadVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val sourceVideoUri = intent.getParcelableExtra<Uri>(VIDEO_SOURCE_URI_INTENT)
        binding.videoPreloadView.player = presenter.initNewPlayer()
        presenter.init(sourceVideoUri!!)
        binding.startVideoCaption.setOnClickListener { startVideoCaption(sourceVideoUri) }
        binding.selectLanguage.setLabel(getString(R.string.language_selection_title))

        languageSelectionModel.getSelectedItem().observe(this, {
            selectedIndex = it
            if (languageItems != null && selectedIndex != null) {
                binding.selectLanguage.setSelectedValue(languageItems!![selectedIndex!!].value)
            }
        })
        languageSelectionModel.getItems().observe(this, {
            languageItems = it
            // set last selected item on first load
            if (languageItems != null && selectedIndex != null) {
                binding.selectLanguage.setSelectedValue(languageItems!![selectedIndex!!].value)
            }
        })

        binding.selectLanguage.setOnClickListener {
            languageItems?.let {
                showBottomSheetSelection(
                    items = it,
                    selectedIndex = selectedIndex
                )
            }
        }

        binding.switchAutodetect.setOnCheckedChangeListener { _, isChecked ->
            binding.selectLanguage.toggleCollapseExpand(isChecked)
        }
        binding.backBtn.setOnClickListener { finish() }

        checkUserLastPrefs()
    }

    private fun showBottomSheetSelection(
        items: List<LanguageItem>,
        selectedIndex: Int? = null
    ) {
        SheetSelection.Builder(this)
            .title(getString(R.string.language_selection_title))
            .items(items.map { SheetSelectionItem(it.key, it.value, it.icon) })
            .showDraggedIndicator(true)
            .also { builder ->
                selectedIndex?.let {
                    builder.selectedPosition(it)
                }
            }
            .searchEnabled(true)
            .searchNotFoundText(getString(R.string.language_selection_search_nothing))
            .onItemClickListener { _, position ->
                languageSelectionModel.setSelected(position)
            }.show()
    }

    private fun checkUserLastPrefs() {
        val selection = userLastSelection.getBoolean(USER_LAST_SELECTION_AUTO, false)
        binding.switchAutodetect.isChecked = selection
        binding.selectLanguage.setVisibility(selection)
        userLastSelection.getInt(USER_LAST_SELECTION_LANGUAGE, -1).also {
            if (it > -1) {
                languageSelectionModel.setSelected(it)
            }
        }
    }


    private fun startVideoCaption(uri: Uri) {
        val mode =
            if (binding.switchAutodetect.isChecked) VideoLoadMode.AUTO_DETECT
            else VideoLoadMode.NO_DETECT
        // language not selected
        if (selectedIndex == null && mode == VideoLoadMode.AUTO_DETECT) {
            binding.selectLanguage.errorLanguageSelection()
            Toast.makeText(
                this,
                getString(R.string.language_selection_please_select),
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        finish()
        startActivity(Intent(this, VideoEditActivity::class.java).apply {
            putExtra(VIDEO_SOURCE_URI_INTENT, uri)
            putExtra(VIDEO_LOAD_MODE, mode)
        })
    }

    override fun onDestroy() {
        presenter.releasePlayer()
        super.onDestroy()
    }

    override fun onPause() {
        presenter.pause()
        userLastSelection.edit {
            putBoolean(
                USER_LAST_SELECTION_AUTO,
                binding.switchAutodetect.isChecked
            )
            selectedIndex?.let { putInt(USER_LAST_SELECTION_LANGUAGE, it) }
        }
        super.onPause()
    }

    companion object {
        private const val USER_LAST_SELECTION_PREF = "USER_LAST_SELECTION_PREF"
        private const val USER_LAST_SELECTION_AUTO = "USER_LAST_SELECTION_AUTO"
        private const val USER_LAST_SELECTION_LANGUAGE = "USER_LAST_SELECTION_LANGUAGE"
    }
}