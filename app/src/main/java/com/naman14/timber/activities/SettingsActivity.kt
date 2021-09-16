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
package com.naman14.timber.activities

import android.os.Bundle
import android.preference.PreferenceFragment
import android.preference.PreferenceManager
import android.view.MenuItem
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.StyleRes
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.afollestad.appthemeengine.ATE
import com.afollestad.appthemeengine.customizers.ATEActivityThemeCustomizer
import com.afollestad.materialdialogs.color.ColorChooserDialog
import com.afollestad.materialdialogs.color.ColorChooserDialog.ColorCallback
import com.naman14.timber.R
import com.naman14.timber.fragments.SettingsFragment
import com.naman14.timber.subfragments.StyleSelectorFragment
import com.naman14.timber.utils.Constants
import com.naman14.timber.utils.PreferencesUtility

class SettingsActivity : BaseThemedActivity(), ColorCallback, ATEActivityThemeCustomizer {
    private var action: String? = null
    public override fun onCreate(savedInstanceState: Bundle?) {
        if (PreferencesUtility.getInstance(this).theme == "dark") setTheme(R.style.AppThemeNormalDark) else if (PreferencesUtility.getInstance(this).theme == "black") setTheme(R.style.AppThemeNormalBlack)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        action = intent.action
        if (action == Constants.SETTINGS_STYLE_SELECTOR) {
            supportActionBar!!.setTitle(R.string.now_playing)
            val what = intent.extras.getString(Constants.SETTINGS_STYLE_SELECTOR_WHAT)
            val fragment: Fragment = StyleSelectorFragment.newInstance(what)
            val fragmentManager = supportFragmentManager
            fragmentManager.beginTransaction()
                    .add(R.id.fragment_container, fragment).commit()
        } else {
            supportActionBar!!.setTitle(R.string.settings)
            val fragment: PreferenceFragment = SettingsFragment()
            val fragmentManager = fragmentManager
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment).commit()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            else -> {
            }
        }
        return super.onOptionsItemSelected(item)
    }

    @StyleRes
    override fun getActivityTheme(): Int {
        return if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("dark_theme", false)) R.style.AppThemeDark else R.style.AppThemeLight
    }

    override fun onColorSelection(dialog: ColorChooserDialog, @ColorInt selectedColor: Int) {
        val config = ATE.config(this, ateKey)
        when (dialog.title) {
            R.string.primary_color -> config.primaryColor(selectedColor)
            R.string.accent_color -> config.accentColor(selectedColor)
        }
        config.commit()
        recreate() // recreation needed to reach the checkboxes in the preferences layout
    }
}