package com.starmaine777.recweight.data

import android.content.Context
import com.google.android.gms.auth.UserRecoverableAuthException
import com.google.android.gms.common.GoogleApiAvailability
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.starmaine777.recweight.R
import io.reactivex.Observable
import timber.log.Timber
import com.google.api.services.sheets.v4.model.*
import com.starmaine777.recweight.error.SpreadSheetsException
import com.starmaine777.recweight.utils.*
import io.reactivex.Observable.create
import java.io.IOException
import kotlin.collections.ArrayList

/**
 * ExportのSpreadSheets操作用
 * Created by 0025331458 on 2017/09/08.
 */
class ExportRepository(val context: Context) {

    companion object {
        private val WRITE_SCOPES = mutableListOf(SheetsScopes.DRIVE_FILE, SheetsScopes.SPREADSHEETS)
        private val SHEET_ID = "1s4SSJwTzQUSQMHbEyROZRYr__NGkUYHd14NIoSrzQdU"

    }

    val credential: GoogleAccountCredential by lazy { GoogleAccountCredential.usingOAuth2(context, WRITE_SCOPES).setBackOff(ExponentialBackOff()) }

    fun exportDatas(context: Context): Observable<String> {
        return create { emitter ->

            if (!isGooglePlayServiceAvailable(context)) {
                val apiAvailability = GoogleApiAvailability.getInstance()
                val statusCode = apiAvailability.isGooglePlayServicesAvailable(context)
                val error = if (apiAvailability.isUserResolvableError(statusCode)) SpreadSheetsException.ERROR_TYPE.PLAY_SERVICE_AVAILABILITY_ERROR else SpreadSheetsException.ERROR_TYPE.FATAL_ERROR
                emitter.onError(SpreadSheetsException(error, statusCode))
            } else if (isAllowedAccountPermission(context)) {
                emitter.onError(SpreadSheetsException(SpreadSheetsException.ERROR_TYPE.ACCOUNT_PERMISSION_DENIED))
            } else if (!existsChoseAccount(context, credential)) {
                emitter.onError(SpreadSheetsException(SpreadSheetsException.ERROR_TYPE.ACCOUNT_NOT_SELECTED))
            } else if (!isDeviceOnline(context)) {
                emitter.onError(SpreadSheetsException(SpreadSheetsException.ERROR_TYPE.DEVICE_OFFLINE))
            } else {


                Timber.d("startExportData")
                val transport = AndroidHttp.newCompatibleTransport()
                val jsonFactory = JacksonFactory.getDefaultInstance()

                val service = Sheets.Builder(transport, jsonFactory, credential)
                        .setApplicationName(context.getString(R.string.app_name))
                        .build()

                Timber.d("startExportData service=$service")


                writeExportDateToSheet(service, SHEET_ID)
                Timber.d("writeExportDateToSheet complete")

//            val timeStamp = System.currentTimeMillis()
//            val calendar = Calendar.getInstance()
//
//            calendar.timeInMillis = timeStamp
//            val content = BatchUpdateSpreadsheetRequest()
//            val requests = ArrayList<Request>()
//            val addSheets = AddSheetRequest()
//            val e = Request()
//            val properties = SheetProperties()
//            val formatter = SimpleDateFormat(EXPORT_TITLE_DATE_STR)
//            properties.title = context.getString(R.string.export_file_name_header) + formatter.format(calendar.time)
//            addSheets.properties = properties
//            e.addSheet = addSheets
//            requests.add(e)
//            content.requests = requests
//            Timber.d("startExportData startBatchUpdate")
//            val response = service.spreadsheets().batchUpdate(properties.title, content)
//            Timber.d("response = $response")
//            emitter.onNext(response.toString())
//
//            val range = properties.title + "!A1:D1"
//            val valueRange = ValueRange()
//            val row = ArrayList<List<Any>>()
//            val col = ArrayList<Any>()
//            col.add("this")
//            col.add("is")
//            col.add("api")
//            col.add("test")
//            row.add(col)
//            valueRange.setValues(row)
//            valueRange.range = range
//            service.spreadsheets().values()
//                    .update(properties.title, range, valueRange)
//                    .setValueInputOption("USER_ENTERED")
//                    .execute()

            }
        }

    }

    @Throws(UserRecoverableAuthIOException::class, IOException::class)
    fun writeExportDateToSheet(service: Sheets, spreadSheetId: String) {
        val values = ArrayList<MutableList<Any>>()
        val row = ArrayList<Any>()
        SHEETS_COLUMNS.values().mapTo(row) { it.name }
        values.add(row)
        val body = ValueRange().setValues(values)
        Timber.d("writeExportDateToSheet !!")
        val result = service.spreadsheets().values().update(spreadSheetId, "aaa!A1:I1", body).setValueInputOption("RAW").execute()
    }


}
