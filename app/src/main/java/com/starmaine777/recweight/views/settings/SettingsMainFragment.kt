package com.starmaine777.recweight.views.settings

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.starmaine777.recweight.R
import com.starmaine777.recweight.views.adapter.SettingItem
import com.starmaine777.recweight.views.adapter.SettingsMainAdapter
import kotlinx.android.synthetic.main.fragment_settings_main.*

/**
 * SettingのメインFragment
 * Created by 0025331458 on 2017/08/10.
 */

class SettingsMainFragment:Fragment() {

    val settingsItems:List<SettingItem> = listOf(
            SettingItem(R.string.settings_main_import, { Log.d("test", "clickMainImport")}),
            SettingItem(R.string.settings_main_export, {Log.d("test", "clickMainExport")})
    )

    companion object {
        val TAG = "SettingsMainFragment"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_settings_main, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerSettingsMain.layoutManager = LinearLayoutManager(context)
        recyclerSettingsMain.adapter = SettingsMainAdapter(settingsItems)
    }
}