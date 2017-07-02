package com.starmaine777.recweight.data

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import java.util.*

/**
 * Created by ai on 2017/07/01.
 * Entity about weight and information
 */

@Entity(tableName = WeightItemEntity.Companion.TABLE_WEIGHT_ITEM)
data class WeightItemEntity(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = COL_ID)val id: Int = 0,
        @ColumnInfo(name = COL_REC_TIME)val recTime: Calendar,
        @ColumnInfo(name = COL_WEIGHT)val weight: Double,
        @ColumnInfo(name = COL_FAT)val fat: Double,
        @ColumnInfo(name = COL_SHOW_DUMBBELL)val showDumbbell: Boolean,
        @ColumnInfo(name = COL_SHOW_LIQUOR)val showLiquor: Boolean,
        @ColumnInfo(name = COL_SHOW_TOILET)val showToilet: Boolean,
        @ColumnInfo(name = COL_SHOW_MOON)val showMoon: Boolean,
        @ColumnInfo(name = COL_SHOW_STAR)val showStar: Boolean,
        @ColumnInfo(name = COL_MEMO)val memo: String) {

    companion object {
        const val TABLE_WEIGHT_ITEM = "weightItem"

        const val COL_ID = "id"
        const val COL_REC_TIME = "recTime"
        const val COL_WEIGHT = "weight"
        const val COL_FAT = "fat"
        const val COL_SHOW_DUMBBELL = "showDumbbell"
        const val COL_SHOW_LIQUOR = "showLiquor"
        const val COL_SHOW_TOILET = "showToilet"
        const val COL_SHOW_MOON = "showMoon"
        const val COL_SHOW_STAR = "showStar"
        const val COL_MEMO = "memo"

    }
}