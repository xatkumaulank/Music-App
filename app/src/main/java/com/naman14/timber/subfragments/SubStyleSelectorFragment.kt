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
package com.naman14.timber.subfragments

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.afollestad.materialdialogs.MaterialDialog
import com.naman14.timber.R
import com.naman14.timber.activities.DonateActivity
import com.naman14.timber.utils.Constants
import com.naman14.timber.utils.NavigationUtils
import com.naman14.timber.utils.PreferencesUtility

class SubStyleSelectorFragment : Fragment() {
    private var editor: SharedPreferences.Editor? = null
    private var preferences: SharedPreferences? = null
    private var currentStyle: LinearLayout? = null
    private var foreground: View? = null
    private var styleImage: ImageView? = null
    private var imgLock: ImageView? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_style_selector_pager, container, false)
        val styleName = rootView.findViewById<View>(R.id.style_name) as TextView
        styleName.text = (arguments!!.getInt(ARG_PAGE_NUMBER) + 1).toString()
        preferences = activity!!.getSharedPreferences(Constants.FRAGMENT_ID, Context.MODE_PRIVATE)
        styleImage = rootView.findViewById<View>(R.id.style_image) as ImageView
        imgLock = rootView.findViewById<View>(R.id.img_lock) as ImageView
        styleImage!!.setOnClickListener {
            if (arguments!!.getInt(ARG_PAGE_NUMBER) >= 4) {
                if (isUnlocked) {
                    setPreferences()
                } else {
                    showPurchaseDialog()
                }
            } else setPreferences()
        }
        when (arguments!!.getInt(ARG_PAGE_NUMBER)) {
            0 -> styleImage!!.setImageResource(R.drawable.timber_1_nowplaying_x)
            1 -> styleImage!!.setImageResource(R.drawable.timber_2_nowplaying_x)
            2 -> styleImage!!.setImageResource(R.drawable.timber_3_nowplaying_x)
            3 -> styleImage!!.setImageResource(R.drawable.timber_4_nowplaying_x)
            4 -> styleImage!!.setImageResource(R.drawable.timber_5_nowplaying_x)
            5 -> styleImage!!.setImageResource(R.drawable.timber_6_nowplaying_x)
        }
        currentStyle = rootView.findViewById<View>(R.id.currentStyle) as LinearLayout
        foreground = rootView.findViewById(R.id.foreground)
        setCurrentStyle()
        return rootView
    }

    private val isUnlocked: Boolean
        private get() = activity != null && PreferencesUtility.getInstance(activity).fullUnlocked()

    override fun onResume() {
        super.onResume()
        updateLockedStatus()
    }

    private fun updateLockedStatus() {
        if (arguments!!.getInt(ARG_PAGE_NUMBER) >= 4 && !isUnlocked) {
            imgLock!!.visibility = View.VISIBLE
            foreground!!.visibility = View.VISIBLE
        } else {
            imgLock!!.visibility = View.GONE
            foreground!!.visibility = View.GONE
        }
    }

    private fun showPurchaseDialog() {
        val dialog = MaterialDialog.Builder(activity!!)
                .title("Purchase")
                .content("This now playing style is available after a one time purchase of any amount. Support development and unlock this style?")
                .positiveText("Support development")
                .neutralText("Restore purchases")
                .onPositive { dialog, which ->
                    startActivity(Intent(activity, DonateActivity::class.java))
                    dialog.dismiss()
                }.onNeutral { dialog, which ->
                    val intent = Intent(activity, DonateActivity::class.java)
                    intent.putExtra("title", "Restoring purchases..")
                    intent.action = "restore"
                    startActivity(intent)
                    dialog.dismiss()
                }
                .show()
    }

    fun setCurrentStyle() {
        val fragmentID = preferences!!.getString(Constants.NOWPLAYING_FRAGMENT_ID, Constants.TIMBER3)
        if (arguments!!.getInt(ARG_PAGE_NUMBER) == NavigationUtils.getIntForCurrentNowplaying(fragmentID)) {
            currentStyle!!.visibility = View.VISIBLE
            foreground!!.visibility = View.VISIBLE
        } else {
            currentStyle!!.visibility = View.GONE
            foreground!!.visibility = View.GONE
        }
    }

    private fun setPreferences() {
        if (arguments!!.getString(WHAT) == Constants.SETTINGS_STYLE_SELECTOR_NOWPLAYING) {
            editor = activity!!.getSharedPreferences(Constants.FRAGMENT_ID, Context.MODE_PRIVATE).edit()
            editor?.putString(Constants.NOWPLAYING_FRAGMENT_ID, styleForPageNumber)
            editor?.apply()
            if (activity != null) PreferencesUtility.getInstance(activity).setNowPlayingThemeChanged(true)
            setCurrentStyle()
            (parentFragment as StyleSelectorFragment?)!!.updateCurrentStyle()
        }
    }

    private val styleForPageNumber: String
        private get() = when (arguments!!.getInt(ARG_PAGE_NUMBER)) {
            0 -> Constants.TIMBER1
            1 -> Constants.TIMBER2
            2 -> Constants.TIMBER3
            3 -> Constants.TIMBER4
            4 -> Constants.TIMBER5
            5 -> Constants.TIMBER6
            else -> Constants.TIMBER3
        }

    companion object {
        private const val ARG_PAGE_NUMBER = "pageNumber"
        private const val WHAT = "what"
        fun newInstance(pageNumber: Int, what: String?): SubStyleSelectorFragment {
            val fragment = SubStyleSelectorFragment()
            val bundle = Bundle()
            bundle.putInt(ARG_PAGE_NUMBER, pageNumber)
            bundle.putString(WHAT, what)
            fragment.arguments = bundle
            return fragment
        }
    }
}