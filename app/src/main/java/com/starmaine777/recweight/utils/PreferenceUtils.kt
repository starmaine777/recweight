package com.starmaine777.recweight.utils

import android.content.Context

/**
 * SharedPreferences操作Util
 * Created by shimizuasami on 2017/11/19.
 */

fun getBoolean(context: Context, key: String, default: Boolean): Boolean =
        context.getSharedPreferences(PREFERENCES_NAME
                , Context.MODE_PRIVATE).getBoolean(key, default)

fun updateBoolean(context: Context, key: String, value: Boolean) =
        context.getSharedPreferences(PREFERENCES_NAME
                , Context.MODE_PRIVATE).edit().putBoolean(key, value).apply()
