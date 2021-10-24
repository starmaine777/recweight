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
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
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
import com.starmaine777.recweight.model.usecase.GetChartRecordsUseCase
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
            GetChartRecordsUseCase(weightItemRepository),
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
        }
        observeViewData()
    }

    private fun observeViewData() {
        viewModel.viewData.observe(viewLifecycleOwner) { data ->
            binding.apply {
                when {
                    data.records == null -> {
                        areaChart.visibility = View.GONE
                        textNoData.visibility = View.GONE
                    }
                    data.records.isEmpty() -> {
                        areaChart.visibility = View.GONE
                        textNoData.visibility = View.VISIBLE
                    }
                    else -> {
                        areaChart.visibility = View.VISIBLE
                        textNoData.visibility = View.GONE
                        showChart(data.records)
                        radioGroupStamps.setOnCheckedChangeListener { _, _ ->
                            showChart(data.records)
                        }
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
                return@setValueFormatter DateUtils.formatDateTime(
                    context,
                    value.toLong(),
                    DateUtils.FORMAT_SHOW_DATE.or(DateUtils.FORMAT_NUMERIC_DATE)
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

    private fun showChart(records: List<WeightItemEntity>) {
        val weightEntries = mutableListOf<Entry>()
        val fatEntries = mutableListOf<Entry>()
        val checkedStamp =
            Stamp.findStampFromMenuId(binding.radioGroupStamps.checkedRadioButtonId)
        val stampDrawer =
            if (checkedStamp != Stamp.NONE) ContextCompat.getDrawable(
                requireContext(),
                checkedStamp.drawableId
            ) else null

        records.forEach { resource ->
            weightEntries.add(
                Entry().apply {
                    x = resource.recTime.timeInMillis.toFloat()
                    y = resource.weight.toFloat()
                    icon = if (checkedStamp.needShowStamp(resource)) stampDrawer else null
                }
            )
            if (resource.fat > 0) {
                fatEntries.add(
                    Entry().apply {
                        x = resource.recTime.timeInMillis.toFloat()
                        y = resource.fat.toFloat()
                    }
                )
            }
        }

        binding.viewChart.apply {
            data = LineData(createFatLine(fatEntries), createWeightLine(weightEntries))
            val nowDate = Calendar.getInstance().timeInMillis.toFloat()
            setVisibleXRangeMaximum(nowDate - getStartCalendar().timeInMillis.toFloat())
            setVisibleXRangeMinimum(nowDate - getStartCalendar().timeInMillis.toFloat())

            setDrawMarkers(true)
            marker = ItemMarkerView(context, R.layout.marker_chart, records)
            invalidate()
        }

        binding.spinnerDuration.visibility = View.VISIBLE
        binding.radioGroupStamps.visibility = View.VISIBLE
    }

    private fun createWeightLine(entries: List<Entry>) =
        LineDataSet(entries, getString(R.string.weight_input_weight_title)).apply {
            color = ContextCompat.getColor(requireContext(), R.color.chart_weight)
            lineWidth = 2.0f
            setDrawCircles(false)
            axisDependency = YAxis.AxisDependency.LEFT
            setDrawIcons(true)
            iconsOffset = MPPointF(0F, -25F)
            setDrawValues(false)
        }

    private fun createFatLine(entries: List<Entry>) =
        if (entries.isEmpty()) null
        else LineDataSet(entries, getString(R.string.weight_input_fat_title)).apply {
            color = ContextCompat.getColor(requireContext(), R.color.chart_fat)
            lineWidth = 1.5f
            setDrawCircles(false)
            axisDependency = YAxis.AxisDependency.RIGHT
            setDrawValues(false)

            axisDependency = YAxis.AxisDependency.RIGHT
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

    override fun onItemSelected(
        p0: AdapterView<*>?,
        p1: View?,
        spinnerSelectedItemPosition: Int,
        p3: Long
    ) {
        updateGranularity(spinnerSelectedItemPosition)
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
    }

    /**
     * チャートデータタップ時に出てくるレコード詳細表示マーカー
     */
    @SuppressLint("ViewConstructor")
    class ItemMarkerView(
        context: Context?,
        layoutResource: Int,
        private var weightRecords: List<WeightItemEntity>
    ) : MarkerView(context, layoutResource) {
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
                target = weightRecords.firstOrNull { e.x == it.recTime.timeInMillis.toFloat() }
                if (target == null) {
                    visibility = View.INVISIBLE
                } else {
                    findViewById<TextView>(R.id.textWeight).text =
                        context.getString(
                            R.string.list_weight_pattern,
                            target!!.weight.toString()
                        )
                    findViewById<TextView>(R.id.textFat).text =
                        if (target!!.fat == 0.0) "" else context.getString(
                            R.string.list_fat_pattern,
                            target!!.fat.toString()
                        )
                    findViewById<TextView>(R.id.textDate).text = DateUtils.formatDateTime(
                        context, target!!.recTime.timeInMillis,
                        DateUtils.FORMAT_SHOW_YEAR
                            .or(DateUtils.FORMAT_SHOW_DATE)
                            .or(DateUtils.FORMAT_NUMERIC_DATE)
                            .or(DateUtils.FORMAT_SHOW_TIME)
                            .or(DateUtils.FORMAT_ABBREV_ALL)
                    )

                    findViewById<ImageButton>(R.id.showDumbbell).isSelected =
                        target!!.showDumbbell
                    findViewById<ImageButton>(R.id.showLiquor).isSelected =
                        target!!.showLiquor
                    findViewById<ImageButton>(R.id.showToilet).isSelected =
                        target!!.showToilet
                    findViewById<ImageButton>(R.id.showMoon).isSelected = target!!.showMoon
                    findViewById<ImageButton>(R.id.showStar).isSelected = target!!.showStar
                }
            }
        }
    }

    enum class Stamp(@IdRes val menuId: Int, @DrawableRes val drawableId: Int) {
        NONE(-1, -1),
        DUMBBELL(R.id.radioDumbbell, R.drawable.stamp_dumbbell_selected) {
            override fun needShowStamp(record: WeightItemEntity): Boolean = record.showDumbbell
        },
        LIQUOR(R.id.radioLiquor, R.drawable.stamp_liquor_selected) {
            override fun needShowStamp(record: WeightItemEntity): Boolean = record.showLiquor
        },
        TOILET(R.id.radioToilet, R.drawable.stamp_toilet_selected) {
            override fun needShowStamp(record: WeightItemEntity): Boolean = record.showToilet
        },
        MOON(R.id.radioMoon, R.drawable.stamp_moon_selected) {
            override fun needShowStamp(record: WeightItemEntity): Boolean = record.showMoon
        },
        STAR(R.id.radioStar, R.drawable.stamp_star_selected) {
            override fun needShowStamp(record: WeightItemEntity): Boolean = record.showStar
        };

        open fun needShowStamp(record: WeightItemEntity): Boolean = false

        companion object {
            fun findStampFromMenuId(@IdRes menuId: Int): Stamp =
                values().find { it.menuId == menuId } ?: NONE
        }

    }
}