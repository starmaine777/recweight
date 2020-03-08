package com.starmaine777.recweight.views

import android.app.Activity
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import android.widget.TimePicker

/**
 * Created by ai on 2017/07/02.
 */
class TimePickerDialogFragment : DialogFragment(), TimePickerDialog.OnTimeSetListener {

    companion object {

        const val TAG = "TimePickerDialogFragment"

        const val RESULT_HOUR = "hour"
        const val RESULT_MINUTE = "time"

        val ARGS_HOUR = "hour"
        val ARGS_MINUTE = "minute"

        fun newInstance(hour: Int, minute: Int): TimePickerDialogFragment {
            return TimePickerDialogFragment()
                    .apply {
                        arguments = Bundle().apply {
                            putInt(ARGS_HOUR, hour)
                            putInt(ARGS_MINUTE, minute)
                        }
                    }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val hour = arguments?.getInt(ARGS_HOUR) ?: 0
        val minute = arguments?.getInt(ARGS_MINUTE) ?: 0

        val dialog = TimePickerDialog(context, this, hour, minute, true)
        return dialog
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        if (targetFragment != null && targetRequestCode != 0) {
            val intent = Intent()
            intent.putExtra(RESULT_HOUR, hourOfDay)
            intent.putExtra(RESULT_MINUTE, minute)

            targetFragment?.onActivityResult(targetRequestCode, Activity.RESULT_OK, intent)
        }
        dismiss()
    }
}