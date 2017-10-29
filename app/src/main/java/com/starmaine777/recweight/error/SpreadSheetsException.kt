package com.starmaine777.recweight.error

/**
 * SpreadSheets処理時のException
 * Created by 0025331458 on 2017/08/22.
 */
class SpreadSheetsException(val type: ERROR_TYPE, val errorCode: Int, val target: String) : Exception() {

    constructor(type: ERROR_TYPE, target: String) : this(type, -1, target)

    constructor(type: ERROR_TYPE, errorCode: Int) : this(type, errorCode, "")

    constructor(type: ERROR_TYPE) : this(type, -1, "")

    enum class ERROR_TYPE {
        ACCOUNT_PERMISSION_DENIED,
        ACCOUNT_NOT_SELECTED,
        PLAY_SERVICE_AVAILABILITY_ERROR,
        DEVICE_OFFLINE,
        SHEETS_URL_ERROR,
        SHEETS_ILLEGAL_TEMPLATE_ERROR,
        NO_DATA,
        FATAL_ERROR,
    }

}
