package com.starmaine777.recweight.data

import android.arch.persistence.room.TypeConverter
import java.util.*

/**
 * Created by 0025331458 on 2017/07/03.
 */
class DBTypeConverters {

    @TypeConverter
    fun fromTimeStamp(value: Long): Calendar {
        val result = Calendar.getInstance()
        result.timeInMillis = value
        return result
    }

    @TypeConverter
    fun calendarToTimestamp(calendar: Calendar): Long {
        return calendar.timeInMillis
    }

}
