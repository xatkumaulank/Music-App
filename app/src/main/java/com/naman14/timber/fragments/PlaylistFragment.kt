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

import android.app.Activity
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.afollestad.appthemeengine.ATE
import com.naman14.timber.R
import com.naman14.timber.adapters.PlaylistAdapter
import com.naman14.timber.dataloaders.PlaylistLoader.getPlaylists
import com.naman14.timber.dialogs.CreatePlaylistDialog
import com.naman14.timber.models.Playlist
import com.naman14.timber.subfragments.PlaylistPagerFragment
import com.naman14.timber.utils.Constants
import com.naman14.timber.utils.PreferencesUtility
import com.naman14.timber.widgets.DividerItemDecoration
import com.naman14.timber.widgets.MultiViewPager
import java.util.*

class PlaylistFragment : Fragment() {
    private var playlistcount = 0
    private var adapter: FragmentStatePagerAdapter? = null
    private var pager: MultiViewPager? = null
    private var recyclerView: RecyclerView? = null
    private var layoutManager: GridLayoutManager? = null
    private var itemDecoration: ItemDecoration? = null
    private var mPreferences: PreferencesUtility? = null
    private var isGrid = false
    private var isDefault = false
    private var showAuto = false
    private var mAdapter: PlaylistAdapter? = null
    private var playlists: MutableList<Playlist>? = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mPreferences = PreferencesUtility.getInstance(activity)
        isGrid = mPreferences!!.playlistView == Constants.PLAYLIST_VIEW_GRID
        isDefault = mPreferences!!.playlistView == Constants.PLAYLIST_VIEW_DEFAULT
        showAuto = mPreferences!!.showAutoPlaylist()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(
                R.layout.fragment_playlist, container, false)
        val toolbar = rootView.findViewById<View>(R.id.toolbar) as Toolbar
        pager = rootView.findViewById<View>(R.id.playlistpager) as MultiViewPager
        recyclerView = rootView.findViewById<View>(R.id.recyclerview) as RecyclerView
        (activity as AppCompatActivity?)!!.setSupportActionBar(toolbar)
        val ab = (activity as AppCompatActivity?)!!.supportActionBar
        ab!!.setHomeAsUpIndicator(R.drawable.ic_menu)
        ab.setDisplayHomeAsUpEnabled(true)
        ab.setTitle(R.string.playlists)
        playlists = getPlaylists(activity!!, showAuto)
        playlistcount = playlists!!.size
        if (isDefault) {
            initPager()
        } else {
            initRecyclerView()
        }
        return rootView
    }

    private fun initPager() {
        pager!!.visibility = View.VISIBLE
        recyclerView!!.visibility = View.GONE
        recyclerView!!.adapter = null
        adapter = object : FragmentStatePagerAdapter(childFragmentManager) {
            override fun getCount(): Int {
                return playlistcount
            }

            override fun getItem(position: Int): Fragment {
                return PlaylistPagerFragment.newInstance(position)
            }
        }
        pager!!.adapter = adapter
        pager!!.offscreenPageLimit = 3
    }

    private fun initRecyclerView() {
        recyclerView!!.visibility = View.VISIBLE
        pager!!.visibility = View.GONE
        setLayoutManager()
        mAdapter = PlaylistAdapter(activity, playlists)
        recyclerView!!.adapter = mAdapter
        //to add spacing between cards
        if (activity != null) {
            setItemDecoration()
        }
    }

    private fun setLayoutManager() {
        layoutManager = if (isGrid) {
            GridLayoutManager(activity, 2)
        } else {
            GridLayoutManager(activity, 1)
        }
        recyclerView!!.layoutManager = layoutManager
    }

    private fun setItemDecoration() {
        if (isGrid) {
            val spacingInPixels = activity!!.resources.getDimensionPixelSize(R.dimen.spacing_card_album_grid)
            itemDecoration = SpacesItemDecoration(spacingInPixels)
        } else {
            itemDecoration = DividerItemDecoration(activity, DividerItemDecoration.VERTICAL_LIST)
        }
        recyclerView!!.addItemDecoration(itemDecoration!!)
    }

    private fun updateLayoutManager(column: Int) {
        recyclerView!!.removeItemDecoration(itemDecoration!!)
        recyclerView!!.adapter = PlaylistAdapter(activity, getPlaylists(activity!!, showAuto))
        layoutManager!!.spanCount = column
        layoutManager!!.requestLayout()
        setItemDecoration()
    }

    inner class SpacesItemDecoration(private val space: Int) : ItemDecoration() {
        override fun getItemOffsets(outRect: Rect, view: View,
                                    parent: RecyclerView, state: RecyclerView.State) {
            outRect.left = space
            outRect.top = space
            outRect.right = space
            outRect.bottom = space
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (PreferenceManager.getDefaultSharedPreferences(activity).getBoolean("dark_theme", false)) {
            ATE.apply(this, "dark_theme")
        } else {
            ATE.apply(this, "light_theme")
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_playlist, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        if (showAuto) {
            menu.findItem(R.id.action_view_auto_playlists).title = "Hide auto playlists"
        } else menu.findItem(R.id.action_view_auto_playlists).title = "Show auto playlists"
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_new_playlist -> {
                CreatePlaylistDialog.newInstance().show(childFragmentManager, "CREATE_PLAYLIST")
                return true
            }
            R.id.menu_show_as_list -> {
                mPreferences!!.playlistView = Constants.PLAYLIST_VIEW_LIST
                isGrid = false
                isDefault = false
                initRecyclerView()
                updateLayoutManager(1)
                return true
            }
            R.id.menu_show_as_grid -> {
                mPreferences!!.playlistView = Constants.PLAYLIST_VIEW_GRID
                isGrid = true
                isDefault = false
                initRecyclerView()
                updateLayoutManager(2)
                return true
            }
            R.id.menu_show_as_default -> {
                mPreferences!!.playlistView = Constants.PLAYLIST_VIEW_DEFAULT
                isDefault = true
                initPager()
                return true
            }
            R.id.action_view_auto_playlists -> {
                if (showAuto) {
                    showAuto = false
                    mPreferences!!.setToggleShowAutoPlaylist(false)
                } else {
                    showAuto = true
                    mPreferences!!.setToggleShowAutoPlaylist(true)
                }
                reloadPlaylists()
                activity!!.invalidateOptionsMenu()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun updatePlaylists(id: Long) {
        playlists = getPlaylists(activity!!, showAuto)
        playlistcount = playlists!!.size
        if (isDefault) {
            adapter!!.notifyDataSetChanged()
            if (id != -1L) {
                val handler = Handler()
                handler.postDelayed({
                    for (i in playlists!!.indices) {
                        val playlistid = playlists!![i].id
                        if (playlistid == id) {
                            pager!!.currentItem = i
                            break
                        }
                    }
                }, 200)
            }
        } else {
            mAdapter!!.updateDataSet(playlists)
        }
    }

    fun reloadPlaylists() {
        playlists = getPlaylists(activity!!, showAuto)
        playlistcount = playlists!!.size
        if (isDefault) {
            initPager()
        } else {
            initRecyclerView()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.ACTION_DELETE_PLAYLIST) {
            if (resultCode == Activity.RESULT_OK) {
                reloadPlaylists()
            }
        }
    }
}