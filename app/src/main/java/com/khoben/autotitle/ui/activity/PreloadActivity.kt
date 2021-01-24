package com.khoben.autotitle.ui.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.core.content.edit
import com.khoben.autotitle.databinding.ActivityPreloadVideoBinding
import com.khoben.autotitle.model.LanguageItem
import com.khoben.autotitle.mvp.presenter.PreloadActivityPresenter
import com.khoben.autotitle.mvp.view.PreloadActivityView
import moxy.MvpAppCompatActivity
import moxy.presenter.InjectPresenter


class PreloadActivity : MvpAppCompatActivity(), PreloadActivityView {
    @InjectPresenter
    lateinit var presenter: PreloadActivityPresenter

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
        val sourceVideoUri = intent.getParcelableExtra<Uri>(MainActivity.VIDEO_SOURCE_URI_INTENT)
        binding.videoPreloadView.player = presenter.initNewPlayer()
        presenter.init(sourceVideoUri!!)
        binding.startVideoCaption.setOnClickListener { startVideoCaption(sourceVideoUri) }
        binding.selectLanguage.init("Language",
            listOf(
                LanguageItem("1", "Russian (Русский)"),
                LanguageItem("2", "English"),
                LanguageItem("3", "Chinese (中文)"),
            )
        )
        binding.switchAutodetect.setOnCheckedChangeListener { _, isChecked ->
            binding.selectLanguage.toggleCollapseExpand(isChecked)
        }
        binding.backBtn.setOnClickListener { finish() }

        checkUserLastPrefs()
    }

    private fun checkUserLastPrefs() {
        val selection = userLastSelection.getBoolean(USER_LAST_SELECTION_AUTO, false)
        binding.switchAutodetect.isChecked = selection
        binding.selectLanguage.setVisibility(selection)
    }


    private fun startVideoCaption(uri: Uri) {
        finish()
        startActivity(Intent(this, VideoEditActivity::class.java).apply {
            putExtra(MainActivity.VIDEO_SOURCE_URI_INTENT, uri)
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
        }
        super.onPause()
    }

    companion object {
        private const val USER_LAST_SELECTION_PREF = "USER_LAST_SELECTION_PREF"
        private const val USER_LAST_SELECTION_AUTO = "USER_LAST_SELECTION_AUTO"
        private const val USER_LAST_SELECTION_LANGUAGE = "USER_LAST_SELECTION_LANGUAGE"
    }
}