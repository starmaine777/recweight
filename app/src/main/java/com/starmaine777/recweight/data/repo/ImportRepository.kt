package com.starmaine777.recweight.data.repo

import android.content.Context
import android.text.TextUtils
import com.google.android.gms.common.GoogleApiAvailability
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.starmaine777.recweight.R
import com.starmaine777.recweight.data.entity.WeightItemEntity
import com.starmaine777.recweight.error.SpreadSheetsException
import com.starmaine777.recweight.error.SpreadSheetsException.ERROR_TYPE
import com.starmaine777.recweight.utils.*
import io.reactivex.Emitter
import io.reactivex.Observable
import org.json.JSONObject
import timber.log.Timber
import java.io.IOException
import java.net.URL

/**
 * Import用Api取得クラス
 * Created by 0025331458 on 2017/08/16.
 */
class ImportRepository(val context: Context) {

    companion object {
        private val READONLY_SCOPES = mutableListOf(SheetsScopes.SPREADSHEETS_READONLY)
        val PATH_PREFIX = "/spreadsheets/d/"
        val PATH_SUFFIX = "/edit"
    }

    val credential: GoogleAccountCredential  by lazy { GoogleAccountCredential.usingOAuth2(context, READONLY_SCOPES).setBackOff(ExponentialBackOff()) }

    fun getResultFromApi(urlStr: String): Observable<Int> {
        return Observable.create { emitter ->
            var sheetsId: String?
            try {
                val url = URL(urlStr)
                val path = url.path
                if (path.startsWith(PATH_PREFIX)) {
                    sheetsId = path.removePrefix(PATH_PREFIX)
                    sheetsId = sheetsId.removeSuffix(PATH_SUFFIX)
                } else {
                    emitter.onError(SpreadSheetsException(ERROR_TYPE.SHEETS_URL_ERROR))
                    return@create
                }
            } catch (e: Exception) {
                e.printStackTrace()
                emitter.onError(SpreadSheetsException(ERROR_TYPE.SHEETS_URL_ERROR))
                return@create
            }

            if (!isGooglePlayServiceAvailable(context)) {
                if (!emitter.isDisposed) {
                    val apiAvailability = GoogleApiAvailability.getInstance()
                    val statusCode = apiAvailability.isGooglePlayServicesAvailable(context)
                    val error = if (apiAvailability.isUserResolvableError(statusCode)) ERROR_TYPE.PLAY_SERVICE_AVAILABILITY_ERROR else ERROR_TYPE.FATAL_ERROR
                    emitter.onError(SpreadSheetsException(error, statusCode))
                }
            } else if (isAllowedAccountPermission(context)) {
                if (!emitter.isDisposed) {
                    emitter.onError(SpreadSheetsException(ERROR_TYPE.ACCOUNT_PERMISSION_DENIED))
                }
            } else if (!existsChoseAccount(context, credential)) {
                if (!emitter.isDisposed) {
                    emitter.onError(SpreadSheetsException(ERROR_TYPE.ACCOUNT_NOT_SELECTED))
                }
            } else if (!isDeviceOnline(context)) {
                if (!emitter.isDisposed) {
                    emitter.onError(SpreadSheetsException(ERROR_TYPE.DEVICE_OFFLINE))
                }
            } else {
                val transport = AndroidHttp.newCompatibleTransport()
                val jsonFactory = JacksonFactory.getDefaultInstance()
                val service = Sheets.Builder(transport, jsonFactory, credential)
                        .setApplicationName(context.getString(R.string.app_name))
                        .build()
                try {

                    val response = JSONObject(service.spreadsheets().get(sheetsId).execute())
                    checkFileTemplate(response)
                    val sheetRow = response.getJSONArray("sheets").getJSONObject(0).getJSONObject("properties").getJSONObject("gridProperties").getInt("rowCount")

                    emitter.onNext(sheetRow * 2)

                    val data = service.spreadsheets().values()
                            .get(sheetsId, "${context.getString(R.string.export_file_sheets_name)}!A1:I$sheetRow")
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
                        emitter.onNext(i)
                    }

                    updateAllItemDiff(context, emitter)
                    emitter.onComplete()
                } catch (e: Exception) {
                    if (!emitter.isDisposed) {
                        emitter.onError(e)
                    }
                }
            }
        }
    }


    @Throws(SpreadSheetsException::class, IOException::class)
    fun checkFileTemplate(response: JSONObject) {

        if (!isCollectFileName(response)) {
            throw SpreadSheetsException(ERROR_TYPE.SHEETS_ILLEGAL_TEMPLATE_ERROR, context.getString(R.string.err_import_illegal_file_name))
        }

        if (!isCollectSheetsSize(response)) {
            throw SpreadSheetsException(ERROR_TYPE.SHEETS_ILLEGAL_TEMPLATE_ERROR, context.getString(R.string.err_import_illegal_sheet_num))
        }

        if (!isCollectSheetsName(response)) {
            throw SpreadSheetsException(ERROR_TYPE.SHEETS_ILLEGAL_TEMPLATE_ERROR, context.getString(R.string.err_import_illegal_sheet_name))
        }

        val sheetColumn = response.getJSONArray("sheets").getJSONObject(0).getJSONObject("properties").getJSONObject("gridProperties").getInt("columnCount")

        if (sheetColumn != SHEETS_COLUMNS.values().size) {
            throw SpreadSheetsException(ERROR_TYPE.SHEETS_ILLEGAL_TEMPLATE_ERROR, context.getString(R.string.err_import_illegal_column_num))
        }
    }

    private fun isCollectFileName(response: JSONObject): Boolean
            = response.getJSONObject("properties").getString("title").startsWith(context.getString(R.string.export_file_name_header))

    private fun isCollectSheetsSize(response: JSONObject): Boolean
            = response.getJSONArray("sheets").length() == 1

    private fun isCollectSheetsName(response: JSONObject): Boolean
            = TextUtils.equals(response.getJSONArray("sheets").getJSONObject(0).getJSONObject("properties").getString("title"), context.getString(R.string.export_file_sheets_name))

    fun isCorrectFirstRow(row: List<Any>): Boolean
            = row.indices.any { TextUtils.equals(context.getString(SHEETS_COLUMNS.values()[it].nameId), row[it] as String) }

    @Throws(SpreadSheetsException::class)
    fun importRowData(row: List<Any>, rowNum: Int) {
        val weightItem = WeightItemEntity()
        for (i in row.indices) {
            val str = row[i] as String
            Timber.d("str = $str")
            try {
                when (SHEETS_COLUMNS.values()[i]) {
                    SHEETS_COLUMNS.DATE -> {
                        val date = convertToCalendar(str, EXPORT_DATE_STR)
                        if (date == null) {
                            throw SpreadSheetsException(ERROR_TYPE.SHEETS_ILLEGAL_TEMPLATE_ERROR, getErrorCell(i, rowNum))
                        } else {
                            weightItem.recTime = date
                        }
                    }
                    SHEETS_COLUMNS.WEIGHT -> {
                        weightItem.weight = str.toDouble()
                    }
                    SHEETS_COLUMNS.FAT -> {
                        weightItem.fat = str.toDouble()
                    }
                    SHEETS_COLUMNS.DUMBBELL -> {
                        weightItem.showDumbbell = str.toBoolean()
                    }
                    SHEETS_COLUMNS.TOILET -> {
                        weightItem.showToilet = str.toBoolean()
                    }
                    SHEETS_COLUMNS.MOON -> {
                        weightItem.showMoon = str.toBoolean()
                    }
                    SHEETS_COLUMNS.LIQUOR -> {
                        weightItem.showLiquor = str.toBoolean()
                    }
                    SHEETS_COLUMNS.STAR -> {
                        weightItem.showStar = str.toBoolean()
                    }
                    SHEETS_COLUMNS.MEMO -> {
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
            = SHEETS_COLUMNS.values()[columnNum].columnName + rowNum

    private fun updateAllItemDiff(context: Context, emitter: Emitter<Int>) {
        val allItem = WeightItemRepository.getWeightItemListOnce(context)

        for (i in allItem.indices) {
            val item = allItem[i]
            if (i == allItem.lastIndex) {
                item.weightDiff = 0.0
                item.fatDiff = 0.0
            } else {
                val beforeItem = allItem[i + 1]
                WeightItemRepository.calculateDiffs(item, beforeItem)
            }
            WeightItemRepository.updateWeightItem(context, item)
            emitter.onNext(allItem.size + i)
        }
    }

}