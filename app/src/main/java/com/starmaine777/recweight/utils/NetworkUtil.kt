package com.starmaine777.recweight.utils

import android.content.Context
import android.net.ConnectivityManager

/**
 * Created by 0025331458 on 2017/08/17.
 */

fun isDeviceOnline(context: Context): Boolean {
    val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    return (connMgr.activeNetworkInfo != null && connMgr.activeNetworkInfo.isConnected)
}