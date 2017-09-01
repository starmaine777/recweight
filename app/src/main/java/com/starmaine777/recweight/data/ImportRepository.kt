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
import com.starmaine777.recweight.utils.EXPORT_DATE_STR
import com.starmaine777.recweight.utils.convertToCalendar
import com.starmaine777.recweight.utils.isDeviceOnline
import io.reactivex.Observable
import org.json.JSONObject
import java.io.IOException

/**
 * Import用Api取得クラス
 * Created by 0025331458 on 2017/08/16.
 */
class ImportRepository(val context: Context) {

    companion object {
        private val READONLY_SCOPES = mutableListOf(SheetsScopes.SPREADSHEETS_READONLY)
        val spreadSheetsId = "1AX6hePR64bAX9JB8G3Z4pHXk2VciKu9VrcaYW8J5f0Q"
        val range = "RecWeight 1!A1:E5"
    }

    enum class COLUMNS(val nameId: Int) {
        DATE(R.string.export_file_sheets_column_date),
        WEIGHT(R.string.export_file_sheets_column_weight),
        FAT(R.string.export_file_sheets_column_fat),
        DUMBBELL(R.string.export_file_sheets_column_dumbbell),
        LIQUOR(R.string.export_file_sheets_column_liquor),
        TOILET(R.string.export_file_sheets_column_toilet),
        MOON(R.string.export_file_sheets_column_moon),
        STAR(R.string.export_file_sheets_column_star),
        MEMO(R.string.export_file_sheets_column_memo),
    }

    val columnArray = arrayOf("A", "B", "C", "D", "E", "F", "G", "H", "I")

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
                throw SpreadSheetsException(ERROR_TYPE.ACCOUNT_PERMISSION_DENIED)
            } else if (TextUtils.isEmpty(credential.selectedAccountName)) {
                throw SpreadSheetsException(ERROR_TYPE.ACCOUNT_NOT_SELECTED)
            } else if (!isDeviceOnline(context)) {
                throw SpreadSheetsException(ERROR_TYPE.DEVICE_OFFLINE)
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

    @Throws(SpreadSheetsException::class, IOException::class)
    fun getDataFromApi(service: Sheets) {
        val response = JSONObject(service.spreadsheets().get(spreadSheetsId).execute())

        if (isExportFolder(response)) {
            Log.d("test", "isExportFolder is true")
            val sheetRow = response.getJSONArray("sheets").getJSONObject(0).getJSONObject("properties").getJSONObject("gridProperties").getInt("rowCount")
            val sheetColumn = response.getJSONArray("sheets").getJSONObject(0).getJSONObject("properties").getJSONObject("gridProperties").getInt("columnCount")

            if (sheetColumn != 9) {
                throw SpreadSheetsException(ERROR_TYPE.SHEETS_ILLEGAL_TEMPLATE_ERROR)
            }

            val data = service.spreadsheets().values()
                    .get(spreadSheetsId, "${context.getString(R.string.export_file_sheets_name)}!A1:I$sheetRow")
                    .execute()
                    .getValues()

            for (i in data.indices) {
                when (i) {
                    0 -> {
                        if (!isCorrectFirstRow(data[i])) {
                            throw SpreadSheetsException(ERROR_TYPE.SHEETS_ILLEGAL_TEMPLATE_ERROR)
                        }
                    }
                    else -> {
                        importRowData(data[i])
                    }
                }
            }

            Log.d("test", "isExportFolder RowCount=$sheetRow")
        } else {
            Log.d("test", "isExportFolder is false")
            throw SpreadSheetsException(ERROR_TYPE.SHEETS_ILLEGAL_TEMPLATE_ERROR)
        }

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

    fun isExportFolder(response: JSONObject): Boolean {
        return response.getJSONObject("properties").getString("title").startsWith(context.getString(R.string.export_file_name_header))
                || response.getJSONArray("sheets").length() == 0
                || TextUtils.equals(response.getJSONArray("sheets").getJSONObject(0).getJSONObject("properties").getString("title"), context.getString(R.string.export_file_sheets_name))
    }

    fun isCorrectFirstRow(row: List<Any>) : Boolean {
        return row.indices.any { TextUtils.equals(context.getString(COLUMNS.values()[it].nameId), row[it] as String) }
    }

    fun importRowData(row : List<Any>) {
        var weightItem = WeightItemEntity()
        for (i in row.indices) {
            val str = row[i] as String
            Log.d("importRepository", "str = $str")
            try {
                when (COLUMNS.values()[i]) {
                    COLUMNS.DATE -> {
                        val date = convertToCalendar(str, EXPORT_DATE_STR)
                        if (date == null) {
                            throw SpreadSheetsException(ERROR_TYPE.SHEETS_ILLEGAL_TEMPLATE_ERROR, -2)
                        } else {
                            weightItem.recTime = date
                        }
                    }
                    COLUMNS.WEIGHT -> {
                        weightItem.weight = str.toDouble()
                    }
                    COLUMNS.FAT -> {
                        weightItem.fat = str.toDouble()
                    }
                    COLUMNS.DUMBBELL -> {
                        weightItem.showDumbbell = str.toBoolean()
                    }
                    COLUMNS.TOILET -> {
                        weightItem.showToilet = str.toBoolean()
                    }
                    COLUMNS.MOON -> {
                        weightItem.showMoon = str.toBoolean()
                    }
                    COLUMNS.LIQUOR -> {
                        weightItem.showLiquor = str.toBoolean()
                    }
                    COLUMNS.STAR -> {
                        weightItem.showStar = str.toBoolean()
                    }
                    COLUMNS.MEMO -> {
                        weightItem.memo = str
                    }
                }
            } catch (e:Exception) {
                e.printStackTrace()
                throw SpreadSheetsException(ERROR_TYPE.SHEETS_ILLEGAL_TEMPLATE_ERROR, -3)
            }
        }
        WeightItemRepository.getDatabase(context).weightItemDao().insert(weightItem)
    }

}