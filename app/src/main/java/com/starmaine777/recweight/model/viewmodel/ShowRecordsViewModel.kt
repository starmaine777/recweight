package com.starmaine777.recweight.model.viewmodel

import androidx.lifecycle.*
import com.starmaine777.recweight.R
import com.starmaine777.recweight.data.entity.WeightItemEntity
import com.starmaine777.recweight.model.usecase.DeleteWeightItemUseCase
import com.starmaine777.recweight.model.usecase.GetWeightItemsUseCase
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.launch
import java.math.BigDecimal

/**
 * ShowRecords操作ViewModel
 * Created by ai on 2017/07/02.
 */

class ShowRecordsViewModel(
    private val getItemsUseCase: GetWeightItemsUseCase,
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
            weightItemList = getItemsUseCase.getItems()
            _viewData.postValue(
                ViewData(
                    state = State.Idle,
                    calculateChartFat(weightItemList)
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

    /**
     * LineChart用のDataを作成する
     * @param context Context
     * @param showStamp どのStampをiconとして表示させるかの区分
     * @return first = WeightのEntryList, second = FatのEntryList
     */
    fun calculateChartFat(rowData: List<WeightItemEntity>): List<WeightItemEntity> {
        val result = mutableListOf<WeightItemEntity>()
        val reverseItemList = ArrayList(rowData).apply { this.reverse() }
        val tempItems = mutableListOf<WeightItemEntity>()

        for (item in reverseItemList) {
            when {
                item.fat == 0.0 -> {
                    tempItems.add(item)
                }
                tempItems.isEmpty() -> {
                    result.add(item.apply { chartFat = item.fat })
                }
                result.isEmpty() -> {
                    tempItems.forEach { tempItem ->
                        result.add(tempItem.apply { chartFat = item.fat })
                    }
                    result.add(item.apply { chartFat = item.fat })
                    tempItems.clear()
                }
                else -> {
                    val startSource = result.last()
                    val lastSource = item.apply { chartFat = item.fat }
                    val slope = calculateFatSlope(startSource, lastSource)
                    tempItems.forEach { tempItem ->
                        result.add(
                            tempItem.apply { chartFat = calculateFat(slope, startSource, tempItem) }
                        )
                    }
                    result.add(lastSource)
                    tempItems.clear()
                }
            }
        }

        // fatなしで未計算リストを処理
        if (tempItems.isNotEmpty()) {
            val fat = if (result.isEmpty()) 0.0 else result.last().fat
            tempItems.forEach { tempItem ->
                result.add(tempItem.apply { chartFat = fat })
            }
        }

        return result
    }

    fun updateChartSourceStamp(stamp: ShowStamp) {
        // TODO : 実装
    }

    private fun calculateFatSlope(start: WeightItemEntity, end: WeightItemEntity): Double =
        ((end.fat - start.fat) / (end.recTime.timeInMillis - start.recTime.timeInMillis))

    private fun calculateFat(
        slope: Double,
        start: WeightItemEntity,
        targetWeightItem: WeightItemEntity
    ): Double {
        var bd =
            BigDecimal((start.fat + slope * (targetWeightItem.recTime.timeInMillis - start.recTime.timeInMillis)))
        bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP)
        return bd.toDouble()
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
        private val getItemsUseCase: GetWeightItemsUseCase,
        private val deleteItemUseCase: DeleteWeightItemUseCase
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ShowRecordsViewModel::class.java)) {
                return ShowRecordsViewModel(
                    getItemsUseCase,
                    deleteItemUseCase
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
