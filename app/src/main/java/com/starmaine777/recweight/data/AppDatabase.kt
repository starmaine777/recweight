package com.starmaine777.recweight.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
            instance = Room.databaseBuilder(
                context.getApplicationContext(),
                AppDatabase::class.java, DB_NAME
            )
                .addMigrations(MIGRATION_1_2)
                .build()
            return instance
        }

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // ignoreなchartFatを足しただけなので何もしない
            }
        }
    }

    abstract fun weightItemDao(): WeightItemDao
}