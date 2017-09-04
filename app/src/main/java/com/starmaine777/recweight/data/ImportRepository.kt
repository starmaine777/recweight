package com.starmaine777.recweight.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.text.TextUtils
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
import com.starmaine777.recweight.utils.PREFERENCE_KEY
import com.starmaine777.recweight.utils.convertToCalendar
import com.starmaine777.recweight.utils.isDeviceOnline
import io.reactivex.Observable
import org.json.JSONObject
import timber.log.Timber
import java.io.IOException

/**
 * Import用Api取得クラス
 * Created by 0025331458 on 2017/08/16.
 */
class ImportRepository(val context: Context) {

    companion object {
        private val READONLY_SCOPES = mutableListOf(SheetsScopes.SPREADSHEETS_READONLY)
        val spreadSheetsId = "1AX6hePR64bAX9JB8G3Z4pHXk2VciKu9VrcaYW8J5f0Q"
    }

    enum class COLUMNS(val nameId: Int, val columnName: String) {
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

    val credential: GoogleAccountCredential  by lazy { GoogleAccountCredential.usingOAuth2(context, READONLY_SCOPES).setBackOff(ExponentialBackOff()) }

    @Throws(SpreadSheetsException::class, IOException::class)
    fun getResultFromApi(): Observable<Sheets.Spreadsheets.Values.BatchGet> {
        return Observable.create { emitter ->
            if (!isGooglePlayServiceAvailable(context)) {
                val apiAvailability = GoogleApiAvailability.getInstance()
                val statusCode = apiAvailability.isGooglePlayServicesAvailable(context)
                val error = if (apiAvailability.isUserResolvableError(statusCode)) ERROR_TYPE.PLAY_SERVICE_AVAILABILITY_ERROR else ERROR_TYPE.FATAL_ERROR
                emitter.onError(SpreadSheetsException(error, statusCode))
            } else if (isAllowedAccountPermission()) {
                emitter.onError(SpreadSheetsException(ERROR_TYPE.ACCOUNT_PERMISSION_DENIED))
            } else if (!existsChoicedAccount()) {
                emitter.onError(SpreadSheetsException(ERROR_TYPE.ACCOUNT_NOT_SELECTED))
            } else if (!isDeviceOnline(context)) {
                emitter.onError(SpreadSheetsException(ERROR_TYPE.DEVICE_OFFLINE))
            } else {
                val transport = AndroidHttp.newCompatibleTransport()
                val jsonFactory = JacksonFactory.getDefaultInstance()
                val service = Sheets.Builder(transport, jsonFactory, credential)
                        .setApplicationName(context.getString(R.string.app_name))
                        .build()

                try {
                    getDataFromApi(service)
                    emitter.onComplete()
                } catch (e: Exception) {
                    emitter.onError(e)
                }
            }
        }
    }

    @Throws(SpreadSheetsException::class, IOException::class)
    fun getDataFromApi(service: Sheets) {
        val response = JSONObject(service.spreadsheets().get(spreadSheetsId).execute())

        if (!isCollectFileName(response)) {
            throw SpreadSheetsException(ERROR_TYPE.SHEETS_ILLEGAL_TEMPLATE_ERROR, context.getString(R.string.err_import_illegal_file_name))
        }

        if (!isCollectSheetsSize(response)) {
            throw SpreadSheetsException(ERROR_TYPE.SHEETS_ILLEGAL_TEMPLATE_ERROR, context.getString(R.string.err_import_illegal_sheet_num))
        }

        if (!isCollectSheetsName(response)) {
            throw SpreadSheetsException(ERROR_TYPE.SHEETS_ILLEGAL_TEMPLATE_ERROR, context.getString(R.string.err_import_illegal_sheet_name))
        }

        Timber.d("isExportFolder is true")
        val sheetRow = response.getJSONArray("sheets").getJSONObject(0).getJSONObject("properties").getJSONObject("gridProperties").getInt("rowCount")
        val sheetColumn = response.getJSONArray("sheets").getJSONObject(0).getJSONObject("properties").getJSONObject("gridProperties").getInt("columnCount")

        if (sheetColumn != COLUMNS.values().size) {
            throw SpreadSheetsException(ERROR_TYPE.SHEETS_ILLEGAL_TEMPLATE_ERROR, context.getString(R.string.err_import_illegal_column_num))
        }

        val data = service.spreadsheets().values()
                .get(spreadSheetsId, "${context.getString(R.string.export_file_sheets_name)}!A1:I$sheetRow")
                .execute()
                .getValues()

        for (i in data.indices) {
            when (i) {
                0 -> {
                    if (!isCorrectFirstRow(data[i])) {
                        throw SpreadSheetsException(ERROR_TYPE.SHEETS_ILLEGAL_TEMPLATE_ERROR, context.getString(R.string.err_import_illegal_column_name))
                    }
                }
                else -> {
                    importRowData(data[i], i)
                }
            }
        }

        Timber.d("isExportFolder RowCount=$sheetRow")
    }

    fun isGooglePlayServiceAvailable(context: Context): Boolean {
        val apiAvailability = GoogleApiAvailability.getInstance()
        return apiAvailability.isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS
    }

    fun isAllowedAccountPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            Timber.d("isAllowedAccountPermission sdk version")
            return true
        } else {
            Timber.d("isAllowedAccountPermission readContactsPermissionCheck ${context.checkSelfPermission(Manifest.permission.GET_ACCOUNTS)}")
            return context.checkSelfPermission(Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED
        }
    }

    fun existsChoicedAccount() :Boolean {
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

    private fun isCollectFileName(response: JSONObject): Boolean
            = response.getJSONObject("properties").getString("title").startsWith(context.getString(R.string.export_file_name_header))

    private fun isCollectSheetsSize(response: JSONObject): Boolean
            = response.getJSONArray("sheets").length() == 1

    private fun isCollectSheetsName(response: JSONObject): Boolean
            = TextUtils.equals(response.getJSONArray("sheets").getJSONObject(0).getJSONObject("properties").getString("title"), context.getString(R.string.export_file_sheets_name))

    fun isCorrectFirstRow(row: List<Any>): Boolean
            = row.indices.any { TextUtils.equals(context.getString(COLUMNS.values()[it].nameId), row[it] as String) }

    fun importRowData(row: List<Any>, rowNum: Int) {
        val weightItem = WeightItemEntity()
        for (i in row.indices) {
            val str = row[i] as String
            Timber.d("str = $str")
            try {
                when (COLUMNS.values()[i]) {
                    COLUMNS.DATE -> {
                        val date = convertToCalendar(str, EXPORT_DATE_STR)
                        if (date == null) {
                            throw SpreadSheetsException(ERROR_TYPE.SHEETS_ILLEGAL_TEMPLATE_ERROR, getErrorCell(i, rowNum))
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
            } catch (e: Exception) {
                e.printStackTrace()
                throw SpreadSheetsException(ERROR_TYPE.SHEETS_ILLEGAL_TEMPLATE_ERROR, getErrorCell(i, rowNum))
            }
        }
        WeightItemRepository.getDatabase(context).weightItemDao().insertItem(weightItem)
    }

    private fun getErrorCell(columnNum: Int, rowNum: Int): String
            = COLUMNS.values()[columnNum].columnName + rowNum

}