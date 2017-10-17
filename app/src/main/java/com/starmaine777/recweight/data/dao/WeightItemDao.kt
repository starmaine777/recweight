package com.starmaine777.recweight.data.dao

import android.arch.persistence.room.*
import com.starmaine777.recweight.data.entity.WeightItemEntity
import io.reactivex.Flowable
import java.util.*

/**
 * WeightItem操作Dao
 * Created by ai on 2017/07/02.
 */

@Dao
interface WeightItemDao {

    @Query("SELECT * FROM ${WeightItemEntity.TABLE_WEIGHT_ITEM} ORDER BY ${WeightItemEntity.COL_REC_TIME} DESC")
    fun getAllListDateSorted(): Flowable<List<WeightItemEntity>>

    @Query("SELECT * FROM ${WeightItemEntity.TABLE_WEIGHT_ITEM} ORDER BY ${WeightItemEntity.COL_REC_TIME} DESC")
    fun getAllListDateSortedOnce(): List<WeightItemEntity>

    @Query("SELECT * FROM ${WeightItemEntity.TABLE_WEIGHT_ITEM} WHERE ID = :id")
    fun getWeightItemById(id:Long): List<WeightItemEntity>

    @Query("SELECT * FROM ${WeightItemEntity.TABLE_WEIGHT_ITEM} WHERE ${WeightItemEntity.COL_REC_TIME} > :targetTime ORDER BY ${WeightItemEntity.COL_REC_TIME} ASC LIMIT 1")
    fun getItemJustAfterRecTime(targetTime:Calendar): List<WeightItemEntity>

    @Query("SELECT * FROM ${WeightItemEntity.TABLE_WEIGHT_ITEM} WHERE ${WeightItemEntity.COL_REC_TIME} < :targetTime ORDER BY ${WeightItemEntity.COL_REC_TIME} DESC LIMIT 1")
    fun getItemJustBeforeRecTime(targetTime:Calendar): List<WeightItemEntity>

    @Insert
    fun insertItem(weightItemEntity: WeightItemEntity)

    @Update
    fun updateItem(weightItemEntity: WeightItemEntity)

    @Delete
    fun deleteItem(weightItemEntity: WeightItemEntity)

    @Query("DELETE FROM ${WeightItemEntity.TABLE_WEIGHT_ITEM}")
    fun deleteAllItem()
}