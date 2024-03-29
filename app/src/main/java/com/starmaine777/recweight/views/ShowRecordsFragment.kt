package com.starmaine777.recweight.views

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.starmaine777.recweight.R
import com.starmaine777.recweight.data.entity.WeightItemEntity
import com.starmaine777.recweight.data.viewmodel.ShowRecordsViewModel
import com.starmaine777.recweight.event.InputFragmentStartEvent
import com.starmaine777.recweight.event.RxBus
import com.starmaine777.recweight.utils.PREFERENCE_KEY
import com.starmaine777.recweight.utils.WEIGHT_INPUT_MODE
import com.starmaine777.recweight.utils.getBoolean
import com.starmaine777.recweight.utils.updateBoolean
import com.starmaine777.recweight.views.settings.SettingsActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_show_records.bottomMain
import kotlinx.android.synthetic.main.fragment_show_records.fab

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
//    private var tutorial: TourGuide? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(requireActivity()).get(ShowRecordsViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        requireActivity().title = getString(R.string.app_name)
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_show_records, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        bottomMain.setOnNavigationItemSelectedListener { item ->

            val fragment: Fragment
            when (item.itemId) {
                R.id.bottom_list -> {
                    fragment = childFragmentManager.findFragmentByTag(RecordListFragment.TAG)
                            ?: RecordListFragment()
                    childFragmentManager.beginTransaction().replace(R.id.list_fragment, fragment, RecordListFragment.TAG).commit()
                    fab.show()
                }
                R.id.bottom_chart -> {
                    fragment = childFragmentManager.findFragmentByTag(RecordChartFragment.TAG)
                            ?: RecordChartFragment()
                    childFragmentManager.beginTransaction().replace(R.id.list_fragment, fragment, RecordChartFragment.TAG).commit()
                    fab.hide()
                }
            }
            return@setOnNavigationItemSelectedListener true
        }

        bottomMain.selectedItemId = R.id.bottom_list
        fab.setOnClickListener { _ ->
//            tutorial?.let {
//                tutorial!!.cleanUp()
//                updateBoolean(requireContext(), PREFERENCE_KEY.NEED_TUTORIAL_INPUT.name, false)
//            }
//
            RxBus.publish(InputFragmentStartEvent(WEIGHT_INPUT_MODE.INPUT, null))
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.getWeightItemList(requireContext())
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

        // TODO : tutorial復活
//        if (getBoolean(requireContext(), PREFERENCE_KEY.NEED_TUTORIAL_INPUT.name, true)) {
//            showTutorial()
//        }
    }

//    private fun showTutorial() {
//        val enterAnimation = AlphaAnimation(0f, 1f)
//        enterAnimation.duration = 600
//        enterAnimation.fillAfter = true
//
//        val exitAnimation = AlphaAnimation(1f, 0f)
//        exitAnimation.duration = 600
//        exitAnimation.fillAfter = true
//
//        tutorial = TourGuide.init(activity).with(TourGuide.Technique.Click)
//                .setToolTip(ToolTip()
//                        .setTitle(getString(R.string.tutorial_input_title))
//                        .setDescription(getString(R.string.tutorial_input_description)))
//                .setOverlay(Overlay().disableClick(true))
//                .playOn(fab)
//    }
//
//    override fun onPause() {
//        super.onPause()
//        tutorial?.cleanUp()
//    }

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
