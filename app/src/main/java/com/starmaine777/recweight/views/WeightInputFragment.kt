package com.starmaine777.recweight.views

import android.app.Activity
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

        val TAG_DIALOGS = "dialogs"

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
        setRecordDate(entity.date.get(Calendar.YEAR), entity.date.get(Calendar.MONTH), entity.date.get(Calendar.DAY_OF_MONTH))
        editDate.setOnClickListener { _ ->
            dialog = DatePickerDialogFragment.newInstance(entity.date.get(Calendar.YEAR), entity.date.get(Calendar.MONTH), entity.date.get(Calendar.DAY_OF_MONTH))
            dialog?.setTargetFragment(this@WeightInputFragment, Consts.REQUEST_INPUT_DATE)
            dialog?.show(fragmentManager, TAG_DIALOGS)
        }

        setRecordTime(entity.date.get(Calendar.HOUR_OF_DAY), entity.date.get(Calendar.MINUTE))
        editTime.setOnClickListener { _ ->
            dialog = TimePickerDialogFragment.newInstance(entity.date.get(Calendar.HOUR_OF_DAY), entity.date.get(Calendar.MINUTE))
            dialog?.setTargetFragment(this@WeightInputFragment, Consts.REQUEST_INPUT_TIME)
            dialog?.show(fragmentManager, TAG_DIALOGS)
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

        when (requestCode) {

            Consts.REQUEST_INPUT_DATE -> {
                if (resultCode != Activity.RESULT_OK) return
                val year = data?.getIntExtra(DatePickerDialogFragment.RESULT_YEAR, -1) ?: -1
                val month = data?.getIntExtra(DatePickerDialogFragment.RESULT_MONTH, -1) ?: -1
                val day = data?.getIntExtra(DatePickerDialogFragment.RESULT_DAY, -1) ?: -1

                if (year >= 0 && month >= 0 && day >= 0) {
                    setRecordDate(year, month, day)
                }
            }

            Consts.REQUEST_INPUT_TIME -> {
                if (resultCode != Activity.RESULT_OK) return
                val hour = data?.getIntExtra(TimePickerDialogFragment.RESULT_HOUR, -1) ?: -1
                val minute = data?.getIntExtra(TimePickerDialogFragment.RESULT_MINUTE, -1) ?: -1

                if (hour >= 0 && minute >= 0) {
                    setRecordTime(hour, minute)
                }
            }

            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    fun setRecordDate(year: Int, month: Int, day: Int) {
        if (entity.date.get(Calendar.YEAR) != year
                || entity.date.get(Calendar.MONTH) != month
                || entity.date.get(Calendar.DAY_OF_MONTH) != day) {
            val newCalendar: Calendar = entity.date.clone() as Calendar
            newCalendar.set(year, month, day)
            entity = entity.copy(date = newCalendar)
        }
        editDate.setText(DateFormat.getDateFormat(activity.applicationContext).format(entity.date.time))
    }

    fun setRecordTime(hour: Int, minute: Int) {
        if (entity.date.get(Calendar.HOUR_OF_DAY) != hour
                || entity.date.get(Calendar.MINUTE) != minute) {
            val newCalendar: Calendar = entity.date.clone() as Calendar
            newCalendar.set(Calendar.HOUR_OF_DAY, hour)
            newCalendar.set(Calendar.MINUTE, minute)
            entity = entity.copy(date = newCalendar)
        }
        editTime.setText(DateFormat.getTimeFormat(activity.applicationContext).format(entity.date.time))
    }

}