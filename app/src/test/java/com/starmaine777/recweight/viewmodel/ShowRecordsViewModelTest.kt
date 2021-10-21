package com.starmaine777.recweight.viewmodel

import com.starmaine777.recweight.data.entity.ChartSource
import com.starmaine777.recweight.data.entity.WeightItemEntity
import com.starmaine777.recweight.model.usecase.DeleteWeightItemUseCase
import com.starmaine777.recweight.model.usecase.GetWeightItemsUseCase
import com.starmaine777.recweight.model.viewmodel.ShowRecordsViewModel
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.util.*


/**
 * ShowRecordsViewModelのTest. 主にChartのデータ作成の処理をテストする
 * Created by Asami-san on 2018/01/15.
 */

class ShowRecordsViewModelTest {

    @MockK
    lateinit var getItemsUseCase: GetWeightItemsUseCase

    @MockK
    lateinit var deleteItemUseCase: DeleteWeightItemUseCase

    lateinit var viewModel: ShowRecordsViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        viewModel = ShowRecordsViewModel(getItemsUseCase, deleteItemUseCase)
    }

    @Test
    fun `createLineChartData_全てにfatあり`() {
        val rowData = createTestEntities(
            firstFatEmpty = false,
            secondFatEmpty = false,
            thirdFatEmpty = false,
            fourthFatEmpty = false,
            fifthFatEmpty = false
        )
        val result = viewModel.createLineSources(rowData)

        Assert.assertEquals(rowData.size, result.size)
        Assert.assertEquals(createExpectList(rowData, listOf(31.0, 30.0, 29.0, 28.0, 27.0)), result)
    }

    // 最後にFatがない==最後から3番目、2番目から計算
    @Test
    fun `createLineChartData_0fatなし`() {
        val rowData = createTestEntities(
            firstFatEmpty = true,
            secondFatEmpty = false,
            thirdFatEmpty = false,
            fourthFatEmpty = false,
            fifthFatEmpty = false
        )
        val result = viewModel.createLineSources(rowData)

        Assert.assertEquals(rowData.size, result.size)
        Assert.assertEquals(createExpectList(rowData, listOf(31.0, 30.0, 29.0, 28.0, 28.0)), result)
    }

    // 途中にFatがない==その前後から計算
    @Test
    fun `createLineChartData_12fatなし`() {
        val rowData = createTestEntities(
            firstFatEmpty = false,
            secondFatEmpty = true,
            thirdFatEmpty = true,
            fourthFatEmpty = false,
            fifthFatEmpty = false
        )
        val result = viewModel.createLineSources(rowData)

        Assert.assertEquals(rowData.size, result.size)
        Assert.assertEquals(createExpectList(rowData, listOf(31.0, 30.0, 29.0, 28.0, 27.0)), result)
    }

    // 最初にFatがない==最初にFatが出てくるまで一定
    @Test
    fun `createLineChartData_fat34なし`() {
        val rowData = createTestEntities(
            firstFatEmpty = false,
            secondFatEmpty = false,
            thirdFatEmpty = false,
            fourthFatEmpty = true,
            fifthFatEmpty = true
        )
        val result = viewModel.createLineSources(rowData)

        Assert.assertEquals(rowData.size, result.size)
        Assert.assertEquals(createExpectList(rowData, listOf(29.0, 29.0, 29.0, 28.0, 27.0)), result)
    }

    // 途中一か所しかFatがない==ずっと一定
    @Test
    fun `createLineChartData_fat0124なし`() {
        val rowData = createTestEntities(
            firstFatEmpty = true,
            secondFatEmpty = true,
            thirdFatEmpty = true,
            fourthFatEmpty = false,
            fifthFatEmpty = true
        )
        val result = viewModel.createLineSources(rowData)

        Assert.assertEquals(rowData.size, result.size)
        Assert.assertEquals(createExpectList(rowData, listOf(30.0, 30.0, 30.0, 30.0, 30.0)), result)
    }

    // 先頭だけFatがある==先頭と同じ
    @Test
    fun `createLineChartData_fat0123なし`() {
        val rowData = createTestEntities(
            firstFatEmpty = true,
            secondFatEmpty = true,
            thirdFatEmpty = true,
            fourthFatEmpty = true,
            fifthFatEmpty = false
        )
        val result = viewModel.createLineSources(rowData)

        Assert.assertEquals(rowData.size, result.size)
        Assert.assertEquals(createExpectList(rowData, listOf(31.0, 31.0, 31.0, 31.0, 31.0)), result)
    }

    // 全部Fatなし 0でくる
    @Test
    fun `createLineChartData_fat全部なし`() {
        val rowData = createTestEntities(
            firstFatEmpty = true,
            secondFatEmpty = true,
            thirdFatEmpty = true,
            fourthFatEmpty = true,
            fifthFatEmpty = true
        )
        val result = viewModel.createLineSources(rowData)

        Assert.assertEquals(rowData.size, result.size)
        Assert.assertEquals(createExpectList(rowData, listOf(0.0, 0.0, 0.0, 0.0, 0.0)), result)
    }

    private fun createTestEntities(
        firstFatEmpty: Boolean,
        secondFatEmpty: Boolean,
        thirdFatEmpty: Boolean,
        fourthFatEmpty: Boolean,
        fifthFatEmpty: Boolean
    ): List<WeightItemEntity> =
        listOf(
            WeightItemEntity(
                Calendar.getInstance(),
                54.0, if (firstFatEmpty) 0.0 else 27.0,
                showDumbbell = true,
                showLiquor = false,
                showToilet = false,
                showMoon = false,
                showStar = false,
                memo = "1st:54.0/27.0"
            ),
            WeightItemEntity(
                Calendar.getInstance().apply { this.add(Calendar.DAY_OF_YEAR, -2) },
                55.0, if (secondFatEmpty) 0.0 else 28.0,
                showDumbbell = false,
                showLiquor = true,
                showToilet = false,
                showMoon = false,
                showStar = false,
                memo = "2nd:55.0/28.0"
            ),
            WeightItemEntity(
                Calendar.getInstance().apply { this.add(Calendar.DAY_OF_YEAR, -4) },
                56.0, if (thirdFatEmpty) 0.0 else 29.0,
                showDumbbell = false,
                showLiquor = false,
                showToilet = true,
                showMoon = false,
                showStar = false,
                memo = "3rd:56.0/29.0"
            ),
            WeightItemEntity(
                Calendar.getInstance().apply { this.add(Calendar.DAY_OF_YEAR, -6) },
                57.0, if (fourthFatEmpty) 0.0 else 30.0,
                showDumbbell = false,
                showLiquor = false,
                showToilet = false,
                showMoon = true,
                showStar = false,
                memo = "4th:57.0/30.0"
            ),
            WeightItemEntity(
                Calendar.getInstance().apply { this.add(Calendar.DAY_OF_YEAR, -8) },
                58.0, if (fifthFatEmpty) 0.0 else 31.0,
                showDumbbell = false,
                showLiquor = false,
                showToilet = false,
                showMoon = false,
                showStar = true,
                memo = "5th:58.0/31.0"
            )
        )

    private fun createExpectList(rowData: List<WeightItemEntity>, fatList: List<Double>) = listOf(
        ChartSource(
            recTime = rowData[4].recTime,
            weight = 58.0,
            fat = fatList[0],
            showIcon = false
        ),
        ChartSource(
            recTime = rowData[3].recTime,
            weight = 57.0,
            fat = fatList[1],
            showIcon = false
        ),
        ChartSource(
            recTime = rowData[2].recTime,
            weight = 56.0,
            fat = fatList[2],
            showIcon = false
        ),
        ChartSource(
            recTime = rowData[1].recTime,
            weight = 55.0,
            fat = fatList[3],
            showIcon = false
        ),
        ChartSource(
            recTime = rowData[0].recTime,
            weight = 54.0,
            fat = fatList[4],
            showIcon = false
        ),
    )
}