package com.starmaine777.recweight.views.settings

import android.accounts.AccountManager
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.afollestad.materialdialogs.MaterialDialog
import com.starmaine777.recweight.R
import com.starmaine777.recweight.data.repo.WeightItemRepository
import com.starmaine777.recweight.event.RxBus
import com.starmaine777.recweight.event.UpdateToolbarEvent
import com.starmaine777.recweight.utils.PREFERENCES_NAME
import com.starmaine777.recweight.utils.PREFERENCE_KEY
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

    private var disposable = CompositeDisposable()
    private var dialog: Dialog? = null
    private fun createSettingItems(): List<SettingItem> =
            listOf(
                    SettingItem(getString(R.string.settings_main_long_tap),
                            activity.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE).getString(PREFERENCE_KEY.LONG_TAP.name, getString(R.string.settings_lt_show_menu)),
                            {
                                showLongTapDialog()
                            }),
                    SettingItem(getString(R.string.settings_main_import), {
                        activity.supportFragmentManager
                                .beginTransaction()
                                .replace(R.id.fragment, ImportUrlFragment(), null).addToBackStack(null).commit()
                    }),
                    SettingItem(getString(R.string.settings_main_export),
                            getLastExportDateString(),
                            {
                                activity.supportFragmentManager
                                        .beginTransaction()
                                        .replace(R.id.fragment, ExportFragment(), null).addToBackStack(null).commit()
                            }),
                    SettingItem(getString(R.string.settings_main_all_delete), {
                        dialog = AlertDialog.Builder(context)
                                .setTitle(R.string.d_settings_all_delete_title)
                                .setMessage(R.string.d_settings_all_delete)
                                .setPositiveButton(android.R.string.ok, { _, _ -> onActivityResult(REQUESTS.DELETE_ALL_WEIGHT_ITEMS.ordinal, Activity.RESULT_OK, null) })
                                .setNegativeButton(android.R.string.cancel, { _, _ -> onActivityResult(REQUESTS.DELETE_ALL_WEIGHT_ITEMS.ordinal, Activity.RESULT_CANCELED, null) })
                                .setOnDismissListener { onActivityResult(REQUESTS.DELETE_ALL_WEIGHT_ITEMS.ordinal, Activity.RESULT_CANCELED, null) }
                                .show()
                    }),
                    SettingItem(getString(R.string.settings_main_account),
                            context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE).getString(PREFERENCE_KEY.ACCOUNT_NAME.name, getString(R.string.settings_account_no_data)),
                            {
                                showChooseAccountDialog()
                            }),
                    SettingItem(getString(R.string.settings_main_license), {
                        activity.supportFragmentManager
                                .beginTransaction()
                                .replace(R.id.fragment, LicenseFragment(), null).addToBackStack(null).commit()
//                }),
//                SettingItem(getString(R.string.settings_main_contact), {
//                    activity.supportFragmentManager
//                            .beginTransaction()
//                            .replace(R.id.fragment, ContactFragment(), null).addToBackStack(null).commit()
                    })

            )

    private fun getLastExportDateString(): String {
        val datetime = activity.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE).getLong(PREFERENCE_KEY.LAST_EXPORT_DATE.name, 0)
        return getString(R.string.settings_main_export_description
                , if (datetime == 0L) "-" else DateUtils.formatDateTime(context, datetime, DateUtils.FORMAT_SHOW_DATE.or(DateUtils.FORMAT_SHOW_TIME).or(DateUtils.FORMAT_NUMERIC_DATE)))
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater?.inflate(R.layout.fragment_settings_main, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerSettingsMain.layoutManager = LinearLayoutManager(context)
    }

    override fun onStart() {
        super.onStart()
        recyclerSettingsMain.adapter = SettingsMainAdapter(createSettingItems())
        RxBus.publish(UpdateToolbarEvent(true, context.getString(R.string.activity_settings)))
        recyclerSettingsMain.adapter = SettingsMainAdapter(createSettingItems())
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
                        .deleteAllItemCompletable(context)
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
            REQUESTS.SHOW_ACCOUNT_PICKER.ordinal -> {
                if (resultCode == Activity.RESULT_OK) {
                    val accountName = data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
                    if (!TextUtils.isEmpty(accountName)) {
                        activity.getSharedPreferences(context.packageName, Context.MODE_PRIVATE).edit().putString(PREFERENCE_KEY.ACCOUNT_NAME.name, accountName).apply()
                        SettingsMainAdapter(createSettingItems())
                    }
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun showLongTapDialog() {
        dialog = MaterialDialog.Builder(context).items(R.array.setting_long_tap).itemsCallbackSingleChoice(-1,
                { _, _, _, text ->
                    context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE).edit().putString(PREFERENCE_KEY.LONG_TAP.name, text.toString()).apply()
                    recyclerSettingsMain.adapter = SettingsMainAdapter(createSettingItems())
                    return@itemsCallbackSingleChoice true
                }).positiveText(android.R.string.ok).show()
    }

    private fun showChooseAccountDialog() {
        val intent: Intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            AccountManager.newChooseAccountIntent(null, null, arrayOf("com.google"), null, null, null, null)
        } else {
            @Suppress("DEPRECATION")
            AccountManager.newChooseAccountIntent(null, null, arrayOf("com.google"), false, null, null, null, null)
        }
        startActivityForResult(intent, REQUESTS.SHOW_ACCOUNT_PICKER.ordinal)
    }

}