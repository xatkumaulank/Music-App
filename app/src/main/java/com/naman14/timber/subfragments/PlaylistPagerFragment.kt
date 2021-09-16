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
package com.naman14.timber.subfragments

import android.content.Context
import android.graphics.Bitmap
import android.os.AsyncTask
import android.os.Bundle
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.naman14.timber.R
import com.naman14.timber.dataloaders.LastAddedLoader.getLastAddedSongs
import com.naman14.timber.dataloaders.PlaylistLoader.getPlaylists
import com.naman14.timber.dataloaders.PlaylistSongLoader.getSongsInPlaylist
import com.naman14.timber.dataloaders.SongLoader.Companion.getSongsForCursor
import com.naman14.timber.dataloaders.TopTracksLoader
import com.naman14.timber.dataloaders.TopTracksLoader.Companion.cursor
import com.naman14.timber.models.Playlist
import com.naman14.timber.models.Song
import com.naman14.timber.utils.Constants
import com.naman14.timber.utils.NavigationUtils
import com.naman14.timber.utils.PreferencesUtility
import com.naman14.timber.utils.TimberUtils
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener
import java.util.*

class PlaylistPagerFragment : Fragment() {
    private val foregroundColors = intArrayOf(R.color.pink_transparent, R.color.green_transparent, R.color.blue_transparent, R.color.red_transparent, R.color.purple_transparent)
    private var pageNumber = 0
    private var songCountInt = 0
    private var totalRuntime = 0
    private var foregroundColor = 0
    private var firstAlbumID: Long = -1
    private var playlist: Playlist? = null
    private var playlistame: TextView? = null
    private var songcount: TextView? = null
    private var playlistnumber: TextView? = null
    private var playlisttype: TextView? = null
    private var runtime: TextView? = null
    private var playlistImage: ImageView? = null
    private var foreground: View? = null
    private var mContext: Context? = null
    private var showAuto = false
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        showAuto = PreferencesUtility.getInstance(activity).showAutoPlaylist()
        val rootView = inflater.inflate(R.layout.fragment_playlist_pager, container, false)
        val playlists: List<Playlist>? = getPlaylists(activity!!, showAuto)
        pageNumber = arguments!!.getInt(ARG_PAGE_NUMBER)
        playlist = playlists!![pageNumber]
        playlistame = rootView.findViewById<View>(R.id.name) as TextView
        playlistnumber = rootView.findViewById<View>(R.id.number) as TextView
        songcount = rootView.findViewById<View>(R.id.songcount) as TextView
        runtime = rootView.findViewById<View>(R.id.runtime) as TextView
        playlisttype = rootView.findViewById<View>(R.id.playlisttype) as TextView
        playlistImage = rootView.findViewById<View>(R.id.playlist_image) as ImageView
        foreground = rootView.findViewById(R.id.foreground)
        playlistImage!!.setOnClickListener {
            val tranitionViews = ArrayList<Pair<*, *>>()
            tranitionViews.add(0, Pair.create(playlistame as View?, "transition_playlist_name"))
            tranitionViews.add(1, Pair.create(playlistImage as View?, "transition_album_art"))
            tranitionViews.add(2, Pair.create(foreground, "transition_foreground"))
            NavigationUtils.navigateToPlaylistDetail(activity, playlistType, firstAlbumID, playlistame!!.text.toString(), foregroundColor, playlist!!.id, tranitionViews)
        }
        mContext = this.context
        setUpPlaylistDetails()
        return rootView
    }

    override fun onViewCreated(view: View, savedinstancestate: Bundle?) {
        loadPlaylistImage().execute("")
    }

    private fun setUpPlaylistDetails() {
        playlistame!!.text = playlist!!.name
        val number = arguments!!.getInt(ARG_PAGE_NUMBER) + 1
        val playlistnumberstring: String
        playlistnumberstring = if (number > 9) {
            number.toString()
        } else {
            "0$number"
        }
        playlistnumber!!.text = playlistnumberstring
        val random = Random()
        val rndInt = random.nextInt(foregroundColors.size)
        foregroundColor = foregroundColors[rndInt]
        foreground!!.setBackgroundColor(foregroundColor)
        if (showAuto) {
            if (pageNumber <= 2) playlisttype!!.visibility = View.VISIBLE
        }
    }

    private val playlistType: String
        private get() = if (showAuto) {
            when (pageNumber) {
                0 -> Constants.NAVIGATE_PLAYLIST_LASTADDED
                1 -> Constants.NAVIGATE_PLAYLIST_RECENT
                2 -> Constants.NAVIGATE_PLAYLIST_TOPTRACKS
                else -> Constants.NAVIGATE_PLAYLIST_USERCREATED
            }
        } else Constants.NAVIGATE_PLAYLIST_USERCREATED

    private inner class loadPlaylistImage : AsyncTask<String?, Void?, String>() {
        protected override fun doInBackground(vararg params: String?): String {
            return if (activity != null) {
                if (showAuto) {
                    when (pageNumber) {
                        0 -> {
                            val lastAddedSongs = getLastAddedSongs(activity!!)
                            songCountInt = lastAddedSongs.size
                            for (song in lastAddedSongs) {
                                totalRuntime += song.duration / 1000 //for some reason default playlists have songs with durations 1000x larger than they should be
                            }
                            if (songCountInt != 0) {
                                firstAlbumID = lastAddedSongs[0].albumId
                                TimberUtils.getAlbumArtUri(firstAlbumID).toString()
                            } else "nosongs"
                        }
                        1 -> {
                            val recentloader = TopTracksLoader(activity!!, TopTracksLoader.QueryType.RecentSongs)
                            val recentsongs: List<Song?> = getSongsForCursor(cursor)
                            songCountInt = recentsongs.size
                            for (song in recentsongs) {
                                totalRuntime += song!!.duration / 1000
                            }
                            if (songCountInt != 0) {
                                firstAlbumID = recentsongs[0]!!.albumId
                                TimberUtils.getAlbumArtUri(firstAlbumID).toString()
                            } else "nosongs"
                        }
                        2 -> {
                            val topTracksLoader = TopTracksLoader(activity!!, TopTracksLoader.QueryType.TopTracks)
                            val topsongs: List<Song?> = getSongsForCursor(cursor)
                            songCountInt = topsongs.size
                            for (song in topsongs) {
                                totalRuntime += song!!.duration / 1000
                            }
                            if (songCountInt != 0) {
                                firstAlbumID = topsongs[0]!!.albumId
                                TimberUtils.getAlbumArtUri(firstAlbumID).toString()
                            } else "nosongs"
                        }
                        else -> {
                            val playlistsongs = getSongsInPlaylist(activity, playlist!!.id)
                            songCountInt = playlistsongs.size
                            for (song in playlistsongs) {
                                totalRuntime += song.duration
                            }
                            if (songCountInt != 0) {
                                firstAlbumID = playlistsongs[0].albumId
                                TimberUtils.getAlbumArtUri(firstAlbumID).toString()
                            } else "nosongs"
                        }
                    }
                } else {
                    val playlistsongs = getSongsInPlaylist(activity, playlist!!.id)
                    songCountInt = playlistsongs.size
                    for (song in playlistsongs) {
                        totalRuntime += song.duration
                    }
                    if (songCountInt != 0) {
                        firstAlbumID = playlistsongs[0].albumId
                        TimberUtils.getAlbumArtUri(firstAlbumID).toString()
                    } else "nosongs"
                }
            } else "context is null"
        }

        override fun onPostExecute(uri: String) {
            ImageLoader.getInstance().displayImage(uri, playlistImage,
                    DisplayImageOptions.Builder().cacheInMemory(true)
                            .showImageOnFail(R.drawable.ic_empty_music2)
                            .resetViewBeforeLoading(true)
                            .build(), object : SimpleImageLoadingListener() {
                override fun onLoadingComplete(imageUri: String, view: View, loadedImage: Bitmap) {}
            })
            songcount!!.text = " " + songCountInt.toString() + " " + mContext!!.getString(R.string.songs)
            runtime!!.text = " " + TimberUtils.makeShortTimeString(mContext, totalRuntime.toLong())
        }

        override fun onPreExecute() {}
    }

    companion object {
        private const val ARG_PAGE_NUMBER = "pageNumber"
        fun newInstance(pageNumber: Int): PlaylistPagerFragment {
            val fragment = PlaylistPagerFragment()
            val bundle = Bundle()
            bundle.putInt(ARG_PAGE_NUMBER, pageNumber)
            fragment.arguments = bundle
            return fragment
        }
    }
}