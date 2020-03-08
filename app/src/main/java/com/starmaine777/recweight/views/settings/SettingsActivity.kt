package com.starmaine777.recweight.views.settings

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils

import com.starmaine777.recweight.R
import com.starmaine777.recweight.event.UpdateToolbarEvent
import com.starmaine777.recweight.event.RxBus
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_settings.*
import timber.log.Timber

class SettingsActivity : AppCompatActivity() {

    val disposal: CompositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportFragmentManager.beginTransaction().replace(R.id.fragment, SettingsMainFragment(), SettingsMainFragment.TAG).commit()
        Timber.d("oncreate backStackEntryCount = ${supportFragmentManager.backStackEntryCount}")

        toolbar_setting.setNavigationIcon(R.drawable.icon_arrow_back)
        toolbar_setting.setNavigationOnClickListener {
            onBackPressed()
        }
        toolbar_setting.setTitle(R.string.activity_settings)
    }

    override fun onStart() {
        super.onStart()
        disposal.add(RxBus.subscribe(UpdateToolbarEvent::class.java).subscribe {
            t ->
            if (t.show) {
                toolbar_setting.setNavigationIcon(R.drawable.icon_arrow_back)
            } else {
                toolbar_setting.navigationIcon = null
            }

            if (!TextUtils.isEmpty(t.title)) {
                toolbar_setting.title = t.title
            }
        })

    }

    override fun onStop() {
        super.onStop()
        disposal.clear()
    }
}
