package com.starmaine777.recweight.views

import android.annotation.SuppressLint
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import com.starmaine777.recweight.R
import com.starmaine777.recweight.data.entity.WeightItemEntity
import com.starmaine777.recweight.data.viewmodel.ShowRecordsViewModel
import kotlinx.android.synthetic.main.fragment_record_chart.*
import timber.log.Timber
import java.util.*
import kotlin.collections.ArrayList

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
        val xAxis = viewChart.xAxis
        xAxis.setValueFormatter { value, _ ->
            return@setValueFormatter DateUtils.formatDateTime(context, value.toLong()
                    , DateUtils.FORMAT_SHOW_DATE.or(DateUtils.FORMAT_NUMERIC_DATE)
            )
        }
        val weightAxis = viewChart.axisLeft
        weightAxis.setValueFormatter { value, _ ->
            return@setValueFormatter value.toString() + "kg"
        }
        val fatAxis = viewChart.axisRight
        fatAxis.setValueFormatter { value, _ ->
            return@setValueFormatter value.toString() + "%"
        }

        spinnerDuration.onItemSelectedListener = this
        radioGroupStamps.setOnCheckedChangeListener { _, _ ->
            Timber.d("radioGroup onCheckedChangeListener ")
            showChart(false)
        }
    }

    override fun onResume() {
        super.onResume()
        showChart(true)
    }

    override fun updateListItem() {
        showChart(false)
    }

    private fun showChart(refreshPosition: Boolean) {
        if (viewModel.weightItemList == null) {

        } else {
            val weights = ArrayList<Entry>()
            val fats = ArrayList<Entry>()

            for (i in viewModel.weightItemList!!.indices) {
                val item = viewModel.weightItemList!![i]
                val weightEntry = Entry(item.recTime.timeInMillis.toFloat(), item.weight.toFloat(), getIcon(item))
                weights.add(weightEntry)

                if (item.fat == 0.0) {
                    if (viewModel.weightItemList!!.size <= 2) {
                        val modelEntity = if (i == 0) {
                            viewModel.weightItemList!![1]
                        } else {
                            viewModel.weightItemList!![0]
                        }
                        fats.add(Entry(item.recTime.timeInMillis.toFloat(), modelEntity.fat.toFloat()))
                    } else {

                        // 前後の値から中間地点を計算(先頭は後ろ二つ、最後尾は前二つから予測)
                        val firstIndex = if (i == 0) 1 else if (i == viewModel.weightItemList!!.size - 1) i - 3 else i - 1
                        val secondIndex = if (i == 0) 2 else if (i == viewModel.weightItemList!!.size - 1) i - 2 else i + 1

                        fats.add(Entry(item.recTime.timeInMillis.toFloat()
                                , getSlope(viewModel.weightItemList!![firstIndex], viewModel.weightItemList!![secondIndex]) * item.recTime.timeInMillis))
                    }
                } else {
                    fats.add(Entry(item.recTime.timeInMillis.toFloat(), item.fat.toFloat()))
                }
            }

            Collections.reverse(weights)
            Collections.reverse(fats)

            val weightDataSet = LineDataSet(weights, getString(R.string.weight_input_weight_title))
            weightDataSet.color = ContextCompat.getColor(context, R.color.chart_weight)
            weightDataSet.lineWidth = 2.0f
            weightDataSet.setDrawCircles(false)
            weightDataSet.axisDependency = YAxis.AxisDependency.LEFT
            weightDataSet.setDrawIcons(true)
            weightDataSet.iconsOffset = MPPointF(0F, -30F)
            weightDataSet.setDrawValues(false)

            val fatDataSet = LineDataSet(fats, getString(R.string.weight_input_fat_title))
            fatDataSet.color = ContextCompat.getColor(context, R.color.chart_fat)
            fatDataSet.lineWidth = 1.5f
            fatDataSet.setDrawCircles(false)
            fatDataSet.axisDependency = YAxis.AxisDependency.RIGHT
            fatDataSet.setDrawValues(false)

            val lineData = LineData(fatDataSet, weightDataSet)
            fatDataSet.axisDependency = YAxis.AxisDependency.RIGHT

            viewChart.data = lineData

            val nowDate = Calendar.getInstance().timeInMillis.toFloat()
            viewChart.setVisibleXRangeMaximum(nowDate - getStartCalendar().timeInMillis.toFloat())
            viewChart.setVisibleXRangeMinimum(nowDate - getStartCalendar().timeInMillis.toFloat())

            if (refreshPosition) viewChart.moveViewToX(nowDate)

            viewChart.setDrawMarkers(true)

            viewChart.marker = ItemMarkerView(context, R.layout.marker_chart, viewModel.weightItemList!!)

            viewChart.invalidate()
        }
    }

    private fun getIcon(item: WeightItemEntity): Drawable? {
        if (radioGroupStamps.checkedRadioButtonId == -1) {
            return null
        }

        var id = 0
        when (radioGroupStamps.checkedRadioButtonId) {
            R.id.radioDumbbell -> if (item.showDumbbell) id = R.drawable.stamp_dumbbell_selected
            R.id.radioLiquor -> if (item.showLiquor) id = R.drawable.stamp_liquor_selected
            R.id.radioToilet -> if (item.showToilet) id = R.drawable.stamp_toilet_selected
            R.id.radioMoon -> if (item.showMoon) id = R.drawable.stamp_moon_selected
            R.id.radioStar -> if (item.showStar) id = R.drawable.stamp_star_selected
        }

        return if (id == 0) null else ContextCompat.getDrawable(context, id)
    }

    private fun getSlope(after: WeightItemEntity, before: WeightItemEntity): Float = ((after.fat - before.fat) / (after.recTime.timeInMillis - before.recTime.timeInMillis)).toFloat()

    private fun getStartCalendar(): Calendar {
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

    @SuppressLint("ViewConstructor")
    class ItemMarkerView(context: Context?, layoutResource: Int, private var itemList: List<WeightItemEntity>) : MarkerView(context, layoutResource) {
        private var mOffset: MPPointF? = null
        private var target: WeightItemEntity? = null
        override fun getOffset(): MPPointF {
            if (mOffset == null) {
                mOffset = MPPointF((-(width / 2)).toFloat(), (-height).toFloat() - 100F)
            }
            return mOffset!!
        }

        @SuppressLint("WrongViewCast")
        override fun refreshContent(e: Entry?, highlight: Highlight?) {
            super.refreshContent(e, highlight)
            if (e == null) {
                visibility = View.INVISIBLE
                target = null
            } else {
                visibility = View.VISIBLE
                target = itemList.firstOrNull { e.x == it.recTime.timeInMillis.toFloat() }
                if (target == null) {
                    visibility = View.INVISIBLE
                } else {
                    findViewById<TextView>(R.id.textWeight).text = context.getString(R.string.list_weight_pattern, target!!.weight.toString())
                    findViewById<TextView>(R.id.textFat).text = if (target!!.fat == 0.0) "" else context.getString(R.string.list_fat_pattern, target!!.fat.toString())
                    findViewById<TextView>(R.id.textDate).text = DateUtils.formatDateTime(context, target!!.recTime.timeInMillis,
                            DateUtils.FORMAT_SHOW_YEAR
                                    .or(DateUtils.FORMAT_SHOW_DATE)
                                    .or(DateUtils.FORMAT_NUMERIC_DATE)
                                    .or(DateUtils.FORMAT_SHOW_TIME)
                                    .or(DateUtils.FORMAT_ABBREV_ALL))

                    findViewById<ImageButton>(R.id.showDumbbell).isSelected = target!!.showDumbbell
                    findViewById<ImageButton>(R.id.showLiquor).isSelected = target!!.showLiquor
                    findViewById<ImageButton>(R.id.showToilet).isSelected = target!!.showToilet
                    findViewById<ImageButton>(R.id.showMoon).isSelected = target!!.showMoon
                    findViewById<ImageButton>(R.id.showStar).isSelected = target!!.showStar
                }
            }
        }
    }

}