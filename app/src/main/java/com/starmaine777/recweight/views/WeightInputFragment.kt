package com.starmaine777.recweight.views

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.text.format.DateFormat
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import com.starmaine777.recweight.R
import com.starmaine777.recweight.data.WeightItemEntity
import com.starmaine777.recweight.data.WeightItemsViewModel
import com.starmaine777.recweight.utils.Consts
import com.starmaine777.recweight.utils.Consts.WEIGHT_INPUT_MODE
import kotlinx.android.synthetic.main.fragment_weight_input.*
import java.text.Format
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
        setRecordDate(entity.recTime.get(Calendar.YEAR), entity.recTime.get(Calendar.MONTH), entity.recTime.get(Calendar.DAY_OF_MONTH))
        editDate.setOnClickListener { _ ->
            dialog = DatePickerDialogFragment.newInstance(entity.recTime.get(Calendar.YEAR), entity.recTime.get(Calendar.MONTH), entity.recTime.get(Calendar.DAY_OF_MONTH))
            dialog?.setTargetFragment(this@WeightInputFragment, Consts.REQUEST_INPUT_DATE)
            dialog?.show(fragmentManager, TAG_DIALOGS)
        }

        setRecordTime(entity.recTime.get(Calendar.HOUR_OF_DAY), entity.recTime.get(Calendar.MINUTE))
        editTime.setOnClickListener { _ ->
            dialog = TimePickerDialogFragment.newInstance(entity.recTime.get(Calendar.HOUR_OF_DAY), entity.recTime.get(Calendar.MINUTE))
            dialog?.setTargetFragment(this@WeightInputFragment, Consts.REQUEST_INPUT_TIME)
            dialog?.show(fragmentManager, TAG_DIALOGS)
        }

//        editWeight.addTextChangedListener(object : TextWatcher {
//            override fun afterTextChanged(s: Editable?) {
//                editWeight.setText(formatInputNumber(s.toString()))
//            }
//
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//            }
//
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//            }
//        })
//
//        editFat.addTextChangedListener(object : TextWatcher {
//            override fun afterTextChanged(s: Editable?) {
//                editFat.setText(formatInputNumber(s.toString()))
//            }
//
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//            }
//
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//            }
//        })
//
//
    }

    fun formatInputNumber(numStr: String): String {
        if (TextUtils.isEmpty(numStr)) return getString(R.string.weight_input_weight_default)

        val numStrSplit = numStr.split(".")
        when (numStrSplit.size) {
            0 -> return getString(R.string.weight_input_weight_default)
            1 -> return "$numStr.0"
            2 -> {
                when (numStrSplit[1].length) {
                    1 -> return numStr
                    2 -> return numStr
                    else -> return String.format(".%2", numStr)
                }
            }
            else -> return getString(R.string.weight_input_weight_default)
        }
    }

    override fun onResume() {
        super.onResume()
        if (editWeight.requestFocus()) {
            val imm: InputMethodManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(editWeight, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    override fun onPause() {
        super.onPause()
        val imm = context.getSystemService(
                Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(activity.currentFocus.windowToken, 0)
        activity.currentFocus.clearFocus()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater?.inflate(R.menu.menu_weight_input, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val itemId = item?.itemId
        if (itemId == R.id.action_done) {
            saveWeightData(true)
        }

        return super.onOptionsItemSelected(item)
    }

    fun saveWeightData(insert: Boolean) {
        entity.copy(weight = editWeight.text.toString().toDouble(),
                fat = editFat.text.toString().toDouble(),
                showDumbbell = toggleDumbbell.isChecked,
                showLiquor = toggleLiquar.isChecked,
                showToilet = toggleToilet.isChecked,
                showMoon = toggleMoon.isChecked,
                showStar = toggleStar.isChecked,
                memo = editMemo.toString()
        )

        Handler().post {
            run {
                val weightInfoVm = WeightItemsViewModel(activity.application)
                weightInfoVm.insertWeightItem(entity)

                val weightInputList = weightInfoVm.getWeightItemList()
                Log.d("test", "weightInfoList == ${weightInputList}, count=${weightInputList.size}")
            }

        }
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
        if (entity.recTime.get(Calendar.YEAR) != year
                || entity.recTime.get(Calendar.MONTH) != month
                || entity.recTime.get(Calendar.DAY_OF_MONTH) != day) {
            val newCalendar: Calendar = entity.recTime.clone() as Calendar
            newCalendar.set(year, month, day)
            entity = entity.copy(recTime = newCalendar)
        }
        editDate.setText(DateFormat.getDateFormat(activity.applicationContext).format(entity.recTime.time))
    }

    fun setRecordTime(hour: Int, minute: Int) {
        if (entity.recTime.get(Calendar.HOUR_OF_DAY) != hour
                || entity.recTime.get(Calendar.MINUTE) != minute) {
            val newCalendar: Calendar = entity.recTime.clone() as Calendar
            newCalendar.set(Calendar.HOUR_OF_DAY, hour)
            newCalendar.set(Calendar.MINUTE, minute)
            entity = entity.copy(recTime = newCalendar)
        }
        editTime.setText(DateFormat.getTimeFormat(activity.applicationContext).format(entity.recTime.time))
    }

}