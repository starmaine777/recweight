package com.starmaine777.recweight.views

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import com.starmaine777.recweight.R
import com.starmaine777.recweight.data.entity.WeightItemEntity
import com.starmaine777.recweight.data.repo.WeightItemRepository
import com.starmaine777.recweight.databinding.FragmentRecordChartBinding
import com.starmaine777.recweight.model.usecase.DeleteWeightItemUseCase
import com.starmaine777.recweight.model.usecase.GetWeightItemsUseCase
import com.starmaine777.recweight.model.viewmodel.ShowRecordsViewModel
import com.starmaine777.recweight.utils.formatInputNumber
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * 体重チャート
 * Created by 0025331458 on 2017/10/18.
 */
class RecordChartFragment : Fragment(), AdapterView.OnItemSelectedListener {

    companion object {
        val TAG = "RecordChartFragment "
    }

    private val weightItemRepository by lazy {
        WeightItemRepository(requireContext())
    }
    lateinit var viewModelFactory: ShowRecordsViewModel.Factory
    private val viewModel: ShowRecordsViewModel by activityViewModels { viewModelFactory }

    private lateinit var binding: FragmentRecordChartBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModelFactory = ShowRecordsViewModel.Factory(
            weightItemRepository,
            GetWeightItemsUseCase(weightItemRepository),
            DeleteWeightItemUseCase(weightItemRepository)
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRecordChartBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initChart()

        binding.apply {
            spinnerDuration.onItemSelectedListener = this@RecordChartFragment
            radioGroupStamps.setOnCheckedChangeListener { _, _ ->
                Timber.d("radioGroup onCheckedChangeListener ")
                showChart(false)
            }
        }
        observeViewData()
    }

    private fun observeViewData() {
        viewModel.viewData.observe(viewLifecycleOwner) { data ->
            binding.apply {
                when {
                    data.list == null -> {
                        areaChart.visibility = View.GONE
                        textNoData.visibility = View.GONE
                    }
                    data.list.isEmpty() -> {
                        areaChart.visibility = View.GONE
                        textNoData.visibility = View.VISIBLE
                    }
                    else -> {
                        areaChart.visibility = View.VISIBLE
                        textNoData.visibility = View.GONE
                        showChart(false)
                    }
                }
            }
        }
    }

    private fun initChart() {
        binding.viewChart.apply {
            isDragDecelerationEnabled = true
            isDragEnabled = true
            setNoDataText(getString(R.string.show_records_no_data))
            val xAxis = xAxis
            xAxis.isGranularityEnabled = true
            updateGranularity(binding.spinnerDuration.selectedItemPosition)
            xAxis.setValueFormatter { value, _ ->
                return@setValueFormatter DateUtils.formatDateTime(context, value.toLong(), DateUtils.FORMAT_SHOW_DATE.or(DateUtils.FORMAT_NUMERIC_DATE)
                        .or(DateUtils.FORMAT_NO_YEAR)
                )
            }
            val weightAxis = axisLeft
            weightAxis.setValueFormatter { value, _ ->
                return@setValueFormatter formatInputNumber(value.toString(), "0.0") + "kg"
            }
            val fatAxis = axisRight
            fatAxis.setValueFormatter { value, _ ->
                return@setValueFormatter formatInputNumber(value.toString(), "0.0") + "%"
            }
        }
    }

    private fun showChart(refreshPosition: Boolean) {
        val showStamp = getShowStamp()
        val lines = viewModel.createLineSources(requireContext(), showStamp)

        val lineData: LineData
        val weightDataSet = LineDataSet(lines.first, getString(R.string.weight_input_weight_title))
        weightDataSet.color = ContextCompat.getColor(requireContext(), R.color.chart_weight)
        weightDataSet.lineWidth = 2.0f
        weightDataSet.setDrawCircles(false)
        weightDataSet.axisDependency = YAxis.AxisDependency.LEFT
        weightDataSet.setDrawIcons(true)
        weightDataSet.iconsOffset = MPPointF(0F, -25F)
        weightDataSet.setDrawValues(false)

        if (!lines.second.isEmpty()) {
            val fatDataSet = LineDataSet(lines.second, getString(R.string.weight_input_fat_title))
            fatDataSet.color = ContextCompat.getColor(requireContext(), R.color.chart_fat)
            fatDataSet.lineWidth = 1.5f
            fatDataSet.setDrawCircles(false)
            fatDataSet.axisDependency = YAxis.AxisDependency.RIGHT
            fatDataSet.setDrawValues(false)

            fatDataSet.axisDependency = YAxis.AxisDependency.RIGHT

            lineData = LineData(fatDataSet, weightDataSet)
        } else {
            lineData = LineData(weightDataSet)
        }

        binding.apply {
            viewChart.data = lineData

            val nowDate = Calendar.getInstance().timeInMillis.toFloat()
            viewChart.setVisibleXRangeMaximum(nowDate - getStartCalendar().timeInMillis.toFloat())
            viewChart.setVisibleXRangeMinimum(nowDate - getStartCalendar().timeInMillis.toFloat())

            if (refreshPosition) viewChart.moveViewToX(nowDate)

            viewChart.setDrawMarkers(true)
            // TODO : listを渡せるようにして修正
            viewModel.viewData.value?.list?.let {
                viewChart.marker = ItemMarkerView(context, R.layout.marker_chart, it)
            }

            spinnerDuration.visibility = View.VISIBLE
            radioGroupStamps.visibility = View.VISIBLE
            viewChart.invalidate()
        }
    }

    private fun getShowStamp(): ShowRecordsViewModel.ShowStamp =
            when (binding.radioGroupStamps.checkedRadioButtonId) {
                R.id.radioDumbbell -> ShowRecordsViewModel.ShowStamp.DUMBBELL
                R.id.radioLiquor -> ShowRecordsViewModel.ShowStamp.LIQUOR
                R.id.radioToilet -> ShowRecordsViewModel.ShowStamp.TOILET
                R.id.radioMoon -> ShowRecordsViewModel.ShowStamp.MOON
                R.id.radioStar -> ShowRecordsViewModel.ShowStamp.STAR
                else -> ShowRecordsViewModel.ShowStamp.NONE
            }

    private fun updateGranularity(spinnerSelectedItemPosition: Int) {
        binding.viewChart.xAxis.granularity =
                when (spinnerSelectedItemPosition) {
                    0 -> TimeUnit.DAYS.toMillis(1).toFloat()
                    1 -> TimeUnit.DAYS.toMillis(7).toFloat()
                    2 -> TimeUnit.DAYS.toMillis(30).toFloat()
                    3 -> TimeUnit.DAYS.toMillis(60).toFloat()
                    else -> 1f
                }
    }

    private fun getStartCalendar(): Calendar {
        val result = Calendar.getInstance()
        val spinnerSelectedIndex = binding.spinnerDuration.selectedItemPosition
        when (spinnerSelectedIndex) {
            0 -> result.add(Calendar.WEEK_OF_YEAR, -1)
            1 -> result.add(Calendar.MONTH, -1)
            2 -> result.add(Calendar.MONTH, -3)
            3 -> result.add(Calendar.YEAR, -1)
        }
        Timber.d("getStartCalendar index = $spinnerSelectedIndex , result = $result")
        return result
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, spinnerSelectedItemPosition: Int, p3: Long) {
        updateGranularity(spinnerSelectedItemPosition)
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