package com.khoben.autotitle.ui.etc

import android.app.AlertDialog
import android.app.Dialog
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import com.khoben.autotitle.R
import com.khoben.autotitle.extension.dp


class HTMLViewerDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val url = arguments?.getString(EXTRA_DOC_URL) ?: ""
        val title = arguments?.getString(EXTRA_TITLE_DIALOG) ?: ""

        val webView = WebView(requireActivity()).apply {
            setBackgroundColor(Color.argb(1, 0, 0, 0))
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true
            loadUrl(url)
        }
        val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
                WebSettingsCompat.setForceDark(webView.settings, WebSettingsCompat.FORCE_DARK_ON)
            }
        }
        return AlertDialog.Builder(requireActivity())
            .setTitle(title)
            .setPositiveButton(android.R.string.ok) { dialog, _ -> dialog.dismiss() }
            .create().apply {
                setView(webView, 0, 15.dp(), 0, 0)
            }
    }


    fun show(activity: AppCompatActivity, url: String, title: String) {
        arguments = Bundle().apply {
            putString(EXTRA_DOC_URL, url)
            putString(EXTRA_TITLE_DIALOG, title)
        }
        show(activity.supportFragmentManager, TAG)
    }

    fun showLicenses(activity: AppCompatActivity) {
        show(
            activity,
            OPENSOURCE_LICENSES_URL,
            activity.getString(R.string.opensource_licenses_title)
        )
    }

    fun showPrivacy(activity: AppCompatActivity) {
        show(activity, PRIVACY_POLICY_URL, activity.getString(R.string.privacy_policy_title))
    }

    fun showTerms(activity: AppCompatActivity) {
        show(activity, TERMS_CONDS_URL, activity.getString(R.string.terms_cond_title))
    }

    companion object {
        private val TAG = "HTML_DIALOG"
        private val EXTRA_DOC_URL = "EXTRA_DOC_URL"
        private val EXTRA_TITLE_DIALOG = "EXTRA_TITLE_DIALOG"

        private val OPENSOURCE_LICENSES_URL = "file:///android_asset/licenses.html"
        private val PRIVACY_POLICY_URL = "file:///android_asset/privacy_policy.html"
        private val TERMS_CONDS_URL = "file:///android_asset/terms_conds.html"
    }
}