/*
 * Copyright (C) 2015 Naman Dwivedi
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 */
package com.naman14.timber.utils

import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.Html
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.afollestad.materialdialogs.MaterialDialog
import com.naman14.timber.R

object Helpers {
    fun showAbout(activity: AppCompatActivity) {
        val fm = activity.supportFragmentManager
        val ft = fm.beginTransaction()
        val prev = fm.findFragmentByTag("dialog_about")
        if (prev != null) {
            ft.remove(prev)
        }
        ft.addToBackStack(null)
        AboutDialog().show(ft, "dialog_about")
    }

    @JvmStatic
    fun getATEKey(context: Context?): String {
        return if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("dark_theme", false)) "dark_theme" else "light_theme"
    }

    class AboutDialog : DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            var appName = "Timber"
            try {
                val pInfo = activity!!.packageManager.getPackageInfo(activity!!.packageName, 0)
                val version = pInfo.versionName
                val versionCode = pInfo.versionCode
                appName = "Timber $version"
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }
            return MaterialDialog.Builder(activity!!)
                    .title(appName)
                    .content(Html.fromHtml(getString(R.string.about_dialog_body)))
                    .positiveText("Dismiss")
                    .build()
        }
    }
}