package com.starmaine777.recweight.views.settings

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.starmaine777.recweight.R
import com.starmaine777.recweight.data.WeightItemRepository
import com.starmaine777.recweight.utils.REQUESTS
import com.starmaine777.recweight.views.adapter.SettingItem
import com.starmaine777.recweight.views.adapter.SettingsMainAdapter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_settings_main.*
import timber.log.Timber

/**
 * SettingのメインFragment
 * Created by 0025331458 on 2017/08/10.
 */

class SettingsMainFragment : Fragment() {

    companion object {
        val TAG = "SettingsMainFragment"
    }

    var disposable = CompositeDisposable()
    var dialog: AlertDialog? = null
    val settingsItems: List<SettingItem> = listOf(
            SettingItem(R.string.settings_main_import, {
                activity.supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.fragment, ImportUrlFragment(), null).addToBackStack(null).commit()
            }),
            SettingItem(R.string.settings_main_all_delete, {
                dialog = AlertDialog.Builder(context)
                        .setTitle(R.string.d_settings_all_delete_title)
                        .setMessage(R.string.d_settings_all_delete)
                        .setPositiveButton(android.R.string.ok, { _, _ -> onActivityResult(REQUESTS.DELETE_ALL_WEIGHT_ITEMS.ordinal, Activity.RESULT_OK, null) })
                        .setNegativeButton(android.R.string.cancel, { _, _ -> onActivityResult(REQUESTS.DELETE_ALL_WEIGHT_ITEMS.ordinal, Activity.RESULT_CANCELED, null) })
                        .setOnDismissListener { onActivityResult(REQUESTS.DELETE_ALL_WEIGHT_ITEMS.ordinal, Activity.RESULT_CANCELED, null) }
                        .show()
            })
    )

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

    override fun onStop() {
        super.onStop()
        dialog?.dismiss()
        dialog = null
        disposable.clear()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Timber.d("onActivityResult requestCode = $requestCode, resultCode = $resultCode")
        when (requestCode) {
            REQUESTS.DELETE_ALL_WEIGHT_ITEMS.ordinal -> {
                if (resultCode == Activity.RESULT_CANCELED) {
                    return
                }

                Timber.d("startDeleteAllItems!")
                WeightItemRepository
                        .deleteAllItem(context)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            Timber.d("completeDeleteAll!")
                            dialog = AlertDialog.Builder(context)
                                    .setTitle(R.string.d_settings_all_delete_title)
                                    .setMessage(R.string.d_settings_complete_all_delete)
                                    .setPositiveButton(android.R.string.ok, null)
                                    .show()
                        }).let { disposable.add(it) }
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }
}