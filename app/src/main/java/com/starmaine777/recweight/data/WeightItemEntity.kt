package com.starmaine777.recweight.data

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey
import java.util.*

/**
 * Created by ai on 2017/07/01.
 * Entity about weight and information
 */

@Entity(tableName = WeightItemEntity.Companion.TABLE_WEIGHT_ITEM)
data class WeightItemEntity(
        @ColumnInfo(name = COL_REC_TIME) var recTime: Calendar,
        @ColumnInfo(name = COL_WEIGHT) var weight: Double,
        @ColumnInfo(name = COL_WEIGHT_DIFF) var weightDiff: Double,
        @ColumnInfo(name = COL_FAT) var fat: Double,
        @ColumnInfo(name = COL_FAT_DIFF) var fatDiff: Double,
        @ColumnInfo(name = COL_SHOW_DUMBBELL) var showDumbbell: Boolean,
        @ColumnInfo(name = COL_SHOW_LIQUOR) var showLiquor: Boolean,
        @ColumnInfo(name = COL_SHOW_TOILET) var showToilet: Boolean,
        @ColumnInfo(name = COL_SHOW_MOON) var showMoon: Boolean,
        @ColumnInfo(name = COL_SHOW_STAR) var showStar: Boolean,
        @ColumnInfo(name = COL_MEMO) var memo: String,
        @ColumnInfo(name = COL_ID) @PrimaryKey(autoGenerate = true) var id: Long = 0) {

    @Ignore
    constructor() : this(Calendar.getInstance(), 0.0, 0.0, 0.0, 0.0, false, false, false, false, false, "")

    companion object {
        const val TABLE_WEIGHT_ITEM = "weightItem"

        const val COL_ID = "id"
        const val COL_REC_TIME = "recTime"
        const val COL_WEIGHT = "weight"
        const val COL_WEIGHT_DIFF = "weightDiff"
        const val COL_FAT = "fat"
        const val COL_FAT_DIFF = "fatDiff"
        const val COL_SHOW_DUMBBELL = "showDumbbell"
        const val COL_SHOW_LIQUOR = "showLiquor"
        const val COL_SHOW_TOILET = "showToilet"
        const val COL_SHOW_MOON = "showMoon"
        const val COL_SHOW_STAR = "showStar"
        const val COL_MEMO = "memo"

    }


    fun weightString(): String = if (weight == 0.0) "" else weight.toString()

    fun fatString(): String = if (fat == 0.0) "" else fat.toString()
}