package com.starmaine777.recweight.data

import android.arch.persistence.room.Room
import android.content.Context
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
                appDataBase = Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DB_NAME).build()
            }
            return appDataBase!!
        }

        fun getWeightItemList(context: Context): Flowable<List<WeightItemEntity>> =
                getDatabase(context).weightItemDao().getAllListDateSorted()

        fun getWeightItemById(context: Context, id: Long): Flowable<List<WeightItemEntity>> =
                getDatabase(context).weightItemDao().getWeightItemById(id)

        fun getWeightItemJustAfterRecTime(context: Context, recTime: Calendar): Flowable<List<WeightItemEntity>> =
                getDatabase(context).weightItemDao().getItemJustAfterRecTime(recTime)

        fun getWeightItemJustBeforeRecTime(context: Context, recTime: Calendar): Flowable<List<WeightItemEntity>> =
                getDatabase(context).weightItemDao().getItemJustBeforeRecTime(recTime)

        fun insertWeightItem(context: Context, weightItemEntity: WeightItemEntity): CompletableFromAction =
                CompletableFromAction(Action {
                    getDatabase(context).weightItemDao().insertItem(weightItemEntity)
                })

        fun updateWeightItem(context: Context, weightItemEntity: WeightItemEntity): CompletableFromAction =
                CompletableFromAction(Action { getDatabase(context).weightItemDao().updateItem(weightItemEntity) })

        fun deleteWeightItem(context: Context, weightItemEntity: WeightItemEntity): CompletableFromAction =
                CompletableFromAction(Action { getDatabase(context).weightItemDao().deleteItem(weightItemEntity) })

        fun deleteAllItem(context: Context): CompletableFromAction =
                CompletableFromAction(Action { getDatabase(context).weightItemDao().deleteAllItem() })
    }
}