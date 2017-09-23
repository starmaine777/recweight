package com.starmaine777.recweight.data

import android.arch.lifecycle.ViewModel
import android.content.Context
import io.reactivex.Flowable
import io.reactivex.internal.operators.completable.CompletableFromAction

/**
 * ShowRecords操作ViewModel
 * Created by ai on 2017/07/02.
 */

class ShowRecordsViewModel : ViewModel() {

    fun getWeightItemList(context: Context): Flowable<List<WeightItemEntity>> = WeightItemRepository.getWeightItemList(context)

    fun deleteItem(context: Context, weightItemEntity: WeightItemEntity): CompletableFromAction = WeightItemRepository.deleteWeightItem(context, weightItemEntity)
}
