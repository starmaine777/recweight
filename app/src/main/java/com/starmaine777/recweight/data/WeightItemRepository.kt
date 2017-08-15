package com.starmaine777.recweight.data

import android.arch.persistence.room.Room
import android.content.Context
import io.reactivex.Flowable
import io.reactivex.functions.Action
import io.reactivex.internal.operators.completable.CompletableFromAction

/**
 * WeightItemEntity操作用Dao
 * Created by 0025331458 on 2017/08/04.
 */
class WeightItemRepository {

    companion object {

        fun getDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DB_NAME).build()
        }

        fun getWeightItemList(context: Context): Flowable<List<WeightItemEntity>> {
            return getDatabase(context).weightItemDao().getAllListDateSorted()
        }

        fun getWeightItemById(context: Context, id: Long): Flowable<List<WeightItemEntity>> {
            return getDatabase(context).weightItemDao().getWeightItemById(id)
        }

        fun insertWeightItem(context: Context, weightItemEntity: WeightItemEntity): CompletableFromAction {
            return CompletableFromAction(Action {
                getDatabase(context).weightItemDao().insert(weightItemEntity)
            })
        }

        fun updateWeightItem(context: Context, weightItemEntity: WeightItemEntity): CompletableFromAction {
            return CompletableFromAction(Action { getDatabase(context).weightItemDao().update(weightItemEntity) })
        }

        fun deleteWeightItem(context: Context, weightItemEntity: WeightItemEntity): CompletableFromAction {
            return CompletableFromAction(Action { getDatabase(context).weightItemDao().delete(weightItemEntity) })
        }
    }
}