package com.starmaine777.recweight.views

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.starmaine777.recweight.R
import com.starmaine777.recweight.data.ShowRecordsViewModel
import com.starmaine777.recweight.data.WeightItemEntity
import com.starmaine777.recweight.event.InputFragmentStartEvent
import com.starmaine777.recweight.event.RxBus
import com.starmaine777.recweight.event.WeightItemClickEvent
import com.starmaine777.recweight.utils.Consts
import com.starmaine777.recweight.views.adapter.RecordListAdapter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_record_list.*

/**
 * 体重記録ListFragment
 * Created by ai on 2017/07/15.
 */

class RecordListFragment : Fragment() {

    companion object {
        val TAG = "RecordListFragment"
    }

    val mWeightItemVm: ShowRecordsViewModel by lazy { ViewModelProviders.of(activity).get(ShowRecordsViewModel::class.java) }
    val disposable = CompositeDisposable()


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater?.inflate(R.layout.fragment_record_list, container, false)
        return view
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerRecords.layoutManager = LinearLayoutManager(context)
    }

    override fun onStart() {
        super.onStart()
        disposable.add(mWeightItemVm.getWeightItemList(context)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    t1: List<WeightItemEntity>? ->
                    val adapter = RecordListAdapter(t1, context)
                    recyclerRecords.adapter = adapter
                }
                ))

        disposable.add(RxBus.subscribe(WeightItemClickEvent::class.java).subscribe({
            t: WeightItemClickEvent ->
            RxBus.publish(InputFragmentStartEvent(Consts.WEIGHT_INPUT_MODE.VIEW, t.weightItemEntity?.id))
        }))
    }

    override fun onStop() {
        super.onStop()

        disposable.dispose()
    }
}
