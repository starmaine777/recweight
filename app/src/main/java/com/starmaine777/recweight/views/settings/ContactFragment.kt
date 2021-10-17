package com.starmaine777.recweight.views.settings

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.starmaine777.recweight.R
import com.starmaine777.recweight.event.RxBus
import com.starmaine777.recweight.event.UpdateToolbarEvent

/**
 * お問い合わせ用Fragment
 * Created by 0025331458 on 2017/11/09.
 */
class ContactFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_contact, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_contact, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_send) {
            fragmentManager?.popBackStack()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        super.onStart()
        RxBus.publish(UpdateToolbarEvent(true, requireContext().getString(R.string.toolbar_title_contact)))
    }


}