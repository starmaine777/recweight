package com.starmaine777.recweight.model.viewmodel

import androidx.lifecycle.*
import com.starmaine777.recweight.R
import com.starmaine777.recweight.data.entity.WeightItemEntity
import com.starmaine777.recweight.model.usecase.DeleteWeightItemUseCase
import com.starmaine777.recweight.model.usecase.GetChartRecordsUseCase
import io.reactivex.disposables.CompositeDisposable
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

    enum class ShowStamp(val drawableId: Int) {
        NONE(-1),
        DUMBBELL(R.drawable.stamp_dumbbell_selected),
        LIQUOR(R.drawable.stamp_liquor_selected),
        TOILET(R.drawable.stamp_toilet_selected),
        MOON(R.drawable.stamp_moon_selected),
        STAR(R.drawable.stamp_star_selected)
    }

    // TODO : 消す
    private var weightItemList: List<WeightItemEntity> = ArrayList()

    private val compositeDisposable = CompositeDisposable()

    fun getWeightItemList() {
        viewModelScope.launch {
            weightItemList = getChartRecordsUseCase.getItems()
            _viewData.postValue(
                ViewData(
                    state = State.Idle,
                    weightItemList
                )
            )
        }
    }


    /**
     * 現在表示しているEntityを削除する.
     * @return 削除が完了したCompletableFromAction
     */
    fun deleteItem(weightItemEntity: WeightItemEntity) {
        viewModelScope.launch {
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
