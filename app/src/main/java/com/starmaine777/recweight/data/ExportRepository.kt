package com.starmaine777.recweight.data

import android.content.Context
import android.text.TextUtils
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
import io.reactivex.Emitter
import io.reactivex.Observable.create
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
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

    fun exportDatas(context: Context): Observable<Int> {
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

                val id = createExportFile(service)

                if (TextUtils.isEmpty(id)) {

                } else {
                    writeExportDateToSheet(service, id!!)
                    emitter.onComplete()
                }
            }
        }

    }

    fun createExportFile(service: Sheets): String? {
        val request = Spreadsheet()
        val timeStamp = System.currentTimeMillis()
        val calendar = Calendar.getInstance()

        calendar.timeInMillis = timeStamp


        val formatter = SimpleDateFormat(EXPORT_TITLE_DATE_STR)
        val title = context.getString(R.string.export_file_name_header) + formatter.format(calendar.time)
        request.spreadsheetId = title
        val properties = SpreadsheetProperties()
        properties.title = title
        request.properties = properties

        val sheet = Sheet()
        val sheetProperties = SheetProperties()
        sheetProperties.title = context.getString(R.string.export_file_sheets_name)
        sheet.properties = sheetProperties
        val sheetList = mutableListOf(sheet)

        request.sheets = sheetList

        try {
            val response = service.spreadsheets().create(request).execute()
            Timber.d("response = $response")
            return response.spreadsheetId
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    @Throws(UserRecoverableAuthIOException::class, IOException::class)
    fun writeExportDateToSheet(service: Sheets, spreadSheetId: String) {
        val values = ArrayList<MutableList<Any?>>()
        val row = ArrayList<Any?>()
        SHEETS_COLUMNS.values().mapTo(row) { it.name }
        values.add(row)
        val body = ValueRange().setValues(values)

        WeightItemRepository.getWeightItemList(context).subscribe { t ->
            if (t.isEmpty()) {
                return@subscribe
            }

            t.map { (recTime, weight, fat, showDumbbell, showLiquor, showToilet, showMoon, showStar, memo) ->
                val weightRow = mutableListOf(convertToExportDateString(recTime),
                        weight,
                        fat,
                        showDumbbell,
                        showLiquor,
                        showToilet,
                        showMoon,
                        showStar,
                        memo
                )
                values.add(weightRow)
            }

            val range = "${context.getString(R.string.export_file_sheets_name)}!" +
                    "${SHEETS_COLUMNS.values()[0].columnName}1:${SHEETS_COLUMNS.values()[SHEETS_COLUMNS.values().size - 1].columnName}${values.size}"

            service.spreadsheets().values().update(spreadSheetId, range, body).setValueInputOption("RAW").execute()
            // TODO:シートサイズ小さくする


        }
    }

    fun convertToExportDateString(calendar: Calendar): String? {
        val formatter = SimpleDateFormat(EXPORT_DATE_STR)
        return formatter.format(calendar.time)
    }


}
