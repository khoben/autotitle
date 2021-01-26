package com.khoben.autotitle.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.khoben.autotitle.BuildConfig
import com.khoben.autotitle.R
import com.khoben.autotitle.ui.etc.OpenSourceLicensesDialog

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        findPreference<Preference>("about_app")?.title = "v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"

        findPreference<Preference>("opensource_licenses")?.setOnPreferenceClickListener {
            OpenSourceLicensesDialog().showLicenses(this.activity as AppCompatActivity)
            true
        }
    }
}