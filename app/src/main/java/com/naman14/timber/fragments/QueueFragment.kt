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
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.appthemeengine.ATE
import com.naman14.timber.MusicPlayer
import com.naman14.timber.R
import com.naman14.timber.activities.BaseActivity
import com.naman14.timber.adapters.PlayingQueueAdapter
import com.naman14.timber.dataloaders.QueueLoader.getQueueSongs
import com.naman14.timber.listeners.MusicStateListener
import com.naman14.timber.widgets.BaseRecyclerView
import com.naman14.timber.widgets.DragSortRecycler

class QueueFragment : Fragment(), MusicStateListener {
    private var mAdapter: PlayingQueueAdapter? = null
    private var recyclerView: BaseRecyclerView? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(
                R.layout.fragment_queue, container, false)
        val toolbar: Toolbar = rootView.findViewById(R.id.toolbar)
        (activity as AppCompatActivity?)!!.setSupportActionBar(toolbar)
        val ab = (activity as AppCompatActivity?)!!.supportActionBar
        ab!!.setHomeAsUpIndicator(R.drawable.ic_menu)
        ab.setDisplayHomeAsUpEnabled(true)
        ab.setTitle(R.string.playing_queue)
        recyclerView = rootView.findViewById(R.id.recyclerview)
        recyclerView!!.layoutManager = LinearLayoutManager(activity)
        recyclerView!!.itemAnimator = null
        recyclerView!!.setEmptyView(activity, rootView.findViewById(R.id.list_empty), "No songs in queue")
        loadQueueSongs().execute("")
        (activity as BaseActivity?)!!.setMusicStateListenerListener(this)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (PreferenceManager.getDefaultSharedPreferences(activity).getBoolean("dark_theme", false)) {
            ATE.apply(this, "dark_theme")
        } else {
            ATE.apply(this, "light_theme")
        }
    }

    override fun restartLoader() {}
    override fun onPlaylistChanged() {}
    override fun onMetaChanged() {
        if (mAdapter != null) mAdapter!!.notifyDataSetChanged()
    }

    private inner class loadQueueSongs : AsyncTask<String?, Void?, String>() {
        protected override fun doInBackground(vararg params: String?): String {
            mAdapter = PlayingQueueAdapter(activity!!, getQueueSongs(activity))
            return "Executed"
        }

        override fun onPostExecute(result: String) {
            recyclerView!!.adapter = mAdapter
            val dragSortRecycler = DragSortRecycler()
            dragSortRecycler.setViewHandleId(R.id.reorder)
            dragSortRecycler.setOnItemMovedListener { from, to ->
                Log.d("queue", "onItemMoved $from to $to")
                val song = mAdapter!!.getSongAt(from)
                mAdapter!!.removeSongAt(from)
                mAdapter!!.addSongTo(to, song)
                mAdapter!!.notifyDataSetChanged()
                MusicPlayer.moveQueueItem(from, to)
            }
            recyclerView!!.addItemDecoration(dragSortRecycler)
            recyclerView!!.addOnItemTouchListener(dragSortRecycler)
            recyclerView!!.addOnScrollListener(dragSortRecycler.scrollListener)
            recyclerView!!.layoutManager!!.scrollToPosition(mAdapter!!.currentlyPlayingPosition)
        }

        override fun onPreExecute() {}
    }
}