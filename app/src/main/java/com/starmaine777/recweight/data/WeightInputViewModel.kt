package com.starmaine777.recweight.data

import android.arch.lifecycle.ViewModel
import android.content.Context
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Action
import io.reactivex.internal.operators.completable.CompletableFromAction
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
     * 現在表示しているEntityを追加/更新する.
     * @param context Context.
     * @param weight
     * @param fat
     * @param showDumbbell
     * @param showLiquor
     * @param showToilet
     * @param showMoon
     * @param showStar
     * @param memo
     * @return 追加/更新が完了したCompletableFromAction
     */
    fun insertOrUpdateWeightItem(context: Context,
                                 weight: Double,
                                 fat: Double,
                                 showDumbbell: Boolean,
                                 showLiquor: Boolean,
                                 showToilet: Boolean,
                                 showMoon: Boolean,
                                 showStar: Boolean,
                                 memo: String): CompletableFromAction {

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
            return insertWeightItem(context)
        } else {
            return updateWeightItem(context, originalEntity!!.recTime)
        }
    }

    private fun insertWeightItem(context: Context): CompletableFromAction =
            CompletableFromAction(Action {
                val nearTimeItems = getNearTimeItems(context, inputEntity.recTime)
                nearTimeItems.second?.let {
                    WeightItemRepository.calculateDiffs(nearTimeItems.second, inputEntity)
                    WeightItemRepository.updateWeightItem(context, nearTimeItems.second!!)
                }

                WeightItemRepository.calculateDiffs(inputEntity, nearTimeItems.first)
                WeightItemRepository.insertWeightItem(context, inputEntity)
            })

    private fun updateWeightItem(context: Context, orgRecTime: Calendar): CompletableFromAction =
            CompletableFromAction(Action {
                val nearTimeItems = getNearTimeItems(context, inputEntity.recTime)

                nearTimeItems.second?.let {
                    WeightItemRepository.calculateDiffs(nearTimeItems.second, inputEntity)
                    WeightItemRepository.updateWeightItem(context, nearTimeItems.second!!)
                }

                WeightItemRepository.calculateDiffs(inputEntity, nearTimeItems.first)
                WeightItemRepository.updateWeightItem(context, inputEntity)

                val orgNearTimeItems = getNearTimeItems(context, orgRecTime)
                orgNearTimeItems.second?.let {
                    WeightItemRepository.calculateDiffs(orgNearTimeItems.second, orgNearTimeItems.first)
                    WeightItemRepository.updateWeightItem(context, orgNearTimeItems.second!!)
                }
            })

    /**
     * 現在表示しているEntityを削除する.
     * @param context Context.
     * @return 削除が完了したCompletableFromAction
     */
    fun deleteWeightItem(context: Context): CompletableFromAction =
            CompletableFromAction(Action {
                val nearTimeItems = getNearTimeItems(context, inputEntity.recTime)
                nearTimeItems.second?.let {
                    WeightItemRepository.calculateDiffs(nearTimeItems.second, nearTimeItems.first)
                    WeightItemRepository.updateWeightItem(context, nearTimeItems.second!!)
                }
                WeightItemRepository.deleteWeightItem(context, inputEntity)
            })

    /**
     * recTimeの直前/直後のEntityを取得
     * @param context Context
     * @param recTime 基準となる時間
     * @return first == 直前のEntity, second = 直後のEntity
     */
    private fun getNearTimeItems(context: Context, recTime: Calendar): Pair<WeightItemEntity?, WeightItemEntity?> {
        val beforeItemList = WeightItemRepository.getWeightItemJustBeforeRecTime(context, recTime)
        val beforeItem = if (beforeItemList.isEmpty()) null else beforeItemList[0]
        val afterItemList = WeightItemRepository.getWeightItemJustAfterRecTime(context, recTime)
        val afterItem = if (afterItemList.isEmpty()) null else afterItemList[0]

        return Pair(beforeItem, afterItem)
    }

}
