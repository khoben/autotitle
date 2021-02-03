package com.khoben.autotitle.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.khoben.autotitle.R
import com.khoben.autotitle.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(ActivitySettingsBinding.inflate(layoutInflater).root)
        setSupportActionBar(findViewById(R.id.settings_toolbar))
        supportActionBar?.let { actionBar ->
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.title = getString(R.string.settings_title)
        }
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings_container, SettingsFragment())
            .commit()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}