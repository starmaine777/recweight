package com.starmaine777.recweight.views

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.widget.DatePicker

/**
 * Created by ai on 2017/07/02.
 */

class DatePickerDialogFragment : DialogFragment(), DatePickerDialog.OnDateSetListener {

    companion object {

        const val TAG = "DatePickerDialogFragment"

        const val RESULT_YEAR = "year"
        const val RESULT_MONTH = "month"
        const val RESULT_DAY = "day"

        val ARGS_YEAR = "year"
        val ARGS_MONTH = "month"
        val ARGS_DAY = "day"

        fun newInstance(year: Int, month: Int, day: Int): DatePickerDialogFragment {
            return DatePickerDialogFragment()
                    .apply {
                        arguments = Bundle().apply {
                            putInt(ARGS_YEAR, year)
                            putInt(ARGS_MONTH, month)
                            putInt(ARGS_DAY, day)
                        }
                    }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val year = arguments?.getInt(ARGS_YEAR) ?: 0
        val month = arguments?.getInt(ARGS_MONTH) ?: 0
        val day = arguments?.getInt(ARGS_DAY) ?: 0

        val dialog = DatePickerDialog(context, this, year, month, day)
        return dialog
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        if (targetFragment != null && targetRequestCode != 0) {
            val intent = Intent()
            intent.putExtra(RESULT_YEAR, year)
            intent.putExtra(RESULT_MONTH, month)
            intent.putExtra(RESULT_DAY, dayOfMonth)
            targetFragment.onActivityResult(targetRequestCode, Activity.RESULT_OK, intent)
        }
        dismiss()
    }


}
