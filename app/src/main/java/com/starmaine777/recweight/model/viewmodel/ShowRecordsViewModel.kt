package com.starmaine777.recweight.model.viewmodel

import androidx.lifecycle.*
import com.starmaine777.recweight.data.entity.WeightItemEntity
import com.starmaine777.recweight.model.usecase.DeleteWeightItemUseCase
import com.starmaine777.recweight.model.usecase.GetChartRecordsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * ShowRecords操作ViewModel
 * Created by ai on 2017/07/02.
 */

class ShowRecordsViewModel(
    private val getChartRecordsUseCase: GetChartRecordsUseCase,
    private val deleteItemUseCase: DeleteWeightItemUseCase
) : ViewModel() {

    private val _viewData = MutableLiveData<ViewData>(ViewData(state = State.InitialLoading))
    val viewData: LiveData<ViewData>
        get() = _viewData

    fun getWeightItemList() {
        viewModelScope.launch(Dispatchers.IO) {
            _viewData.postValue(
                ViewData(
                    state = State.Idle,
                    getChartRecordsUseCase.getItems()
                )
            )
        }
    }

    /**
     * 現在表示しているEntityを削除する.
     * @return 削除が完了したCompletableFromAction
     */
    fun deleteItem(weightItemEntity: WeightItemEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            deleteItemUseCase.deleteItem(weightItemEntity)

            getWeightItemList()
        }
    }

    data class ViewData(
        val state: State,
        val records: List<WeightItemEntity>? = null,
    )

    enum class State {
        InitialLoading,
        Idle,
    }

    class Factory(
        private val getChartRecordsUseCase: GetChartRecordsUseCase,
        private val deleteItemUseCase: DeleteWeightItemUseCase
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ShowRecordsViewModel::class.java)) {
                return ShowRecordsViewModel(
                    getChartRecordsUseCase,
                    deleteItemUseCase
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
