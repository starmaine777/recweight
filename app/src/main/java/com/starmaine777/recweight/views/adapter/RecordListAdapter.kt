package com.starmaine777.recweight.views.adapter

import android.app.Activity
import android.content.Context
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.starmaine777.recweight.R
import com.starmaine777.recweight.data.entity.WeightItemEntity
import com.starmaine777.recweight.databinding.ItemWeightBinding
import com.starmaine777.recweight.event.RxBus
import com.starmaine777.recweight.event.WeightItemClickEvent
import com.starmaine777.recweight.utils.formatInputNumber
import timber.log.Timber

/**
 * 体重リストのAdapter
 * Created by ai on 2017/07/15.
 */

class RecordListAdapter(var recordItems: List<WeightItemEntity>, var context: Context) : RecyclerView.Adapter<RecordListAdapter.RecordViewHolder>() {

    init {
        if (context == Activity::class) {
            context = (context as Activity).applicationContext
        }
    }

    override fun onBindViewHolder(holder: RecordViewHolder, position: Int) {
        if (position + 1 == recordItems.size) {
            holder.bind(recordItems.get(position), null)
        } else {
            holder.bind(recordItems.get(position), recordItems.get(position + 1))
        }
    }


    override fun getItemCount(): Int = recordItems.size ?: 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordViewHolder =
            RecordViewHolder(ItemWeightBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    class RecordViewHolder(private val binding: ItemWeightBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: WeightItemEntity, nextItem: WeightItemEntity?): Unit = with(itemView) {

            Timber.d("ItemBind recTime = ${item.recTime}, weight =${item.weight}, fat= ${item.fat}")
            binding.apply {
                textDate.text = DateUtils.formatDateTime(context, item.recTime.timeInMillis,
                        DateUtils.FORMAT_SHOW_YEAR
                                .or(DateUtils.FORMAT_SHOW_DATE)
                                .or(DateUtils.FORMAT_NUMERIC_DATE)
                                .or(DateUtils.FORMAT_SHOW_TIME)
                                .or(DateUtils.FORMAT_SHOW_WEEKDAY)
                                .or(DateUtils.FORMAT_ABBREV_ALL))
                textWeight.text = context.getString(R.string.list_weight_pattern, formatInputNumber(item.weight.toString(), context.getString(R.string.weight_input_fat_default)))
                if (item.fat == 0.0) {
                    textFat.visibility = View.INVISIBLE
                } else {
                    textFat.text = context.getString(R.string.list_fat_pattern, formatInputNumber(item.fat.toString(), context.getString(R.string.weight_input_fat_default)))
                    textFat.visibility = View.VISIBLE
                }

                val diff: Double
                if (nextItem == null) {
                    diff = 0.0
                } else {
                    diff = nextItem.weight - item.weight
                }

                imageRatio.setImageResource(
                        when {
                            diff < 0 -> R.drawable.weight_up
                            diff > 0 -> R.drawable.weight_down
                            else -> R.drawable.weight_keep
                        }
                )

                toggleDumbbell.isChecked = item.showDumbbell
                toggleLiquor.isChecked = item.showLiquor
                toggleToilet.isChecked = item.showToilet
                toggleMoon.isChecked = item.showMoon
                toggleStar.isChecked = item.showStar
                textMemo.text = item.memo

                root.setOnClickListener {
                    RxBus.publish(WeightItemClickEvent(item))
                }
                root.setOnLongClickListener {
                    RxBus.publish(WeightItemClickEvent(item, true))
                    return@setOnLongClickListener true
                }
            }
        }
    }
}