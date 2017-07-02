package com.starmaine777.recweight.data

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.persistence.room.Room

/**
 * Created by ai on 2017/07/02.
 */

class WeightItemsViewModel(application: Application) : AndroidViewModel(application) {

    fun getDatabase(): AppDatabase {
        return Room.databaseBuilder(getApplication(), AppDatabase::class.java, AppDatabase.DB_NAME).build()
    }


    fun getWeightItemList(): List<WeightItemEntity> {
        return getDatabase().weightItemDao().getAllListDateSorted()
    }

    fun insertWeightItem(weightItemEntity: WeightItemEntity) {
        return getDatabase().weightItemDao().insert(weightItemEntity)
    }

}
