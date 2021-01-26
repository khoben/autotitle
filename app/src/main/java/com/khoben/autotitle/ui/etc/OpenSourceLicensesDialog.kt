package com.khoben.autotitle.ui.etc

import android.app.AlertDialog
import android.app.Dialog
import android.content.res.Configuration
import android.os.Bundle
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import com.khoben.autotitle.extension.dp


class OpenSourceLicensesDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val webView = WebView(requireActivity()).apply {
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true
            loadUrl("file:///android_asset/licenses.html")
        }
        val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
                WebSettingsCompat.setForceDark(webView.settings, WebSettingsCompat.FORCE_DARK_ON)
            }
        }
        return AlertDialog.Builder(requireActivity())
            .setTitle("Open Source Licenses")
            .setPositiveButton(android.R.string.ok) { dialog, _ -> dialog.dismiss() }
            .create().apply {
                setView(webView, 0, 15.dp(), 0, 0)
            }
    }


    fun showLicenses(activity: AppCompatActivity) {
        show(activity.supportFragmentManager, "dialog_licenses")
    }
}