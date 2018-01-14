package com.starmaine777.recweight.viewmodel

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import com.github.mikephil.charting.data.Entry
import com.starmaine777.recweight.data.entity.WeightItemEntity
import com.starmaine777.recweight.data.viewmodel.ShowRecordsViewModel
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*


/**
 * ShowRecordsViewModelのTest. 主にChartのデータ作成の処理をテストする
 * Created by Asami-san on 2018/01/15.
 */
@RunWith(AndroidJUnit4::class)
class ShowRecordsViewModelTest {

    private val viewModel = ShowRecordsViewModel()
    private val context = InstrumentationRegistry.getTargetContext()

    @Test
    fun `createLineChartData_全てにfatあり_Stampなし`() {
        viewModel.weightItemList = createTestEntities(false, false, false, false, false)
        val result = viewModel.createLineSources(context, ShowRecordsViewModel.ShowStamp.NONE)

        equalsLineSourcesAndItemList(result, -1)
    }

    // 最後にFatがない==最後から3番目、2番目から計算
    @Test
    fun `createLineChartData_0fatなし_StampDumbbell`() {
        viewModel.weightItemList = createTestEntities(true, false, false, false, false)
        val result = viewModel.createLineSources(context, ShowRecordsViewModel.ShowStamp.DUMBBELL)

        equalsLineSourcesAndItemList(result, 0)
    }

    // 途中にFatがない==その前後から計算
    @Test
    fun `createLineChartData_12fatなし_StampLiquor`() {
        viewModel.weightItemList = createTestEntities(false, true, true, false, false)
        val result = viewModel.createLineSources(context, ShowRecordsViewModel.ShowStamp.LIQUOR)

        equalsLineSourcesAndItemList(result, 1)
    }

    // 最初にFatがない==最初にFatが出てくるまで一定
    @Test
    fun `createLineChartData_fat34なし_StampToilet`() {
        viewModel.weightItemList = createTestEntities(false, false, false, true, true)
        val result = viewModel.createLineSources(context, ShowRecordsViewModel.ShowStamp.TOILET)

        equalsLineSourcesAndItemList(result, 2, 29.0F)
    }

    // 途中一か所しかFatがない==ずっと一定
    @Test
    fun `createLineChartData_fat0124なし_StampMoon`() {
        viewModel.weightItemList = createTestEntities(true, true, true, false, true)
        val result = viewModel.createLineSources(context, ShowRecordsViewModel.ShowStamp.MOON)

        equalsLineSourcesAndItemList(result, 3, 30.0F)
    }

    // 先頭だけFatがある==先頭と同じ
    @Test
    fun `createLineChartData_fat0123なし_StampStar`() {
        viewModel.weightItemList = createTestEntities(true, true, true, true, false)
        val result = viewModel.createLineSources(context, ShowRecordsViewModel.ShowStamp.STAR)

        equalsLineSourcesAndItemList(result, 4, 31.0F)
    }

    // 全部Fatなし==Listがemptyでくる
    @Test
    fun `createLineChartData_fat全部なし_StampStar`() {
        viewModel.weightItemList = createTestEntities(true, true, true, true, true)
        val result = viewModel.createLineSources(context, ShowRecordsViewModel.ShowStamp.STAR)

        equalsLineSourcesAndItemList(result, 4, isEmptyFatSource = true)
    }

    private fun createTestEntities(firstFatEmpty: Boolean, secondFatEmpty: Boolean, thirdFatEmpty: Boolean, fourthFatEmpty: Boolean, fifthFatEmpty: Boolean): List<WeightItemEntity> =
            listOf(
                    WeightItemEntity(Calendar.getInstance(), 54.0, if (firstFatEmpty) 0.0 else 27.0,
                            true, false, false, false, false, "1st:54.0/27.0"),
                    WeightItemEntity(Calendar.getInstance().apply { this.add(Calendar.DAY_OF_YEAR, -2) }, 55.0, if (secondFatEmpty) 0.0 else 28.0,
                            false, true, false, false, false, "2nd:55.0/28.0"),
                    WeightItemEntity(Calendar.getInstance().apply { this.add(Calendar.DAY_OF_YEAR, -4) }, 56.0, if (thirdFatEmpty) 0.0 else 29.0,
                            false, false, true, false, false, "3rd:56.0/29.0"),
                    WeightItemEntity(Calendar.getInstance().apply { this.add(Calendar.DAY_OF_YEAR, -6) }, 57.0, if (fourthFatEmpty) 0.0 else 30.0,
                            false, false, false, true, false, "4th:57.0/30.0"),
                    WeightItemEntity(Calendar.getInstance().apply { this.add(Calendar.DAY_OF_YEAR, -8) }, 58.0, if (fifthFatEmpty) 0.0 else 31.0,
                            false, false, false, false, true, "5th:58.0/31.0")
            )

    private fun equalsLineSourcesAndItemList(lineSource: Pair<List<Entry>, List<Entry>>, indexOfShowStamp: Int, standardFat: Float = 0F, isEmptyFatSource: Boolean = false) {
        for (i in 0..(viewModel.weightItemList.size - 1)) {
            val reverseIndex = viewModel.weightItemList.size - 1 - i
            Assert.assertEquals(lineSource.first[reverseIndex].x, viewModel.weightItemList[i].recTime.timeInMillis.toFloat())
            Assert.assertEquals(lineSource.first[reverseIndex].y, viewModel.weightItemList[i].weight.toFloat())
            if (indexOfShowStamp == i) {
                Assert.assertNotNull(lineSource.first[reverseIndex].icon)
            } else {
                Assert.assertNull(lineSource.first[reverseIndex].icon)
            }
            if (!isEmptyFatSource) {
                Assert.assertEquals(lineSource.second[reverseIndex].x, viewModel.weightItemList[i].recTime.timeInMillis.toFloat())
                if (viewModel.weightItemList[i].fat == 0.0) {
                    if (standardFat == 0F) {
                        Assert.assertEquals(lineSource.second[reverseIndex].y, 27.0F + i)
                    } else {
                        Assert.assertEquals(lineSource.second[reverseIndex].y, standardFat)
                    }
                } else {
                    Assert.assertEquals(lineSource.second[reverseIndex].y, viewModel.weightItemList[i].fat.toFloat())
                }
            }
        }

        if (isEmptyFatSource) {
            Assert.assertNotNull(lineSource.second)
            Assert.assertEquals(lineSource.second.size, 0)
        }
    }
}