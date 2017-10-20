package com.starmaine777.recweight.data

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import com.starmaine777.recweight.data.dao.WeightItemDao
import com.starmaine777.recweight.data.entity.WeightItemEntity

/**
 * Created by ai on 2017/07/02.
 */

@Database(entities = arrayOf(WeightItemEntity::class), version = 2)
@TypeConverters(DBTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {

    companion object {
        const val DB_NAME = "rec_weight_db"
    }

    abstract fun weightItemDao(): WeightItemDao
}