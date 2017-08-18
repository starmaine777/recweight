package com.starmaine777.recweight.views.settings

import android.Manifest
import android.accounts.AccountManager
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.common.GoogleApiAvailability
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.starmaine777.recweight.data.ImportRepository
import com.starmaine777.recweight.utils.Consts

/**
 * Created by 0025331458 on 2017/08/16.
 */
class ImportUrlFragment:Fragment(), ImportRepository.ImportEventListener {
    val importRepo :ImportRepository by lazy {ImportRepository(context, this)}
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        importRepo.getResultFromApi()
        return super.onCreateView(inflater, container, savedInstanceState)
    }


    override fun onError(errorCode: ImportRepository.ERROR) {
        Log.d("test", "onError!!!! $errorCode")
        when (errorCode) {
            ImportRepository.ERROR.ACCOUNT_PERMISSION_DENIED -> {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {

                } else {
//                    if (activity.shouldShowRequestPermissionRationale(Manifest.permission.GET_ACCOUNTS)) {
//                        Log.d("test", "shouldShowRequestPermissionRationale true")
//
//                    } else {
                        Log.d("test", "shouldShowRequestPermissionRationale false")
                        ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.GET_ACCOUNTS), Consts.REQUESTS.SHOW_ACCOUNT_PERMISSION.ordinal)
//                    }
                }
            }
            ImportRepository.ERROR.GPS_AVAILABILITY_ERROR -> {
                val apiAvailability = GoogleApiAvailability.getInstance()
                apiAvailability.getErrorDialog(activity, errorCode.statusCode, Consts.REQUESTS.SHOW_GOOGLE_PLAY_SERVICE.ordinal).show()
            }
            ImportRepository.ERROR.GPS_AVAILABILITY_FATAL_ERROR -> {

            }
        }


    }

    override fun showAccountChoice(credential: GoogleAccountCredential) {
        startActivityForResult(credential.newChooseAccountIntent(), Consts.REQUESTS.SHOW_ACCOUNT_PICKER.ordinal)
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
                        importRepo.retryGetResult(accountName!!)
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