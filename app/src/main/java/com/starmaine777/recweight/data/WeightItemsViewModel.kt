package com.starmaine777.recweight.data

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.persistence.room.Room
import android.util.Log
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Action
import io.reactivex.internal.operators.completable.CompletableFromAction
import io.reactivex.schedulers.Schedulers
import java.util.*

/**
 * WeightItemEntity操作DAO
 * Created by ai on 2017/07/02.
 */

class WeightItemsViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        val TAG = "WeightItemsViewModel"
    }

    var inputEntity: WeightItemEntity = WeightItemEntity(Calendar.getInstance(), 0.0, 0.0, false, false, false, false, false, "")

    fun getDatabase(): AppDatabase {
        return Room.databaseBuilder(getApplication(), AppDatabase::class.java, AppDatabase.DB_NAME).build()
    }

    fun getWeightItemList(): Flowable<List<WeightItemEntity>> {
        return getDatabase().weightItemDao().getAllListDateSorted()
    }

    fun getWeightItemById(id: Long): Flowable<List<WeightItemEntity>> {
        return getDatabase().weightItemDao().getWeightItemById(id)
    }

    fun insertWeightItem(weightItemEntity: WeightItemEntity): CompletableFromAction {
        return CompletableFromAction(Action {
            getDatabase().weightItemDao().insert(weightItemEntity)
        })
    }

    fun updateWeightItem(weightItemEntity: WeightItemEntity): CompletableFromAction {
        return CompletableFromAction(Action { getDatabase().weightItemDao().update(weightItemEntity) })
    }

    fun createInputEntity() {
        inputEntity = WeightItemEntity(Calendar.getInstance(), 0.0, 0.0, false, false, false, false, false, "")
    }

    /**
     * WeightItemを登録/更新する
     */
    fun insertOrUpdateWeightItem(recTime: Calendar,
                                 weight: Double,
                                 fat: Double,
                                 showDumbbell: Boolean,
                                 showLiquor: Boolean,
                                 showToilet: Boolean,
                                 showMoon: Boolean,
                                 showStar: Boolean,
                                 memo: String,
                                 callback: () -> Unit
    ) {

        Log.d(TAG, "insertOrupdateWeightItem id = ${inputEntity.id}")
        inputEntity = inputEntity.copy(recTime = recTime,
                weight = weight,
                fat = fat,
                showDumbbell = showDumbbell,
                showLiquor = showLiquor,
                showToilet = showToilet,
                showMoon = showMoon,
                showStar = showStar,
                memo = memo)

        val disposable = CompositeDisposable()


        disposable.add(
                getWeightItemById(inputEntity.id)
                        .subscribeOn(Schedulers.io())
                        .subscribe { t: List<WeightItemEntity> ->
                            Log.d(TAG, "getWeightItemEntityById $t")

                            if (t.isEmpty()) {
                                disposable.add(insertWeightItem(inputEntity)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe {
                                            Log.d(TAG, "insertEntity complete")
                                            callback()
                                            disposable.dispose()
                                        })
                            } else {
                                disposable.add(updateWeightItem(inputEntity)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe {
                                            Log.d(TAG, "updateItemComplete complete")
                                            callback()
                                            disposable.dispose()
                                        })
                            }
                        }
        )
    }
}
