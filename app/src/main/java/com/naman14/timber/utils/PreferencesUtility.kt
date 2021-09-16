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
package com.naman14.timber.utils

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Environment
import android.preference.PreferenceManager
import com.naman14.timber.MusicPlayer
import com.naman14.timber.MusicService

class PreferencesUtility(context: Context) {
    private var connManager: ConnectivityManager? = null
    fun setOnSharedPreferenceChangeListener(listener: OnSharedPreferenceChangeListener?) {
        mPreferences?.registerOnSharedPreferenceChangeListener(listener)
    }

    var isArtistsInGrid: Boolean
        get() = mPreferences!!.getBoolean(TOGGLE_ARTIST_GRID, true)
        set(b) {
            val editor = mPreferences?.edit()
            editor?.putBoolean(TOGGLE_ARTIST_GRID, b)
            editor?.apply()
        }
    var isAlbumsInGrid: Boolean
        get() = mPreferences!!.getBoolean(TOGGLE_ALBUM_GRID, true)
        set(b) {
            val editor = mPreferences?.edit()
            editor?.putBoolean(TOGGLE_ALBUM_GRID, b)
            editor?.apply()
        }

    fun pauseEnabledOnDetach(): Boolean {
        return mPreferences!!.getBoolean(TOGGLE_HEADPHONE_PAUSE, true)
    }

    val theme: String
        get() = mPreferences!!.getString(THEME_PREFERNCE, "light")
    var startPageIndex: Int
        get() = mPreferences!!.getInt(START_PAGE_INDEX, 0)
        set(index) {
            val editor = mPreferences?.edit()
            editor?.putInt(START_PAGE_INDEX, index)
            editor?.apply()
        }

    fun setLastOpenedAsStartPagePreference(preference: Boolean) {
        val editor = mPreferences?.edit()
        editor?.putBoolean(START_PAGE_PREFERENCE_LASTOPENED, preference)
        editor?.apply()
    }

    fun lastOpenedIsStartPagePreference(): Boolean {
        return mPreferences!!.getBoolean(START_PAGE_PREFERENCE_LASTOPENED, true)
    }

    private fun setSortOrder(key: String, value: String) {
        val editor = mPreferences?.edit()
        editor?.putString(key, value)
        editor?.apply()
    }

    var artistSortOrder: String
        get() = mPreferences!!.getString(ARTIST_SORT_ORDER, SortOrder.ArtistSortOrder.ARTIST_A_Z)
        set(value) {
            setSortOrder(ARTIST_SORT_ORDER, value)
        }
    var artistSongSortOrder: String
        get() = mPreferences!!.getString(ARTIST_SONG_SORT_ORDER,
                SortOrder.ArtistSongSortOrder.SONG_A_Z)
        set(value) {
            setSortOrder(ARTIST_SONG_SORT_ORDER, value)
        }
    var artistAlbumSortOrder: String
        get() = mPreferences!!.getString(ARTIST_ALBUM_SORT_ORDER,
                SortOrder.ArtistAlbumSortOrder.ALBUM_A_Z)
        set(value) {
            setSortOrder(ARTIST_ALBUM_SORT_ORDER, value)
        }
    var albumSortOrder: String
        get() = mPreferences!!.getString(ALBUM_SORT_ORDER, SortOrder.AlbumSortOrder.ALBUM_A_Z)
        set(value) {
            setSortOrder(ALBUM_SORT_ORDER, value)
        }
    var albumSongSortOrder: String
        get() = mPreferences!!.getString(ALBUM_SONG_SORT_ORDER,
                SortOrder.AlbumSongSortOrder.SONG_TRACK_LIST)
        set(value) {
            setSortOrder(ALBUM_SONG_SORT_ORDER, value)
        }
    var songSortOrder: String
        get() = mPreferences!!.getString(SONG_SORT_ORDER, SortOrder.SongSortOrder.SONG_A_Z)
        set(value) {
            setSortOrder(SONG_SORT_ORDER, value)
        }

    fun didNowplayingThemeChanged(): Boolean {
        return mPreferences!!.getBoolean(NOW_PLAYNG_THEME_VALUE, false)
    }

    fun setNowPlayingThemeChanged(value: Boolean) {
        val editor = mPreferences?.edit()
        editor?.putBoolean(NOW_PLAYNG_THEME_VALUE, value)
        editor?.apply()
    }

    val xPosedTrackselectorEnabled: Boolean
        get() = mPreferences!!.getBoolean(TOGGLE_XPOSED_TRACKSELECTOR, false)
    var playlistView: Int
        get() = mPreferences!!.getInt(TOGGLE_PLAYLIST_VIEW, 0)
        set(i) {
            val editor = mPreferences?.edit()
            editor?.putInt(TOGGLE_PLAYLIST_VIEW, i)
            editor?.apply()
        }

    fun showAutoPlaylist(): Boolean {
        return mPreferences!!.getBoolean(TOGGLE_SHOW_AUTO_PLAYLIST, true)
    }

    fun setToggleShowAutoPlaylist(b: Boolean) {
        val editor = mPreferences?.edit()
        editor!!.putBoolean(TOGGLE_SHOW_AUTO_PLAYLIST, b)
        editor!!.apply()
    }

    /** @parm lastAddedMillis timestamp in millis used as a cutoff for last added playlist
     */
    var lastAddedCutoff: Long
        get() = mPreferences!!.getLong(LAST_ADDED_CUTOFF, 0L)
        set(lastAddedMillis) {
            mPreferences?.edit()!!.putLong(LAST_ADDED_CUTOFF, lastAddedMillis).apply()
        }
    val isGesturesEnabled: Boolean
        get() = mPreferences!!.getBoolean(GESTURES, true)

    fun storeLastFolder(path: String?) {
        val editor = mPreferences!!.edit()
        editor.putString(LAST_FOLDER, path)
        editor.apply()
    }

    val lastFolder: String
        get() = mPreferences!!.getString(LAST_FOLDER, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).path)

    fun fullUnlocked(): Boolean {
        return mPreferences!!.getBoolean(FULL_UNLOCKED, false)
    }

    fun setFullUnlocked(b: Boolean) {
        val editor = mPreferences?.edit()
        editor?.putBoolean(FULL_UNLOCKED, b)
        editor?.apply()
    }

    val setAlbumartLockscreen: Boolean
        get() = mPreferences!!.getBoolean(SHOW_LOCKSCREEN_ALBUMART, true)

    fun updateService(extras: Bundle?) {
        if (!MusicPlayer.isPlaybackServiceConnected()) return
        val intent = Intent(context, MusicService::class.java)
        intent.action = MusicService.UPDATE_PREFERENCES
        intent.putExtras(extras)
        context?.startService(intent)
    }

    fun loadArtistAndAlbumImages(): Boolean {
        if (mPreferences!!.getBoolean(ARTIST_ALBUM_IMAGE, true)) {
            if (!mPreferences!!.getBoolean(ARTIST_ALBUM_IMAGE_MOBILE, true)) {
                if (connManager == null) connManager = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val ni = connManager!!.activeNetworkInfo
                return ni != null && ni.type == ConnectivityManager.TYPE_WIFI
            }
            return true
        }
        return false
    }

    fun alwaysLoadAlbumImagesFromLastfm(): Boolean {
        return mPreferences!!.getBoolean(ALWAYS_LOAD_ALBUM_IMAGES_LASTFM, false)
    }

    companion object {
        const val ARTIST_SORT_ORDER = "artist_sort_order"
        const val ARTIST_SONG_SORT_ORDER = "artist_song_sort_order"
        const val ARTIST_ALBUM_SORT_ORDER = "artist_album_sort_order"
        const val ALBUM_SORT_ORDER = "album_sort_order"
        const val ALBUM_SONG_SORT_ORDER = "album_song_sort_order"
        const val SONG_SORT_ORDER = "song_sort_order"
        private const val NOW_PLAYING_SELECTOR = "now_paying_selector"
        private const val TOGGLE_ANIMATIONS = "toggle_animations"
        private const val TOGGLE_SYSTEM_ANIMATIONS = "toggle_system_animations"
        private const val TOGGLE_ARTIST_GRID = "toggle_artist_grid"
        private const val TOGGLE_ALBUM_GRID = "toggle_album_grid"
        private const val TOGGLE_PLAYLIST_VIEW = "toggle_playlist_view"
        private const val TOGGLE_SHOW_AUTO_PLAYLIST = "toggle_show_auto_playlist"
        private const val LAST_FOLDER = "last_folder"
        private const val TOGGLE_HEADPHONE_PAUSE = "toggle_headphone_pause"
        private const val THEME_PREFERNCE = "theme_preference"
        private const val START_PAGE_INDEX = "start_page_index"
        private const val START_PAGE_PREFERENCE_LASTOPENED = "start_page_preference_latopened"
        private const val NOW_PLAYNG_THEME_VALUE = "now_playing_theme_value"
        private const val TOGGLE_XPOSED_TRACKSELECTOR = "toggle_xposed_trackselector"
        const val LAST_ADDED_CUTOFF = "last_added_cutoff"
        const val GESTURES = "gestures"
        const val FULL_UNLOCKED = "full_version_unlocked"
        private const val SHOW_LOCKSCREEN_ALBUMART = "show_albumart_lockscreen"
        private const val ARTIST_ALBUM_IMAGE = "artist_album_image"
        private const val ARTIST_ALBUM_IMAGE_MOBILE = "artist_album_image_mobile"
        private const val ALWAYS_LOAD_ALBUM_IMAGES_LASTFM = "always_load_album_images_lastfm"
        private var sInstance: PreferencesUtility? = null
        private var mPreferences: SharedPreferences? = null
        private val context: Context? = null
        @JvmStatic
        fun getInstance(context: Context): PreferencesUtility? {
            if (sInstance == null) {
                sInstance = PreferencesUtility(context.applicationContext)
            }
            return sInstance
        }
    }

    init {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    }
}