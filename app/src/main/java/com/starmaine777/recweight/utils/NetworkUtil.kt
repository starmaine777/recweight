package com.starmaine777.recweight.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Build
import android.text.TextUtils
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.starmaine777.recweight.R
import timber.log.Timber

/**
 * ネットワーク関係のUtil
 * Created by 0025331458 on 2017/08/17.
 */

enum class SHEETS_COLUMNS(val nameId: Int, val columnName: String) {
    DATE(R.string.export_file_sheets_column_date, "A"),
    WEIGHT(R.string.export_file_sheets_column_weight, "B"),
    FAT(R.string.export_file_sheets_column_fat, "C"),
    DUMBBELL(R.string.export_file_sheets_column_dumbbell, "D"),
    LIQUOR(R.string.export_file_sheets_column_liquor, "E"),
    TOILET(R.string.export_file_sheets_column_toilet, "F"),
    MOON(R.string.export_file_sheets_column_moon, "G"),
    STAR(R.string.export_file_sheets_column_star, "H"),
    MEMO(R.string.export_file_sheets_column_memo, "I"),
}

fun isDeviceOnline(context: Context): Boolean {
    val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    return (connMgr.activeNetworkInfo != null && connMgr.activeNetworkInfo.isConnected)
}

fun isGooglePlayServiceAvailable(context: Context): Boolean {
    val apiAvailability = GoogleApiAvailability.getInstance()
    return apiAvailability.isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS
}

fun isAllowedAccountPermission(context: Context): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
        Timber.d("isAllowedAccountPermission sdk version")
        return true
    } else {
        Timber.d("isAllowedAccountPermission readContactsPermissionCheck ${context.checkSelfPermission(Manifest.permission.GET_ACCOUNTS)}")
        return context.checkSelfPermission(Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED
    }
}

fun existsChoseAccount(context: Context, credential: GoogleAccountCredential): Boolean {
    if (!TextUtils.isEmpty(credential.selectedAccountName)) return true
    val savedName = context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE).getString(PREFERENCE_KEY.ACCOUNT_NAME.name, "")
    Timber.d("savedName = $savedName")
    if (!TextUtils.isEmpty(savedName)) {
        credential.selectedAccountName = savedName
        return true
    } else {
        return false
    }
}
