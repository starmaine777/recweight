package com.starmaine777.recweight.views

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.starmaine777.recweight.R
import com.starmaine777.recweight.event.InputFragmentStartEvent
import com.starmaine777.recweight.event.RxBus
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_show_records.*

class ShowRecordsActivity : AppCompatActivity() {

    var disposable:Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_records)

        supportFragmentManager.beginTransaction().replace(R.id.fragment, ShowRecordsFragment(), ShowRecordsFragment.TAG).commit()

        setSupportActionBar(toolbar)
    }

    override fun onResume() {
        super.onResume()
        disposable = RxBus.subscribe(InputFragmentStartEvent::class.java).subscribe(
                { t: InputFragmentStartEvent ->
                    Log.d("test", "createInputFragment backStackCount = ${supportFragmentManager.backStackEntryCount}")
                    val inputFragment = WeightInputFragment.newInstance(t.viewMode, t.id)
                    supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.fragment, inputFragment, t.viewMode.name)
                            .addToBackStack(t.viewMode.name)
                            .commit()
                })
    }

    override fun onPause() {
        super.onPause()
        disposable?.dispose()
    }

    override fun onBackPressed() {
//        super.onBackPressed()
        Log.d("test", "onBackPressed supportFragmentManager backStackEntryCount = ${supportFragmentManager.backStackEntryCount}")
        if (supportFragmentManager.backStackEntryCount > 0) {
            Log.d("test", "onBackPressed popBackStack!")
            supportFragmentManager.popBackStackImmediate()
        }
        Log.d("test", "onBackPressed end supportFragmentManager backStackEntryCount = ${supportFragmentManager.backStackEntryCount}")

    }

}
