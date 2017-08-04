package com.starmaine777.recweight.data

import android.arch.lifecycle.ViewModel
import android.content.Context
import android.text.TextUtils
import android.util.Log
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.*
import java.util.logging.Logger

/**
 * WeightItemEntity操作ViewModel
 * Created by ai on 2017/07/02.
 */

class WeightInputViewModel : ViewModel() {

    companion object {
        val TAG = "WeightInputViewModel"
    }

    var selectedEntityId: Long? = 0L
    fun selectedEntityId(context: Context, id: Long?) {
        Log.d(TAG, "selectedEntityId id = $id")
        if (id == null) {
            selectedEntityId = null
            inputEntity = WeightItemEntity(Calendar.getInstance(), 0.0, 0.0, false, false, false, false, false, "")
        } else {
            selectedEntityId = id
            WeightItemRepository.getWeightItemById(context, selectedEntityId!!)
                    .subscribeOn(Schedulers.io())
                    .subscribe { t: List<WeightItemEntity> ->
                        if (t.isEmpty()) {
                            inputEntity = WeightItemEntity(Calendar.getInstance(), 0.0, 0.0, false, false, false, false, false, "")
                        } else {
                            inputEntity = t[0]
                        }
                        calendar = inputEntity.recTime.clone() as Calendar
                        Log.d(TAG, "selectedEntity getInputEntities $inputEntity")
                    }
        }
    }

    var inputEntity: WeightItemEntity = WeightItemEntity(Calendar.getInstance(), 0.0, 0.0, false, false, false, false, false, "")

    var calendar: Calendar = inputEntity.recTime.clone() as Calendar

    /**
     * WeightItemを登録/更新する
     */
    fun insertOrUpdateWeightItem(context: Context,
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
        inputEntity = inputEntity.copy(recTime = calendar,
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
                WeightItemRepository.getWeightItemById(context, inputEntity.id)
                        .subscribeOn(Schedulers.io())
                        .subscribe { t: List<WeightItemEntity> ->
                            Log.d(TAG, "getWeightItemEntityById $t")

                            if (t.isEmpty()) {
                                disposable.add(WeightItemRepository.insertWeightItem(context, inputEntity)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe {
                                            Log.d(TAG, "insertEntity complete")
                                            callback()
                                            disposable.dispose()
                                        })
                            } else {
                                disposable.add(WeightItemRepository.updateWeightItem(context, inputEntity)
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
