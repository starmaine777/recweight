package com.starmaine777.recweight

import android.app.Application
import com.crashlytics.android.Crashlytics
import io.fabric.sdk.android.Fabric
import timber.log.Timber

/**
 * アプリケーションクラス
 * Created by 0025331458 on 2017/09/04.
 */

class RecWeightApp : Application() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        Fabric.with(this, Crashlytics())
    }
}
