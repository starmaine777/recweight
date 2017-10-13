package com.starmaine777.recweight.data

import android.arch.lifecycle.ViewModel
import android.content.Context
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.*

/**
 * WeightItemEntity操作ViewModel
 * Created by ai on 2017/07/02.
 */

class WeightInputViewModel : ViewModel() {

    companion object {
        val TAG = "WeightInputViewModel"
    }

    var selectedEntityId: Long? = 0L
    fun selectedEntityId(context: Context, id: Long?, successCallback: () -> Unit, errorCallback: () -> Unit) {
        Timber.d("selectedEntityId id = $id")
        if (id == null) {
            selectedEntityId = null
            inputEntity = WeightItemEntity()
            originalEntity = inputEntity
            isCreate = true
        } else {
            selectedEntityId = id
            val disposable = CompositeDisposable()

            WeightItemRepository.getWeightItemById(context, selectedEntityId!!)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ t: List<WeightItemEntity> ->
                        if (t.isEmpty()) {
                            inputEntity = WeightItemEntity()
                            isCreate = true
                        } else {
                            inputEntity = t[0]
                            isCreate = false
                        }
                        originalEntity = inputEntity.copy()
                        calendar = inputEntity.recTime.clone() as Calendar
                        successCallback()
                        Timber.d("selectedEntity getInputEntities $inputEntity")
                        disposable.clear()
                    }, { _ ->
                        Timber.d("selectedEntity Error!")
                        errorCallback()
                        disposable.clear()
                    }
                    ).let { disposable.add(it) }
        }
    }

    var isCreate = true
    var inputEntity: WeightItemEntity = WeightItemEntity()
    private var originalEntity: WeightItemEntity? = inputEntity

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

        Timber.d("insertOrupdateWeightItem id = ${inputEntity.id}")
        inputEntity = inputEntity.copy(recTime = calendar,
                weight = weight,
                fat = fat,
                showDumbbell = showDumbbell,
                showLiquor = showLiquor,
                showToilet = showToilet,
                showMoon = showMoon,
                showStar = showStar,
                memo = memo)

        if (isCreate) {
            insertWeightItem(context, callback, {})
        } else {
            updateWeightItem(context, callback, {})
        }
    }

    private fun insertWeightItem(context: Context, successCallback: () -> Unit, errorCallback: () -> Unit) {
        var beforeWeight: WeightItemEntity? = null
        var afterWeight: WeightItemEntity? = null

        var disposable = CompositeDisposable()
        WeightItemRepository.getWeightItemJustBeforeRecTime(context, inputEntity.recTime)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap { t1 ->
                    Timber.d("insertWeightItem 1!")
                    if (!t1.isEmpty()) beforeWeight = t1[0]
                    return@flatMap WeightItemRepository.getWeightItemJustAfterRecTime(context, inputEntity.recTime)
                }
                .subscribe({
                    t1 ->
                    Timber.d("insertWeightItem 2!")
                    if (!t1.isEmpty()) afterWeight = t1[0]

                    afterWeight?.let {
                        afterWeight!!.weightDiff = afterWeight!!.weight - inputEntity.weight
                        afterWeight!!.fatDiff = afterWeight!!.fat - inputEntity.fat

                        WeightItemRepository.updateWeightItem(context, afterWeight!!)
                    }

                    if (beforeWeight != null) {
                        inputEntity.weightDiff = inputEntity.weight - beforeWeight!!.weight
                        inputEntity.fatDiff = inputEntity.fat - beforeWeight!!.fat
                    } else {
                        inputEntity.weightDiff = 0.0
                        inputEntity.fatDiff = 0.0
                    }

                    if (isCreate) {
                        WeightItemRepository.insertWeightItem(context, inputEntity)
                    } else {
                        WeightItemRepository.updateWeightItem(context, inputEntity)
                    }
                    disposable.clear()
                    successCallback()
                }, {
                    e ->
                    e.printStackTrace()
                    errorCallback()
                }).let { disposable.add(it) }
    }

    private fun updateWeightItem(context: Context, successCallback: () -> Unit, errorCallback: () -> Unit) {
        var beforeWeight: WeightItemEntity? = null
        var afterWeight: WeightItemEntity? = null

        var disposable = CompositeDisposable()
        WeightItemRepository.getWeightItemJustBeforeRecTime(context, inputEntity.recTime)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap { t1 ->
                    if (!t1.isEmpty()) beforeWeight = t1[0]
                    return@flatMap WeightItemRepository.getWeightItemJustAfterRecTime(context, inputEntity.recTime)
                }
                .flatMap {
                    t1 ->
                    if (!t1.isEmpty()) afterWeight = t1[0]
                    afterWeight?.let {
                        afterWeight!!.weightDiff = afterWeight!!.weight - inputEntity.weight
                        afterWeight!!.fatDiff = afterWeight!!.fat - inputEntity.fat

                        Timber.d("updateWeightItem after1 = $afterWeight")
                        WeightItemRepository.updateWeightItem(context, afterWeight!!)
                    }

                    if (beforeWeight != null) {
                        inputEntity.weightDiff = inputEntity.weight - beforeWeight!!.weight
                        inputEntity.fatDiff = inputEntity.fat - beforeWeight!!.fat
                    } else {
                        inputEntity.weightDiff = 0.0
                        inputEntity.fatDiff = 0.0
                    }

                    Timber.d("updateWeightItem update 1= $inputEntity")
                    WeightItemRepository.updateWeightItem(context, inputEntity)
                    return@flatMap WeightItemRepository.getWeightItemJustAfterRecTime(context, originalEntity!!.recTime)
                }
                .flatMap { t1 ->
                    if (!t1.isEmpty()) beforeWeight = t1[0]
                    return@flatMap WeightItemRepository.getWeightItemJustAfterRecTime(context, originalEntity!!.recTime)
                }
                .subscribe({
                    t1 ->
                    if (!t1.isEmpty()) afterWeight = t1[0]

                    afterWeight?.let {
                        if (beforeWeight == null) {
                            afterWeight!!.weightDiff = 0.0
                            afterWeight!!.fatDiff = 0.0
                        } else {
                            afterWeight!!.weightDiff = afterWeight!!.weight - beforeWeight!!.weight
                            afterWeight!!.fatDiff = afterWeight!!.fat - beforeWeight!!.fat
                        }
                        Timber.d("updateWeightItem after 1= $inputEntity")
                        WeightItemRepository.updateWeightItem(context, afterWeight!!)
                    }
                    disposable.clear()
                    successCallback()
                }, {
                    e ->
                    e.printStackTrace()
                    errorCallback()
                }).let { disposable.add(it) }
    }


    fun deleteWeightItem(context: Context, successCallback: () -> Unit, errorCallback: () -> Unit) {
        deleteWeightItem(context, inputEntity, successCallback, errorCallback)
    }

    private fun deleteWeightItem(context: Context, target: WeightItemEntity, successCallback: () -> Unit, errorCallback: () -> Unit) {
        var beforeWeight: WeightItemEntity? = null
        var afterWeight: WeightItemEntity? = null

        WeightItemRepository.getWeightItemJustBeforeRecTime(context, target.recTime)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap { t1 ->
                    Timber.d("deleteItem1")
                    if (!t1.isEmpty()) beforeWeight = t1[0]
                    return@flatMap WeightItemRepository.getWeightItemJustAfterRecTime(context, inputEntity.recTime)
                }
                .subscribe({
                    t1 ->
                    Timber.d("deleteItem2")
                    if (!t1.isEmpty()) afterWeight = t1[0]

                    afterWeight?.let {
                        if (beforeWeight == null) {
                            afterWeight!!.weightDiff = 0.0
                            afterWeight!!.fatDiff = 0.0
                        } else {
                            afterWeight!!.weightDiff = afterWeight!!.weight - beforeWeight!!.weight
                            afterWeight!!.fatDiff = afterWeight!!.fat - beforeWeight!!.fat
                        }
                        WeightItemRepository.updateWeightItem(context, afterWeight!!)
                    }
                    WeightItemRepository.deleteWeightItem(context, inputEntity)
                    successCallback()
                }, {
                    e ->
                    e.printStackTrace()
                    errorCallback()
                })
    }

}
