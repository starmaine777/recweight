package com.starmaine777.recweight.data

import android.content.Context
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.starmaine777.recweight.R
import com.starmaine777.recweight.utils.EXPORT_TITLE_DATE_STR
import io.reactivex.Observable
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import com.google.api.services.sheets.v4.model.*

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

            Timber.d("startExportData")
            val transport = AndroidHttp.newCompatibleTransport()
            val jsonFactory = JacksonFactory.getDefaultInstance()

            val credential: GoogleCredential? = null
            val service = Sheets.Builder(transport, jsonFactory, credential)
                    .setApplicationName(context.getString(R.string.app_name))
                    .build()

            Timber.d("startExportData service=$service")

            val timeStamp = System.currentTimeMillis()
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = timeStamp
            val content = BatchUpdateSpreadsheetRequest()
            val requests = ArrayList<Request>()
            val addSheets = AddSheetRequest()
            val e = Request()
            val properties = SheetProperties()
            val formatter = SimpleDateFormat(EXPORT_TITLE_DATE_STR)
            properties.title = context.getString(R.string.export_file_name_header) + formatter.format(calendar.time)
            addSheets.properties = properties
            e.addSheet = addSheets
            requests.add(e)
            content.requests = requests
            Timber.d("startExportData startBatchUpdate")
            val response = service.spreadsheets().batchUpdate(properties.title, content)
            Timber.d("response = $response")
            emitter.onNext(response.toString())

            val range = properties.title + "!A1:D1"
            val valueRange = ValueRange()
            val row = ArrayList<Any>()
            val col = ArrayList<Any>()
            col.add("this")
            col.add("is")
            col.add("api")
            col.add("test")
            row.add(col)
//            valueRange.setValue(row)
            valueRange.setRange(range)
            service.spreadsheets().values()
                    .update(properties.title, range, valueRange)
                    .setValueInputOption("USER_ENTERED")
                    .execute()


        }

    }

}
