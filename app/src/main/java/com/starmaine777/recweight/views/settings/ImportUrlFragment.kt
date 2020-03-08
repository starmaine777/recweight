package com.starmaine777.recweight.views.settings

import android.Manifest
import android.accounts.AccountManager
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.TextUtils
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.material.snackbar.Snackbar
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.starmaine777.recweight.R
import com.starmaine777.recweight.data.repo.ImportRepository
import com.starmaine777.recweight.error.SpreadSheetsException
import com.starmaine777.recweight.error.SpreadSheetsException.ERROR_TYPE
import com.starmaine777.recweight.event.RxBus
import com.starmaine777.recweight.event.UpdateToolbarEvent
import com.starmaine777.recweight.utils.PREFERENCE_KEY
import com.starmaine777.recweight.utils.REQUESTS
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_import_url.areaProgress
import kotlinx.android.synthetic.main.fragment_import_url.areaUrlInput
import kotlinx.android.synthetic.main.fragment_import_url.buttonImportStart
import kotlinx.android.synthetic.main.fragment_import_url.editImportUrl
import kotlinx.android.synthetic.main.fragment_import_url.progressImport
import timber.log.Timber

/**
 * ImportのURL入力Fragment
 * Created by 0025331458 on 2017/08/16.
 */
class ImportUrlFragment : Fragment() {

    companion object {
        val TAG = "ImportUrlFragment"
    }

    val importRepo: ImportRepository by lazy { ImportRepository(requireContext()) }
    var disposable = CompositeDisposable()
    var dialog: AlertDialog? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_import_url, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        editImportUrl.setOnEditorActionListener { _, _, _ ->
            startImport()
            return@setOnEditorActionListener true
        }

        buttonImportStart.setOnClickListener {
            startImport()
        }

        view.setOnKeyListener { v, _, keyEvent ->
            // import中はbackさせない
            Timber.d("setOnKeyListener keyEvent = $keyEvent")
            if (v != editImportUrl
                    && keyEvent.keyCode == KeyEvent.KEYCODE_BACK
                    && disposable.size() > 0
            ) {
                if (keyEvent.action == KeyEvent.ACTION_UP) {
                    Snackbar.make(this@ImportUrlFragment.view!!, R.string.snack_settings_import_backpress, Snackbar.LENGTH_SHORT).show()
                }
                return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }
        view.isFocusableInTouchMode = true
    }

    private fun changeInputView(startProgress: Boolean) {
        if (startProgress) {
            areaUrlInput.visibility = View.GONE
            areaProgress.visibility = View.VISIBLE
            progressImport.isIndeterminate = true
        } else {
            areaUrlInput.visibility = View.VISIBLE
            areaProgress.visibility = View.GONE
            progressImport.isIndeterminate = false
        }
    }


    private fun startToGetSpreadSheetsData() {
        Timber.d("startToGetSpreadSheetsData!!!")
        var isImporting = false
        RxBus.publish(UpdateToolbarEvent(false))
        importRepo.getResultFromApi(editImportUrl.text.toString())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ index ->
                    if (isImporting) {
                        progressImport.progress++
                    } else {
                        // 各行import開始時.ProgressBarの設定変更
                        progressImport.isIndeterminate = false
                        progressImport.max = index
                        progressImport.progress = 0
                        isImporting = true
                    }
                }, { t: Throwable ->
                    Timber.d("Error happened! $t")
                    if (t is SpreadSheetsException) {
                        Timber.d("Error happened! ${t.type}, code = ${t.errorCode}")
                        when (t.type) {
                            ERROR_TYPE.ACCOUNT_PERMISSION_DENIED -> {
                                requestPermissions(arrayOf(Manifest.permission.GET_ACCOUNTS), REQUESTS.SHOW_ACCOUNT_PERMISSION.ordinal)
                            }
                            ERROR_TYPE.ACCOUNT_NOT_SELECTED -> {
                                startActivityForResult(importRepo.credential.newChooseAccountIntent(), REQUESTS.SHOW_ACCOUNT_PICKER.ordinal)
                            }
                            ERROR_TYPE.PLAY_SERVICE_AVAILABILITY_ERROR -> {
                                val apiAvailability = GoogleApiAvailability.getInstance()
                                apiAvailability.getErrorDialog(activity, t.errorCode, REQUESTS.SHOW_GOOGLE_PLAY_SERVICE.ordinal).show()
                            }
                            ERROR_TYPE.DEVICE_OFFLINE -> {
                                showRetryDialog(R.string.err_offline_title, R.string.err_offline)
                            }
                            ERROR_TYPE.FATAL_ERROR -> {
                                showRetryDialog(R.string.err_fatal_title, R.string.err_fatal)
                            }
                            ERROR_TYPE.SHEETS_URL_ERROR -> {
                                showRetryDialog(R.string.err_incorrect_url_title, R.string.err_incorrect_url)
                            }
                            ERROR_TYPE.SHEETS_ILLEGAL_TEMPLATE_ERROR -> {
                                showRetryDialog(getString(R.string.err_import_title_illegal_template),
                                        getString(R.string.err_import_illegal_template, t.target))
                            }
                            else -> {
                                // ないはず
                                showRetryDialog(R.string.err_fatal_title, R.string.err_fatal)
                            }
                        }
                    } else if (t is UserRecoverableAuthIOException) {
                        Timber.d("UserRecoverableAuthIOException startActivity")
                        startActivityForResult(t.intent, REQUESTS.REQUEST_AUTHORIZATION.ordinal)
                        RxBus.publish(UpdateToolbarEvent(true))
                    } else {
                        Timber.e(t, "Import exception!")
                    }
                }, {
                    Timber.d("Completed!!!")
                    val messageSb = StringBuilder()
                    messageSb.append(getString(R.string.d_import_complete))
                    if (!importRepo.errorDates.isEmpty()) {
                        messageSb.append("\n\n\n${getString(R.string.d_import_complete_skip)}\n\n")
                        for (item in importRepo.errorDates) {
                            messageSb.append("$item\n")
                        }
                    }

                    dialog = AlertDialog.Builder(requireContext())
                            .setTitle(R.string.d_import_complete_title)
                            .setMessage(messageSb.toString())
                            .setOnDismissListener { fragmentManager?.popBackStackImmediate() }
                            .setPositiveButton(android.R.string.ok
                                    , { dialog, _ -> dialog.dismiss() })
                            .show()

                }).let { disposable.add(it) }
    }

    override fun onStart() {
        super.onStart()
        RxBus.publish(UpdateToolbarEvent(true, getString(R.string.toolbar_title_import)))
    }

    override fun onResume() {
        super.onResume()

        val inputMethodManager = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(editImportUrl, InputMethodManager.SHOW_IMPLICIT)
        editImportUrl.requestFocus()
    }

    override fun onPause() {
        super.onPause()
        hideKeyboard()
    }

    override fun onStop() {
        super.onStop()
        disposable.dispose()
        dialog?.dismiss()
        dialog = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Timber.d("onActivityResult requestCode = $requestCode")
        when (requestCode) {
            REQUESTS.SHOW_ACCOUNT_PICKER.ordinal -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        val accountName = data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
                        if (!TextUtils.isEmpty(accountName)) {
                            val editor = requireContext().getSharedPreferences(requireContext().packageName, Context.MODE_PRIVATE).edit()
                            editor.putString(PREFERENCE_KEY.ACCOUNT_NAME.name, accountName)
                            editor.apply()
                            importRepo.credential.selectedAccountName = accountName!!
                            startToGetSpreadSheetsData()
                        }
                    }
                    Activity.RESULT_CANCELED -> {
                        changeInputView(false)
                    }
                }
            }

            REQUESTS.SHOW_GOOGLE_PLAY_SERVICE.ordinal -> {
                if (resultCode == Activity.RESULT_OK) {
                    startToGetSpreadSheetsData()
                }
            }

            REQUESTS.REQUEST_AUTHORIZATION.ordinal -> {
                when (resultCode) {
                    Activity.RESULT_OK -> startToGetSpreadSheetsData()
                    Activity.RESULT_CANCELED -> changeInputView(false)
                }
            }

            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        Timber.d("onRequestPermissionResult requestCode = $requestCode")
        when (requestCode) {
            REQUESTS.SHOW_ACCOUNT_PERMISSION.ordinal -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startToGetSpreadSheetsData()
                } else {
                    showRetryDialog(getString(R.string.err_no_permission_title), getString(R.string.err_no_permission, getString(R.string.err_permission_account)))
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun hideKeyboard() {
        if (editImportUrl.hasFocus()) editImportUrl.clearFocus()

        val inputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (requireActivity().currentFocus != null) {
            inputMethodManager.hideSoftInputFromWindow(requireActivity().currentFocus!!.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        }

    }

    private fun startImport() {
        if (TextUtils.isEmpty(editImportUrl.text)) {
            dialog = AlertDialog.Builder(requireContext())
                    .setTitle(R.string.err_title_input)
                    .setMessage(R.string.err_url_empty)
                    .setPositiveButton(android.R.string.ok, null)
                    .show()
        } else {
            hideKeyboard()
            changeInputView(true)
            startToGetSpreadSheetsData()
        }
    }

    private fun showRetryDialog(titleId: Int, messageId: Int) {
        showRetryDialog(resources.getString(titleId), resources.getString(messageId))
    }

    private fun showRetryDialog(title: String, message: String) {
        if (TextUtils.isEmpty(message)) {
            return
        }

        val builder = AlertDialog.Builder(requireContext())
        if (!TextUtils.isEmpty(title)) {
            builder.setTitle(title)
        }
        builder.setMessage(message)
                .setOnDismissListener { changeInputView(false) }
                .setPositiveButton(android.R.string.ok
                        , { dialog, _ -> dialog.dismiss() })
        dialog = builder.show()
    }
}