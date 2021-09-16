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
package com.naman14.timber.activities

import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.MenuItemCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.naman14.timber.R
import com.naman14.timber.adapters.SearchAdapter
import com.naman14.timber.dataloaders.AlbumLoader
import com.naman14.timber.dataloaders.ArtistLoader
import com.naman14.timber.dataloaders.SongLoader
import com.naman14.timber.provider.SearchHistory
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class SearchActivity : BaseActivity(), SearchView.OnQueryTextListener, OnTouchListener {
    private val mSearchExecutor: Executor = Executors.newSingleThreadExecutor()
    private var mSearchTask: AsyncTask<*, *, *>? = null
    private var mSearchView: SearchView? = null
    private var mImm: InputMethodManager? = null
    private var queryString: String? = null
    private var adapter: SearchAdapter? = null
    private var recyclerView: RecyclerView? = null
    private val searchResults: MutableList<Any?> = emptyList<Any?>() as MutableList<Any?>
    var bundle: Bundle? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        mImm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        recyclerView = findViewById<View>(R.id.recyclerview) as RecyclerView
        recyclerView!!.layoutManager = LinearLayoutManager(this)
        adapter = SearchAdapter(this)
        recyclerView!!.adapter = adapter
        if (savedInstanceState != null && savedInstanceState.containsKey("QUERY_STRING")) {
            bundle = savedInstanceState
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (queryString != null) {
            outState.putString("QUERY_STRING", queryString)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_search, menu)
        mSearchView = MenuItemCompat.getActionView(menu.findItem(R.id.menu_search)) as SearchView
        mSearchView!!.setOnQueryTextListener(this)
        mSearchView!!.queryHint = getString(R.string.search_library)
        mSearchView!!.setIconifiedByDefault(false)
        mSearchView!!.isIconified = false
        MenuItemCompat.setOnActionExpandListener(menu.findItem(R.id.menu_search), object : MenuItemCompat.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                finish()
                return false
            }
        })
        menu.findItem(R.id.menu_search).expandActionView()
        if (bundle != null && bundle!!.containsKey("QUERY_STRING")) {
            mSearchView!!.setQuery(bundle!!.getString("QUERY_STRING"), true)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val item = menu.findItem(R.id.action_search)
        item.isVisible = false
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            else -> {
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        onQueryTextChange(query)
        hideInputManager()
        return true
    }

    override fun onQueryTextChange(newText: String): Boolean {
        if (newText == queryString) {
            return true
        }
        if (mSearchTask != null) {
            mSearchTask!!.cancel(false)
            mSearchTask = null
        }
        queryString = newText
        if (queryString!!.trim { it <= ' ' } == "") {
            searchResults.clear()
            adapter!!.updateSearchResults(searchResults)
            adapter!!.notifyDataSetChanged()
        } else {
            mSearchTask = SearchTask().executeOnExecutor(mSearchExecutor, queryString)
            Log.d("AAAABBBBBB", "TaskCanelled? " + (mSearchTask as AsyncTask<String?, Void?, ArrayList<Any?>?>?)?.isCancelled())
        }
        return true
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        hideInputManager()
        return false
    }

    override fun onDestroy() {
        if (mSearchTask != null && mSearchTask!!.status != AsyncTask.Status.FINISHED) {
            mSearchTask!!.cancel(false)
        }
        super.onDestroy()
    }

    fun hideInputManager() {
        if (mSearchView != null) {
            if (mImm != null) {
                mImm!!.hideSoftInputFromWindow(mSearchView!!.windowToken, 0)
            }
            mSearchView!!.clearFocus()
            SearchHistory.getInstance(this).addSearchString(queryString)
        }
    }

    private inner class SearchTask : AsyncTask<String?, Void?, ArrayList<Any?>?>() {
        protected override fun doInBackground(vararg params: String?): ArrayList<Any?>? {
            val results = ArrayList<Any?>(27)
            val songList = SongLoader.searchSongs(this@SearchActivity, params[0], 10)
            if (!songList.isEmpty()) {
                results.add(getString(R.string.songs))
                results.addAll(songList)
            }
            var canceled = isCancelled
            if (canceled) {
                return null
            }
            val albumList = AlbumLoader.getAlbums(this@SearchActivity, params[0], 7)
            if (!albumList.isEmpty()) {
                results.add(getString(R.string.albums))
                results.addAll(albumList)
            }
            canceled = isCancelled
            if (canceled) {
                return null
            }
            val artistList = ArtistLoader.getArtists(this@SearchActivity, params[0], 7)
            if (!artistList.isEmpty()) {
                results.add(getString(R.string.artists))
                results.addAll(artistList)
            }
            if (results.size == 0) {
                results.add(getString(R.string.nothing_found))
            }
            return results
        }

        override fun onPostExecute(objects: ArrayList<Any?>?) {
            super.onPostExecute(objects)
            mSearchTask = null
            if (objects != null) {
                adapter!!.updateSearchResults(objects)
                adapter!!.notifyDataSetChanged()
            }
        }
    }
}