package com.starmaine777.recweight.model.viewmodel

import android.content.Context
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.*
import com.github.mikephil.charting.data.Entry
import com.starmaine777.recweight.R
import com.starmaine777.recweight.data.entity.WeightItemEntity
import com.starmaine777.recweight.data.repo.WeightItemRepository
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
    private val weightRepository: WeightItemRepository,
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

    /**
     * LineChart用のDataを作成する
     * @param context Context
     * @param showStamp どのStampをiconとして表示させるかの区分
     * @return first = WeightのEntryList, second = FatのEntryList
     */
    fun createLineSources(context: Context, showStamp: ShowStamp): Pair<List<Entry>, List<Entry>> {
        val weights = ArrayList<Entry>()
        val fats = ArrayList<Entry>()
        val icon = if (showStamp == ShowStamp.NONE) null else AppCompatResources.getDrawable(
            context,
            showStamp.drawableId
        )
        val reverseItemList = ArrayList(weightItemList).apply { this.reverse() }

        var fatAddedIndex = -1
        for (i in reverseItemList.indices) {
            val item = reverseItemList[i]
            val weightEntry = Entry(
                item.recTime.timeInMillis.toFloat(),
                item.weight.toFloat(),
                if (needShowIcon(item, showStamp)) icon else null
            )
            weights.add(weightEntry)

            if (item.fat != 0.0) {
                if (fatAddedIndex == -1) {
                    (0..(i - 1)).mapTo(fats) {
                        Entry(
                            reverseItemList[it].recTime.timeInMillis.toFloat(),
                            reverseItemList[i].fat.toFloat()
                        )
                    }
                } else {
                    val start = reverseItemList[fatAddedIndex]
                    val end = reverseItemList[i]
                    val slope = calculateFatSlope(start, end)
                    if (fatAddedIndex != i - 1) {
                        // それまでのものを計算
                        ((fatAddedIndex + 1)..(i - 1)).mapTo(fats) {
                            Entry(
                                reverseItemList[it].recTime.timeInMillis.toFloat(),
                                calculateFat(slope, start, reverseItemList[it])
                            )
                        }
                    }
                }
                fats.add(Entry(item.recTime.timeInMillis.toFloat(), item.fat.toFloat()))
                fatAddedIndex = i
            }
        }

        // Fat未計算分を計算する
        if (fatAddedIndex > -1 && fatAddedIndex != weightItemList.size - 1) {
            if (fatAddedIndex == 0) {
                (1..(reverseItemList.size - 1)).mapTo(fats) {
                    Entry(reverseItemList[it].recTime.timeInMillis.toFloat(), fats[0].y)
                }
            } else {
                val start = fats[fatAddedIndex - 1]
                val end = fats[fatAddedIndex]
                val slope = calculateFatSlopeByFatEntry(start, end)
                ((fatAddedIndex + 1)..(reverseItemList.size - 1)).mapTo(fats) {
                    Entry(
                        reverseItemList[it].recTime.timeInMillis.toFloat(),
                        calculateFat(slope, reverseItemList[fatAddedIndex], reverseItemList[it])
                    )
                }
            }
        }
        return Pair(weights, fats)
    }

    /**
     * Iconを表示するかどうかの判別式
     * @params item 対象のWeightItemEntity
     * @params showStampTyp 表示するスタンプの種類
     * @return true == 表示, false == 非表示
     */
    private fun needShowIcon(item: WeightItemEntity, showStampType: ShowStamp): Boolean =
        when (showStampType) {
            ShowStamp.NONE -> false
            ShowStamp.DUMBBELL -> item.showDumbbell
            ShowStamp.LIQUOR -> item.showLiquor
            ShowStamp.TOILET -> item.showToilet
            ShowStamp.MOON -> item.showMoon
            ShowStamp.STAR -> item.showStar
        }

    private fun calculateFatSlopeByFatEntry(start: Entry, end: Entry): Double =
        ((end.y - start.y) / (end.x - start.x)).toDouble()

    private fun calculateFatSlope(start: WeightItemEntity, end: WeightItemEntity): Double =
        ((end.fat - start.fat) / (end.recTime.timeInMillis - start.recTime.timeInMillis))

    private fun calculateFat(
        slope: Double,
        start: WeightItemEntity,
        target: WeightItemEntity
    ): Float {
        var bd =
            BigDecimal((start.fat + slope * (target.recTime.timeInMillis - start.recTime.timeInMillis)))
        bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP)
        return bd.toFloat()
    }

    data class ViewData(
        val state: State,
        val list: List<WeightItemEntity>? = null
    )

    enum class State {
        InitialLoading,
        Idle,
    }

    class Factory(
        private val weightRepository: WeightItemRepository,
        private val getItemsUseCase: GetWeightItemsUseCase,
        private val deleteItemUseCase: DeleteWeightItemUseCase
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ShowRecordsViewModel::class.java)) {
                return ShowRecordsViewModel(
                    weightRepository,
                    getItemsUseCase,
                    deleteItemUseCase
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
