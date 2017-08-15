package com.starmaine777.recweight.views

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.starmaine777.recweight.R
import com.starmaine777.recweight.event.InputFragmentStartEvent
import com.starmaine777.recweight.event.RxBus
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_show_records.*

class ShowRecordsActivity : AppCompatActivity() {

    var disposable= CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_records)

        supportFragmentManager.beginTransaction().replace(R.id.fragment, ShowRecordsFragment(), ShowRecordsFragment.TAG).commit()

        setSupportActionBar(toolbar)
    }

    override fun onResume() {
        super.onResume()
        disposable.add(RxBus.subscribe(InputFragmentStartEvent::class.java).subscribe(
                { t: InputFragmentStartEvent ->
                    val inputFragment = WeightInputFragment.newInstance(t.viewMode, t.id)
                    supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.fragment, inputFragment, t.viewMode.name)
                            .addToBackStack(t.viewMode.name)
                            .commit()
                }))
    }

    override fun onPause() {
        super.onPause()
        disposable.dispose()
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStackImmediate()
        }
    }

}