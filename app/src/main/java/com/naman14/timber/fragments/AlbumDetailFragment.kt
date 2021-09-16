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

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.transition.Transition
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.appthemeengine.ATE
import com.afollestad.appthemeengine.Config
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.naman14.timber.MusicPlayer
import com.naman14.timber.R
import com.naman14.timber.adapters.AlbumSongsAdapter
import com.naman14.timber.dataloaders.AlbumLoader.getAlbum
import com.naman14.timber.dataloaders.AlbumSongLoader.getSongsForAlbum
import com.naman14.timber.dialogs.AddPlaylistDialog.Companion.newInstance
import com.naman14.timber.listeners.SimplelTransitionListener
import com.naman14.timber.models.Album
import com.naman14.timber.models.Song
import com.naman14.timber.utils.*
import com.naman14.timber.widgets.DividerItemDecoration
import com.nostra13.universalimageloader.core.assist.FailReason
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener
import net.steamcrafted.materialiconlib.MaterialDrawableBuilder

class AlbumDetailFragment : Fragment() {
    private var albumID: Long = -1
    private var albumArt: ImageView? = null
    private var artistArt: ImageView? = null
    private var albumTitle: TextView? = null
    private var albumDetails: TextView? = null
    private var mContext: AppCompatActivity? = null
    private var recyclerView: RecyclerView? = null
    private var mAdapter: AlbumSongsAdapter? = null
    private var toolbar: Toolbar? = null
    private var album: Album? = null
    private var collapsingToolbarLayout: CollapsingToolbarLayout? = null
    private var appBarLayout: AppBarLayout? = null
    private var fab: FloatingActionButton? = null
    private var loadFailed = false
    private var mPreferences: PreferencesUtility? = null
    private var primaryColor = -1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            albumID = arguments!!.getLong(Constants.ALBUM_ID)
        }
        mContext = context as AppCompatActivity?
        mPreferences = PreferencesUtility.getInstance(context)
    }

    @TargetApi(21)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(
                R.layout.fragment_album_detail, container, false)
        albumArt = rootView.findViewById<View>(R.id.album_art) as ImageView
        artistArt = rootView.findViewById<View>(R.id.artist_art) as ImageView
        albumTitle = rootView.findViewById<View>(R.id.album_title) as TextView
        albumDetails = rootView.findViewById<View>(R.id.album_details) as TextView
        toolbar = rootView.findViewById<View>(R.id.toolbar) as Toolbar
        fab = rootView.findViewById<View>(R.id.fab) as FloatingActionButton
        if (arguments!!.getBoolean("transition")) {
            albumArt!!.transitionName = arguments!!.getString("transition_name")
        }
        recyclerView = rootView.findViewById<View>(R.id.recyclerview) as RecyclerView
        collapsingToolbarLayout = rootView.findViewById<View>(R.id.collapsing_toolbar) as CollapsingToolbarLayout
        appBarLayout = rootView.findViewById<View>(R.id.app_bar) as AppBarLayout
        recyclerView!!.isEnabled = false
        recyclerView!!.layoutManager = LinearLayoutManager(activity)
        album = getAlbum(activity!!, albumID)
        setAlbumart()
        setUpEverything()
        fab!!.setOnClickListener {
            val handler = Handler()
            handler.postDelayed({
                val adapter = recyclerView!!.adapter as AlbumSongsAdapter?
                MusicPlayer.playAll(activity, adapter!!.songIds, 0, albumID, TimberUtils.IdType.Album, true)
                NavigationUtils.navigateToNowplaying(activity, false)
            }, 150)
        }
        return rootView
    }

    private fun setupToolbar() {
        (activity as AppCompatActivity?)!!.setSupportActionBar(toolbar)
        val ab = (activity as AppCompatActivity?)!!.supportActionBar
        ab!!.setDisplayHomeAsUpEnabled(true)
        collapsingToolbarLayout!!.title = album!!.title
    }

    private fun setAlbumart() {
        ImageUtils.loadAlbumArtIntoView(album!!.id, albumArt, object : ImageLoadingListener {
            override fun onLoadingStarted(imageUri: String, view: View) {}
            override fun onLoadingFailed(imageUri: String, view: View, failReason: FailReason) {
                loadFailed = true
                val builder = MaterialDrawableBuilder.with(context)
                        .setIcon(MaterialDrawableBuilder.IconValue.SHUFFLE)
                        .setColor(TimberUtils.getBlackWhiteColor(Config.accentColor(context!!, Helpers.getATEKey(context))))
                ATEUtils.setFabBackgroundTint(fab, Config.accentColor(context!!, Helpers.getATEKey(context)))
                fab!!.setImageDrawable(builder.build())
            }

            override fun onLoadingComplete(imageUri: String, view: View, loadedImage: Bitmap) {
                try {
                    Palette.Builder(loadedImage).generate { palette ->
                        val swatch = palette!!.vibrantSwatch
                        if (swatch != null) {
                            primaryColor = swatch.rgb
                            collapsingToolbarLayout!!.setContentScrimColor(primaryColor)
                            if (activity != null) ATEUtils.setStatusBarColor(activity, Helpers.getATEKey(activity), primaryColor)
                        } else {
                            val swatchMuted = palette.mutedSwatch
                            if (swatchMuted != null) {
                                primaryColor = swatchMuted.rgb
                                collapsingToolbarLayout!!.setContentScrimColor(primaryColor)
                                if (activity != null) ATEUtils.setStatusBarColor(activity, Helpers.getATEKey(activity), primaryColor)
                            }
                        }
                        if (activity != null) {
                            val builder = MaterialDrawableBuilder.with(activity)
                                    .setIcon(MaterialDrawableBuilder.IconValue.SHUFFLE)
                                    .setSizeDp(30)
                            if (primaryColor != -1) {
                                builder.setColor(TimberUtils.getBlackWhiteColor(primaryColor))
                                ATEUtils.setFabBackgroundTint(fab, primaryColor)
                                fab!!.setImageDrawable(builder.build())
                            } else {
                                if (context != null) {
                                    ATEUtils.setFabBackgroundTint(fab, Config.accentColor(context!!, Helpers.getATEKey(context)))
                                    builder.setColor(TimberUtils.getBlackWhiteColor(Config.accentColor(context!!, Helpers.getATEKey(context))))
                                    fab!!.setImageDrawable(builder.build())
                                }
                            }
                        }
                    }
                } catch (ignored: Exception) {
                }
            }

            override fun onLoadingCancelled(imageUri: String, view: View) {}
        }
        )
    }

    private fun setAlbumDetails() {
        val songCount = TimberUtils.makeLabel(activity, R.plurals.Nsongs, album!!.songCount)
        val year = if (album!!.year != 0) " - " + album!!.year.toString() else ""
        albumTitle!!.text = album!!.title
        albumDetails!!.text = album!!.artistName + " - " + songCount + year
    }

    private fun setUpAlbumSongs() {
        val songList: MutableList<Song> = getSongsForAlbum(activity!!, albumID)
        mAdapter = AlbumSongsAdapter(activity!!, songList, albumID)
        recyclerView!!.addItemDecoration(DividerItemDecoration(activity, DividerItemDecoration.VERTICAL_LIST))
        recyclerView!!.adapter = mAdapter
    }

    private fun setUpEverything() {
        setupToolbar()
        setAlbumDetails()
        setUpAlbumSongs()
    }

    private fun reloadAdapter() {
        object : AsyncTask<Void?, Void?, Void?>() {
            protected override fun doInBackground(vararg unused: Void?): Void? {
                val songList: MutableList<Song> = getSongsForAlbum(activity!!, albumID)
                mAdapter?.updateDataSet(songList)
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
        inflater.inflate(R.menu.album_detail, menu)
        if (activity != null) ATE.applyMenu(activity!!, "dark_theme", menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_go_to_artist -> NavigationUtils.goToArtist(getContext(), album!!.artistId)
            R.id.popup_song_addto_queue -> MusicPlayer.addToQueue(context, mAdapter!!.songIds, -1, TimberUtils.IdType.NA)
            R.id.popup_song_addto_playlist -> newInstance(mAdapter!!.songIds).show(mContext!!.supportFragmentManager, "ADD_PLAYLIST")
            R.id.menu_sort_by_az -> {
                mPreferences!!.albumSongSortOrder = SortOrder.AlbumSongSortOrder.SONG_A_Z
                reloadAdapter()
                return true
            }
            R.id.menu_sort_by_za -> {
                mPreferences!!.albumSongSortOrder = SortOrder.AlbumSongSortOrder.SONG_Z_A
                reloadAdapter()
                return true
            }
            R.id.menu_sort_by_year -> {
                mPreferences!!.albumSongSortOrder = SortOrder.AlbumSongSortOrder.SONG_YEAR
                reloadAdapter()
                return true
            }
            R.id.menu_sort_by_duration -> {
                mPreferences!!.albumSongSortOrder = SortOrder.AlbumSongSortOrder.SONG_DURATION
                reloadAdapter()
                return true
            }
            R.id.menu_sort_by_track_number -> {
                mPreferences!!.albumSongSortOrder = SortOrder.AlbumSongSortOrder.SONG_TRACK_LIST
                reloadAdapter()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        val ateKey = Helpers.getATEKey(activity)
        toolbar!!.setBackgroundColor(Color.TRANSPARENT)
        if (primaryColor != -1 && activity != null) {
            collapsingToolbarLayout!!.setContentScrimColor(primaryColor)
            ATEUtils.setFabBackgroundTint(fab, primaryColor)
            ATEUtils.setStatusBarColor(activity, ateKey, primaryColor)
        }
    }

    private inner class EnterTransitionListener : SimplelTransitionListener() {
        @TargetApi(21)
        override fun onTransitionEnd(paramTransition: Transition) {
            FabAnimationUtils.scaleIn(fab)
        }

        override fun onTransitionStart(paramTransition: Transition) {
            FabAnimationUtils.scaleOut(fab, 0, null)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(id: Long, useTransition: Boolean, transitionName: String?): AlbumDetailFragment {
            val fragment = AlbumDetailFragment()
            val args = Bundle()
            args.putLong(Constants.ALBUM_ID, id)
            args.putBoolean("transition", useTransition)
            if (useTransition) args.putString("transition_name", transitionName)
            fragment.arguments = args
            return fragment
        }
    }
}