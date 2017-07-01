package com.starmaine777.recweight.views

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.text.format.DateFormat
import android.view.*
import com.starmaine777.recweight.R
import com.starmaine777.recweight.entity.WeightItemEntity
import com.starmaine777.recweight.utils.Consts
import com.starmaine777.recweight.utils.Consts.WEIGHT_INPUT_MODE
import kotlinx.android.synthetic.main.fragment_weight_input.*
import java.util.*

/**
 * Created by 0025331458 on 2017/06/29.
 * Fragment to input weight and informations
 */
class WeightInputFragment : Fragment() {


    val weightInputMode: WEIGHT_INPUT_MODE by lazy { arguments.getSerializable(ARGS_MODE) as WEIGHT_INPUT_MODE }
    var entity: WeightItemEntity = WeightItemEntity(Calendar.getInstance(), 0.0, 0.0, false, false, false, false, false, "")
    var dialog: DialogFragment? = null

    companion object {

        const val TAG = "WeightInputFragment"

        val ARGS_MODE = "mode"

        fun newInstance(weightInputMode: WEIGHT_INPUT_MODE): WeightInputFragment {
            val fragment = WeightInputFragment()
            val bundle = Bundle()
            bundle.putSerializable(ARGS_MODE, weightInputMode)
            fragment.arguments = bundle

            return fragment
        }
    }


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_weight_input, container, false)

        setHasOptionsMenu(true)
        return view
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        setRecordTime(entity.date)
        editTime.setOnClickListener { _ ->
            dialog = DateTimePickerDialog.newInstance(entity.date, DateTimePickerDialog.DATE_TIME_INPUT_MODE.DATE_TIME)
            dialog?.setTargetFragment(this@WeightInputFragment, Consts.REQUEST_INPUT_DATE_TIME)
            dialog?.show(fragmentManager, DateTimePickerDialog.TAG)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater?.inflate(R.menu.menu_weight_input, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val itemId = item?.itemId
        if (itemId == R.id.action_done) {

        }

        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == Consts.REQUEST_INPUT_DATE_TIME) {
            val newCalendar = Calendar.getInstance()
            val year = data?.getIntExtra(DateTimePickerDialog.RESULT_YEAR, -1) ?: -1
            val month = data?.getIntExtra(DateTimePickerDialog.RESULT_MONTH, -1) ?: -1
            val day = data?.getIntExtra(DateTimePickerDialog.RESULT_DAY, -1) ?: -1
            val hour = data?.getIntExtra(DateTimePickerDialog.RESULT_HOUR, -1) ?: -1
            val minute = data?.getIntExtra(DateTimePickerDialog.RESULT_MINUTE, -1) ?: -1

            if (year >= 0 && month >= 0 && day >= 0 && hour >= 0 && minute >= 0) {
                newCalendar.set(year, month, day, hour, minute)
                setRecordTime(newCalendar)
            }

        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    fun setRecordTime(newCalendar: Calendar) {
        if (entity.date != newCalendar) {
            entity = entity.copy(date = newCalendar)
        }
        val dateText = "${DateFormat.getDateFormat(activity.applicationContext).format(entity.date.time)} ${DateFormat.getTimeFormat(activity.applicationContext).format(entity.date.time)}"
        editTime.setText(dateText)
    }
}