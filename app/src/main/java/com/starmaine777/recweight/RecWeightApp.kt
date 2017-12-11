package com.starmaine777.recweight

import android.app.Application
import com.crashlytics.android.Crashlytics
import com.google.android.gms.ads.MobileAds
import io.fabric.sdk.android.Fabric
import timber.log.Timber

/**
 * アプリケーションクラス
 * Created by 0025331458 on 2017/09/04.
 */

class RecWeightApp : Application() {

    override fun onCreate() {
        super.onCreate()

        MobileAds.initialize(this, getString(R.string.ad_app_id))

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Fabric.with(this, Crashlytics())
        }
    }
}
