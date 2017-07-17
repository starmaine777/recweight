package com.starmaine777.recweight.views.adapter

import android.app.Activity
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.starmaine777.recweight.R
import com.starmaine777.recweight.data.WeightItemEntity
import com.starmaine777.recweight.utils.formatInputNumber
import kotlinx.android.synthetic.main.item_weight.view.*

/**
 * Created by ai on 2017/07/15.
 */

class RecordListAdapter(var recordItems: List<WeightItemEntity>?, var context: Context) : RecyclerView.Adapter<RecordListAdapter.RecordViewHolder>() {

    init {
        if (context == Activity::class) {
            context = (context as Activity).applicationContext
        }
        Log.d("test", "AdapterCreated")
    }

    override fun onBindViewHolder(holder: RecordViewHolder?, position: Int): Unit = holder!!.bind(recordItems?.get(position))

    override fun getItemCount(): Int = recordItems?.size ?: 0

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecordViewHolder? {
        Log.d("test", "onCreatedViewHolder")
        return RecordViewHolder(LayoutInflater.from(parent?.context).inflate(R.layout.item_weight, parent, false))
    }


    class RecordViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: WeightItemEntity?): Unit = with(itemView) {

            itemView.textDate.text = DateUtils.formatDateTime(context, item?.recTime!!.timeInMillis,
                    DateUtils.FORMAT_SHOW_YEAR
                            .and(DateUtils.FORMAT_SHOW_DATE)
                            .and(DateUtils.FORMAT_NUMERIC_DATE)
                            .and(DateUtils.FORMAT_SHOW_TIME))
//                    DateUtils.FORMAT_NUMERIC_DATE.and(DateUtils.FORMAT_SHOW_TIME))
            itemView.textWeight.text = context.getString(R.string.list_weight_pattern, formatInputNumber(item?.weight.toString(), "0.0"))
            itemView.textFat.text = context.getString(R.string.list_fat_pattern, formatInputNumber(item?.fat.toString(), "0.0"))
            itemView.toggleDumbbell.isChecked = item?.showDumbbell
            itemView.toggleLiquor.isChecked = item?.showLiquor
            itemView.toggleToilet.isChecked = item?.showToilet
            itemView.toggleMoon.isChecked = item?.showMoon
            itemView.toggleStar.isChecked = item?.showStar
            itemView.textMemo.text = item?.memo
        }
    }
}