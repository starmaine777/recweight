package com.starmaine777.recweight.views

import android.app.AlertDialog
import android.app.Dialog
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.afollestad.materialdialogs.MaterialDialog
import com.starmaine777.recweight.R
import com.starmaine777.recweight.data.ShowRecordsViewModel
import com.starmaine777.recweight.data.WeightItemEntity
import com.starmaine777.recweight.event.InputFragmentStartEvent
import com.starmaine777.recweight.event.RxBus
import com.starmaine777.recweight.event.WeightItemClickEvent
import com.starmaine777.recweight.utils.PREFERENCES_NAME
import com.starmaine777.recweight.utils.PREFERENCE_KEY
import com.starmaine777.recweight.utils.WEIGHT_INPUT_MODE
import com.starmaine777.recweight.views.adapter.RecordListAdapter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_record_list.*
import timber.log.Timber

/**
 * 体重記録ListFragment
 * Created by ai on 2017/07/15.
 */

class RecordListFragment : Fragment() {

    companion object {
        val TAG = "RecordListFragment"
    }

    val weightItemVm: ShowRecordsViewModel by lazy { ViewModelProviders.of(activity).get(ShowRecordsViewModel::class.java) }
    val disposable = CompositeDisposable()
    var dialog: Dialog? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater?.inflate(R.layout.fragment_record_list, container, false)
        return view
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerRecords.layoutManager = LinearLayoutManager(context)
    }

    override fun onStart() {
        super.onStart()
        weightItemVm.getWeightItemList(context)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ t1: List<WeightItemEntity>? ->
                    val adapter = RecordListAdapter(t1, context)
                    recyclerRecords.adapter = adapter
                }).let { disposable.add(it) }

        RxBus.subscribe(WeightItemClickEvent::class.java)
                .subscribe({ t: WeightItemClickEvent ->

                    Timber.d("weightItemClickEvent longtap?=${t.isLongTap}")

                    if (t.isLongTap) {
                        t.weightItemEntity?.let { onItemLongTap(it) }
                    } else {
                        RxBus.publish(InputFragmentStartEvent(WEIGHT_INPUT_MODE.VIEW, t.weightItemEntity?.id))
                    }

                }).let { disposable.add(it) }
    }

    override fun onStop() {
        super.onStop()
        disposable.clear()
        dialog?.dismiss()
        dialog = null
    }

    private fun onItemLongTap(item: WeightItemEntity) {
        val value = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE).getString(PREFERENCE_KEY.LONG_TAP.name, resources.getStringArray(R.array.setting_long_tap)[0])
        when (value) {
            getString(R.string.settings_lt_show_menu) -> showMenuDialog(item)
            getString(R.string.settings_lt_edit) -> RxBus.publish(InputFragmentStartEvent(WEIGHT_INPUT_MODE.INPUT, item.id))
            getString(R.string.settings_lt_delete) -> showDeleteConfirmDialog(item)
        }
    }

    private fun showMenuDialog(item: WeightItemEntity) {
        dialog = MaterialDialog.Builder(context)
                .items(R.array.long_tap_menus)
                .itemsCallback({ md, _, position, _ ->
                    md.dismiss()
                    when (position) {
                        0 -> {
                            // update
                            RxBus.publish(InputFragmentStartEvent(WEIGHT_INPUT_MODE.INPUT, item.id))
                        }
                        1 -> {
                            // delete
                            showDeleteConfirmDialog(item)
                        }
                    }
                })
                .show()
    }

    private fun showDeleteConfirmDialog(item: WeightItemEntity) {
        dialog = AlertDialog.Builder(context)
                .setTitle(R.string.d_delete_item_title)
                .setMessage(R.string.d_delete_item)
                .setPositiveButton(android.R.string.ok, { _, _ ->
                    weightItemVm.deleteItem(context, item)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe {
                            }.let { disposable.add(it) }
                })
                .setNegativeButton(android.R.string.cancel, { _, _ -> })
                .show()
    }
}

