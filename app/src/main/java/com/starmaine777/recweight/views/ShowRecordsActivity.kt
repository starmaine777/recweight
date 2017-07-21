package com.starmaine777.recweight.views

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.starmaine777.recweight.R
import com.starmaine777.recweight.utils.Consts
import kotlinx.android.synthetic.main.activity_show_records.*

class ShowRecordsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_records)

        supportFragmentManager.beginTransaction().replace(R.id.fragment, ShowRecordsFragment(), ShowRecordsFragment.TAG).commit()

        setSupportActionBar(toolbar)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == Consts.REQUESTS.EDIT_WEIGHT_ITEM.ordinal) {
            val inputFragment = WeightInputFragment.newInstance(Consts.WEIGHT_INPUT_MODE.EDIT)
            supportFragmentManager.beginTransaction().replace(R.id.fragment, inputFragment, WeightInputFragment.TAG).addToBackStack(WeightInputFragment.TAG).commit()
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

}
