package com.starmaine777.recweight.data.repo

import android.content.Context
import com.starmaine777.recweight.data.AppDatabase
import com.starmaine777.recweight.data.entity.WeightItemEntity
import io.reactivex.functions.Action
import io.reactivex.internal.operators.completable.CompletableFromAction
import java.util.*

/**
 * WeightItemEntity操作用Dao
 * Created by 0025331458 on 2017/08/04.
 */
class WeightItemRepository(context: Context) {

    private var appDataBase: AppDatabase = AppDatabase.build(context)

    suspend fun getWeightItemList(): List<WeightItemEntity> =
        appDataBase.weightItemDao().getAllListDateSorted()

    fun getWeightItemListOnce(): List<WeightItemEntity> =
        appDataBase.weightItemDao().getAllListDateSortedOnce()

    fun getWeightItemById(id: Long): List<WeightItemEntity> =
        appDataBase.weightItemDao().getWeightItemById(id)

    fun getWeightItemJustAfterRecTime(recTime: Calendar): List<WeightItemEntity> =
        appDataBase.weightItemDao().getItemJustAfterRecTime(recTime)

    fun getWeightItemJustBeforeRecTime(
        recTime: Calendar
    ): List<WeightItemEntity> =
        appDataBase.weightItemDao().getItemJustBeforeRecTime(recTime)

    fun insertWeightItem(weightItemEntity: WeightItemEntity) =
        appDataBase.weightItemDao().insertItem(weightItemEntity)

    fun updateWeightItem(weightItemEntity: WeightItemEntity) =
        appDataBase.weightItemDao().updateItem(weightItemEntity)

    fun deleteWeightItem(weightItemEntity: WeightItemEntity) =
        appDataBase.weightItemDao().deleteItem(weightItemEntity)

    fun deleteAllItemCompletable(): CompletableFromAction =
        CompletableFromAction(Action { appDataBase.weightItemDao().deleteAllItem() })

    /**
     * recTimeの直前/直後のEntityを取得
     * @param recTime 基準となる時間
     * @return first == 直前のEntity, second = 直後のEntity
     */
    fun getNearTimeItems(
        recTime: Calendar
    ): Pair<WeightItemEntity?, WeightItemEntity?> {
        val beforeItemList = getWeightItemJustBeforeRecTime(recTime)
        val beforeItem = if (beforeItemList.isEmpty()) null else beforeItemList[0]
        val afterItemList = getWeightItemJustAfterRecTime(recTime)
        val afterItem = if (afterItemList.isEmpty()) null else afterItemList[0]

        return Pair(beforeItem, afterItem)
    }
}