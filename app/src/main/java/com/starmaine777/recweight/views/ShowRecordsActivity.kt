package com.starmaine777.recweight.views

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.starmaine777.recweight.R
import kotlinx.android.synthetic.main.activity_show_records.*

class ShowRecordsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_records)

        supportFragmentManager.beginTransaction().replace(R.id.fragment, ShowRecordsFragment(), ShowRecordsFragment.TAG).commit()

        setSupportActionBar(toolbar)
    }

}
