package com.starmaine777.recweight.model.viewmodel

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.starmaine777.recweight.data.entity.WeightItemEntity
import com.starmaine777.recweight.data.repo.WeightItemRepository
import io.reactivex.functions.Action
import io.reactivex.internal.operators.completable.CompletableFromAction
import timber.log.Timber
import java.util.*

/**
 * WeightItemEntity操作ViewModel
 * Created by ai on 2017/07/02.
 */

class WeightInputViewModel(private val weightRepository: WeightItemRepository) : ViewModel() {

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

                    val idList = weightRepository.getWeightItemById(context, id)
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

    private var isCreate = true
    var inputEntity: WeightItemEntity = WeightItemEntity()
    private var originalEntity: WeightItemEntity? = inputEntity

    var calendar: Calendar = inputEntity.recTime.clone() as Calendar

    @Throws(SQLiteConstraintException::class)
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
             * @throws SQLiteConstraintException 同時刻のものがあった場合にthrowされる
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
        inputEntity.recTime.set(Calendar.SECOND, 0)
        inputEntity.recTime.set(Calendar.MILLISECOND, 0)

        return CompletableFromAction(Action {
            if (isCreate) {
                weightRepository.insertWeightItem(context, inputEntity)
            } else {
                weightRepository.updateWeightItem(context, inputEntity)
            }
        })
    }

    /**
     * 現在表示しているEntityを削除する.
     * @param context Context.
     * @return 削除が完了したCompletableFromAction
     */
    fun deleteWeightItem(context: Context): CompletableFromAction = CompletableFromAction(Action { weightRepository.deleteWeightItem(context, inputEntity) })

    class Factory(private val weightRepository: WeightItemRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(WeightInputViewModel::class.java)) {
                return WeightInputViewModel(weightRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }


}
