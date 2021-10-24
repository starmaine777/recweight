package com.starmaine777.recweight.model.usecase

import com.starmaine777.recweight.data.entity.WeightItemEntity
import com.starmaine777.recweight.data.repo.WeightItemRepository
import java.math.BigDecimal

/**
 * Created by asami-san on 2021/10/18.
 * 体重レコードを取得する. 合わせて、chart表示用Fatも計算する
 */
class GetChartRecordsUseCase(private val weightItemRepository: WeightItemRepository) {

    suspend fun getItems(): List<WeightItemEntity> =
        calculateChartFat(weightItemRepository.getWeightItemList())

    /**
     * LineChart用のDataを作成する
     */
    private fun calculateChartFat(rowData: List<WeightItemEntity>): List<WeightItemEntity> {
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

}