package com.starmaine777.recweight.data

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.persistence.room.Room
import io.reactivex.Flowable
import io.reactivex.functions.Action
import io.reactivex.internal.operators.completable.CompletableFromAction
import java.util.*

/**
 * Created by ai on 2017/07/02.
 */

class WeightItemsViewModel(application: Application) : AndroidViewModel(application) {

    var inputEntity: WeightItemEntity = WeightItemEntity(Calendar.getInstance(), 0.0, 0.0, false, false, false, false, false, "")

    fun getDatabase(): AppDatabase {
        return Room.databaseBuilder(getApplication(), AppDatabase::class.java, AppDatabase.DB_NAME).build()
    }

    fun getWeightItemList(): Flowable<List<WeightItemEntity>> {
        return getDatabase().weightItemDao().getAllListDateSorted()
    }

    fun insertWeightItem(weightItemEntity: WeightItemEntity): CompletableFromAction {
        return CompletableFromAction(Action {
            getDatabase().weightItemDao().insert(weightItemEntity)
        })
    }
}
