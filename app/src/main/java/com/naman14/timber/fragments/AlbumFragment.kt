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
import com.naman14.timber.adapters.AlbumAdapter
import com.naman14.timber.dataloaders.AlbumLoader.getAllAlbums
import com.naman14.timber.utils.PreferencesUtility
import com.naman14.timber.utils.SortOrder
import com.naman14.timber.widgets.BaseRecyclerView
import com.naman14.timber.widgets.DividerItemDecoration
import com.naman14.timber.widgets.FastScroller

class AlbumFragment : Fragment() {
    private var mAdapter: AlbumAdapter? = null
    private var recyclerView: BaseRecyclerView? = null
    private var fastScroller: FastScroller? = null
    private var layoutManager: GridLayoutManager? = null
    private var itemDecoration: ItemDecoration? = null
    private var mPreferences: PreferencesUtility? = null
    private var isGrid = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mPreferences = PreferencesUtility.getInstance(activity)
        isGrid = mPreferences!!.isAlbumsInGrid
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(
                R.layout.fragment_recyclerview, container, false)
        recyclerView = rootView.findViewById(R.id.recyclerview)
        fastScroller = rootView.findViewById(R.id.fastscroller)
        recyclerView!!.setEmptyView(activity, rootView.findViewById(R.id.list_empty), "No media found")
        setLayoutManager()
        if (activity != null) loadAlbums().execute("")
        return rootView
    }

    private fun setLayoutManager() {
        if (isGrid) {
            layoutManager = GridLayoutManager(activity, 2)
            fastScroller!!.visibility = View.GONE
        } else {
            layoutManager = GridLayoutManager(activity, 1)
            fastScroller!!.visibility = View.VISIBLE
            fastScroller!!.setRecyclerView(recyclerView)
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
        recyclerView!!.adapter = AlbumAdapter(activity, getAllAlbums(activity!!))
        layoutManager!!.spanCount = column
        layoutManager!!.requestLayout()
        setItemDecoration()
    }

    private fun reloadAdapter() {
        object : AsyncTask<Void?, Void?, Void?>() {
            protected override fun doInBackground(vararg unused: Void?): Void? {
                val albumList = getAllAlbums(activity!!)
                mAdapter!!.updateDataSet(albumList)
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
        inflater.inflate(R.menu.album_sort_by, menu)
        inflater.inflate(R.menu.menu_show_as, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_sort_by_az -> {
                mPreferences!!.albumSortOrder = SortOrder.AlbumSortOrder.ALBUM_A_Z
                reloadAdapter()
                return true
            }
            R.id.menu_sort_by_za -> {
                mPreferences!!.albumSortOrder = SortOrder.AlbumSortOrder.ALBUM_Z_A
                reloadAdapter()
                return true
            }
            R.id.menu_sort_by_year -> {
                mPreferences!!.albumSortOrder = SortOrder.AlbumSortOrder.ALBUM_YEAR
                reloadAdapter()
                return true
            }
            R.id.menu_sort_by_artist -> {
                mPreferences!!.albumSortOrder = SortOrder.AlbumSortOrder.ALBUM_ARTIST
                reloadAdapter()
                return true
            }
            R.id.menu_sort_by_number_of_songs -> {
                mPreferences!!.albumSortOrder = SortOrder.AlbumSortOrder.ALBUM_NUMBER_OF_SONGS
                reloadAdapter()
                return true
            }
            R.id.menu_show_as_list -> {
                mPreferences!!.isAlbumsInGrid = false
                isGrid = false
                updateLayoutManager(1)
                return true
            }
            R.id.menu_show_as_grid -> {
                mPreferences!!.isAlbumsInGrid = true
                isGrid = true
                updateLayoutManager(2)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
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

    private inner class loadAlbums : AsyncTask<String?, Void?, String>() {
        protected override fun doInBackground(vararg params: String?): String {
            if (activity != null) mAdapter = AlbumAdapter(activity, getAllAlbums(activity!!))
            return "Executed"
        }

        override fun onPostExecute(result: String) {
            recyclerView!!.adapter = mAdapter
            //to add spacing between cards
            if (activity != null) {
                setItemDecoration()
            }
        }

        override fun onPreExecute() {}
    }
}