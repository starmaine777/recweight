package com.starmaine777.recweight.data

import android.Manifest
import android.annotation.TargetApi
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
import com.starmaine777.recweight.utils.isDeviceOnline
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.io.IOException

/**
 * Created by 0025331458 on 2017/08/16.
 */
class ImportRepository(val context: Context, val listener: ImportEventListener) {

    enum class ERROR(var statusCode:Int) {
        ACCOUNT_PERMISSION_DENIED(0),
        GPS_AVAILABILITY_ERROR(0),
        GPS_AVAILABILITY_FATAL_ERROR(0),
        DEVICE_OFFLINE(0)
    }


    interface ImportEventListener {
        fun onError(errorCode: ERROR)
        fun showAccountChoice(credential: GoogleAccountCredential)
    }


    companion object {
        private val READONLY_SCOPES = mutableListOf(SheetsScopes.SPREADSHEETS_READONLY)
        val spreadSheetsId = "1mfbm9TcTq4clJxzur3S8LSP-ctWAls3pJdKkKFH5Qnw"
        val range = "フォームの回答 1!A1:E5"
    }

    val credential: GoogleAccountCredential  by lazy { GoogleAccountCredential.usingOAuth2(context, READONLY_SCOPES).setBackOff(ExponentialBackOff()) }

    fun retryGetResult(accountName:String) {
        credential.selectedAccountName = accountName
        getResultFromApi()
    }

    fun getResultFromApi() {
        if (!isGooglePlayServiceAvailable(context)) {
            Log.d("test", "getResultFromApi 1")
            showGooglePlayServiceError(context)
        } else if (isAllowedAccountPermission()) {
            Log.d("test", "getResultFromApi 2")
            listener.onError(ERROR.ACCOUNT_PERMISSION_DENIED)
        } else if (TextUtils.isEmpty(credential.selectedAccountName)) {
            Log.d("test", "getResultFromApi 3")
            listener.showAccountChoice(credential)
        } else if (!isDeviceOnline(context)) {
            Log.d("test", "getResultFromApi 4")
            listener.onError(ERROR.DEVICE_OFFLINE)
        } else {
            Log.d("test", "getResultFromApi 5")
            val transport = AndroidHttp.newCompatibleTransport()
            val jsonFactory = JacksonFactory.getDefaultInstance()
            val service = Sheets.Builder(transport, jsonFactory, credential).build()

            Single.create<String> { getDataFromApi(service) }.subscribeOn(Schedulers.io())
        }
    }


    @Throws(IOException::class)
    fun getDataFromApi(service: Sheets){
        val result = ArrayList<String>()
        val response = service.spreadsheets().values().batchGet(spreadSheetsId)

        Log.d("test", "response = " + response.ranges)

    }


    fun isGooglePlayServiceAvailable(context: Context): Boolean {
        val apiAvailability = GoogleApiAvailability.getInstance()
        return apiAvailability.isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS
    }

    fun showGooglePlayServiceError(context: Context) {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val statusCode = apiAvailability.isGooglePlayServicesAvailable(context)
        val error = if (apiAvailability.isUserResolvableError(statusCode)) ERROR.GPS_AVAILABILITY_ERROR else ERROR.GPS_AVAILABILITY_FATAL_ERROR
        error.statusCode = statusCode
        listener.onError(error)
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