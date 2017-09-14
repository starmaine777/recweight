package com.starmaine777.recweight.views.settings

import android.Manifest
import android.content.Intent
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
import com.starmaine777.recweight.data.ExportRepository
import com.starmaine777.recweight.error.SpreadSheetsException
import com.starmaine777.recweight.event.UpdateToolbarEvent
import com.starmaine777.recweight.event.RxBus
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

    var disposable = CompositeDisposable()
    var dialog: AlertDialog? = null
    val exportRepo: ExportRepository by lazy { ExportRepository(context) }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_export, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnExportShare.setOnClickListener {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, exportRepo.exportedUrlStr)

            startActivity(intent)
        }

        view?.setOnKeyListener { v, _, keyEvent ->
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
        RxBus.publish(UpdateToolbarEvent(false, context.getString(R.string.toolbar_title_import)))
    }

    override fun onResume() {
        super.onResume()
        exportData()
    }

    override fun onStop() {
        super.onStop()
        disposable.dispose()
    }

    @Throws()
    fun exportData() {
        Timber.d("exportData repo=$exportRepo")
        exportRepo.exportDatas(context)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
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
                            else -> {
                                showRetryDialog(R.string.err_fatal_title, R.string.err_fatal)
                            }
                        }
                    } else if (t is UserRecoverableAuthIOException) {
                        Timber.d("UserRecoverableAuthIOException startActivity")
                        startActivityForResult(t.intent, REQUESTS.REQUEST_AUTHORIZATION.ordinal)
                    }
                }, {
                    Timber.d("complete exportData!! repo=$exportRepo url=${exportRepo.exportedUrlStr}")
                    editExportUrl.setText(exportRepo.exportedUrlStr)
                    areaProgress.visibility = View.GONE
                    areaExportUrl.visibility = View.VISIBLE
                    RxBus.publish(UpdateToolbarEvent(true))

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