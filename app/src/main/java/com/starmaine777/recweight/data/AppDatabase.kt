package com.starmaine777.recweight.data

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase

/**
 * Created by ai on 2017/07/02.
 */

@Database(entities = arrayOf(WeightItemEntity::class), version = 1)
abstract class AppDatabase : RoomDatabase() {

    companion object {
        const val DB_NAME = "rec_weight_db"
    }

    abstract fun weightItemDao(): WeightItemDao
}