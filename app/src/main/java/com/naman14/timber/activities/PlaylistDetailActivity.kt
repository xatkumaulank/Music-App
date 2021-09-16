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

import android.annotation.TargetApi
import android.content.Intent
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.transition.Transition
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.StyleRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.appthemeengine.Config
import com.afollestad.appthemeengine.customizers.ATEActivityThemeCustomizer
import com.afollestad.appthemeengine.customizers.ATEToolbarCustomizer
import com.afollestad.materialdialogs.MaterialDialog
import com.naman14.timber.R
import com.naman14.timber.adapters.SongsListAdapter
import com.naman14.timber.dataloaders.*
import com.naman14.timber.listeners.SimplelTransitionListener
import com.naman14.timber.models.Song
import com.naman14.timber.utils.Constants
import com.naman14.timber.utils.TimberUtils
import com.naman14.timber.widgets.DividerItemDecoration
import com.naman14.timber.widgets.DragSortRecycler
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import java.util.*

class PlaylistDetailActivity : BaseActivity(), ATEActivityThemeCustomizer, ATEToolbarCustomizer {
    private var action: String? = null
    private var playlistID: Long = 0
    private val playlistsMap = HashMap<String?, Runnable>()
    private val mContext: AppCompatActivity = this@PlaylistDetailActivity
    private var mAdapter: SongsListAdapter? = null
    private var recyclerView: RecyclerView? = null
    private var blurFrame: ImageView? = null
    private var playlistname: TextView? = null
    private var foreground: View? = null
    private var animate = false
    private val playlistLastAdded = Runnable { loadLastAdded().execute("") }
    private val playlistRecents = Runnable { loadRecentlyPlayed().execute("") }
    private val playlistToptracks = Runnable { loadTopTracks().execute("") }
    private val playlistUsercreated = Runnable { loadUserCreatedPlaylist().execute("") }
    @TargetApi(21)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playlist_detail)
        action = intent.action
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setTitle("")
        playlistsMap[Constants.NAVIGATE_PLAYLIST_LASTADDED] = playlistLastAdded
        playlistsMap[Constants.NAVIGATE_PLAYLIST_RECENT] = playlistRecents
        playlistsMap[Constants.NAVIGATE_PLAYLIST_TOPTRACKS] = playlistToptracks
        playlistsMap[Constants.NAVIGATE_PLAYLIST_USERCREATED] = playlistUsercreated
        recyclerView = findViewById<View>(R.id.recyclerview) as RecyclerView
        blurFrame = findViewById<View>(R.id.blurFrame) as ImageView
        playlistname = findViewById<View>(R.id.name) as TextView
        foreground = findViewById(R.id.foreground)
        recyclerView!!.layoutManager = LinearLayoutManager(this)
        setAlbumart()
        animate = intent.getBooleanExtra(Constants.ACTIVITY_TRANSITION, false)
        if (animate && TimberUtils.isLollipop()) {
            if (savedInstanceState != null && savedInstanceState.containsKey("ROTATION_RECREATION")) {
                setUpSongs()
            } else {
                window.enterTransition.addListener(EnterTransitionListener())
            }
        } else {
            setUpSongs()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("ROTATION_RECREATION", "Rotacion")
    }

    private fun setAlbumart() {
        playlistname!!.text = intent.extras.getString(Constants.PLAYLIST_NAME)
        foreground!!.setBackgroundColor(intent.extras.getInt(Constants.PLAYLIST_FOREGROUND_COLOR))
        loadBitmap(TimberUtils.getAlbumArtUri(intent.extras.getLong(Constants.ALBUM_ID)).toString())
    }

    private fun setUpSongs() {
        val navigation = playlistsMap[action]
        if (navigation != null) {
            navigation.run()
            val dragSortRecycler = DragSortRecycler()
            dragSortRecycler.setViewHandleId(R.id.reorder)
            dragSortRecycler.setOnItemMovedListener { from, to ->
                Log.d("playlist", "onItemMoved $from to $to")
                val song = mAdapter!!.getSongAt(from)
                mAdapter!!.removeSongAt(from)
                mAdapter!!.addSongTo(to, song)
                mAdapter!!.notifyDataSetChanged()
                MediaStore.Audio.Playlists.Members.moveItem(contentResolver,
                        playlistID, from, to)
            }
            recyclerView!!.addItemDecoration(dragSortRecycler)
            recyclerView!!.addOnItemTouchListener(dragSortRecycler)
            recyclerView!!.addOnScrollListener(dragSortRecycler.scrollListener)
        } else {
            Log.d("PlaylistDetail", "mo action specified")
        }
    }

    private fun loadBitmap(uri: String) {
        ImageLoader.getInstance().displayImage(uri, blurFrame,
                DisplayImageOptions.Builder().cacheInMemory(true)
                        .showImageOnFail(R.drawable.ic_empty_music2)
                        .resetViewBeforeLoading(true)
                        .build())
    }

    private fun setRecyclerViewAapter() {
        recyclerView!!.adapter = mAdapter
        if (animate && TimberUtils.isLollipop()) {
            val handler = Handler()
            handler.postDelayed({ recyclerView!!.addItemDecoration(DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL_LIST, R.drawable.item_divider_white)) }, 250)
        } else recyclerView!!.addItemDecoration(DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL_LIST, R.drawable.item_divider_white))
    }

    @StyleRes
    override fun getActivityTheme(): Int {
        return if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("dark_theme", false)) R.style.AppTheme_FullScreen_Dark else R.style.AppTheme_FullScreen_Light
    }

    private inner class loadLastAdded : AsyncTask<String?, Void?, String>() {
         override fun doInBackground(vararg params: String?): String {
            val lastadded = LastAddedLoader.getLastAddedSongs(mContext)
            mAdapter = SongsListAdapter(mContext, lastadded, true, animate)
            mAdapter!!.setPlaylistId(playlistID)
            return "Executed"
        }

        override fun onPostExecute(result: String) {
            setRecyclerViewAapter()
        }

        override fun onPreExecute() {}
    }

    private inner class loadRecentlyPlayed : AsyncTask<String?, Void?, String>() {
         override fun doInBackground(vararg params: String?): String {
            val loader = TopTracksLoader(mContext, TopTracksLoader.QueryType.RecentSongs)
            val recentsongs: List<Song> = SongLoader.getSongsForCursor(TopTracksLoader.cursor)
            mAdapter = SongsListAdapter(mContext, recentsongs, true, animate)
            mAdapter!!.setPlaylistId(playlistID)
            return "Executed"
        }

        override fun onPostExecute(result: String) {
            setRecyclerViewAapter()
        }

        override fun onPreExecute() {}
    }

    private inner class loadTopTracks : AsyncTask<String?, Void?, String>() {
         override fun doInBackground(vararg params: String?): String {
            val loader = TopTracksLoader(mContext, TopTracksLoader.QueryType.TopTracks)
            val toptracks: List<Song> = SongLoader.getSongsForCursor(TopTracksLoader.cursor)
            mAdapter = SongsListAdapter(mContext, toptracks, true, animate)
            mAdapter!!.setPlaylistId(playlistID)
            return "Executed"
        }

        override fun onPostExecute(result: String) {
            setRecyclerViewAapter()
        }

        override fun onPreExecute() {}
    }

    private inner class loadUserCreatedPlaylist : AsyncTask<String?, Void?, String>() {
        protected override fun doInBackground(vararg params: String?): String {
            playlistID = intent.extras.getLong(Constants.PLAYLIST_ID)
            val playlistsongs = PlaylistSongLoader.getSongsInPlaylist(mContext, playlistID)
            mAdapter = SongsListAdapter(mContext, playlistsongs, true, animate)
            mAdapter!!.setPlaylistId(playlistID)
            return "Executed"
        }

        override fun onPostExecute(result: String) {
            setRecyclerViewAapter()
        }

        override fun onPreExecute() {}
    }

    private inner class EnterTransitionListener : SimplelTransitionListener() {
        @TargetApi(21)
        override fun onTransitionEnd(paramTransition: Transition) {
            setUpSongs()
        }

        override fun onTransitionStart(paramTransition: Transition) {}
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_playlist_detail, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        if (action == Constants.NAVIGATE_PLAYLIST_USERCREATED) {
            menu.findItem(R.id.action_delete_playlist).isVisible = true
            menu.findItem(R.id.action_clear_auto_playlist).isVisible = false
        } else {
            menu.findItem(R.id.action_delete_playlist).isVisible = false
            menu.findItem(R.id.action_clear_auto_playlist).title = "Clear " + playlistname!!.text.toString()
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                super.onBackPressed()
                return true
            }
            R.id.action_delete_playlist -> showDeletePlaylistDialog()
            R.id.action_clear_auto_playlist -> clearAutoPlaylists()
            else -> {
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showDeletePlaylistDialog() {
        MaterialDialog.Builder(this)
                .title("Delete playlist?")
                .content("Are you sure you want to delete playlist " + playlistname!!.text.toString() + " ?")
                .positiveText("Delete")
                .negativeText("Cancel")
                .onPositive { dialog, which ->
                    PlaylistLoader.deletePlaylists(this@PlaylistDetailActivity, playlistID)
                    val returnIntent = Intent()
                    setResult(RESULT_OK, returnIntent)
                    finish()
                }
                .onNegative { dialog, which -> dialog.dismiss() }
                .show()
    }

    private fun clearAutoPlaylists() {
        when (action) {
            Constants.NAVIGATE_PLAYLIST_LASTADDED -> TimberUtils.clearLastAdded(this)
            Constants.NAVIGATE_PLAYLIST_RECENT -> TimberUtils.clearRecent(this)
            Constants.NAVIGATE_PLAYLIST_TOPTRACKS -> TimberUtils.clearTopTracks(this)
        }
        val returnIntent = Intent()
        setResult(RESULT_OK, returnIntent)
        finish()
    }

    override fun onMetaChanged() {
        super.onMetaChanged()
        if (mAdapter != null) mAdapter!!.notifyDataSetChanged()
    }

    override fun getToolbarColor(): Int {
        return Color.TRANSPARENT
    }

    override fun getLightToolbarMode(): Int {
        return Config.LIGHT_TOOLBAR_AUTO
    }
}