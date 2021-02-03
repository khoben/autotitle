package com.khoben.autotitle.ui.activity

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.DropDownPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.khoben.autotitle.App
import com.khoben.autotitle.BuildConfig
import com.khoben.autotitle.R
import com.khoben.autotitle.common.DisplayUtils
import com.khoben.autotitle.ui.etc.HTMLViewerDialog

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        findPreference<DropDownPreference>("app_theme")?.setOnPreferenceChangeListener { _, newValue ->
            DisplayUtils.setAppUi(requireActivity(), newValue as String)
            true
        }

        findPreference<Preference>("about_app")?.title =
            "v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"

        findPreference<Preference>("feedback")?.setOnPreferenceClickListener {
            sendEmailFeedback(
                App.appFeedbackEmail,
                "AutoTitle Feedback (${Build.MANUFACTURER} ${Build.MODEL}*Android ${Build.VERSION.SDK_INT}*${BuildConfig.VERSION_NAME})"
            )
            true
        }

        findPreference<Preference>("privacy_policy")?.setOnPreferenceClickListener {
            HTMLViewerDialog().showPrivacy(this.activity as AppCompatActivity)
            true
        }

        findPreference<Preference>("terms_conditions")?.setOnPreferenceClickListener {
            HTMLViewerDialog().showTerms(this.activity as AppCompatActivity)
            true
        }

        findPreference<Preference>("opensource_licenses")?.setOnPreferenceClickListener {
            HTMLViewerDialog().showLicenses(this.activity as AppCompatActivity)
            true
        }
    }

    private fun sendEmailFeedback(email: String, subject: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$email?subject=$subject")
        }
        try {
            startActivity(intent)
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(activity, getString(R.string.email_app_notfound), Toast.LENGTH_SHORT)
                .show()
        }
    }
}