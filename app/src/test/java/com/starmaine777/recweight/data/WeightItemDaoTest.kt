package com.starmaine777.recweight.data

import android.text.TextUtils
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.starmaine777.recweight.data.entity.WeightItemEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import timber.log.Timber
import java.util.*

/**
 * WeightItemRepositoryのTest
 * Created by 0025331458 on 2017/10/05.
 */
@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class WeightItemDaoTest {

    private lateinit var database: AppDatabase
    private val testDispatcher = TestCoroutineDispatcher()
    private val testScope = TestCoroutineScope(testDispatcher)

    @Before
    fun initDb() {
        database = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            AppDatabase::class.java
        )
            .setTransactionExecutor(testDispatcher.asExecutor())
            .setQueryExecutor(testDispatcher.asExecutor())
            .allowMainThreadQueries().build()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun getAllWeightItemListNoUser() {
        testScope.runBlockingTest {
            Assert.assertTrue(database.weightItemDao().getAllListDateSorted().isEmpty())
        }
    }

    @Test
    fun getWeightItemListByIdNoUser() {
        Assert.assertTrue(database.weightItemDao().getWeightItemById(0).isEmpty())
    }

    @Test
    fun insertAndGetWeightById() {
        testScope.runBlockingTest {
            database.weightItemDao().insertItem(ITEM1)
            val items = database.weightItemDao().getAllListDateSorted()
            Assert.assertEquals(items.size, 1)
            Assert.assertEquals(items[0].id, 1L)
            Assert.assertEquals(items[0], ITEM1.copy(id = 1))
            val item = database.weightItemDao().getWeightItemById(1)

            Assert.assertEquals(item.size, 1)
            Assert.assertEquals(item[0].id, 1L)
            Assert.assertTrue(equalItems(item[0], ITEM1.copy(id = 1)))
        }
    }

    @Test
    fun updateWeightItem() {
        database.weightItemDao().insertItem(ITEM1)

        val item = database.weightItemDao().getWeightItemById(1)[0]
        val updatedItem = item.copy(
                weight = 55.0,
                fat = 25.0,
                showDumbbell = false,
                showLiquor = true,
                showMoon = false,
                showStar = true,
                showToilet = false,
                memo = "updatedMemo"
        )
        database.weightItemDao().updateItem(updatedItem)

        val idQueryItems = database.weightItemDao().getWeightItemById(1)

        Assert.assertEquals(idQueryItems.size, 1)
        Assert.assertEquals(idQueryItems, listOf(updatedItem))
    }

    @Test
    fun getAllListItemByRecTimeSorts() {
        val thirdItem = ITEM1.copy(id = 1)
        database.weightItemDao().insertItem(thirdItem)
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val secondItem = ITEM3.copy(recTime = calendar.clone() as Calendar, id = 2)
        database.weightItemDao().insertItem(secondItem)
        calendar.add(Calendar.DAY_OF_MONTH, 5)
        val firstItem = ITEM5.copy(recTime = calendar.clone() as Calendar, id = 3)
        database.weightItemDao().insertItem(firstItem)
        calendar.add(Calendar.DAY_OF_MONTH, -10)
        val fifthItem = ITEM2.copy(recTime = calendar.clone() as Calendar, id = 4)
        database.weightItemDao().insertItem(fifthItem)
        calendar.add(Calendar.DAY_OF_MONTH, 2)
        val forthItem = ITEM4.copy(recTime = calendar.clone() as Calendar, id = 5)
        database.weightItemDao().insertItem(forthItem)

        val sortedExpectedList = listOf(firstItem, secondItem, thirdItem, forthItem, fifthItem)
        testScope.runBlockingTest {
            val items = database.weightItemDao().getAllListDateSorted()
            Assert.assertEquals(sortedExpectedList, items)
        }
    }

    @Test
    fun getItemById() {
        database.weightItemDao().insertItem(ITEM1)
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val item3 = ITEM3.copy(recTime = calendar.clone() as Calendar)
        database.weightItemDao().insertItem(item3)
        calendar.add(Calendar.DAY_OF_MONTH, 5)
        val item5 = ITEM5.copy(recTime = calendar.clone() as Calendar)
        database.weightItemDao().insertItem(item5)
        calendar.add(Calendar.DAY_OF_MONTH, -10)
        val item2 = ITEM2.copy(recTime = calendar.clone() as Calendar)
        database.weightItemDao().insertItem(item2)
        calendar.add(Calendar.DAY_OF_MONTH, 2)
        val item4 = ITEM4.copy(recTime = calendar.clone() as Calendar)
        database.weightItemDao().insertItem(item4)

        val expectedList = listOf(ITEM1, item3, item5, item2, item4)
        val query2 = database.weightItemDao().getWeightItemById(2)
        Assert.assertEquals(query2.size, 1)
        Assert.assertEquals(query2[0].id, 2L)
        Assert.assertTrue(equalItems(query2[0], expectedList[1]))

        val query4 = database.weightItemDao().getWeightItemById(4)
        Assert.assertEquals(query4.size, 1)
        Assert.assertEquals(query4[0].id, 4L)
        Assert.assertTrue(equalItems(query4[0], expectedList[3]))
    }

    @Test
    fun getItemJustAfterTargetTime() {
        database.weightItemDao().insertItem(ITEM1)
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val item3 = ITEM3.copy(recTime = calendar.clone() as Calendar)
        database.weightItemDao().insertItem(item3)
        calendar.add(Calendar.DAY_OF_MONTH, 5)
        val item5 = ITEM5.copy(recTime = calendar.clone() as Calendar)
        database.weightItemDao().insertItem(item5)
        calendar.add(Calendar.DAY_OF_MONTH, -10)
        val item2 = ITEM2.copy(recTime = calendar.clone() as Calendar)
        database.weightItemDao().insertItem(item2)
        calendar.add(Calendar.DAY_OF_MONTH, 2)
        val item4 = ITEM4.copy(recTime = calendar.clone() as Calendar)
        database.weightItemDao().insertItem(item4)

        val sortedExpectedList = listOf(item2, item4, ITEM1, item3, item5)

        val afterItems = database.weightItemDao().getItemJustAfterRecTime(ITEM1.recTime)
        Assert.assertEquals(afterItems.size, 1)
        Assert.assertTrue(equalItems(afterItems[0], sortedExpectedList[3]))
    }

    @Test
    fun getItemJustBeforeTargetTime() {
        database.weightItemDao().insertItem(ITEM1)
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val item3 = ITEM3.copy(recTime = calendar.clone() as Calendar)
        database.weightItemDao().insertItem(item3)
        calendar.add(Calendar.DAY_OF_MONTH, 5)
        val item5 = ITEM5.copy(recTime = calendar.clone() as Calendar)
        database.weightItemDao().insertItem(item5)
        calendar.add(Calendar.DAY_OF_MONTH, -10)
        val item2 = ITEM2.copy(recTime = calendar.clone() as Calendar)
        database.weightItemDao().insertItem(item2)
        calendar.add(Calendar.DAY_OF_MONTH, 2)
        val item4 = ITEM4.copy(recTime = calendar.clone() as Calendar)
        database.weightItemDao().insertItem(item4)

        val sortedExpectedList = listOf(item2, item4, ITEM1, item3, item5)

        val beforeItems = database.weightItemDao().getItemJustBeforeRecTime(ITEM1.recTime)
        Assert.assertEquals(beforeItems.size, 1)
        Assert.assertTrue(equalItems(beforeItems[0], sortedExpectedList[1]))
    }

    @Test
    fun deleteWeightItem() {
        database.weightItemDao().insertItem(ITEM1)
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val item3 = ITEM3.copy(recTime = calendar.clone() as Calendar)
        database.weightItemDao().insertItem(item3)
        calendar.add(Calendar.DAY_OF_MONTH, 5)
        val item5 = ITEM5.copy(recTime = calendar.clone() as Calendar)
        database.weightItemDao().insertItem(item5)
        calendar.add(Calendar.DAY_OF_MONTH, -10)
        val item2 = ITEM2.copy(recTime = calendar.clone() as Calendar)
        database.weightItemDao().insertItem(item2)
        calendar.add(Calendar.DAY_OF_MONTH, 2)
        val item4 = ITEM4.copy(recTime = calendar.clone() as Calendar)
        database.weightItemDao().insertItem(item4)

        testScope.runBlockingTest {
            val allItemList = database.weightItemDao().getAllListDateSorted()
            database.weightItemDao().deleteItem(allItemList[2])
            val expectedList =
                listOf(allItemList[0], allItemList[1], allItemList[3], allItemList[4])
            val deletedAllItem = database.weightItemDao().getAllListDateSorted()
            Timber.d("allItemListSize = ${allItemList.size}, deletedAllItemSize = ${deletedAllItem.size}, expectedListSize = ${expectedList.size}")
            Assert.assertEquals(expectedList, database.weightItemDao().getAllListDateSorted())
        }
    }

    @Test
    fun deleteAllWeightItem() {
        database.weightItemDao().insertItem(ITEM1)
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val item3 = ITEM3.copy(recTime = calendar.clone() as Calendar)
        database.weightItemDao().insertItem(item3)
        calendar.add(Calendar.DAY_OF_MONTH, 5)
        val item5 = ITEM5.copy(recTime = calendar.clone() as Calendar)
        database.weightItemDao().insertItem(item5)
        calendar.add(Calendar.DAY_OF_MONTH, -10)
        val item2 = ITEM2.copy(recTime = calendar.clone() as Calendar)
        database.weightItemDao().insertItem(item2)
        calendar.add(Calendar.DAY_OF_MONTH, 2)
        val item4 = ITEM4.copy(recTime = calendar.clone() as Calendar)
        database.weightItemDao().insertItem(item4)

        testScope.runBlockingTest {
            Assert.assertTrue(database.weightItemDao().getAllListDateSorted().isNotEmpty())
            database.weightItemDao().deleteAllItem()
            Assert.assertTrue(database.weightItemDao().getAllListDateSorted().isEmpty())
        }
    }

    fun equalItems(item1: WeightItemEntity, item2: WeightItemEntity): Boolean = item1.recTime == item2.recTime
            && item1.weight == item2.weight
            && item1.fat == item2.fat
            && item1.showDumbbell == item2.showDumbbell
            && item1.showLiquor == item2.showLiquor
            && item1.showMoon == item2.showMoon
            && item1.showStar == item2.showStar
            && item1.showToilet == item2.showToilet
            && TextUtils.equals(item1.memo, item2.memo)

    val ITEM1 = WeightItemEntity(Calendar.getInstance(), 50.0, 20.0, true, false, true, false, true, "memo1")

    val ITEM2 = WeightItemEntity(Calendar.getInstance(), 52.0, 22.2, true, false, false, true, true, "memo2")

    val ITEM3 = WeightItemEntity(Calendar.getInstance(), 55.0, 25.0, false, true, true, false, false, "memo3")

    val ITEM4 = WeightItemEntity(Calendar.getInstance(), 54.5, 24.0, false, false, true, false, true, "memo4")

    val ITEM5 = WeightItemEntity(Calendar.getInstance(), 58.0, 25.5, true, false, true, true, false, "memo5")

}