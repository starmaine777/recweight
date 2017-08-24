package com.starmaine777.recweight.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.text.TextUtils
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.starmaine777.recweight.R
import com.starmaine777.recweight.error.SpreadSheetsException
import com.starmaine777.recweight.error.SpreadSheetsException.ERROR_TYPE
import com.starmaine777.recweight.utils.isDeviceOnline
import io.reactivex.Observable
import java.io.IOException

/**
 * Import用Api取得クラス
 * Created by 0025331458 on 2017/08/16.
 */
class ImportRepository(val context: Context) {

    companion object {
        private val READONLY_SCOPES = mutableListOf(SheetsScopes.SPREADSHEETS_READONLY)
        val spreadSheetsId = "1mfbm9TcTq4clJxzur3S8LSP-ctWAls3pJdKkKFH5Qnw"
        val range = "フォームの回答 1!A1:E5"
    }

    val credential: GoogleAccountCredential  by lazy { GoogleAccountCredential.usingOAuth2(context, READONLY_SCOPES).setBackOff(ExponentialBackOff()) }

    fun retryGetResult(accountName: String) {
        Log.d("test", "retryGetResult")
        credential.selectedAccountName = accountName
        getResultFromApi()
    }

    @Throws(SpreadSheetsException::class, IOException::class)
    fun getResultFromApi(): Observable<Sheets.Spreadsheets.Values.BatchGet> {
        return Observable.create {
            if (!isGooglePlayServiceAvailable(context)) {
                val apiAvailability = GoogleApiAvailability.getInstance()
                val statusCode = apiAvailability.isGooglePlayServicesAvailable(context)
                val error = if (apiAvailability.isUserResolvableError(statusCode)) ERROR_TYPE.PLAY_SERVICE_AVAILABILITY_ERROR else ERROR_TYPE.FATAL_ERROR
                throw SpreadSheetsException(error, statusCode)
            } else if (isAllowedAccountPermission()) {
                throw SpreadSheetsException(ERROR_TYPE.ACCOUNT_PERMISSION_DENIED, -1)
            } else if (TextUtils.isEmpty(credential.selectedAccountName)) {
                throw SpreadSheetsException(ERROR_TYPE.ACCOUNT_NOT_SELECTED, -1)
            } else if (!isDeviceOnline(context)) {
                throw SpreadSheetsException(ERROR_TYPE.DEVICE_OFFLINE, -1)
            } else {
                val transport = AndroidHttp.newCompatibleTransport()
                val jsonFactory = JacksonFactory.getDefaultInstance()
                val service = Sheets.Builder(transport, jsonFactory, credential)
                        .setApplicationName(context.getString(R.string.app_name))
                        .build()

                getDataFromApi(service)
            }
        }
    }

    @Throws(IOException::class)
    fun getDataFromApi(service: Sheets) {
        val result = ArrayList<String>()
        val response = service.spreadsheets().get(spreadSheetsId).execute()

        Log.d("test", "response = " + response)

    }


    fun isGooglePlayServiceAvailable(context: Context): Boolean {
        val apiAvailability = GoogleApiAvailability.getInstance()
        return apiAvailability.isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS
    }

    fun isAllowedAccountPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            Log.d("test", "isAllowedAccountPermission sdk version")
            return true
        } else {
            Log.d("test", "isAllowedAccountPermission readContactsPermissionCheck ${context.checkSelfPermission(Manifest.permission.GET_ACCOUNTS)}")
            return context.checkSelfPermission(Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED
        }
    }
}