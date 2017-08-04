package com.starmaine777.recweight.data

import android.arch.lifecycle.ViewModel
import android.content.Context
import android.util.Log
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.*

/**
 * ShowRecords操作ViewModel
 * Created by ai on 2017/07/02.
 */

class ShowRecordsViewModel : ViewModel() {

    fun getWeightItemList(context: Context): Flowable<List<WeightItemEntity>> {
        return WeightItemRepository.getWeightItemList(context)
    }
}
