package com.starmaine777.recweight.views

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.*
import com.starmaine777.recweight.R
import com.starmaine777.recweight.event.InputFragmentStartEvent
import com.starmaine777.recweight.event.RxBus
import com.starmaine777.recweight.utils.Consts
import kotlinx.android.synthetic.main.fragment_show_records.*

/**
 * 記録表示用親Fragment
 * Created by 0025331458 on 2017/07/21.
 */

class ShowRecordsFragment : Fragment() {

    companion object {
        val TAG = "ShowRecordsFragment"
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        activity.title = getString(R.string.app_name)
        setHasOptionsMenu(true)
        return inflater?.inflate(R.layout.fragment_show_records, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        childFragmentManager.beginTransaction().replace(R.id.list_fragment, RecordListFragment(), RecordListFragment.TAG).commit()


        fab.setOnClickListener { _ ->
            RxBus.publish(InputFragmentStartEvent(Consts.WEIGHT_INPUT_MODE.INPUT, null))
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.action_settings) {
            return true
        }

        return super.onOptionsItemSelected(item)
    }

}
