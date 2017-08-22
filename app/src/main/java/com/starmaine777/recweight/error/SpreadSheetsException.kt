package com.starmaine777.recweight.error

/**
 * SpreadSheets処理時のException
 * Created by 0025331458 on 2017/08/22.
 */
class SpreadSheetsException(val type: ERROR_TYPE, val errorCode: Int) : Exception() {

    enum class ERROR_TYPE {
        ACCOUNT_PERMISSION_DENIED,
        ACCOUNT_NOT_SELECTED,
        PLAY_SERVICE_AVAILABILITY_ERROR,
        DEVICE_OFFLINE,
        FATAL_ERROR,
    }
}
