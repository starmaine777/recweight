package com.starmaine777.recweight.analytics

import android.util.Log
import com.crashlytics.android.Crashlytics
import timber.log.Timber

/**
 * ClashReport通知TimberPlantクラス
 * Created by shimizuasami on 2018/01/22.
 */
class CrashReportingTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String?, t: Throwable?) {
        if (priority == Log.VERBOSE || priority == Log.DEBUG) {
            return
        }
        Crashlytics.log(priority, tag, message)

        t?.let {
            if (priority == Log.ERROR || priority == Log.WARN) {
                Crashlytics.logException(it)
            }
        }
    }
}