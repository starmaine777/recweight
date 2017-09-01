package com.starmaine777.recweight.views.settings

import android.Manifest
import android.accounts.AccountManager
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.text.TextUtils
import android.util.Log
import com.google.android.gms.common.GoogleApiAvailability
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.starmaine777.recweight.R
import com.starmaine777.recweight.data.ImportRepository
import com.starmaine777.recweight.error.SpreadSheetsException
import com.starmaine777.recweight.error.SpreadSheetsException.ERROR_TYPE
import com.starmaine777.recweight.utils.PREFERENCE_KEY
import com.starmaine777.recweight.utils.REQUESTS
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

/**
 * ImportのURL入力Fragment
 * Created by 0025331458 on 2017/08/16.
 */
class ImportUrlFragment : Fragment() {
    val importRepo: ImportRepository by lazy { ImportRepository(context) }
    var disposable = CompositeDisposable()
    var dialog: AlertDialog? = null

    override fun onStart() {
        super.onStart()
        startToGetSpleadSheetsData()
        disposable = CompositeDisposable()
    }

    private fun startToGetSpleadSheetsData() {
        importRepo.getResultFromApi()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Log.d("test", "subscribed!!!")
                }, { t: Throwable ->
                    Log.d("test", "Error happened! $t")
                    if (t is SpreadSheetsException) {
                        Log.d("test", "Error happened! ${t.type}, code = ${t.errorCode}")
                        when (t.type) {
                            ERROR_TYPE.ACCOUNT_PERMISSION_DENIED -> {
                                ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.GET_ACCOUNTS), REQUESTS.SHOW_ACCOUNT_PERMISSION.ordinal)
                            }
                            ERROR_TYPE.ACCOUNT_NOT_SELECTED -> {
                                startActivityForResult(importRepo.credential.newChooseAccountIntent(), REQUESTS.SHOW_ACCOUNT_PICKER.ordinal)
                            }
                            ERROR_TYPE.PLAY_SERVICE_AVAILABILITY_ERROR -> {
                                val apiAvailability = GoogleApiAvailability.getInstance()
                                apiAvailability.getErrorDialog(activity, t.errorCode, REQUESTS.SHOW_GOOGLE_PLAY_SERVICE.ordinal).show()
                            }
                            ERROR_TYPE.DEVICE_OFFLINE -> {
                            }
                            ERROR_TYPE.FATAL_ERROR -> {
                                showErrorDialog(R.string.err_import_title_fatal, R.string.err_import_fatal)
                            }
                            ERROR_TYPE.SHEETS_ILLEGAL_TEMPLATE_ERROR -> {
                            }
                        }
                    } else if (t is UserRecoverableAuthIOException) {
                        Log.d("test", "UserRecoverableAuthIOException startActivity")
                        startActivityForResult(t.intent, REQUESTS.REQUEST_AUTHORIZATION.ordinal)
                    }
                }).let { disposable.add(it) }
    }

    override fun onStop() {
        super.onStop()
        disposable.dispose()
        dialog?.dismiss()
        dialog = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUESTS.SHOW_ACCOUNT_PERMISSION.ordinal -> {
                importRepo.getResultFromApi()
            }

            REQUESTS.SHOW_ACCOUNT_PICKER.ordinal -> {
                if (resultCode == Activity.RESULT_OK) {
                    val accountName = data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
                    if (!TextUtils.isEmpty(accountName)) {
                        val editor = activity.getPreferences(Context.MODE_PRIVATE).edit()
                        editor.putString(PREFERENCE_KEY.ACCOUNT_NAME.name, accountName)
                        editor.apply()
                        importRepo.credential.selectedAccountName = accountName!!
                        startToGetSpleadSheetsData()
                    }
                }
            }

            REQUESTS.SHOW_GOOGLE_PLAY_SERVICE.ordinal -> {
                if (resultCode == Activity.RESULT_OK) {
                    startToGetSpleadSheetsData()
                }
            }

            REQUESTS.REQUEST_AUTHORIZATION.ordinal -> {
                if (resultCode == Activity.RESULT_OK) {
                    startToGetSpleadSheetsData()
                }
            }

            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        Log.d("test", "onRequestPermissionResult requestCode = $requestCode")
        when (requestCode) {
            REQUESTS.SHOW_ACCOUNT_PERMISSION.ordinal -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startToGetSpleadSheetsData()
                } else {

                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    fun showErrorDialog(titleId:Int, messageId:Int) {


        showErrorDialog(resources.getString(titleId), resources.getString(messageId))
    }

    fun showErrorDialog(title:String, message:String) {
        if (TextUtils.isEmpty(message)) {
            return
        }

        val builder = AlertDialog.Builder(context)
        if (!TextUtils.isEmpty(title)) {
            builder.setTitle(title)
        }
        builder.setMessage(message)
                .setOnDismissListener { fragmentManager.popBackStack() }
                .setPositiveButton(android.R.string.ok
                        , {dialog, _ -> dialog.dismiss()})

        dialog = builder.show()
    }

}