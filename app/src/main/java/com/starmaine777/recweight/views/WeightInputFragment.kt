package com.starmaine777.recweight.views

import android.app.Activity
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.text.TextUtils
import android.text.format.DateFormat
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import com.starmaine777.recweight.R
import com.starmaine777.recweight.data.WeightItemsViewModel
import com.starmaine777.recweight.databinding.FragmentWeightInputBinding
import com.starmaine777.recweight.event.InputFragmentStartEvent
import com.starmaine777.recweight.event.RxBus
import com.starmaine777.recweight.utils.Consts
import com.starmaine777.recweight.utils.Consts.WEIGHT_INPUT_MODE
import com.starmaine777.recweight.utils.formatInputNumber
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_weight_input.*
import java.util.*

/**
 * Created by 0025331458 on 2017/06/29.
 * Fragment to input weight and informations
 */
class WeightInputFragment : Fragment() {

    val viewMode: WEIGHT_INPUT_MODE by lazy { arguments.getSerializable(ARGS_MODE) as WEIGHT_INPUT_MODE }
    var dialog: DialogFragment? = null
    val disposable = CompositeDisposable()
    val weightInfoVm: WeightItemsViewModel by lazy { ViewModelProviders.of(activity).get(WeightItemsViewModel::class.java) }
    val calendar: Calendar by lazy {weightInfoVm.inputEntity.recTime.clone() as Calendar}

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val dataBinding = DataBindingUtil.inflate<FragmentWeightInputBinding>(inflater, R.layout.fragment_weight_input, container, false)

        activity.title = getString(if (viewMode == WEIGHT_INPUT_MODE.INPUT) R.string.toolbar_title_weight_input else R.string.toolbar_title_weight_view)
        setHasOptionsMenu(true)
        dataBinding.weightItem = weightInfoVm.inputEntity
        return dataBinding.root
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {

        setRecordDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
        editDate.setOnClickListener { _ ->
            dialog = DatePickerDialogFragment.newInstance(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
            dialog?.setTargetFragment(this@WeightInputFragment, Consts.REQUESTS.INPUT_DATE.ordinal)
            dialog?.show(fragmentManager, TAG_DIALOGS)
        }

        setRecordTime(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))
        editTime.setOnClickListener { _ ->
            dialog = TimePickerDialogFragment.newInstance(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))
            dialog?.setTargetFragment(this@WeightInputFragment, Consts.REQUESTS.INPUT_TIME.ordinal)
            dialog?.show(fragmentManager, TAG_DIALOGS)
        }

        editWeight.setOnFocusChangeListener { _, hasFocus -> if (!hasFocus) editWeight.setText(formatInputNumber(editWeight.text.toString(), getString(R.string.weight_input_weight_default))) }
        editFat.setOnFocusChangeListener { _, hasFocus -> if (!hasFocus) editFat.setText(formatInputNumber(editFat.text.toString(), getString(R.string.weight_input_fat_default))) }

        if (viewMode == WEIGHT_INPUT_MODE.VIEW) {
            showViewMode()
            fab.setOnClickListener {
                Log.d(ShowRecordsFragment.TAG, "fab!!!!")
                RxBus.publish(InputFragmentStartEvent(viewMode = WEIGHT_INPUT_MODE.INPUT))
            }
        } else {
            fab.hide()
            editMemo.maxLines = 5
        }
    }

    fun showViewMode() {
        editDate.isEnabled = false
        editTime.isEnabled = false
        editWeight.isEnabled = false
        editFat.isEnabled = false
        toggleDumbbell.isEnabled = false
        toggleLiquor.isEnabled = false
        toggleToilet.isEnabled = false
        toggleMoon.isEnabled = false
        toggleStar.isEnabled = false
        editMemo.isEnabled = false
    }


    override fun onResume() {
        super.onResume()
        if (viewMode == WEIGHT_INPUT_MODE.INPUT && editWeight.requestFocus()) {
            val imm: InputMethodManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(editWeight, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    override fun onPause() {
        super.onPause()
        if (activity.currentFocus != null) {
            val imm = context.getSystemService(
                    Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(activity.currentFocus.windowToken, 0)
            activity.currentFocus.clearFocus()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater?.inflate(R.menu.menu_weight_input, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        super.onPrepareOptionsMenu(menu)
        when (viewMode) {
            WEIGHT_INPUT_MODE.INPUT -> {
                menu?.findItem(R.id.action_delete)?.isVisible = false
                menu?.findItem(R.id.action_done)?.isVisible = true
            }
            WEIGHT_INPUT_MODE.VIEW -> {
                menu?.findItem(R.id.action_delete)?.isVisible = true
                menu?.findItem(R.id.action_done)?.isVisible = false
            }
        }

    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val itemId = item?.itemId
        if (itemId == R.id.action_done) {
            saveWeightData()
        }

        return super.onOptionsItemSelected(item)
    }

    fun saveWeightData() {
        if (TextUtils.isEmpty(editWeight.text)) {
            AlertDialog.Builder(context)
                    .setTitle(R.string.err_title_input)
                    .setMessage(R.string.err_weight_empty)
                    .setPositiveButton(android.R.string.ok, null)
                    .show()
            return
        }

        weightInfoVm.inputEntity = weightInfoVm.inputEntity.copy(
                recTime = calendar,
                weight = editWeight.text.toString().toDouble(),
                fat = if (TextUtils.isEmpty(editFat.text)) 0.0 else editFat.text.toString().toDouble(),
                showDumbbell = toggleDumbbell.isChecked,
                showLiquor = toggleLiquor.isChecked,
                showToilet = toggleToilet.isChecked,
                showMoon = toggleMoon.isChecked,
                showStar = toggleStar.isChecked,
                memo = editMemo.text.toString()
        )

        disposable.add(weightInfoVm.insertWeightItem(weightInfoVm.inputEntity)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    Log.d(TAG, "complete insertWeightItem::weight = ${weightInfoVm.inputEntity.weight}, fat = ${weightInfoVm.inputEntity.fat}")
                    fragmentManager.popBackStack()
                })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        when (requestCode) {

            Consts.REQUESTS.INPUT_DATE.ordinal -> {
                if (resultCode != Activity.RESULT_OK) return
                val year = data?.getIntExtra(DatePickerDialogFragment.RESULT_YEAR, -1) ?: -1
                val month = data?.getIntExtra(DatePickerDialogFragment.RESULT_MONTH, -1) ?: -1
                val day = data?.getIntExtra(DatePickerDialogFragment.RESULT_DAY, -1) ?: -1

                if (year >= 0 && month >= 0 && day >= 0) {
                    setRecordDate(year, month, day)
                }
            }

            Consts.REQUESTS.INPUT_TIME.ordinal -> {
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
        if (calendar.get(Calendar.YEAR) != year
                || calendar.get(Calendar.MONTH) != month
                || calendar.get(Calendar.DAY_OF_MONTH) != day) {
            calendar.set(year, month, day)
        }
        editDate.setText(DateFormat.getDateFormat(activity.applicationContext).format(calendar.time))
    }

    fun setRecordTime(hour: Int, minute: Int) {
        if (calendar.get(Calendar.HOUR_OF_DAY) != hour
                || calendar.get(Calendar.MINUTE) != minute) {
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)
        }
        editTime.setText(DateFormat.getTimeFormat(activity.applicationContext).format(calendar.time))
    }

}