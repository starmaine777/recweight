package com.starmaine777.recweight.views

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.starmaine777.recweight.R
import com.starmaine777.recweight.data.viewmodel.ShowRecordsViewModel

/**
 * 体重チャート
 * Created by 0025331458 on 2017/10/18.
 */
class RecordChartFragment : Fragment(), ShowRecordsFragment.ShowRecordsEventListener {

    private lateinit var viewModel: ShowRecordsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(activity).get(ShowRecordsViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_record_chart, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun updateListItem() {
    }

}