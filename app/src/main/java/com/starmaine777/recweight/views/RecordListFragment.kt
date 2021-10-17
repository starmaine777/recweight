package com.starmaine777.recweight.views

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.starmaine777.recweight.R
import com.starmaine777.recweight.data.entity.WeightItemEntity
import com.starmaine777.recweight.model.viewmodel.ShowRecordsViewModel
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
import kotlinx.android.synthetic.main.fragment_record_list.areaNoData
import kotlinx.android.synthetic.main.fragment_record_list.recyclerRecords
import timber.log.Timber

/**
 * 体重記録ListFragment
 * Created by ai on 2017/07/15.
 */

class RecordListFragment : Fragment(), ShowRecordsFragment.ShowRecordsEventListener {

    companion object {
        val TAG = "RecordListFragment"
    }

    private lateinit var viewModel: ShowRecordsViewModel
    private val disposable = CompositeDisposable()
    var dialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(requireActivity()).get(ShowRecordsViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_record_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerRecords.layoutManager = LinearLayoutManager(context)
    }

    override fun onStart() {
        super.onStart()

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

    override fun onResume() {
        super.onResume()
        updateListItem()
    }

    override fun onStop() {
        super.onStop()
        disposable.clear()
        dialog?.dismiss()
        dialog = null
    }

    override fun updateListItem() {
        if (viewModel.weightItemList.isEmpty()) {
            recyclerRecords.visibility = View.GONE
            areaNoData.visibility = View.VISIBLE
        } else {
            recyclerRecords.visibility = View.VISIBLE
            areaNoData.visibility = View.GONE

            val adapter = RecordListAdapter(viewModel.weightItemList, requireContext())
            recyclerRecords.adapter = adapter
        }
    }

    private fun onItemLongTap(item: WeightItemEntity) {
        val value = requireContext().getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE).getString(PREFERENCE_KEY.LONG_TAP.name, resources.getStringArray(R.array.setting_long_tap)[0])
        when (value) {
            getString(R.string.settings_lt_show_menu) -> showMenuDialog(item)
            getString(R.string.settings_lt_edit) -> RxBus.publish(InputFragmentStartEvent(WEIGHT_INPUT_MODE.INPUT, item.id))
            getString(R.string.settings_lt_delete) -> showDeleteConfirmDialog(item)
        }
    }

    private fun showMenuDialog(item: WeightItemEntity) {
        dialog = MaterialDialog.Builder(requireContext())
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
                    viewModel.deleteItem(requireContext(), item)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe {
                            }.let { disposable.add(it) }
                })
                .setNegativeButton(android.R.string.cancel, { _, _ -> })
                .show()
    }
}

