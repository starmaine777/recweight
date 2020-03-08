package com.starmaine777.recweight.data

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.starmaine777.recweight.data.dao.WeightItemDao
import com.starmaine777.recweight.data.entity.WeightItemEntity


/**
 * DB
 * Created by ai on 2017/07/02.
 */

@Database(entities = arrayOf(WeightItemEntity::class), version = 1)
@TypeConverters(DBTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {

    companion object {
        const val DB_NAME = "app_db"
        private lateinit var instance: AppDatabase

        fun build(context: Context): AppDatabase {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    AppDatabase::class.java, DB_NAME)
                    .build()
            return instance
        }
    }

    abstract fun weightItemDao(): WeightItemDao
}