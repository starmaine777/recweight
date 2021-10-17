package com.starmaine777.recweight.views

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.starmaine777.recweight.R
import com.starmaine777.recweight.databinding.ActivityShowRecordsBinding
import com.starmaine777.recweight.event.InputFragmentStartEvent
import com.starmaine777.recweight.event.RxBus
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber


class ShowRecordsActivity : AppCompatActivity() {

    var disposable = CompositeDisposable()
    private lateinit var binding: ActivityShowRecordsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShowRecordsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportFragmentManager.beginTransaction().replace(R.id.fragment, ShowRecordsFragment(), ShowRecordsFragment.TAG).commit()

        setSupportActionBar(binding.toolbar)
        MobileAds.initialize(this)
    }

    override fun onStart() {
        super.onStart()
        binding.viewAds.loadAd(AdRequest.Builder().build())
    }

    override fun onResume() {
        super.onResume()
        RxBus.subscribe(InputFragmentStartEvent::class.java).subscribe(
                { t: InputFragmentStartEvent ->
                    Timber.d("")
                    val inputFragment = WeightInputFragment.newInstance(t.viewMode, t.id)
                    supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.fragment, inputFragment, t.viewMode.name)
                            .addToBackStack(t.viewMode.name)
                            .commit()
                }).let { disposable.add(it) }
    }

    override fun onPause() {
        super.onPause()
        disposable.clear()
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount == 0) {
            finish()
        } else {
            supportFragmentManager.popBackStackImmediate()
        }
    }

}
