package com.starmaine777.recweight.views.settings

import android.Manifest
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AlertDialog
import android.text.TextUtils
import com.google.android.gms.common.GoogleApiAvailability
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.starmaine777.recweight.R
import com.starmaine777.recweight.data.ExportRepository
import com.starmaine777.recweight.error.SpreadSheetsException
import com.starmaine777.recweight.utils.REQUESTS
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

/**
 * Created by 0025331458 on 2017/09/12.
 */
class ExportFragment : Fragment() {

    var disposable = CompositeDisposable()
    var dialog: AlertDialog? = null

    override fun onResume() {
        super.onResume()
        exportDatas()
    }

    override fun onStop() {
        super.onStop()
        disposable.dispose()
    }

    @Throws()
    fun exportDatas() {
        val exportRepo = ExportRepository(context)
        exportRepo.exportDatas(context)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    value ->
                    Timber.d("createNewSheets result = $value")
                }, { t: Throwable ->
                    Timber.d("Error happened! $t")
                    if (t is SpreadSheetsException) {
                        Timber.d("Error happened! ${t.type}, code = ${t.errorCode}")
                        when (t.type) {
                            SpreadSheetsException.ERROR_TYPE.ACCOUNT_PERMISSION_DENIED -> {
                                requestPermissions(arrayOf(Manifest.permission.GET_ACCOUNTS), REQUESTS.SHOW_ACCOUNT_PERMISSION.ordinal)
                            }
                            SpreadSheetsException.ERROR_TYPE.ACCOUNT_NOT_SELECTED -> {
                                startActivityForResult(exportRepo.credential.newChooseAccountIntent(), REQUESTS.SHOW_ACCOUNT_PICKER.ordinal)
                            }
                            SpreadSheetsException.ERROR_TYPE.PLAY_SERVICE_AVAILABILITY_ERROR -> {
                                val apiAvailability = GoogleApiAvailability.getInstance()
                                apiAvailability.getErrorDialog(activity, t.errorCode, REQUESTS.SHOW_GOOGLE_PLAY_SERVICE.ordinal).show()
                            }
                            SpreadSheetsException.ERROR_TYPE.DEVICE_OFFLINE -> {
                            }
                            SpreadSheetsException.ERROR_TYPE.FATAL_ERROR -> {
                                showRetryDialog(R.string.err_fatal_title, R.string.err_fatal)
                            }
                            SpreadSheetsException.ERROR_TYPE.SHEETS_URL_ERROR -> {
                                showRetryDialog(R.string.err_incorrect_url_title, R.string.err_incorrect_url)
                            }
                            SpreadSheetsException.ERROR_TYPE.SHEETS_ILLEGAL_TEMPLATE_ERROR -> {
                                showRetryDialog(getString(R.string.err_import_title_illegal_template),
                                        getString(R.string.err_import_illegal_template, t.target))
                            }
                        }
                    } else if (t is UserRecoverableAuthIOException) {
                        Timber.d("UserRecoverableAuthIOException startActivity")
                        startActivityForResult(t.intent, REQUESTS.REQUEST_AUTHORIZATION.ordinal)
                    }
                }, {
                }).let { disposable.add(it) }
    }

    fun showRetryDialog(titleId: Int, messageId: Int) {
        showRetryDialog(resources.getString(titleId), resources.getString(messageId))
    }

    fun showRetryDialog(title: String, message: String) {
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
                        , { dialog, _ -> dialog.dismiss() })
        dialog = builder.show()
    }


}