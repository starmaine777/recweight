package com.starmaine777.recweight.views

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteConstraintException
import android.os.Bundle
import android.text.TextUtils
import android.text.format.DateFormat
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.starmaine777.recweight.R
import com.starmaine777.recweight.data.repo.WeightItemRepository
import com.starmaine777.recweight.databinding.FragmentWeightInputBinding
import com.starmaine777.recweight.event.InputFragmentStartEvent
import com.starmaine777.recweight.event.RxBus
import com.starmaine777.recweight.model.viewmodel.WeightInputViewModel
import com.starmaine777.recweight.utils.REQUESTS
import com.starmaine777.recweight.utils.WEIGHT_INPUT_MODE
import com.starmaine777.recweight.utils.formatInputNumber
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.*

/**
 * Created by 0025331458 on 2017/06/29.
 * Fragment to input weight and information
 */
class WeightInputFragment : Fragment() {

    private val viewMode: WEIGHT_INPUT_MODE by lazy { arguments?.getSerializable(ARGS_MODE) as WEIGHT_INPUT_MODE }
    private var dialog: DialogFragment? = null
    private var alertDialog: AlertDialog? = null
    private val viewModelFactory: WeightInputViewModel.Factory = WeightInputViewModel.Factory(WeightItemRepository())
    private val viewModel: WeightInputViewModel by activityViewModels { viewModelFactory }
    private lateinit var binding: FragmentWeightInputBinding

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentWeightInputBinding.inflate(inflater)

        requireActivity().title = getString(if (viewMode == WEIGHT_INPUT_MODE.INPUT) R.string.toolbar_title_weight_input else R.string.toolbar_title_weight_view)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        viewModel.selectedEntityId(requireContext(), arguments?.getLong(ARGS_ID))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    val weightItem = viewModel.inputEntity
                    binding.apply {
                        editWeight.setText(weightItem.weightString())
                        editFat.setText(weightItem.fatString())
                        toggleDumbbell.isChecked = weightItem.showDumbbell
                        toggleLiquor.isChecked = weightItem.showLiquor
                        toggleToilet.isChecked = weightItem.showToilet
                        toggleMoon.isChecked = weightItem.showMoon
                        toggleStar.isChecked = weightItem.showStar
                        editMemo.setText(weightItem.memo)

                        setRecordDate(viewModel.calendar.get(Calendar.YEAR), viewModel.calendar.get(Calendar.MONTH), viewModel.calendar.get(Calendar.DAY_OF_MONTH))
                        editDate.setOnClickListener { _ ->
                            dialog = DatePickerDialogFragment.newInstance(viewModel.calendar.get(Calendar.YEAR), viewModel.calendar.get(Calendar.MONTH), viewModel.calendar.get(Calendar.DAY_OF_MONTH))
                            dialog?.setTargetFragment(this@WeightInputFragment, REQUESTS.INPUT_DATE.ordinal)
                            dialog?.show(requireFragmentManager(), TAG_DIALOGS)
                        }

                        setRecordTime(viewModel.calendar.get(Calendar.HOUR_OF_DAY), viewModel.calendar.get(Calendar.MINUTE))
                        editTime.setOnClickListener { _ ->
                            dialog = TimePickerDialogFragment.newInstance(viewModel.calendar.get(Calendar.HOUR_OF_DAY), viewModel.calendar.get(Calendar.MINUTE))
                            dialog?.setTargetFragment(this@WeightInputFragment, REQUESTS.INPUT_TIME.ordinal)
                            dialog?.show(requireFragmentManager(), TAG_DIALOGS)
                        }

                        editWeight.setOnFocusChangeListener { _, hasFocus -> if (!hasFocus && !editWeight.text.isEmpty()) editWeight.setText(formatInputNumber(editWeight.text.toString(), getString(R.string.weight_input_weight_default))) }
                        editFat.setOnFocusChangeListener { _, hasFocus -> if (!hasFocus && !editFat.text.isEmpty()) editFat.setText(formatInputNumber(editFat.text.toString(), getString(R.string.weight_input_fat_default))) }

                        if (viewMode == WEIGHT_INPUT_MODE.VIEW) {
                            showViewMode()
                            fab.setOnClickListener {
                                RxBus.publish(InputFragmentStartEvent(WEIGHT_INPUT_MODE.INPUT, arguments?.getLong(ARGS_ID)))
                            }
                        } else {
                            fab.hide()
                            editMemo.maxLines = 5
                        }
                    }

                }, {
                    alertDialog = AlertDialog.Builder(requireContext())
                            .setTitle(R.string.err_title_db)
                            .setMessage(R.string.err_title_db)
                            .setPositiveButton(android.R.string.ok, { _, _ ->
                                requireFragmentManager().popBackStack()
                            })
                            .setOnDismissListener { requireFragmentManager().popBackStack() }
                            .show()
                })
    }

    private fun showViewMode() {
        binding.apply {
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
    }


    override fun onStart() {
        super.onStart()
        if (viewMode == WEIGHT_INPUT_MODE.INPUT && binding.editWeight.requestFocus()) {
            val imm: InputMethodManager = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.editWeight, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    override fun onPause() {
        super.onPause()
        if (requireActivity().currentFocus != null) {
            val imm = requireContext().getSystemService(
                    Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(requireActivity().currentFocus?.windowToken, 0)
            requireActivity().currentFocus?.clearFocus()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_weight_input, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        when (viewMode) {
            WEIGHT_INPUT_MODE.INPUT -> {
                menu.findItem(R.id.action_delete)?.isVisible = false
                menu.findItem(R.id.action_done)?.isVisible = true
            }
            WEIGHT_INPUT_MODE.VIEW -> {
                menu.findItem(R.id.action_delete)?.isVisible = true
                menu.findItem(R.id.action_done)?.isVisible = false
            }
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_done -> saveWeightData()
            R.id.action_delete -> deleteWeightData()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun saveWeightData() {
        Timber.d("saveWeightData")
        if (TextUtils.isEmpty(binding.editWeight.text)) {
            AlertDialog.Builder(requireContext())
                    .setTitle(R.string.err_title_input)
                    .setMessage(R.string.err_weight_empty)
                    .setPositiveButton(android.R.string.ok, null)
                    .show()
            return
        }

        val fatText = binding.editFat.text
        if (!fatText.isEmpty() && fatText.toString().toDouble() >= 100.0) {
            AlertDialog.Builder(requireContext())
                    .setTitle(R.string.err_title_input)
                    .setMessage(R.string.err_fat_over_100)
                    .setPositiveButton(android.R.string.ok, null)
                    .show()
            return
        }

        val progressDialog = ProgressDialog(requireContext())
        progressDialog.setCancelable(false)
        progressDialog.show()

        binding.apply {
            viewModel.insertOrUpdateWeightItem(
                    requireContext(),
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
                        requireFragmentManager().popBackStack()
                    }, { e ->
                        e.printStackTrace()
                        progressDialog.dismiss()
                        if (e is SQLiteConstraintException) {
                            AlertDialog.Builder(requireContext())
                                    .setTitle(R.string.err_title_db)
                                    .setMessage(R.string.err_db_registered_same_time)
                                    .setPositiveButton(android.R.string.ok, null)
                                    .show()
                        }
                    })
        }
    }

    private fun deleteWeightData() {
        if (viewMode == WEIGHT_INPUT_MODE.VIEW) {

            val progressDialog = ProgressDialog(requireContext())
            progressDialog.setCancelable(false)
            progressDialog.show()

            viewModel.deleteWeightItem(requireContext())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        progressDialog.dismiss()
                        requireFragmentManager().popBackStack()
                    }, { e ->
                        e.printStackTrace()
                        progressDialog.dismiss()
                        AlertDialog.Builder(requireContext())
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

    private fun setRecordDate(year: Int, month: Int, day: Int) {
        if (viewModel.calendar.get(Calendar.YEAR) != year
                || viewModel.calendar.get(Calendar.MONTH) != month
                || viewModel.calendar.get(Calendar.DAY_OF_MONTH) != day) {
            viewModel.calendar.set(year, month, day)
        }
        requireContext().let {
            binding.editDate.setText(
                    DateFormat.getDateFormat(requireContext()).format(viewModel.calendar.time))
        }
    }

    private fun setRecordTime(hour: Int, minute: Int) {
        if (viewModel.calendar.get(Calendar.HOUR_OF_DAY) != hour
                || viewModel.calendar.get(Calendar.MINUTE) != minute) {
            viewModel.calendar.set(Calendar.HOUR_OF_DAY, hour)
            viewModel.calendar.set(Calendar.MINUTE, minute)
        }
        requireContext().let {
            binding.editTime.setText(
                    DateFormat.getTimeFormat(requireActivity()).format(viewModel.calendar.time))
        }
    }

}