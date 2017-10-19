package com.starmaine777.recweight.views

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.starmaine777.recweight.R
import com.starmaine777.recweight.data.viewmodel.ShowRecordsViewModel
import kotlinx.android.synthetic.main.fragment_record_chart.*
import timber.log.Timber
import java.util.*

/**
 * 体重チャート
 * Created by 0025331458 on 2017/10/18.
 */
class RecordChartFragment : Fragment(), ShowRecordsFragment.ShowRecordsEventListener, AdapterView.OnItemSelectedListener {

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
        viewChart.isDragDecelerationEnabled = true
        viewChart.isDragEnabled = true
        spinnerDuration.onItemSelectedListener = this
    }

    override fun onResume() {
        super.onResume()
        showChart(true)
    }

    override fun updateListItem() {
        showChart(false)
    }

    private fun showChart(refreshPosition : Boolean) {
        if (viewModel.weightItemList == null) {

        } else {
            val values = ArrayList<Entry>()

            viewModel.weightItemList!!.mapTo(values) { Entry(it.recTime.timeInMillis.toFloat(), it.weight.toFloat()) }

            Collections.reverse(values)

            val dataSet = LineDataSet(values, "weight")
            dataSet.color = R.color.colorAccent
            dataSet.setDrawCircles(false)
            val data = LineData(dataSet)
            viewChart.data = data

            val nowDate = Calendar.getInstance().timeInMillis.toFloat()
            viewChart.setVisibleXRangeMaximum(nowDate - getStartCalendar().timeInMillis.toFloat())
            viewChart.setVisibleXRangeMinimum(nowDate - getStartCalendar().timeInMillis.toFloat())

            if (refreshPosition) viewChart.moveViewToX(nowDate)

            viewChart.invalidate()
        }
    }

    private fun getStartCalendar():Calendar {
        val result = Calendar.getInstance()
        val spinnerSelectedIndex = spinnerDuration.selectedItemPosition
        when (spinnerSelectedIndex) {
            0 -> result.add(Calendar.WEEK_OF_YEAR, -1)
            1 -> result.add(Calendar.MONTH, -1)
            2 -> result.add(Calendar.MONTH, -3)
            3 -> result.add(Calendar.YEAR, -1)
        }
        Timber.d("getStartCalendar index = $spinnerSelectedIndex , result = $result")
        return result
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        updateListItem()
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
    }

}