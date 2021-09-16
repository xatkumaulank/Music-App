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

import android.os.AsyncTask
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.naman14.timber.R
import com.naman14.timber.activities.BaseActivity
import com.naman14.timber.adapters.SongsListAdapter
import com.naman14.timber.dataloaders.SongLoader.Companion.getAllSongs
import com.naman14.timber.listeners.MusicStateListener
import com.naman14.timber.models.Song
import com.naman14.timber.utils.PreferencesUtility
import com.naman14.timber.utils.SortOrder
import com.naman14.timber.widgets.BaseRecyclerView
import com.naman14.timber.widgets.DividerItemDecoration
import com.naman14.timber.widgets.FastScroller

class SongsFragment : Fragment(), MusicStateListener {
    private var mAdapter: SongsListAdapter? = null
    private var recyclerView: BaseRecyclerView? = null
    private var mPreferences: PreferencesUtility? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mPreferences = PreferencesUtility.getInstance(activity)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(
                R.layout.fragment_recyclerview, container, false)
        recyclerView = rootView.findViewById(R.id.recyclerview)
        recyclerView!!.layoutManager = LinearLayoutManager(activity)
        recyclerView!!.setEmptyView(activity, rootView.findViewById(R.id.list_empty), "No media found")
        val fastScroller: FastScroller = rootView.findViewById(R.id.fastscroller)
        fastScroller.setRecyclerView(recyclerView)
        loadSongs().execute("")
        (activity as BaseActivity?)!!.setMusicStateListenerListener(this)
        return rootView
    }

    override fun restartLoader() {}
    override fun onPlaylistChanged() {}
    override fun onMetaChanged() {
        if (mAdapter != null) mAdapter!!.notifyDataSetChanged()
    }

    private fun reloadAdapter() {
        object : AsyncTask<Void?, Void?, Void?>() {
            protected override fun doInBackground(vararg unused: Void?): Void? {
                val songList: MutableList<Song?> = getAllSongs(activity!!)
                mAdapter!!.updateDataSet(songList)
                return null
            }

            override fun onPostExecute(aVoid: Void?) {
                mAdapter!!.notifyDataSetChanged()
            }
        }.execute()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.song_sort_by, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_sort_by_az -> {
                mPreferences!!.songSortOrder = SortOrder.SongSortOrder.SONG_A_Z
                reloadAdapter()
                return true
            }
            R.id.menu_sort_by_za -> {
                mPreferences!!.songSortOrder = SortOrder.SongSortOrder.SONG_Z_A
                reloadAdapter()
                return true
            }
            R.id.menu_sort_by_artist -> {
                mPreferences!!.songSortOrder = SortOrder.SongSortOrder.SONG_ARTIST
                reloadAdapter()
                return true
            }
            R.id.menu_sort_by_album -> {
                mPreferences!!.songSortOrder = SortOrder.SongSortOrder.SONG_ALBUM
                reloadAdapter()
                return true
            }
            R.id.menu_sort_by_year -> {
                mPreferences!!.songSortOrder = SortOrder.SongSortOrder.SONG_YEAR
                reloadAdapter()
                return true
            }
            R.id.menu_sort_by_duration -> {
                mPreferences!!.songSortOrder = SortOrder.SongSortOrder.SONG_DURATION
                reloadAdapter()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private inner class loadSongs : AsyncTask<String?, Void?, String>() {
        protected override fun doInBackground(vararg params: String?): String {
            if (activity != null) mAdapter = SongsListAdapter((activity as AppCompatActivity?)!!, getAllSongs(activity!!), false, false)
            return "Executed"
        }

        override fun onPostExecute(result: String) {
            recyclerView!!.adapter = mAdapter
            if (activity != null) recyclerView!!.addItemDecoration(DividerItemDecoration(activity, DividerItemDecoration.VERTICAL_LIST))
        }

        override fun onPreExecute() {}
    }
}