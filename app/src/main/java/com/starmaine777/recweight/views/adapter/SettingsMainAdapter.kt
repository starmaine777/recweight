package com.starmaine777.recweight.views.adapter

import androidx.recyclerview.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

/**
 * Settingsメイン用のAdapter
 * Created by 0025331458 on 2017/08/10.
 */
class SettingsMainAdapter(var itemList: List<SettingItem>?) : RecyclerView.Adapter<SettingsMainAdapter.SettingsMainViewHolder>() {

    enum class SETTING_ITEM_VIEW_TYPE {
        SINGLE_LINE,
        TWO_LINE
    }

    override fun getItemCount(): Int = itemList?.size ?: 0

    override fun getItemViewType(position: Int): Int {
        val item = itemList?.get(position)
        return if (!TextUtils.isEmpty(item?.description)) SETTING_ITEM_VIEW_TYPE.TWO_LINE.ordinal else SETTING_ITEM_VIEW_TYPE.SINGLE_LINE.ordinal
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SettingsMainViewHolder {
        val layoutId = if (viewType == SETTING_ITEM_VIEW_TYPE.TWO_LINE.ordinal) android.R.layout.simple_list_item_2 else android.R.layout.simple_list_item_1

        return SettingsMainViewHolder(LayoutInflater.from(parent.context).inflate(layoutId, parent, false))
    }

    override fun onBindViewHolder(holder: SettingsMainViewHolder, position: Int) {
        holder.bind(itemList?.get(position))
    }


    class SettingsMainViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: SettingItem?) {
            itemView.setOnClickListener {
                item?.onClick?.invoke()
            }
            itemView.findViewById<TextView>(android.R.id.text1).text = item?.title
            if (!TextUtils.isEmpty(item?.description)) {
                itemView.findViewById<TextView>(android.R.id.text2).text = item!!.description
            }
        }
    }
}

class SettingItem(var title: String, var description: String, val onClick: () -> Unit) {
    constructor(title: String, onClick: () -> Unit) : this(title, "", onClick)
}

