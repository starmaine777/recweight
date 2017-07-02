package com.starmaine777.recweight.data

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import android.arch.persistence.room.Update

/**
 * Created by ai on 2017/07/02.
 */

@Dao
interface WeightItemDao {

    @Query("SELECT * FROM ${WeightItemEntity.TABLE_WEIGHT_ITEM} ORDER BY recTime DESC")
    fun getAllListDateSorted(): List<WeightItemEntity>

    @Query("SELECT * FROM ${WeightItemEntity.TABLE_WEIGHT_ITEM} WHERE ${WeightItemEntity.COL_ID} = :id")
    fun getListById(id: Int): List<WeightItemEntity>

    @Insert
    fun insert(weightItemEntity: WeightItemEntity)

    @Update
    fun update(weightItemEntity: WeightItemEntity)

}