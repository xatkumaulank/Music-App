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

import android.graphics.Rect
import android.os.AsyncTask
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.naman14.timber.R
import com.naman14.timber.adapters.ArtistAdapter
import com.naman14.timber.dataloaders.ArtistLoader.getAllArtists
import com.naman14.timber.utils.PreferencesUtility
import com.naman14.timber.utils.SortOrder
import com.naman14.timber.widgets.BaseRecyclerView
import com.naman14.timber.widgets.DividerItemDecoration
import com.naman14.timber.widgets.FastScroller

class ArtistFragment : Fragment() {
    private var mAdapter: ArtistAdapter? = null
    private var recyclerView: BaseRecyclerView? = null
    private var layoutManager: GridLayoutManager? = null
    private var itemDecoration: ItemDecoration? = null
    private var mPreferences: PreferencesUtility? = null
    private var isGrid = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mPreferences = PreferencesUtility.getInstance(activity)
        isGrid = mPreferences!!.isArtistsInGrid
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(
                R.layout.fragment_recyclerview, container, false)
        recyclerView = rootView.findViewById(R.id.recyclerview)
        val fastScroller: FastScroller = rootView.findViewById(R.id.fastscroller)
        fastScroller.setRecyclerView(recyclerView)
        recyclerView!!.setEmptyView(activity, rootView.findViewById(R.id.list_empty), "No media found")
        setLayoutManager()
        if (activity != null) loadArtists().execute("")
        return rootView
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
        recyclerView!!.adapter = ArtistAdapter(activity, getAllArtists(activity!!))
        layoutManager!!.spanCount = column
        layoutManager!!.requestLayout()
        setItemDecoration()
    }

    private fun reloadAdapter() {
        object : AsyncTask<Void?, Void?, Void?>() {
            protected override fun doInBackground(vararg unused: Void?): Void? {
                val artistList = getAllArtists(activity!!)
                mAdapter!!.updateDataSet(artistList)
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
        inflater.inflate(R.menu.artist_sort_by, menu)
        inflater.inflate(R.menu.menu_show_as, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_sort_by_az -> {
                mPreferences!!.artistSortOrder = SortOrder.ArtistSortOrder.ARTIST_A_Z
                reloadAdapter()
                return true
            }
            R.id.menu_sort_by_za -> {
                mPreferences!!.artistSortOrder = SortOrder.ArtistSortOrder.ARTIST_Z_A
                reloadAdapter()
                return true
            }
            R.id.menu_sort_by_number_of_songs -> {
                mPreferences!!.artistSortOrder = SortOrder.ArtistSortOrder.ARTIST_NUMBER_OF_SONGS
                reloadAdapter()
                return true
            }
            R.id.menu_sort_by_number_of_albums -> {
                mPreferences!!.artistSortOrder = SortOrder.ArtistSortOrder.ARTIST_NUMBER_OF_ALBUMS
                reloadAdapter()
                return true
            }
            R.id.menu_show_as_list -> {
                mPreferences!!.isArtistsInGrid = false
                isGrid = false
                updateLayoutManager(1)
                return true
            }
            R.id.menu_show_as_grid -> {
                mPreferences!!.isArtistsInGrid = true
                isGrid = true
                updateLayoutManager(2)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private inner class loadArtists : AsyncTask<String?, Void?, String>() {
        protected override fun doInBackground(vararg params: String?): String {
            if (activity != null) mAdapter = ArtistAdapter(activity, getAllArtists(activity!!))
            return "Executed"
        }

        override fun onPostExecute(result: String) {
            if (mAdapter != null) {
                mAdapter!!.setHasStableIds(true)
                recyclerView!!.adapter = mAdapter
            }
            if (activity != null) {
                setItemDecoration()
            }
        }

        override fun onPreExecute() {}
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
}