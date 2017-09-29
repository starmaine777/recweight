package com.starmaine777.recweight.utils

import com.starmaine777.recweight.R

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

val PREFERENCES_NAME = "RecWeightSettings"

enum class PREFERENCE_KEY {
    ACCOUNT_NAME,
    LONG_TAP
}

val EXPORT_TITLE_DATE_STR = "yyyyMMdd_HHmm"
val EXPORT_DATE_STR = "yyyy/MM/dd HH:mm:ss"
