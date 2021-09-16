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

import android.annotation.TargetApi
import android.app.Activity
import android.app.ActivityOptions
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.util.Pair
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.naman14.timber.R
import com.naman14.timber.activities.*
import com.naman14.timber.fragments.AlbumDetailFragment
import com.naman14.timber.fragments.ArtistDetailFragment
import com.naman14.timber.nowplaying.*
import java.util.*

object NavigationUtils {
    @TargetApi(21)
    fun navigateToAlbum(context: Activity, albumID: Long, transitionViews: Pair<View?, String?>?) {
        val transaction = (context as AppCompatActivity).supportFragmentManager.beginTransaction()
        val fragment: Fragment
        transaction.setCustomAnimations(R.anim.activity_fade_in,
                R.anim.activity_fade_out, R.anim.activity_fade_in, R.anim.activity_fade_out)
        fragment = AlbumDetailFragment.newInstance(albumID, false, null)
        transaction.hide(context.supportFragmentManager.findFragmentById(R.id.fragment_container)!!)
        transaction.add(R.id.fragment_container, fragment)
        transaction.addToBackStack(null).commit()
    }

    @TargetApi(21)
    fun navigateToArtist(context: Activity, artistID: Long, transitionViews: Pair<View?, String?>?) {
        val transaction = (context as AppCompatActivity).supportFragmentManager.beginTransaction()
        val fragment: Fragment
        transaction.setCustomAnimations(R.anim.activity_fade_in,
                R.anim.activity_fade_out, R.anim.activity_fade_in, R.anim.activity_fade_out)
        fragment = ArtistDetailFragment.newInstance(artistID, false, null)
        transaction.hide(context.supportFragmentManager.findFragmentById(R.id.fragment_container)!!)
        transaction.add(R.id.fragment_container, fragment)
        transaction.addToBackStack(null).commit()
    }

    fun goToArtist(context: Context, artistId: Long) {
        val intent = Intent(context, MainActivity::class.java)
        intent.action = Constants.NAVIGATE_ARTIST
        intent.putExtra(Constants.ARTIST_ID, artistId)
        context.startActivity(intent)
    }

    fun goToAlbum(context: Context, albumId: Long) {
        val intent = Intent(context, MainActivity::class.java)
        intent.action = Constants.NAVIGATE_ALBUM
        intent.putExtra(Constants.ALBUM_ID, albumId)
        context.startActivity(intent)
    }

    fun goToLyrics(context: Context) {
        val intent = Intent(context, MainActivity::class.java)
        intent.action = Constants.NAVIGATE_LYRICS
        context.startActivity(intent)
    }

    fun navigateToNowplaying(context: Activity, withAnimations: Boolean) {
        val intent = Intent(context, NowPlayingActivity::class.java)
        context.startActivity(intent)
    }

    @JvmStatic
    fun getNowPlayingIntent(context: Context?): Intent {
        val intent = Intent(context, MainActivity::class.java)
        intent.action = Constants.NAVIGATE_NOWPLAYING
        return intent
    }

    fun navigateToSettings(context: Activity) {
        val intent = Intent(context, SettingsActivity::class.java)
        intent.action = Constants.NAVIGATE_SETTINGS
        context.startActivity(intent)
    }

    fun navigateToSearch(context: Activity) {
        val intent = Intent(context, SearchActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
        intent.action = Constants.NAVIGATE_SEARCH
        context.startActivity(intent)
    }

    @TargetApi(21)
    fun navigateToPlaylistDetail(context: Activity, action: String?, firstAlbumID: Long, playlistName: String?, foregroundcolor: Int, playlistID: Long, transitionViews: ArrayList<Pair<*, *>?>?) {
        val intent = Intent(context, PlaylistDetailActivity::class.java)
        intent.action = action
        intent.putExtra(Constants.PLAYLIST_ID, playlistID)
        intent.putExtra(Constants.PLAYLIST_FOREGROUND_COLOR, foregroundcolor)
        intent.putExtra(Constants.ALBUM_ID, firstAlbumID)
        intent.putExtra(Constants.PLAYLIST_NAME, playlistName)
        intent.putExtra(Constants.ACTIVITY_TRANSITION, transitionViews != null)
        if (transitionViews != null && TimberUtils.isLollipop()) {
            val options = ActivityOptions.makeSceneTransitionAnimation(context, transitionViews[0] as Pair<View, String>?, transitionViews[1] as Pair<View, String>?, transitionViews[2] as Pair<View, String>?)
            context.startActivityForResult(intent, Constants.ACTION_DELETE_PLAYLIST, options.toBundle())
        } else {
            context.startActivityForResult(intent, Constants.ACTION_DELETE_PLAYLIST)
        }
    }

    fun navigateToEqualizer(context: Activity) {
        try {
            // The google MusicFX apps need to be started using startActivityForResult
            context.startActivityForResult(TimberUtils.createEffectsIntent(), 666)
        } catch (notFound: ActivityNotFoundException) {
            Toast.makeText(context, "Equalizer not found", Toast.LENGTH_SHORT).show()
        }
    }

    fun getNavigateToStyleSelectorIntent(context: Activity?, what: String?): Intent {
        val intent = Intent(context, SettingsActivity::class.java)
        intent.action = Constants.SETTINGS_STYLE_SELECTOR
        intent.putExtra(Constants.SETTINGS_STYLE_SELECTOR_WHAT, what)
        return intent
    }

    fun getFragmentForNowplayingID(fragmentID: String?): Fragment {
        return when (fragmentID) {
            Constants.TIMBER1 -> Timber1()
            Constants.TIMBER2 -> Timber2()
            Constants.TIMBER3 -> Timber3()
            Constants.TIMBER4 -> Timber4()
            Constants.TIMBER5 -> Timber5()
            Constants.TIMBER6 -> Timber6()
            else -> Timber1()
        }
    }

    fun getIntForCurrentNowplaying(nowPlaying: String?): Int {
        return when (nowPlaying) {
            Constants.TIMBER1 -> 0
            Constants.TIMBER2 -> 1
            Constants.TIMBER3 -> 2
            Constants.TIMBER4 -> 3
            Constants.TIMBER5 -> 4
            Constants.TIMBER6 -> 5
            else -> 2
        }
    }
}