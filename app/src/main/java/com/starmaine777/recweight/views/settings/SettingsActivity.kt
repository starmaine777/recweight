package com.starmaine777.recweight.views.settings

import android.os.Bundle
import android.text.TextUtils
import androidx.appcompat.app.AppCompatActivity
import com.starmaine777.recweight.R
import com.starmaine777.recweight.databinding.ActivitySettingsBinding
import com.starmaine777.recweight.event.RxBus
import com.starmaine777.recweight.event.UpdateToolbarEvent
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    val disposal: CompositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportFragmentManager.beginTransaction().replace(R.id.fragment, SettingsMainFragment(), SettingsMainFragment.TAG).commit()
        Timber.d("oncreate backStackEntryCount = ${supportFragmentManager.backStackEntryCount}")

        binding.toolbarSetting.apply {
            setNavigationIcon(R.drawable.icon_arrow_back)
            setNavigationOnClickListener {
                onBackPressed()
            }
            setTitle(R.string.activity_settings)
        }
    }

    override fun onStart() {
        super.onStart()
        disposal.add(RxBus.subscribe(UpdateToolbarEvent::class.java).subscribe { t ->
            binding.toolbarSetting.apply {
                if (t.show) {
                    setNavigationIcon(R.drawable.icon_arrow_back)
                } else {
                    navigationIcon = null
                }

                if (!TextUtils.isEmpty(t.title)) {
                    title = t.title
                }
            }
        })

    }

    override fun onStop() {
        super.onStop()
        disposal.clear()
    }
}
