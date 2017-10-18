package com.starmaine777.recweight.data.viewmodel

import android.arch.lifecycle.ViewModel
import android.content.Context
import com.starmaine777.recweight.data.entity.WeightItemEntity
import com.starmaine777.recweight.data.repo.WeightItemRepository
import io.reactivex.Flowable
import io.reactivex.internal.operators.completable.CompletableFromAction

/**
 * ShowRecords操作ViewModel
 * Created by ai on 2017/07/02.
 */

class ShowRecordsViewModel : ViewModel() {

    var weightItemList:List<WeightItemEntity>? = null

    fun getWeightItemList(context: Context): Flowable<List<WeightItemEntity>> = WeightItemRepository.getWeightItemList(context)

    /**
     * 現在表示しているEntityを削除する.
     * @param context Context.
     * @return 削除が完了したCompletableFromAction
     */
    fun deleteItem(context: Context, weightItemEntity: WeightItemEntity): CompletableFromAction =
            WeightItemRepository.deleteWeightItemWithDiffUpdate(context, weightItemEntity)
}
