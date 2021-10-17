package com.starmaine777.recweight.views.settings

import android.accounts.AccountManager
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.starmaine777.recweight.R
import com.starmaine777.recweight.data.repo.WeightItemRepository
import com.starmaine777.recweight.databinding.FragmentSettingsMainBinding
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
import timber.log.Timber

/**
 * SettingのメインFragment
 * Created by 0025331458 on 2017/08/10.
 */

class SettingsMainFragment() : Fragment() {

    companion object {
        val TAG = "SettingsMainFragment"
    }

    private lateinit var binding: FragmentSettingsMainBinding

    private var disposable = CompositeDisposable()
    private var dialog: Dialog? = null
    private var weightRepository = WeightItemRepository()

    private fun createSettingItems(): List<SettingItem> =
            listOf(
                    SettingItem(getString(R.string.settings_main_long_tap),
                            requireActivity().getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE).getString(PREFERENCE_KEY.LONG_TAP.name, getString(R.string.settings_lt_show_menu))
                                    ?: getString(R.string.settings_lt_show_menu),
                            {
                                showLongTapDialog()
                            }),
                    SettingItem(getString(R.string.settings_main_import), {
                        requireActivity().supportFragmentManager
                                .beginTransaction()
                                .replace(R.id.fragment, ImportUrlFragment(), null).addToBackStack(null).commit()
                    }),
                    SettingItem(getString(R.string.settings_main_export),
                            getLastExportDateString(),
                            {
                                requireActivity().supportFragmentManager
                                        .beginTransaction()
                                        .replace(R.id.fragment, ExportFragment(), null).addToBackStack(null).commit()
                            }),
                    SettingItem(getString(R.string.settings_main_all_delete), {
                        dialog = AlertDialog.Builder(requireContext())
                                .setTitle(R.string.d_settings_all_delete_title)
                                .setMessage(R.string.d_settings_all_delete)
                                .setPositiveButton(android.R.string.ok, { _, _ -> onActivityResult(REQUESTS.DELETE_ALL_WEIGHT_ITEMS.ordinal, Activity.RESULT_OK, null) })
                                .setNegativeButton(android.R.string.cancel, { _, _ -> onActivityResult(REQUESTS.DELETE_ALL_WEIGHT_ITEMS.ordinal, Activity.RESULT_CANCELED, null) })
                                .setOnDismissListener { onActivityResult(REQUESTS.DELETE_ALL_WEIGHT_ITEMS.ordinal, Activity.RESULT_CANCELED, null) }
                                .show()
                    }),
                    SettingItem(getString(R.string.settings_main_account),
                            requireActivity().getSharedPreferences(requireContext().packageName, Context.MODE_PRIVATE).getString(PREFERENCE_KEY.ACCOUNT_NAME.name, getString(R.string.settings_account_no_data))
                                    ?: getString(R.string.settings_account_no_data),
                            {
                                showChooseAccountDialog()
                            }),
                    SettingItem(getString(R.string.settings_main_license), {
                        requireFragmentManager()
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
        val datetime = requireActivity().getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE).getLong(PREFERENCE_KEY.LAST_EXPORT_DATE.name, 0)
        return getString(R.string.settings_main_export_description, if (datetime == 0L) "-" else DateUtils.formatDateTime(context, datetime, DateUtils.FORMAT_SHOW_DATE.or(DateUtils.FORMAT_SHOW_TIME).or(DateUtils.FORMAT_NUMERIC_DATE)))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSettingsMainBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerSettingsMain.layoutManager = LinearLayoutManager(context)
    }

    override fun onStart() {
        super.onStart()
        RxBus.publish(UpdateToolbarEvent(true, getString(R.string.activity_settings)))
        binding.recyclerSettingsMain.adapter = SettingsMainAdapter(createSettingItems())
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
                weightRepository
                        .deleteAllItemCompletable(requireContext())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            Timber.d("completeDeleteAll!")
                            dialog = AlertDialog.Builder(requireContext())
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
                        requireActivity().getSharedPreferences(requireContext().packageName, Context.MODE_PRIVATE).edit().putString(PREFERENCE_KEY.ACCOUNT_NAME.name, accountName).apply()
                        SettingsMainAdapter(createSettingItems())
                    }
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun showLongTapDialog() {
        dialog = MaterialDialog.Builder(requireContext())
                .items(R.array.setting_long_tap)
                .itemsCallbackSingleChoice(-1,
                        { _, _, _, text ->
                            if (text.isBlank()) return@itemsCallbackSingleChoice true
                            requireContext().getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE).edit().putString(PREFERENCE_KEY.LONG_TAP.name, text.toString()).apply()
                            binding.recyclerSettingsMain.adapter = SettingsMainAdapter(createSettingItems())
                            return@itemsCallbackSingleChoice true
                        })
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.cancel)
                .show()
        val items = resources.getStringArray(R.array.setting_long_tap)
        (dialog as MaterialDialog).selectedIndex = items.indexOf(requireContext().getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE).getString(PREFERENCE_KEY.LONG_TAP.name, items[0]))

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