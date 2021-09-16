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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.naman14.timber.R
import com.naman14.timber.adapters.ArtistSongAdapter
import com.naman14.timber.dataloaders.ArtistSongLoader.getSongsForArtist
import com.naman14.timber.models.Song
import com.naman14.timber.utils.Constants
import com.naman14.timber.widgets.DividerItemDecoration
import java.util.*

class ArtistMusicFragment : Fragment() {
    private var artistID: Long = -1
    private var mSongAdapter: ArtistSongAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            artistID = arguments!!.getLong(Constants.ARTIST_ID)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(
                R.layout.fragment_artist_music, container, false)
        songsRecyclerview = rootView.findViewById<View>(R.id.recycler_view_songs) as RecyclerView
        setUpSongs()
        return rootView
    }

    private fun setUpSongs() {
        songsRecyclerview!!.layoutManager = LinearLayoutManager(activity)
        var songList: MutableList<Song>
        songList = getSongsForArtist(activity!!, artistID)

        // adding one dummy song to top of arraylist
        //there will be albums header at this position in recyclerview
        songList.add(0, Song(-1, -1, -1, "dummy", "dummy", "dummy", -1, -1))
        mSongAdapter = ArtistSongAdapter(activity!!, songList, artistID)
        songsRecyclerview!!.addItemDecoration(DividerItemDecoration(activity, DividerItemDecoration.VERTICAL_LIST))
        songsRecyclerview!!.adapter = mSongAdapter
    }

    companion object {
        var songsRecyclerview: RecyclerView? = null
        fun newInstance(id: Long): ArtistMusicFragment {
            val fragment = ArtistMusicFragment()
            val args = Bundle()
            args.putLong(Constants.ARTIST_ID, id)
            fragment.arguments = args
            return fragment
        }
    }
}