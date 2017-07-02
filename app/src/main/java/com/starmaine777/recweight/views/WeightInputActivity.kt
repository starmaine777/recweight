package com.starmaine777.recweight.views

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import com.starmaine777.recweight.R
import com.starmaine777.recweight.utils.Consts

class WeightInputActivity : AppCompatActivity() {

    companion object {
        const val ARGS_INPUT_TYPE = "inputType"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weight_input)

        val inputFragment: Fragment = WeightInputFragment.newInstance(intent.getSerializableExtra(ARGS_INPUT_TYPE) as Consts.WEIGHT_INPUT_MODE)

        supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment, inputFragment, WeightInputFragment.TAG)
                .commit()
    }
}
