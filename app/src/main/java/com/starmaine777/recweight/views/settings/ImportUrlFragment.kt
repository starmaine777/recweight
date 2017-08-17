package com.starmaine777.recweight.views.settings

import android.accounts.AccountManager
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
    }

    override fun showAccountChoice(credential: GoogleAccountCredential) {
        startActivityForResult(credential.newChooseAccountIntent(), Consts.REQUESTS.SHOW_ACCOUNT_PICKER.ordinal)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            Consts.REQUESTS.SHOW_ACCOUNT_PICKER.ordinal -> {
                if (resultCode == Activity.RESULT_OK) {
                    val accountName = data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
                    if (!TextUtils.isEmpty(accountName)) {
                        val editor = activity.getPreferences(Context.MODE_PRIVATE).edit()
                        editor.putString(Consts.PREFERENCE_KEY.ACCOUNT_NAME.name, accountName)
                        editor.apply()
                        importRepo.getResultFromApi()
                    }
                }
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }



}