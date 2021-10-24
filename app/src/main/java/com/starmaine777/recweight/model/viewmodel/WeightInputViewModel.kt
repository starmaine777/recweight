package com.starmaine777.recweight.model.viewmodel

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.*
import com.starmaine777.recweight.data.entity.WeightItemEntity
import com.starmaine777.recweight.data.repo.WeightItemRepository
import com.starmaine777.recweight.model.usecase.DeleteWeightItemUseCase
import io.reactivex.functions.Action
import io.reactivex.internal.operators.completable.CompletableFromAction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*

/**
 * WeightItemEntity操作ViewModel
 * Created by ai on 2017/07/02.
 */

class WeightInputViewModel(
    private val weightRepository: WeightItemRepository,
    private val deleteItemUseCase: DeleteWeightItemUseCase
) : ViewModel() {

    private val _viewData = MutableLiveData(ViewData(State.NotInitialized))
    val viewData: LiveData<ViewData>
        get() = _viewData

    data class ViewData(
        val state: State,
        val entity: WeightItemEntity? = null,
        val isUpdateMode: Boolean = false
    )

    enum class State {
        NotInitialized,
        Idle,
        Saving,
        Saved,
        Deleted,
    }

    companion object {
        val TAG = "WeightInputViewModel"
    }

    /**
     * IDからEntityを取得する.
     * @param context Context
     * @param id 対象のid. nullの時はcreate扱い.
     * @return inputEntityを設定後のCompletableFromAction
     */
    fun selectedEntityId(id: Long?) {
        Timber.d("selectedEntityId id = $id")
        if (id == null) {
            _viewData.postValue(ViewData(State.Idle, WeightItemEntity(), isUpdateMode = false))
        } else {
            viewModelScope.launch(Dispatchers.IO) {
                val idList = weightRepository.getWeightItemById(id)
                if (idList.isEmpty()) {
                    _viewData.postValue(
                        ViewData(
                            State.Idle,
                            WeightItemEntity(),
                            isUpdateMode = false
                        )
                    )
                } else {
                    _viewData.postValue(ViewData(State.Idle, idList[0], isUpdateMode = true))
                }
            }
        }

    }

    var inputEntity: WeightItemEntity = WeightItemEntity()

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
            if (_viewData.value?.isUpdateMode == true) {
                weightRepository.updateWeightItem(inputEntity)
            } else {
                weightRepository.insertWeightItem(inputEntity)
            }
        })
    }

    /**
     * 現在表示しているEntityを削除する.
     * @return 削除が完了したCompletableFromAction
     */
    fun deleteWeightItem() {
        viewModelScope.launch {
            deleteItemUseCase.deleteItem(inputEntity)
            _viewData.value = _viewData.value!!.copy(State.Deleted)
        }
    }

    class Factory(
        private val weightRepository: WeightItemRepository,
        private val deleteItemUseCase: DeleteWeightItemUseCase
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(WeightInputViewModel::class.java)) {
                return WeightInputViewModel(weightRepository, deleteItemUseCase) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }


}
