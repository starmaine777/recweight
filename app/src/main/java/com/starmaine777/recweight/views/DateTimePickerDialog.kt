package com.starmaine777.recweight.views

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.starmaine777.recweight.R
import kotlinx.android.synthetic.main.dialog_date_time_picker.*
import java.util.*

/**
 * Created by ai on 2017/07/01.
 * Dialog to input date and time
 */

class DateTimePickerDialog : DialogFragment() {

    enum class DATE_TIME_INPUT_MODE {
        DATE,
        TIME,
        DATE_TIME
    }

    companion object {
        const val RESULT_YEAR = "year"
        const val RESULT_MONTH = "month"
        const val RESULT_DAY = "day"
        const val RESULT_HOUR = "hour"
        const val RESULT_MINUTE = "time"

        const val TAG = "DateTimePickerDialog"

        val ARGS_CALENDAR: String = "dateTime"
        val ARGS_DATE_TIME_INPUT_MODE: String = "inputMode"

        fun newInstance(dateTime: Calendar, DATETIMEInputMode: DATE_TIME_INPUT_MODE): DateTimePickerDialog {
            val fragment = DateTimePickerDialog()
            val bundle = Bundle()
            bundle.putSerializable(ARGS_CALENDAR, dateTime)
            bundle.putSerializable(ARGS_DATE_TIME_INPUT_MODE, DATETIMEInputMode)
            fragment.arguments = bundle

            return fragment
        }
    }

    val dateTime: Calendar by lazy { arguments.getSerializable(ARGS_CALENDAR) as Calendar }
    val inputMode: DATE_TIME_INPUT_MODE by lazy { arguments.getSerializable(ARGS_DATE_TIME_INPUT_MODE) as DATE_TIME_INPUT_MODE }

    var year = -1
    var month = -1
    var day = -1
    var hour = -1
    var minute = -1

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater!!.inflate(R.layout.dialog_date_time_picker, container, false)
        return view
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        changeViewMode(inputMode != DATE_TIME_INPUT_MODE.TIME)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
    }

    fun changeViewMode(isDateMode: Boolean) {
        if (isDateMode) {
            datePicker.visibility = View.VISIBLE
            timePicker.visibility = View.GONE

            datePicker.init(dateTime.get(Calendar.YEAR),
                    dateTime.get(Calendar.MONTH),
                    dateTime.get(Calendar.DAY_OF_MONTH),
                    { _, year, monthOfYear, dayOfMonth ->
                        this.year = year
                        this.month = monthOfYear
                        this.day = dayOfMonth
                        if (inputMode == DATE_TIME_INPUT_MODE.DATE) {
                            sendResult()
                            dismiss()
                        } else {
                            dateTime.set(year, monthOfYear, dayOfMonth)
                            changeViewMode(false)
                        }
                    })
        } else {
            datePicker.visibility = View.GONE
            timePicker.visibility = View.VISIBLE
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                timePicker.currentHour = dateTime.get(Calendar.HOUR)
                timePicker.currentMinute = dateTime.get(Calendar.MINUTE)
            } else {
                timePicker.hour = dateTime.get(Calendar.HOUR)
                timePicker.minute = dateTime.get(Calendar.MINUTE)
            }
            timePicker.setOnTimeChangedListener { _, hourOfDay, minute ->
                this.hour = hourOfDay
                this.minute = minute
                sendResult()
                dismiss()
            }
        }
    }

    fun sendResult() {

        if (targetFragment == null || targetRequestCode == 0) return

        val intent: Intent = Intent()
        if (year >= 0 && month >= 0 && day >= 0) {
            intent.putExtra(RESULT_YEAR, this.year)
            intent.putExtra(RESULT_MONTH, this.month)
            intent.putExtra(RESULT_DAY, this.day)
        }
        if (hour >= 0 && minute >= 0) {
            intent.putExtra(RESULT_HOUR, this.hour)
            intent.putExtra(RESULT_MINUTE, this.minute)
        }
        targetFragment!!.onActivityResult(targetRequestCode, Activity.RESULT_OK, intent)
    }
}
