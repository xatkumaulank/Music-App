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

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.view.*
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.palette.graphics.Palette
import com.afollestad.appthemeengine.ATE
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.naman14.timber.MusicPlayer
import com.naman14.timber.R
import com.naman14.timber.adapters.ArtistSongAdapter
import com.naman14.timber.dataloaders.ArtistLoader.getArtist
import com.naman14.timber.dataloaders.ArtistSongLoader.getSongsForArtist
import com.naman14.timber.dialogs.AddPlaylistDialog.Companion.newInstance
import com.naman14.timber.lastfmapi.LastFmClient
import com.naman14.timber.lastfmapi.callbacks.ArtistInfoListener
import com.naman14.timber.lastfmapi.models.ArtistQuery
import com.naman14.timber.lastfmapi.models.LastfmArtist
import com.naman14.timber.models.Song
import com.naman14.timber.utils.*
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener

class ArtistDetailFragment : Fragment() {
    private var artistID: Long = -1
    private var artistArt: ImageView? = null
    private var toolbar: Toolbar? = null
    private var collapsingToolbarLayout: CollapsingToolbarLayout? = null
    private var appBarLayout: AppBarLayout? = null
    private var largeImageLoaded = false
    private var primaryColor = -1
    private var mAdapter: ArtistSongAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            artistID = arguments!!.getLong(Constants.ARTIST_ID)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(
                R.layout.fragment_artist_detail, container, false)
        artistArt = rootView.findViewById<View>(R.id.artist_art) as ImageView
        collapsingToolbarLayout = rootView.findViewById<View>(R.id.collapsing_toolbar) as CollapsingToolbarLayout
        appBarLayout = rootView.findViewById<View>(R.id.app_bar) as AppBarLayout
        if (arguments!!.getBoolean("transition")) {
            artistArt!!.transitionName = arguments!!.getString("transition_name")
        }
        toolbar = rootView.findViewById<View>(R.id.toolbar) as Toolbar
        setupToolbar()
        setUpArtistDetails()
        childFragmentManager.beginTransaction().replace(R.id.container, ArtistMusicFragment.newInstance(artistID)).commit()
        return rootView
    }

    private fun setupToolbar() {
        (activity as AppCompatActivity?)!!.setSupportActionBar(toolbar)
        val ab = (activity as AppCompatActivity?)!!.supportActionBar
        ab!!.setDisplayShowTitleEnabled(false)
        ab.setDisplayHomeAsUpEnabled(true)
    }

    private fun setUpArtistDetails() {
        val artist = getArtist(activity!!, artistID)
        val songList: MutableList<Song> = getSongsForArtist(activity!!, artistID)
        mAdapter = ArtistSongAdapter(activity!!, songList, artistID)
        collapsingToolbarLayout!!.title = artist.name
        LastFmClient.getInstance(activity).getArtistInfo(ArtistQuery(artist.name), object : ArtistInfoListener {
            override fun artistInfoSucess(artist: LastfmArtist) {
                if (artist != null) {
                    ImageLoader.getInstance().displayImage(artist.mArtwork[4].mUrl, artistArt,
                            DisplayImageOptions.Builder().cacheInMemory(true)
                                    .cacheOnDisk(true)
                                    .showImageOnFail(R.drawable.ic_empty_music2)
                                    .build(), object : SimpleImageLoadingListener() {
                        override fun onLoadingComplete(imageUri: String, view: View, loadedImage: Bitmap) {
                            largeImageLoaded = true
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
                                }
                            } catch (ignored: Exception) {
                            }
                        }
                    })
                    val handler = Handler()
                    handler.postDelayed({ setBlurredPlaceholder(artist) }, 100)
                }
            }

            override fun artistInfoFailed() {}
        })
    }

    private fun setBlurredPlaceholder(artist: LastfmArtist) {
        ImageLoader.getInstance().loadImage(artist.mArtwork[1].mUrl, object : SimpleImageLoadingListener() {
            override fun onLoadingComplete(imageUri: String, view: View, loadedImage: Bitmap) {
                if (activity != null && !largeImageLoaded) setBlurredAlbumArt().execute(loadedImage)
            }
        })
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.artist_detail, menu)
        if (activity != null) ATE.applyMenu(activity!!, "dark_theme", menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.popup_song_addto_queue -> MusicPlayer.addToQueue(context, mAdapter!!.songIds, -1, TimberUtils.IdType.NA)
            R.id.popup_song_addto_playlist -> newInstance(mAdapter!!.songIds).show(activity!!.supportFragmentManager, "ADD_PLAYLIST")
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        toolbar!!.setBackgroundColor(Color.TRANSPARENT)
        if (primaryColor != -1 && activity != null) {
            collapsingToolbarLayout!!.setContentScrimColor(primaryColor)
            val ateKey = Helpers.getATEKey(activity)
            ATEUtils.setStatusBarColor(activity, ateKey, primaryColor)
        }
    }

    private inner class setBlurredAlbumArt : AsyncTask<Bitmap?, Void?, Drawable?>() {
        protected override fun doInBackground(vararg loadedImage: Bitmap?): Drawable? {
            var drawable: Drawable? = null
            try {
                drawable = ImageUtils.createBlurredImageFromBitmap(loadedImage[0], activity, 3)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return drawable
        }

        override fun onPostExecute(result: Drawable?) {
            if (result != null && !largeImageLoaded) {
                artistArt!!.setImageDrawable(result)
            }
        }

        override fun onPreExecute() {}
    }

    companion object {
        @JvmStatic
        fun newInstance(id: Long, useTransition: Boolean, transitionName: String?): ArtistDetailFragment {
            val fragment = ArtistDetailFragment()
            val args = Bundle()
            args.putLong(Constants.ARTIST_ID, id)
            args.putBoolean("transition", useTransition)
            if (useTransition) args.putString("transition_name", transitionName)
            fragment.arguments = args
            return fragment
        }
    }
}