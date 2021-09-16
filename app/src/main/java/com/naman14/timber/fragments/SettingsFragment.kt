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
package com.naman14.timber.fragments

import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.graphics.Color
import android.os.Bundle
import android.preference.ListPreference
import android.preference.Preference
import android.preference.Preference.OnPreferenceChangeListener
import android.preference.Preference.OnPreferenceClickListener
import android.preference.PreferenceFragment
import android.preference.SwitchPreference
import android.view.View
import com.afollestad.appthemeengine.ATE
import com.afollestad.appthemeengine.Config
import com.afollestad.appthemeengine.prefs.ATECheckBoxPreference
import com.afollestad.appthemeengine.prefs.ATEColorPreference
import com.afollestad.materialdialogs.color.ColorChooserDialog
import com.naman14.timber.R
import com.naman14.timber.activities.DonateActivity
import com.naman14.timber.activities.SettingsActivity
import com.naman14.timber.dialogs.LastFmLoginDialog
import com.naman14.timber.lastfmapi.LastFmClient
import com.naman14.timber.utils.Constants
import com.naman14.timber.utils.NavigationUtils
import com.naman14.timber.utils.PreferencesUtility

class SettingsFragment : PreferenceFragment(), OnSharedPreferenceChangeListener {
    private var lastFMlogedin = false
    private var nowPlayingSelector: Preference? = null
    private var lastFMlogin: Preference? = null
    private var lockscreen: Preference? = null
    private var xposed: Preference? = null
    private val toggleAnimations: SwitchPreference? = null
    private val themePreference: ListPreference? = null
    private var startPagePreference: ListPreference? = null
    private var mPreferences: PreferencesUtility? = null
    private var mAteKey: String? = null
    override fun onCreate(savedInstanceState: Bundle) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.preferences)
        mPreferences = PreferencesUtility.getInstance(activity)
        lockscreen = findPreference(LOCKSCREEN)
        nowPlayingSelector = findPreference(NOW_PLAYING_SELECTOR)
        xposed = findPreference(XPOSED)
        lastFMlogin = findPreference(LASTFM_LOGIN)
        updateLastFM()
        //        themePreference = (ListPreference) findPreference(KEY_THEME);
        startPagePreference = findPreference(KEY_START_PAGE) as ListPreference
        nowPlayingSelector!!.intent = NavigationUtils.getNavigateToStyleSelectorIntent(activity, Constants.SETTINGS_STYLE_SELECTOR_NOWPLAYING)
        setPreferenceClickListeners()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences,
                                           key: String) {
    }

    private fun setPreferenceClickListeners() {

//        themePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
//            @Override
//            public boolean onPreferenceChange(Preference preference, Object newValue) {
//                Intent i = getActivity().getBaseContext().getPackageManager().getLaunchIntentForPackage(getActivity().getBaseContext().getPackageName());
//                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                startActivity(i);
//                return true;
//            }
//        });
        startPagePreference!!.onPreferenceChangeListener = OnPreferenceChangeListener { preference, newValue ->
            when (newValue as String) {
                "last_opened" -> mPreferences!!.setLastOpenedAsStartPagePreference(true)
                "songs" -> {
                    mPreferences!!.setLastOpenedAsStartPagePreference(false)
                    mPreferences!!.startPageIndex = 0
                }
                "albums" -> {
                    mPreferences!!.setLastOpenedAsStartPagePreference(false)
                    mPreferences!!.startPageIndex = 1
                }
                "artists" -> {
                    mPreferences!!.setLastOpenedAsStartPagePreference(false)
                    mPreferences!!.startPageIndex = 2
                }
            }
            true
        }
        val restoreIntent = Intent(activity, DonateActivity::class.java)
        restoreIntent.putExtra("title", "Restoring purchases..")
        restoreIntent.action = "restore"
        findPreference("support_development").intent = Intent(activity, DonateActivity::class.java)
        findPreference("restore_purchases").intent = restoreIntent
        lockscreen!!.onPreferenceChangeListener = OnPreferenceChangeListener { preference, newValue ->
            val extras = Bundle()
            extras.putBoolean("lockscreen", newValue as Boolean)
            mPreferences!!.updateService(extras)
            true
        }
        xposed!!.onPreferenceChangeListener = OnPreferenceChangeListener { preference, newValue ->
            val extras = Bundle()
            extras.putBoolean("xtrack", newValue as Boolean)
            mPreferences!!.updateService(extras)
            true
        }
        lastFMlogin!!.onPreferenceClickListener = OnPreferenceClickListener {
            if (lastFMlogedin) {
                LastFmClient.getInstance(activity).logout()
                val extras = Bundle()
                extras.putString("lf_token", "logout")
                extras.putString("lf_user", null)
                mPreferences!!.updateService(extras)
                updateLastFM()
            } else {
                val lastFmLoginDialog = LastFmLoginDialog()
                lastFmLoginDialog.show(childFragmentManager, LastFmLoginDialog.FRAGMENT_NAME)
            }
            true
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle) {
        super.onViewCreated(view, savedInstanceState)
        invalidateSettings()
        ATE.apply(view, mAteKey)
    }

    fun invalidateSettings() {
        mAteKey = (activity as SettingsActivity).ateKey
        val primaryColorPref = findPreference("primary_color") as ATEColorPreference
        primaryColorPref.setColor(Config.primaryColor(activity, mAteKey), Color.BLACK)
        primaryColorPref.onPreferenceClickListener = OnPreferenceClickListener {
            ColorChooserDialog.Builder(activity as SettingsActivity, R.string.primary_color)
                    .preselect(Config.primaryColor(activity, mAteKey))
                    .show()
            true
        }
        val accentColorPref = findPreference("accent_color") as ATEColorPreference
        accentColorPref.setColor(Config.accentColor(activity, mAteKey), Color.BLACK)
        accentColorPref.onPreferenceClickListener = OnPreferenceClickListener {
            ColorChooserDialog.Builder(activity as SettingsActivity, R.string.accent_color)
                    .preselect(Config.accentColor(activity, mAteKey))
                    .show()
            true
        }
        findPreference("dark_theme").onPreferenceChangeListener = OnPreferenceChangeListener { preference, newValue -> // Marks both theme configs as changed so MainActivity restarts itself on return
            Config.markChanged(activity, "light_theme")
            Config.markChanged(activity, "dark_theme")
            // The dark_theme preference value gets saved by Android in the default PreferenceManager.
            // It's used in getATEKey() of both the Activities.
            activity.recreate()
            true
        }
        val statusBarPref = findPreference("colored_status_bar") as ATECheckBoxPreference
        val navBarPref = findPreference("colored_nav_bar") as ATECheckBoxPreference
        statusBarPref.isChecked = Config.coloredStatusBar(activity, mAteKey)
        statusBarPref.onPreferenceChangeListener = OnPreferenceChangeListener { preference, newValue ->
            ATE.config(activity, mAteKey)
                    .coloredStatusBar((newValue as Boolean))
                    .apply(activity)
            true
        }
        navBarPref.isChecked = Config.coloredNavigationBar(activity, mAteKey)
        navBarPref.onPreferenceChangeListener = OnPreferenceChangeListener { preference, newValue ->
            ATE.config(activity, mAteKey)
                    .coloredNavigationBar((newValue as Boolean))
                    .apply(activity)
            true
        }
    }

    fun updateLastFM() {
        val username = LastFmClient.getInstance(activity).username
        if (username != null) {
            lastFMlogedin = true
            lastFMlogin!!.title = "Logout"
            lastFMlogin!!.summary = String.format(getString(R.string.lastfm_loged_in), username)
        } else {
            lastFMlogedin = false
            lastFMlogin!!.title = "Login"
            lastFMlogin!!.summary = getString(R.string.lastfm_pref)
        }
    }

    companion object {
        private const val NOW_PLAYING_SELECTOR = "now_playing_selector"
        private const val LASTFM_LOGIN = "lastfm_login"
        private const val LOCKSCREEN = "show_albumart_lockscreen"
        private const val XPOSED = "toggle_xposed_trackselector"
        private const val KEY_ABOUT = "preference_about"
        private const val KEY_SOURCE = "preference_source"
        private const val KEY_THEME = "theme_preference"
        private const val TOGGLE_ANIMATIONS = "toggle_animations"
        private const val TOGGLE_SYSTEM_ANIMATIONS = "toggle_system_animations"
        private const val KEY_START_PAGE = "start_page_preference"
    }
}