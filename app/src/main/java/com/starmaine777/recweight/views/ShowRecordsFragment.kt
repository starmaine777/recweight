package com.starmaine777.recweight.views

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.starmaine777.recweight.R
import com.starmaine777.recweight.databinding.FragmentShowRecordsBinding
import com.starmaine777.recweight.event.InputFragmentStartEvent
import com.starmaine777.recweight.event.RxBus
import com.starmaine777.recweight.utils.WEIGHT_INPUT_MODE
import com.starmaine777.recweight.views.settings.SettingsActivity
import io.reactivex.disposables.CompositeDisposable

/**
 * 記録表示用親Fragment
 * Created by 0025331458 on 2017/07/21.
 */

class ShowRecordsFragment : Fragment() {

    companion object {
        val TAG = "ShowRecordsFragment"
    }

    private val disposable = CompositeDisposable()

    private lateinit var binding: FragmentShowRecordsBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        requireActivity().title = getString(R.string.app_name)
        setHasOptionsMenu(true)
        binding = FragmentShowRecordsBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
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
                RxBus.publish(InputFragmentStartEvent(WEIGHT_INPUT_MODE.INPUT, null))
            }
        }
    }

    override fun onStop() {
        super.onStop()
        disposable.clear()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
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
