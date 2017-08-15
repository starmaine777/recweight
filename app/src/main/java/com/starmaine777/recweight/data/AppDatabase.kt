package com.starmaine777.recweight.data

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters

/**
 * Created by ai on 2017/07/02.
 */

@Database(entities = arrayOf(WeightItemEntity::class), version = 1)
@TypeConverters(DBTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {

    companion object {
        const val DB_NAME = "rec_weight_db"
    }

    abstract fun weightItemDao(): WeightItemDao
}