package com.starmaine777.recweight.utils

/**
 * Created by ai on 2017/07/01.
 * global consts
 */

enum class REQUESTS {
    INPUT_WEIGHT_ITEM,
    VIEW_WEIGHT_ITEM,
    INPUT_DATE,
    INPUT_TIME,
    SHOW_ACCOUNT_PICKER,
    SHOW_GOOGLE_PLAY_SERVICE,
    SHOW_ACCOUNT_PERMISSION,
    REQUEST_AUTHORIZATION,
    DELETE_ALL_WEIGHT_ITEMS
}

enum class WEIGHT_INPUT_MODE {
    INPUT,
    VIEW
}

enum class PREFERENCE_KEY {
    ACCOUNT_NAME
}

val EXPORT_DATE_STR = "yyyy/MM/dd HH:mm:ss"
