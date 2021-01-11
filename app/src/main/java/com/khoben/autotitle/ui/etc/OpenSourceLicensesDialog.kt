package com.khoben.autotitle.ui.etc

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.khoben.autotitle.extension.dp


class OpenSourceLicensesDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val webView = WebView(requireActivity()).apply {
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true
            loadUrl("file:///android_asset/licenses.html")
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