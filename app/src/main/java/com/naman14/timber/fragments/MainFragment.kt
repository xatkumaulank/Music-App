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

import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.afollestad.appthemeengine.ATE
import com.afollestad.appthemeengine.Config
import com.google.android.material.tabs.TabLayout
import com.naman14.timber.R
import com.naman14.timber.utils.ATEUtils
import com.naman14.timber.utils.Helpers
import com.naman14.timber.utils.PreferencesUtility
import java.util.*

class MainFragment : Fragment() {
    private var mPreferences: PreferencesUtility? = null
    private var viewPager: ViewPager? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mPreferences = PreferencesUtility.getInstance(activity)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(
                R.layout.fragment_main, container, false)
        val toolbar = rootView.findViewById<View>(R.id.toolbar) as Toolbar
        (activity as AppCompatActivity?)!!.setSupportActionBar(toolbar)
        val ab = (activity as AppCompatActivity?)!!.supportActionBar
        ab!!.setHomeAsUpIndicator(R.drawable.ic_menu)
        ab.setDisplayHomeAsUpEnabled(true)
        viewPager = rootView.findViewById<View>(R.id.viewpager) as ViewPager
        if (viewPager != null) {
            setupViewPager(viewPager!!)
            viewPager!!.offscreenPageLimit = 2
        }
        val tabLayout = rootView.findViewById<View>(R.id.tabs) as TabLayout
        tabLayout.setupWithViewPager(viewPager)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (PreferenceManager.getDefaultSharedPreferences(activity).getBoolean("dark_theme", false)) {
            ATE.apply(this, "dark_theme")
        } else {
            ATE.apply(this, "light_theme")
        }
        viewPager!!.currentItem = mPreferences!!.startPageIndex
    }

    private fun setupViewPager(viewPager: ViewPager) {
        val adapter = Adapter(childFragmentManager)
        adapter.addFragment(SongsFragment(), this.getString(R.string.songs))
        adapter.addFragment(AlbumFragment(), this.getString(R.string.albums))
        adapter.addFragment(ArtistFragment(), this.getString(R.string.artists))
        viewPager.adapter = adapter
    }

    override fun onPause() {
        super.onPause()
        if (mPreferences!!.lastOpenedIsStartPagePreference()) {
            mPreferences!!.startPageIndex = viewPager!!.currentItem
        }
    }

    override fun onResume() {
        super.onResume()
        val ateKey = Helpers.getATEKey(activity)
        ATEUtils.setStatusBarColor(activity, ateKey, Config.primaryColor(activity!!, ateKey))
    }

    override fun onStart() {
        super.onStart()
    }

    internal class Adapter(fm: FragmentManager?) : FragmentPagerAdapter(fm!!) {
        private val mFragments: MutableList<Fragment> = ArrayList()
        private val mFragmentTitles: MutableList<String> = ArrayList()
        fun addFragment(fragment: Fragment, title: String) {
            mFragments.add(fragment)
            mFragmentTitles.add(title)
        }

        override fun getItem(position: Int): Fragment {
            return mFragments[position]
        }

        override fun getCount(): Int {
            return mFragments.size
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return mFragmentTitles[position]
        }
    }
}