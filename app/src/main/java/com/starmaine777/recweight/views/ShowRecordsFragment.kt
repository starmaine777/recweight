package com.starmaine777.recweight.views

import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.*
import com.starmaine777.recweight.R
import com.starmaine777.recweight.data.entity.WeightItemEntity
import com.starmaine777.recweight.data.viewmodel.ShowRecordsViewModel
import com.starmaine777.recweight.event.InputFragmentStartEvent
import com.starmaine777.recweight.event.RxBus
import com.starmaine777.recweight.utils.WEIGHT_INPUT_MODE
import com.starmaine777.recweight.views.settings.SettingsActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_show_records.*

/**
 * 記録表示用親Fragment
 * Created by 0025331458 on 2017/07/21.
 */

class ShowRecordsFragment : Fragment() {

    companion object {
        val TAG = "ShowRecordsFragment"
        val CHILD_FRAGMENT_TAG = "showWeightFragment"
    }

    interface ShowRecordsEventListener {
        fun updateListItem()
    }

    enum class SHOW_TYPE(var fragment: Fragment?, var listener: ShowRecordsEventListener?, var iconId: Int) {
        LIST(null, null, R.drawable.icon_show_list),
        CHART(null, null, R.drawable.icon_show_chart)
    }

    private var showType: SHOW_TYPE = SHOW_TYPE.LIST

    private lateinit var viewModel: ShowRecordsViewModel
    private val disposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(activity).get(ShowRecordsViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        activity.title = getString(R.string.app_name)
        setHasOptionsMenu(true)
        return inflater?.inflate(R.layout.fragment_show_records, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        changeRecordsFragment(SHOW_TYPE.LIST)

        fab.setOnClickListener { _ ->
            RxBus.publish(InputFragmentStartEvent(WEIGHT_INPUT_MODE.INPUT, null))
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.getWeightItemList(context)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ t1: List<WeightItemEntity>? ->
                    viewModel.weightItemList = t1
                    showType.listener?.let {
                        showType.listener!!.updateListItem()
                    }

                }).let { disposable.add(it) }
    }

    override fun onStop() {
        super.onStop()
        disposable.clear()
    }

    private fun changeRecordsFragment(newShowType: SHOW_TYPE) {
        showType = newShowType
        if (showType.fragment == null) {
            var fragment: Fragment? = null
            var listener: ShowRecordsEventListener? = null
            when (showType) {
                SHOW_TYPE.LIST -> {
                    fragment = RecordListFragment()
                    listener = fragment
                }
                SHOW_TYPE.CHART -> {
                    fragment = RecordChartFragment()
                    listener = fragment
                }
            }
            showType.fragment = fragment
            showType.listener = listener
        }
        showType.fragment?.let {
            childFragmentManager.beginTransaction().replace(R.id.list_fragment, showType.fragment, CHILD_FRAGMENT_TAG).commit()
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater?.inflate(R.menu.menu_main, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings -> {
                val intent = Intent(activity, SettingsActivity::class.java)
                startActivity(intent)
            }
            R.id.action_change_show_type -> {
                changeRecordsFragment(if (showType == SHOW_TYPE.LIST) SHOW_TYPE.CHART else SHOW_TYPE.LIST)
                item.setIcon(showType.iconId)
            }
        }

        return super.onOptionsItemSelected(item)
    }

}
