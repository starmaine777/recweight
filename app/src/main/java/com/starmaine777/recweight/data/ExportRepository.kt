package com.starmaine777.recweight.data

import android.content.Context
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.api.services.sheets.v4.model.*
import com.starmaine777.recweight.R
import io.reactivex.Observable
import timber.log.Timber

/**
 * ExportのSpreadSheets操作用
 * Created by 0025331458 on 2017/09/08.
 */
class ExportRepository(val context: Context) {

    companion object {
        private val WRITE_SCOPES = mutableListOf(SheetsScopes.DRIVE_FILE, SheetsScopes.SPREADSHEETS)
    }

    val credential: GoogleAccountCredential by lazy { GoogleAccountCredential.usingOAuth2(context, WRITE_SCOPES).setBackOff(ExponentialBackOff()) }

    fun exportDatas(context: Context): Observable<String> {
        return Observable.create { emitter ->


            val transport = AndroidHttp.newCompatibleTransport()
            val jsonFactory = JacksonFactory.getDefaultInstance()

            val credential: GoogleCredential? = null
            val service = Sheets.Builder(transport, jsonFactory, credential)
                    .setApplicationName(context.getString(R.string.app_name))
                    .build()

            val timeStamp = System.currentTimeMillis()
            val content = BatchUpdateSpreadsheetRequest()
            val requests = ArrayList<Request>()
            val addSheets = AddSheetRequest()
            val e = Request()
            val properties = SheetProperties()
            properties.title = context.getString(R.string.export_file_name_header) + timeStamp
            addSheets.properties = properties
            e.addSheet = addSheets
            requests.add(e)
            content.requests = requests
            val response = service.spreadsheets().batchUpdate(properties.title, content).execute()
            Timber.d("response = $response")
            emitter.onNext(response.toString())
        }

    }

}
