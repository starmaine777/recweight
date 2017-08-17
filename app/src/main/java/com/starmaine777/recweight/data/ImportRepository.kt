package com.starmaine777.recweight.data

import android.content.Context
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

    enum class ERROR {
        GPS_AVAILABILITY_ERROR,
        GPS_AVAILABILITY_FATAL_ERROR,

        DEVICE_OFFLINE


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

    fun getResultFromApi() {
        if (isGooglePlayServiceAvailable(context)) {

        } else if (TextUtils.isEmpty(credential.selectedAccountName)) {
            listener.showAccountChoice(credential)
        } else if (!isDeviceOnline(context)) {
            listener.onError(ERROR.DEVICE_OFFLINE)
        } else {
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
        listener.onError(if (apiAvailability.isUserResolvableError(apiAvailability.isGooglePlayServicesAvailable(context))) ERROR.GPS_AVAILABILITY_ERROR else ERROR.GPS_AVAILABILITY_FATAL_ERROR)
    }

}