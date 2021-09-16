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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import com.naman14.timber.R
import com.naman14.timber.dataloaders.ArtistLoader.getArtist
import com.naman14.timber.lastfmapi.LastFmClient
import com.naman14.timber.lastfmapi.callbacks.ArtistInfoListener
import com.naman14.timber.lastfmapi.models.ArtistQuery
import com.naman14.timber.lastfmapi.models.LastfmArtist
import com.naman14.timber.subfragments.ArtistTagFragment
import com.naman14.timber.utils.Constants
import com.naman14.timber.widgets.MultiViewPager

class ArtistBioFragment : Fragment() {
    var artistID: Long = -1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            artistID = arguments!!.getLong(Constants.ARTIST_ID)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(
                R.layout.fragment_artist_bio, container, false)
        val artist = getArtist(activity!!, artistID)
        LastFmClient.getInstance(activity).getArtistInfo(ArtistQuery(artist.name), object : ArtistInfoListener {
            override fun artistInfoSucess(artist: LastfmArtist) {}
            override fun artistInfoFailed() {}
        })
        val pager = rootView.findViewById<View>(R.id.tagspager) as MultiViewPager
        val adapter: FragmentStatePagerAdapter = object : FragmentStatePagerAdapter(activity!!.supportFragmentManager) {
            override fun getCount(): Int {
                return 20
            }

            override fun getItem(position: Int): Fragment {
                return ArtistTagFragment.newInstance(position)
            }
        }
        pager.adapter = adapter
        return rootView
    }

    companion object {
        fun newInstance(id: Long): ArtistBioFragment {
            val fragment = ArtistBioFragment()
            val args = Bundle()
            args.putLong(Constants.ARTIST_ID, id)
            fragment.arguments = args
            return fragment
        }
    }
}