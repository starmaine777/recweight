package com.starmaine777.recweight.data.repo

import android.content.Context
import com.starmaine777.recweight.data.AppDatabase
import com.starmaine777.recweight.data.entity.WeightItemEntity
import io.reactivex.Flowable
import io.reactivex.functions.Action
import io.reactivex.internal.operators.completable.CompletableFromAction
import java.util.*

/**
 * WeightItemEntity操作用Dao
 * Created by 0025331458 on 2017/08/04.
 */
class WeightItemRepository {

    companion object {

        private var appDataBase: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            if (appDataBase == null) {
                appDataBase = AppDatabase.build(context)
            }
            return appDataBase!!
        }

        fun getWeightItemList(context: Context): Flowable<List<WeightItemEntity>> =
                getDatabase(context).weightItemDao().getAllListDateSorted()

        fun getWeightItemListOnce(context: Context): List<WeightItemEntity> =
                getDatabase(context).weightItemDao().getAllListDateSortedOnce()

        fun getWeightItemById(context: Context, id: Long): List<WeightItemEntity> =
                getDatabase(context).weightItemDao().getWeightItemById(id)

        fun getWeightItemJustAfterRecTime(context: Context, recTime: Calendar): List<WeightItemEntity> =
                getDatabase(context).weightItemDao().getItemJustAfterRecTime(recTime)

        fun getWeightItemJustBeforeRecTime(context: Context, recTime: Calendar): List<WeightItemEntity> =
                getDatabase(context).weightItemDao().getItemJustBeforeRecTime(recTime)

        fun insertWeightItem(context: Context, weightItemEntity: WeightItemEntity) =
                getDatabase(context).weightItemDao().insertItem(weightItemEntity)

        fun updateWeightItem(context: Context, weightItemEntity: WeightItemEntity) =
                getDatabase(context).weightItemDao().updateItem(weightItemEntity)

        fun deleteWeightItem(context: Context, weightItemEntity: WeightItemEntity) =
                getDatabase(context).weightItemDao().deleteItem(weightItemEntity)

        fun deleteAllItemCompletable(context: Context): CompletableFromAction =
                CompletableFromAction(Action { getDatabase(context).weightItemDao().deleteAllItem() })

        /**
         * 現在表示しているEntityを削除する.
         * @param context Context.
         * @return 削除が完了したCompletableFromAction
         */
        fun deleteWeightItemWithDiffUpdate(context: Context, entity: WeightItemEntity): CompletableFromAction =
                CompletableFromAction(Action {
                    val nearTimeItems = getNearTimeItems(context, entity.recTime)
                    nearTimeItems.second?.let {
                        calculateDiffs(nearTimeItems.second, nearTimeItems.first)
                        updateWeightItem(context, nearTimeItems.second!!)
                    }
                    deleteWeightItem(context, entity)
                })

        /**
         * recTimeの直前/直後のEntityを取得
         * @param context Context
         * @param recTime 基準となる時間
         * @return first == 直前のEntity, second = 直後のEntity
         */
        fun getNearTimeItems(context: Context, recTime: Calendar): Pair<WeightItemEntity?, WeightItemEntity?> {
            val beforeItemList = getWeightItemJustBeforeRecTime(context, recTime)
            val beforeItem = if (beforeItemList.isEmpty()) null else beforeItemList[0]
            val afterItemList = getWeightItemJustAfterRecTime(context, recTime)
            val afterItem = if (afterItemList.isEmpty()) null else afterItemList[0]

            return Pair(beforeItem, afterItem)
        }

        /**
         * weightDiff,fatDiffの計算.
         * @param after 更新されるEntity.
         * @param before afterの直前のEntity
         */
        fun calculateDiffs(after: WeightItemEntity?, before: WeightItemEntity?) {
            after?.let {
                if (before == null) {
                    after.weightDiff = 0.0
                    after.fatDiff = 0.0
                } else {
                    after.weightDiff = after.weight - before.weight
                    after.fatDiff = after.fat - before.fat
                }
            }
        }
    }
}