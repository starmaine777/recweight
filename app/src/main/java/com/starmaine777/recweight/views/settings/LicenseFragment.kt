package com.starmaine777.recweight.views.settings

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.starmaine777.recweight.R
import com.starmaine777.recweight.event.RxBus
import com.starmaine777.recweight.event.UpdateToolbarEvent
import kotlinx.android.synthetic.main.fragment_license.*
import net.yslibrary.licenseadapter.GitHubLicenseEntry
import net.yslibrary.licenseadapter.LicenseAdapter
import net.yslibrary.licenseadapter.LicenseEntry
import net.yslibrary.licenseadapter.Licenses

/**
 * 利用ライセンス一覧画面
 * Created by 0025331458 on 2017/09/25.
 */

class LicenseFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater!!.inflate(R.layout.fragment_license, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val dataSet = ArrayList<LicenseEntry>()

        dataSet.add(GitHubLicenseEntry(Licenses.NAME_APACHE_V2, "ReactiveX/RxJava", ".x", null, Licenses.FILE_NO_EXTENSION))
        dataSet.add(Licenses.fromGitHubApacheV2("JakeWharton/timber"))
        dataSet.add(Licenses.fromGitHubMIT("afollestad/material-dialogs"))
        dataSet.add(Licenses.fromGitHubApacheV2("yshrsmz/LicenseAdapter"))

        val adapter = LicenseAdapter(dataSet)
        recyclerLicense.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recyclerLicense.adapter = adapter

        Licenses.load(dataSet)
    }

    override fun onStart() {
        super.onStart()
        RxBus.publish(UpdateToolbarEvent(true, context.getString(R.string.toolbar_title_license)))
    }
}
