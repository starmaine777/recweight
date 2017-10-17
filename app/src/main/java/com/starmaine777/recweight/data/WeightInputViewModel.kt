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

    /**
     * IDからEntityを取得する.
     * @param context Context
     * @param id 対象のid. nullの時はcreate扱い.
     * @return inputEntityを設定後のCompletableFromAction
     */
    fun selectedEntityId(context: Context, id: Long?): CompletableFromAction =
            CompletableFromAction(Action {
                Timber.d("selectedEntityId id = $id")
                if (id == null) {
                    inputEntity = WeightItemEntity()
                    originalEntity = inputEntity
                    isCreate = true
                } else {

                    val idList = WeightItemRepository.getWeightItemById(context, id)
                    if (idList.isEmpty()) {
                        inputEntity = WeightItemEntity()
                        isCreate = true
                    } else {
                        inputEntity = idList[0]
                        isCreate = false
                    }
                    originalEntity = inputEntity.copy()
                    calendar = inputEntity.recTime.clone() as Calendar
                    Timber.d("selectedEntity getInputEntities $inputEntity")
                }

            })

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
                val nearTimeItems = WeightItemRepository.getNearTimeItems(context, inputEntity.recTime)
                nearTimeItems.second?.let {
                    WeightItemRepository.calculateDiffs(nearTimeItems.second, inputEntity)
                    WeightItemRepository.updateWeightItem(context, nearTimeItems.second!!)
                }

                WeightItemRepository.calculateDiffs(inputEntity, nearTimeItems.first)
                WeightItemRepository.insertWeightItem(context, inputEntity)
            })

    private fun updateWeightItem(context: Context, orgRecTime: Calendar): CompletableFromAction =
            CompletableFromAction(Action {
                val nearTimeItems = WeightItemRepository.getNearTimeItems(context, inputEntity.recTime)

                nearTimeItems.second?.let {
                    WeightItemRepository.calculateDiffs(nearTimeItems.second, inputEntity)
                    WeightItemRepository.updateWeightItem(context, nearTimeItems.second!!)
                }

                WeightItemRepository.calculateDiffs(inputEntity, nearTimeItems.first)
                WeightItemRepository.updateWeightItem(context, inputEntity)

                val orgNearTimeItems = WeightItemRepository.getNearTimeItems(context, orgRecTime)
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
    fun deleteWeightItem(context: Context): CompletableFromAction = WeightItemRepository.deleteWeightItemWithDiffUpdate(context, inputEntity)

}
