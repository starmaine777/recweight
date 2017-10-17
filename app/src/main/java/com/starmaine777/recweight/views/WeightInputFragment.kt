package com.starmaine777.recweight.views

import android.app.Activity
import android.app.ProgressDialog
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
import android.view.*
import android.view.inputmethod.InputMethodManager
import com.starmaine777.recweight.R
import com.starmaine777.recweight.data.WeightInputViewModel
import com.starmaine777.recweight.databinding.FragmentWeightInputBinding
import com.starmaine777.recweight.event.InputFragmentStartEvent
import com.starmaine777.recweight.event.RxBus
import com.starmaine777.recweight.utils.WEIGHT_INPUT_MODE
import com.starmaine777.recweight.utils.REQUESTS
import com.starmaine777.recweight.utils.formatInputNumber
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_weight_input.*
import timber.log.Timber
import java.util.*

/**
 * Created by 0025331458 on 2017/06/29.
 * Fragment to input weight and informations
 */
class WeightInputFragment : Fragment() {

    val viewMode: WEIGHT_INPUT_MODE by lazy { arguments.getSerializable(ARGS_MODE) as WEIGHT_INPUT_MODE }
    var dialog: DialogFragment? = null
    var alertDialog: AlertDialog? = null
    val weightInputVm: WeightInputViewModel by lazy { ViewModelProviders.of(activity).get(WeightInputViewModel::class.java) }
    var dataBinding: FragmentWeightInputBinding? = null

    companion object {

        const val TAG = "WeightInputFragment"

        val ARGS_MODE = "mode"
        val ARGS_ID = "id"

        val TAG_DIALOGS = "dialogs"

        fun newInstance(weightInputMode: WEIGHT_INPUT_MODE, id: Long?): WeightInputFragment {
            Timber.d("newInstance mode = $weightInputMode, id = $id")
            val fragment = WeightInputFragment()
            val bundle = Bundle()
            bundle.putSerializable(ARGS_MODE, weightInputMode)
            if (id != null) {
                bundle.putLong(ARGS_ID, id)
            }
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
        dataBinding = DataBindingUtil.inflate<FragmentWeightInputBinding>(inflater, R.layout.fragment_weight_input, container, false)

        activity.title = getString(if (viewMode == WEIGHT_INPUT_MODE.INPUT) R.string.toolbar_title_weight_input else R.string.toolbar_title_weight_view)
        setHasOptionsMenu(true)
        return dataBinding!!.root
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        weightInputVm.selectedEntityId(context, arguments.getLong(ARGS_ID), {
            //dataGetSuccess
            dataBinding?.weightItem = weightInputVm.inputEntity

            setRecordDate(weightInputVm.calendar.get(Calendar.YEAR), weightInputVm.calendar.get(Calendar.MONTH), weightInputVm.calendar.get(Calendar.DAY_OF_MONTH))
            editDate.setOnClickListener { _ ->
                dialog = DatePickerDialogFragment.newInstance(weightInputVm.calendar.get(Calendar.YEAR), weightInputVm.calendar.get(Calendar.MONTH), weightInputVm.calendar.get(Calendar.DAY_OF_MONTH))
                dialog?.setTargetFragment(this@WeightInputFragment, REQUESTS.INPUT_DATE.ordinal)
                dialog?.show(fragmentManager, TAG_DIALOGS)
            }

            setRecordTime(weightInputVm.calendar.get(Calendar.HOUR_OF_DAY), weightInputVm.calendar.get(Calendar.MINUTE))
            editTime.setOnClickListener { _ ->
                dialog = TimePickerDialogFragment.newInstance(weightInputVm.calendar.get(Calendar.HOUR_OF_DAY), weightInputVm.calendar.get(Calendar.MINUTE))
                dialog?.setTargetFragment(this@WeightInputFragment, REQUESTS.INPUT_TIME.ordinal)
                dialog?.show(fragmentManager, TAG_DIALOGS)
            }

            editWeight.setOnFocusChangeListener { _, hasFocus -> if (!hasFocus) editWeight.setText(formatInputNumber(editWeight.text.toString(), getString(R.string.weight_input_weight_default))) }
            editFat.setOnFocusChangeListener { _, hasFocus -> if (!hasFocus) editFat.setText(formatInputNumber(editFat.text.toString(), getString(R.string.weight_input_fat_default))) }

            if (viewMode == WEIGHT_INPUT_MODE.VIEW) {
                showViewMode()
                fab.setOnClickListener {
                    RxBus.publish(InputFragmentStartEvent(WEIGHT_INPUT_MODE.INPUT, arguments.getLong(ARGS_ID)))
                }
            } else {
                fab.hide()
                editMemo.maxLines = 5
            }
        }, {
            //Error
            alertDialog = AlertDialog.Builder(context)
                    .setTitle(R.string.err_title_db)
                    .setMessage(R.string.err_title_db)
                    .setPositiveButton(android.R.string.ok, { _, _ ->
                        fragmentManager.popBackStack()
                    })
                    .setOnDismissListener { fragmentManager.popBackStack() }
                    .show()
        })
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
        when (item?.itemId) {
            R.id.action_done -> saveWeightData()
            R.id.action_delete -> deleteWeightData()
        }
        return super.onOptionsItemSelected(item)
    }

    fun saveWeightData() {
        Timber.d("saveWeightData")
        if (TextUtils.isEmpty(editWeight.text)) {
            AlertDialog.Builder(context)
                    .setTitle(R.string.err_title_input)
                    .setMessage(R.string.err_weight_empty)
                    .setPositiveButton(android.R.string.ok, null)
                    .show()
            return
        }

        val progressDialog = ProgressDialog(context)
        progressDialog.setCancelable(false)
        progressDialog.show()

        weightInputVm.insertOrUpdateWeightItem(
                context,
                editWeight.text.toString().toDouble(),
                if (TextUtils.isEmpty(editFat.text)) 0.0 else editFat.text.toString().toDouble(),
                toggleDumbbell.isChecked,
                toggleLiquor.isChecked,
                toggleToilet.isChecked,
                toggleMoon.isChecked,
                toggleStar.isChecked,
                editMemo.text.toString())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Timber.d(TAG, "insertOrUpdateWeightItem complete")
                    progressDialog.dismiss()
                    fragmentManager.popBackStack()
                }, {
                    e ->
                    e.printStackTrace()
                })
    }

    fun deleteWeightData() {
        if (viewMode == WEIGHT_INPUT_MODE.VIEW) {

            val progressDialog = ProgressDialog(context)
            progressDialog.setCancelable(false)
            progressDialog.show()



            weightInputVm.deleteWeightItem(context)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        progressDialog.dismiss()
                        fragmentManager.popBackStack()
                    }, {
                        AlertDialog.Builder(context)
                                .setTitle(R.string.err_title_db)
                                .setMessage(R.string.err_db)
                                .setPositiveButton(android.R.string.ok, null)
                                .show()
                    })
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        when (requestCode) {

            REQUESTS.INPUT_DATE.ordinal -> {
                if (resultCode != Activity.RESULT_OK) return
                val year = data?.getIntExtra(DatePickerDialogFragment.RESULT_YEAR, -1) ?: -1
                val month = data?.getIntExtra(DatePickerDialogFragment.RESULT_MONTH, -1) ?: -1
                val day = data?.getIntExtra(DatePickerDialogFragment.RESULT_DAY, -1) ?: -1

                if (year >= 0 && month >= 0 && day >= 0) {
                    setRecordDate(year, month, day)
                }
            }

            REQUESTS.INPUT_TIME.ordinal -> {
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
        if (weightInputVm.calendar.get(Calendar.YEAR) != year
                || weightInputVm.calendar.get(Calendar.MONTH) != month
                || weightInputVm.calendar.get(Calendar.DAY_OF_MONTH) != day) {
            weightInputVm.calendar.set(year, month, day)
        }
        editDate.setText(DateFormat.getDateFormat(context).format(weightInputVm.calendar.time))
    }

    fun setRecordTime(hour: Int, minute: Int) {
        if (weightInputVm.calendar.get(Calendar.HOUR_OF_DAY) != hour
                || weightInputVm.calendar.get(Calendar.MINUTE) != minute) {
            weightInputVm.calendar.set(Calendar.HOUR_OF_DAY, hour)
            weightInputVm.calendar.set(Calendar.MINUTE, minute)
        }
        editTime.setText(DateFormat.getTimeFormat(activity.applicationContext).format(weightInputVm.calendar.time))
    }

}