package com.starmaine777.recweight.views

import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.*
import com.starmaine777.recweight.R
import com.starmaine777.recweight.data.entity.WeightItemEntity
import com.starmaine777.recweight.data.viewmodel.ShowRecordsViewModel
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
    }

    interface ShowRecordsEventListener {
        fun updateListItem()
    }

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
        bottomMain.setOnNavigationItemSelectedListener { item ->

            val fragment: Fragment
            when (item.itemId) {
                R.id.bottom_list -> {
                    fragment = childFragmentManager.findFragmentByTag(RecordListFragment.TAG) ?: RecordListFragment()
                    childFragmentManager.beginTransaction().replace(R.id.list_fragment, fragment, RecordListFragment.TAG).commit()
                }
                R.id.bottom_chart -> {
                    fragment = childFragmentManager.findFragmentByTag(RecordChartFragment.TAG) ?: RecordChartFragment()
                    childFragmentManager.beginTransaction().replace(R.id.list_fragment, fragment, RecordChartFragment.TAG).commit()
                }
            }
            return@setOnNavigationItemSelectedListener true
        }

        bottomMain.selectedItemId = R.id.bottom_list
    }

    override fun onStart() {
        super.onStart()
        viewModel.getWeightItemList(context)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ t1: List<WeightItemEntity> ->
                    viewModel.weightItemList = t1
                    val tag: String =
                            when (bottomMain.selectedItemId) {
                                R.id.bottom_list -> RecordListFragment.TAG
                                R.id.bottom_chart -> RecordChartFragment.TAG
                                else -> {
                                    ""
                                }
                            }
                    val fragment = childFragmentManager.findFragmentByTag(tag)
                    fragment?.let {
                        if (fragment is ShowRecordsEventListener) {
                            fragment.updateListItem()
                        }
                    }
                }).let { disposable.add(it) }
    }

    override fun onStop() {
        super.onStop()
        disposable.clear()
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
        }

        return super.onOptionsItemSelected(item)
    }

}
