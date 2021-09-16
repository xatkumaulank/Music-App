package com.naman14.timber.dialogs

import android.app.Dialog
import android.app.DialogFragment
import android.app.ProgressDialog
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.MaterialDialog.SingleButtonCallback
import com.naman14.timber.R
import com.naman14.timber.fragments.SettingsFragment
import com.naman14.timber.lastfmapi.LastFmClient
import com.naman14.timber.lastfmapi.callbacks.UserListener
import com.naman14.timber.lastfmapi.models.UserLoginQuery

/**
 * Created by christoph on 17.07.16.
 */
class LastFmLoginDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle): Dialog {
        return MaterialDialog.Builder(activity).positiveText("Login").negativeText(getString(R.string.cancel)).title(getString(R.string.lastfm_login)).customView(R.layout.dialog_lastfm_login, false).onPositive(SingleButtonCallback { dialog, which ->
            val username = (dialog.findViewById(R.id.lastfm_username) as EditText).text.toString()
            val password = (dialog.findViewById(R.id.lastfm_password) as EditText).text.toString()
            if (username.length == 0 || password.length == 0) return@SingleButtonCallback
            val progressDialog = ProgressDialog(activity)
            progressDialog.setMessage("Logging in..")
            progressDialog.show()
            LastFmClient.getInstance(activity).getUserLoginInfo(UserLoginQuery(username, password), object : UserListener {
                override fun userSuccess() {
                    progressDialog.dismiss()
                    if (targetFragment is SettingsFragment) {
                        (targetFragment as SettingsFragment).updateLastFM()
                    }
                }

                override fun userInfoFailed() {
                    progressDialog.dismiss()
                    Toast.makeText(targetFragment.activity, getString(R.string.lastfm_login_failture), Toast.LENGTH_SHORT).show()
                }
            })
        }).build()
    }

    companion object {
        const val FRAGMENT_NAME = "LastFMLogin"
    }
}