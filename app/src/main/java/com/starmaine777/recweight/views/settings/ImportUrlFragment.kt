package com.starmaine777.recweight.views.settings

import android.Manifest
import android.accounts.AccountManager
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.common.GoogleApiAvailability
import com.starmaine777.recweight.data.ImportRepository
import com.starmaine777.recweight.error.SpreadSheetsException
import com.starmaine777.recweight.error.SpreadSheetsException.ERROR_TYPE
import com.starmaine777.recweight.utils.Consts
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

/**
 * Created by 0025331458 on 2017/08/16.
 */
class ImportUrlFragment : Fragment() {
    val importRepo: ImportRepository by lazy { ImportRepository(context) }
    var disposable = CompositeDisposable()

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }

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
                        when (t.type) {
                            ERROR_TYPE.ACCOUNT_PERMISSION_DENIED -> {
                                ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.GET_ACCOUNTS), Consts.REQUESTS.SHOW_ACCOUNT_PERMISSION.ordinal)
                            }
                            ERROR_TYPE.ACCOUNT_NOT_SELECTED -> {
                                startActivityForResult(importRepo.credential.newChooseAccountIntent(), Consts.REQUESTS.SHOW_ACCOUNT_PICKER.ordinal)
                            }
                            ERROR_TYPE.PLAY_SERVICE_AVAILABILITY_ERROR -> {
                                val apiAvailability = GoogleApiAvailability.getInstance()
                                apiAvailability.getErrorDialog(activity, t.errorCode, Consts.REQUESTS.SHOW_GOOGLE_PLAY_SERVICE.ordinal).show()
                            }
                            ERROR_TYPE.DEVICE_OFFLINE -> {
                            }
                            ERROR_TYPE.FATAL_ERROR -> {
                            }
                        }
                    } else {
                        t.printStackTrace()
                    }
                }).let { disposable.add(it) }
    }

    override fun onStop() {
        super.onStop()
        disposable.dispose()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            Consts.REQUESTS.SHOW_ACCOUNT_PERMISSION.ordinal -> {
                importRepo.getResultFromApi()
            }

            Consts.REQUESTS.SHOW_ACCOUNT_PICKER.ordinal -> {
                if (resultCode == Activity.RESULT_OK) {
                    val accountName = data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
                    if (!TextUtils.isEmpty(accountName)) {
                        val editor = activity.getPreferences(Context.MODE_PRIVATE).edit()
                        editor.putString(Consts.PREFERENCE_KEY.ACCOUNT_NAME.name, accountName)
                        editor.apply()
                        importRepo.credential.selectedAccountName = accountName!!
                        startToGetSpleadSheetsData()
                    }
                }
            }

            Consts.REQUESTS.SHOW_GOOGLE_PLAY_SERVICE.ordinal -> {
                if (resultCode == Activity.RESULT_OK) {
                    importRepo.getResultFromApi()
                }
            }

            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        Log.d("test", "onRequestPermissionResult requestCode = $requestCode")
        when (requestCode) {
            Consts.REQUESTS.SHOW_ACCOUNT_PERMISSION.ordinal -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    importRepo.getResultFromApi()
                } else {

                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


}