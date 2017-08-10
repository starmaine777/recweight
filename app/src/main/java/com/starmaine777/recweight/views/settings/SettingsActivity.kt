package com.starmaine777.recweight.views.settings

import android.support.v7.app.AppCompatActivity
import android.os.Bundle

import com.starmaine777.recweight.R
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportFragmentManager.beginTransaction().replace(R.id.fragment, SettingsMainFragment(), SettingsMainFragment.TAG).commit()
    }
}
