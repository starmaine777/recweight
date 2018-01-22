package com.starmaine777.recweight.views.settings

import android.Manifest
import android.accounts.AccountManager
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.text.TextUtils
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.common.GoogleApiAvailability
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.starmaine777.recweight.R
import com.starmaine777.recweight.data.repo.ExportRepository
import com.starmaine777.recweight.error.SpreadSheetsException
import com.starmaine777.recweight.event.RxBus
import com.starmaine777.recweight.event.UpdateToolbarEvent
import com.starmaine777.recweight.utils.PREFERENCE_KEY
import com.starmaine777.recweight.utils.REQUESTS
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_export.*
import timber.log.Timber

/**
 * Export用画面
 * Created by 0025331458 on 2017/09/12.
 */
class ExportFragment : Fragment() {

    companion object {
        val TAG = "ExportFragment"
    }

    private var disposable = CompositeDisposable()
    private var dialog: AlertDialog? = null
    private val exportRepo: ExportRepository by lazy { ExportRepository(context) }
    private var isExporting = false

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater?.inflate(R.layout.fragment_export, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnExportShare.setOnClickListener {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, exportRepo.exportedUrlStr)

            startActivity(intent)
        }

        view?.setOnKeyListener { _, _, keyEvent ->
            // import中はbackさせない
            Timber.d("setOnKeyListener keyEvent = $keyEvent")
            if (keyEvent.keyCode == KeyEvent.KEYCODE_BACK
                    && disposable.size() > 0
                    ) {
                if (keyEvent.action == KeyEvent.ACTION_UP) {
                    Snackbar.make(this@ExportFragment.view!!, R.string.snack_settings_import_backpress, Snackbar.LENGTH_SHORT).show()
                }
                return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }
        view?.isFocusableInTouchMode = true
    }

    override fun onStart() {
        super.onStart()
        RxBus.publish(UpdateToolbarEvent(false, context.getString(R.string.toolbar_title_export)))
        exportData()
    }

    override fun onStop() {
        super.onStop()
        disposable.clear()
    }

    @Throws()
    private fun exportData() {
        Timber.d(Throwable(), "exportData repo=$exportRepo", null)
        isExporting = false
        progressExport.isIndeterminate = true
        exportRepo.exportData(context)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ index ->
                    if (isExporting) {
                        progressExport.progress++
                    } else {
                        progressExport.isIndeterminate = false
                        progressExport.max = index
                        progressExport.progress = 0
                        isExporting = true
                    }
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
                                showRetryDialog(R.string.err_offline_title, R.string.err_offline)
                            }
                            SpreadSheetsException.ERROR_TYPE.FATAL_ERROR -> {
                                showRetryDialog(R.string.err_fatal_title, R.string.err_fatal)
                            }
                            SpreadSheetsException.ERROR_TYPE.NO_DATA -> {
                                showRetryDialog(R.string.err_no_data_title, R.string.err_no_data)
                            }
                            else -> {
                                showRetryDialog(R.string.err_fatal_title, R.string.err_fatal)
                            }
                        }
                    } else if (t is UserRecoverableAuthIOException) {
                        Timber.d("UserRecoverableAuthIOException startActivity")
                        startActivityForResult(t.intent, REQUESTS.REQUEST_AUTHORIZATION.ordinal)
                    } else {
                        Timber.e(t, "Export exception!")
                    }
                }, {
                    Timber.d("complete exportData!! repo=$exportRepo url=${exportRepo.exportedUrlStr}")
                    editExportUrl.setText(exportRepo.exportedUrlStr)
                    areaProgress.visibility = View.GONE
                    areaExportUrl.visibility = View.VISIBLE
                    RxBus.publish(UpdateToolbarEvent(true))

                }).let { disposable.add(it) }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Timber.d("onActivityResult requestCode=$requestCode resultCode=$resultCode date=$data")
        when (requestCode) {
            REQUESTS.SHOW_ACCOUNT_PICKER.ordinal -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        val accountName = data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
                        if (!TextUtils.isEmpty(accountName)) {
                            val editor = activity.getSharedPreferences(context.packageName, Context.MODE_PRIVATE).edit()
                            editor.putString(PREFERENCE_KEY.ACCOUNT_NAME.name, accountName)
                            editor.apply()
                            exportRepo.credential.selectedAccountName = accountName!!
                            exportData()
                        }
                        exportData()
                    }
                    Activity.RESULT_CANCELED -> {
                        fragmentManager.popBackStack()
                    }
                }
            }
            REQUESTS.SHOW_GOOGLE_PLAY_SERVICE.ordinal -> {
                when (resultCode) {
                    Activity.RESULT_OK -> exportData()
                    Activity.RESULT_CANCELED -> fragmentManager.popBackStack()
                }
            }
            REQUESTS.REQUEST_AUTHORIZATION.ordinal -> {
                when (resultCode) {
                    Activity.RESULT_OK -> exportData()
                    Activity.RESULT_CANCELED -> fragmentManager.popBackStack()
                }
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        Timber.d("onRequestPermissionResult requestCode = $requestCode grantResults=$grantResults")
        when (requestCode) {
            REQUESTS.SHOW_ACCOUNT_PERMISSION.ordinal -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    exportData()
                } else {
                    showRetryDialog(getString(R.string.err_no_permission_title), getString(R.string.err_no_permission, getString(R.string.err_permission_account)))
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }


    private fun showRetryDialog(titleId: Int, messageId: Int) {
        showRetryDialog(resources.getString(titleId), resources.getString(messageId))
    }

    private fun showRetryDialog(title: String, message: String) {
        if (TextUtils.isEmpty(message)) {
            return
        }

        val builder = AlertDialog.Builder(context)
        if (!TextUtils.isEmpty(title)) {
            builder.setTitle(title)
        }
        builder.setMessage(message)
                .setOnDismissListener {
                    fragmentManager?.let {
                        if (fragmentManager.backStackEntryCount > 0) fragmentManager.popBackStack()
                    }
                }
                .setPositiveButton(android.R.string.ok
                        , { dialog, _ -> dialog.dismiss() })
        dialog = builder.show()
    }


}