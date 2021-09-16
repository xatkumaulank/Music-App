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
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import com.naman14.timber.R
import com.naman14.timber.utils.Constants
import com.naman14.timber.utils.NavigationUtils
import com.naman14.timber.widgets.MultiViewPager

class StyleSelectorFragment : Fragment() {
    var ACTION = "action"
    private var adapter: FragmentStatePagerAdapter? = null
    private var pager: MultiViewPager? = null
    private var selectorFragment: SubStyleSelectorFragment? = null
    private var preferences: SharedPreferences? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            ACTION = arguments!!.getString(Constants.SETTINGS_STYLE_SELECTOR_WHAT)
        }
        preferences = activity!!.getSharedPreferences(Constants.FRAGMENT_ID, Context.MODE_PRIVATE)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_style_selector, container, false)
        if (ACTION == Constants.SETTINGS_STYLE_SELECTOR_NOWPLAYING) {
        }
        pager = rootView.findViewById<View>(R.id.pager) as MultiViewPager
        adapter = object : FragmentStatePagerAdapter(childFragmentManager) {
            override fun getCount(): Int {
                return 6
            }

            override fun getItem(position: Int): Fragment {
                selectorFragment = SubStyleSelectorFragment.newInstance(position, ACTION)
                return selectorFragment!!
            }

            override fun getItemPosition(`object`: Any): Int {
                return POSITION_NONE
            }
        }
        pager!!.adapter = adapter
        scrollToCurrentStyle()
        return rootView
    }

    fun updateCurrentStyle() {
        if (selectorFragment != null) {
            adapter!!.notifyDataSetChanged()
            scrollToCurrentStyle()
        }
    }

    fun scrollToCurrentStyle() {
        val fragmentID = preferences!!.getString(Constants.NOWPLAYING_FRAGMENT_ID, Constants.TIMBER3)
        pager!!.currentItem = NavigationUtils.getIntForCurrentNowplaying(fragmentID)
    }

    companion object {
        fun newInstance(what: String?): StyleSelectorFragment {
            val fragment = StyleSelectorFragment()
            val bundle = Bundle()
            bundle.putString(Constants.SETTINGS_STYLE_SELECTOR_WHAT, what)
            fragment.arguments = bundle
            return fragment
        }
    }
}