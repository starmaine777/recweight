package com.starmaine777.recweight.utils

import android.text.TextUtils
import android.util.Log
import java.util.regex.Pattern

/**
 * Created by ai on 2017/07/12.
 */

fun formatInputNumber(numStr: String, default: String): String {
    if (TextUtils.isEmpty(numStr)) return default

    val regex = "[-]?[0-9]+(\\.*[0-9]*)"
    val pattern = Pattern.compile(regex)

    if (!pattern.matcher(numStr).find()) {
        return default
    }

    val numStrSplit = numStr.split(".")
    when (numStrSplit.size) {
        0 -> return default
        1 -> return "$numStr.0"
        2 -> {
            when (numStrSplit[1].length) {
                1 -> return numStr
                2 -> return numStr
                else -> return String.format("%1.2f", numStr.toDouble())
            }
        }
        else -> return default
    }
}


