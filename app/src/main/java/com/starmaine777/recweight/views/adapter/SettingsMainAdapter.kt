package com.starmaine777.recweight.views.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

/**
 * Settingsメイン用のAdapter
 * Created by 0025331458 on 2017/08/10.
 */
class SettingsMainAdapter(var itemList: List<SettingItem>?) : RecyclerView.Adapter<SettingsMainAdapter.SettingsMainViewHolder>() {
    override fun onBindViewHolder(holder: SettingsMainViewHolder?, position: Int) {
        val item = itemList?.get(position)
        holder!!.bind(itemList?.get(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): SettingsMainViewHolder {
        return SettingsMainViewHolder(LayoutInflater.from(parent?.context).inflate(android.R.layout.simple_list_item_1, parent, false))
    }

    override fun getItemCount(): Int = itemList?.size ?:0


    class SettingsMainViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind (item:SettingItem?) {
            itemView.setOnClickListener { item?.clickListener }
            (itemView.findViewById(android.R.id.text1) as TextView).text = itemView.context.getString(item?.titleId!!)
        }
    }
}

class SettingItem(val titleId: Int, val clickListener:() -> Unit)