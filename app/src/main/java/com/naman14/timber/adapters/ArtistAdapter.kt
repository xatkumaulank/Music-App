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
package com.naman14.timber.adapters

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.appthemeengine.Config
import com.naman14.timber.R
import com.naman14.timber.lastfmapi.LastFmClient
import com.naman14.timber.lastfmapi.callbacks.ArtistInfoListener
import com.naman14.timber.lastfmapi.models.ArtistQuery
import com.naman14.timber.lastfmapi.models.LastfmArtist
import com.naman14.timber.models.Artist
import com.naman14.timber.utils.Helpers
import com.naman14.timber.utils.NavigationUtils
import com.naman14.timber.utils.PreferencesUtility
import com.naman14.timber.utils.TimberUtils
import com.naman14.timber.widgets.BubbleTextGetter
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.assist.FailReason
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener

class ArtistAdapter(private val mContext: Activity?, private var arraylist: List<Artist>?) : RecyclerView.Adapter<ArtistAdapter.ItemHolder>(), BubbleTextGetter {
    private val isGrid: Boolean
    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ItemHolder {
        return if (isGrid) {
            val v = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_artist_grid, null)
            ItemHolder(v)
        } else {
            val v = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_artist, null)
            ItemHolder(v)
        }
    }

    override fun onBindViewHolder(itemHolder: ItemHolder, i: Int) {
        val localItem = arraylist!![i]
        itemHolder.name.text = localItem.name
        val albumNmber = TimberUtils.makeLabel(mContext, R.plurals.Nalbums, localItem.albumCount)
        val songCount = TimberUtils.makeLabel(mContext, R.plurals.Nsongs, localItem.songCount)
        itemHolder.albums.text = TimberUtils.makeCombinedString(mContext, albumNmber, songCount)
        LastFmClient.getInstance(mContext).getArtistInfo(ArtistQuery(localItem.name), object : ArtistInfoListener {
            override fun artistInfoSucess(artist: LastfmArtist) {
                if (artist != null && artist.mArtwork != null) {
                    if (isGrid) {
                        ImageLoader.getInstance().displayImage(artist.mArtwork[2].mUrl, itemHolder.artistImage,
                                DisplayImageOptions.Builder().cacheInMemory(true)
                                        .cacheOnDisk(true)
                                        .showImageOnLoading(R.drawable.ic_empty_music2)
                                        .resetViewBeforeLoading(true)
                                        .displayer(FadeInBitmapDisplayer(400))
                                        .build(), object : SimpleImageLoadingListener() {
                            override fun onLoadingComplete(imageUri: String, view: View, loadedImage: Bitmap) {
                                if (isGrid && loadedImage != null) {
                                    Palette.Builder(loadedImage).generate { palette ->
                                        val color = palette!!.getVibrantColor(Color.parseColor("#66000000"))
                                        itemHolder.footer.setBackgroundColor(color)
                                        val swatch = palette.vibrantSwatch
                                        val textColor: Int
                                        textColor = if (swatch != null) {
                                            getOpaqueColor(swatch.titleTextColor)
                                        } else Color.parseColor("#ffffff")
                                        itemHolder.name.setTextColor(textColor)
                                        itemHolder.albums.setTextColor(textColor)
                                    }
                                }
                            }

                            override fun onLoadingFailed(imageUri: String, view: View, failReason: FailReason) {
                                if (isGrid) {
                                    itemHolder.footer.setBackgroundColor(0)
                                    if (mContext != null) {
                                        val textColorPrimary = Config.textColorPrimary(mContext, Helpers.getATEKey(mContext))
                                        itemHolder.name.setTextColor(textColorPrimary)
                                        itemHolder.albums.setTextColor(textColorPrimary)
                                    }
                                }
                            }
                        })
                    } else {
                        ImageLoader.getInstance().displayImage(artist.mArtwork[1].mUrl, itemHolder.artistImage,
                                DisplayImageOptions.Builder().cacheInMemory(true)
                                        .cacheOnDisk(true)
                                        .showImageOnLoading(R.drawable.ic_empty_music2)
                                        .resetViewBeforeLoading(true)
                                        .displayer(FadeInBitmapDisplayer(400))
                                        .build())
                    }
                }
            }

            override fun artistInfoFailed() {}
        })
        if (TimberUtils.isLollipop()) itemHolder.artistImage.transitionName = "transition_artist_art$i"
    }

    override fun getItemId(position: Int): Long {
        return arraylist!![position].id
    }

    override fun getItemCount(): Int {
        return if (null != arraylist) arraylist!!.size else 0
    }

    override fun getTextToShowInBubble(pos: Int): String {
        return if (arraylist == null || arraylist!!.size == 0) "" else Character.toString(arraylist!![pos].name[0])
    }

    fun updateDataSet(arrayList: List<Artist>?) {
        arraylist = arrayList
    }

    inner class ItemHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        var name: TextView
        var albums: TextView
        var artistImage: ImageView
        var footer: View
        override fun onClick(v: View) {
            NavigationUtils.navigateToArtist(mContext, arraylist!![adapterPosition].id,
                    Pair(artistImage, "transition_artist_art$adapterPosition"))
        }

        init {
            name = view.findViewById<View>(R.id.artist_name) as TextView
            albums = view.findViewById<View>(R.id.album_song_count) as TextView
            artistImage = view.findViewById<View>(R.id.artistImage) as ImageView
            footer = view.findViewById(R.id.footer)
            view.setOnClickListener(this)
        }
    }

    companion object {
        fun getOpaqueColor(@ColorInt paramInt: Int): Int {
            return -0x1000000 or paramInt
        }
    }

    init {
        isGrid = PreferencesUtility.getInstance(mContext).isArtistsInGrid
    }
}