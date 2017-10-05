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
        } else {
            selectedEntityId = id
            val disposable = CompositeDisposable()

            WeightItemRepository.getWeightItemById(context, selectedEntityId!!)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ t: List<WeightItemEntity> ->
                        if (t.isEmpty()) {
                            inputEntity = WeightItemEntity()
                        } else {
                            inputEntity = t[0]
                        }
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

    var inputEntity: WeightItemEntity = WeightItemEntity()

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

        val disposable = CompositeDisposable()


        disposable.add(
                WeightItemRepository.getWeightItemById(context, inputEntity.id)
                        .subscribeOn(Schedulers.io())
                        .subscribe { t: List<WeightItemEntity> ->
                            Timber.d("getWeightItemEntityById $t")

                            if (t.isEmpty()) {
                                disposable.add(WeightItemRepository.insertWeightItem(context, inputEntity)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe {
                                            Timber.d("insertEntity complete")
                                            callback()
                                            disposable.clear()
                                        })
                            } else {
                                disposable.add(WeightItemRepository.updateWeightItem(context, inputEntity)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe {
                                            Timber.d("updateItemComplete complete")
                                            callback()
                                            disposable.clear()
                                        })
                            }
                        }
        )
    }

    fun deleteWeightItem(context: Context, successCallback: () -> Unit, errorCallback: () -> Unit) {
        val disposable = CompositeDisposable()

        disposable.add(
                WeightItemRepository.deleteWeightItem(context, inputEntity)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe ({
                            successCallback()
                            disposable.dispose()
                        }, {
                            errorCallback()
                        }))

    }

}
