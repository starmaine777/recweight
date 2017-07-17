package com.starmaine777.recweight.views.adapter

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.starmaine777.recweight.R
import com.starmaine777.recweight.data.WeightItemEntity
import kotlinx.android.synthetic.main.item_weight.view.*

/**
 * Created by ai on 2017/07/15.
 */

class RecordListAdapter(var recordItems: List<WeightItemEntity>?) : RecyclerView.Adapter<RecordListAdapter.RecordViewHolder>() {

    init {
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

            itemView.textWeight.text = item?.weight.toString()
            itemView.textFat.text = item?.fat.toString()
            itemView.toggleDumbbell.isChecked = item?.showDumbbell ?: false
            itemView.toggleLiquor.isChecked = item?.showLiquor ?: false
            itemView.toggleToilet.isChecked = item?.showToilet ?: false
            itemView.toggleMoon.isChecked = item?.showMoon ?: false
            itemView.toggleStar.isChecked = item?.showStar ?: false
            itemView.textMemo.text = item?.memo
        }
    }
}