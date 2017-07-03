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
        @ColumnInfo(name = COL_REC_TIME)var recTime: Calendar = Calendar.getInstance(),
        @ColumnInfo(name = COL_WEIGHT)var weight: Double = 0.0,
        @ColumnInfo(name = COL_FAT)var fat: Double = 0.0,
        @ColumnInfo(name = COL_SHOW_DUMBBELL)var showDumbbell: Boolean = false,
        @ColumnInfo(name = COL_SHOW_LIQUOR)var showLiquor: Boolean = false,
        @ColumnInfo(name = COL_SHOW_TOILET)var showToilet: Boolean = false,
        @ColumnInfo(name = COL_SHOW_MOON)var showMoon: Boolean = false,
        @ColumnInfo(name = COL_SHOW_STAR)var showStar: Boolean = false,
        @ColumnInfo(name = COL_MEMO)var memo: String = "") {

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
    @ColumnInfo(name = COL_ID)
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}