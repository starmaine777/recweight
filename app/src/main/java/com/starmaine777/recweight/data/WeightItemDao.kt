package com.starmaine777.recweight.data

import android.arch.persistence.room.*
import io.reactivex.Flowable

/**
 * WeightItem操作Dao
 * Created by ai on 2017/07/02.
 */

@Dao
interface WeightItemDao {

    @Query("SELECT * FROM ${WeightItemEntity.TABLE_WEIGHT_ITEM} ORDER BY recTime DESC")
    fun getAllListDateSorted(): Flowable<List<WeightItemEntity>>

    @Query("SELECT * FROM ${WeightItemEntity.TABLE_WEIGHT_ITEM} WHERE ID = :id")
    fun getWeightItemById(id:Long): Flowable<List<WeightItemEntity>>

    @Insert
    fun insert(weightItemEntity: WeightItemEntity)

    @Update
    fun update(weightItemEntity: WeightItemEntity)

    @Delete
    fun delete(weightItemEntity: WeightItemEntity)
}