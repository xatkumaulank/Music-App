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
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.appthemeengine.Config
import com.naman14.timber.R
import com.naman14.timber.models.Album
import com.naman14.timber.utils.Helpers
import com.naman14.timber.utils.NavigationUtils
import com.naman14.timber.utils.PreferencesUtility
import com.naman14.timber.utils.TimberUtils
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.assist.FailReason
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener

class AlbumAdapter(private val mContext: Activity?, private var arraylist: List<Album>?) : RecyclerView.Adapter<AlbumAdapter.ItemHolder>() {
    private val isGrid: Boolean
    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ItemHolder {
        return if (isGrid) {
            val v = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_album_grid, null)
            ItemHolder(v)
        } else {
            val v = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_album_list, null)
            ItemHolder(v)
        }
    }

    override fun onBindViewHolder(itemHolder: ItemHolder, i: Int) {
        val localItem = arraylist!![i]
        itemHolder.title.text = localItem.title
        itemHolder.artist.text = localItem.artistName
        ImageLoader.getInstance().displayImage(TimberUtils.getAlbumArtUri(localItem.id).toString(), itemHolder.albumArt,
                DisplayImageOptions.Builder().cacheInMemory(true)
                        .showImageOnLoading(R.drawable.ic_empty_music2)
                        .resetViewBeforeLoading(true)
                        .displayer(FadeInBitmapDisplayer(400))
                        .build(), object : SimpleImageLoadingListener() {
            override fun onLoadingComplete(imageUri: String, view: View, loadedImage: Bitmap) {
                if (isGrid) {
                    Palette.Builder(loadedImage).generate { palette ->
                        val swatch = palette!!.vibrantSwatch
                        if (swatch != null) {
                            val color = swatch.rgb
                            itemHolder.footer.setBackgroundColor(color)
                            val textColor = TimberUtils.getBlackWhiteColor(swatch.titleTextColor)
                            itemHolder.title.setTextColor(textColor)
                            itemHolder.artist.setTextColor(textColor)
                        } else {
                            val mutedSwatch = palette.mutedSwatch
                            if (mutedSwatch != null) {
                                val color = mutedSwatch.rgb
                                itemHolder.footer.setBackgroundColor(color)
                                val textColor = TimberUtils.getBlackWhiteColor(mutedSwatch.titleTextColor)
                                itemHolder.title.setTextColor(textColor)
                                itemHolder.artist.setTextColor(textColor)
                            }
                        }
                    }
                }
            }

            override fun onLoadingFailed(imageUri: String, view: View, failReason: FailReason) {
                if (isGrid) {
                    itemHolder.footer.setBackgroundColor(0)
                    if (mContext != null) {
                        val textColorPrimary = Config.textColorPrimary(mContext, Helpers.getATEKey(mContext))
                        itemHolder.title.setTextColor(textColorPrimary)
                        itemHolder.artist.setTextColor(textColorPrimary)
                    }
                }
            }
        })
        if (TimberUtils.isLollipop()) itemHolder.albumArt.transitionName = "transition_album_art$i"
    }

    override fun getItemCount(): Int {
        return if (null != arraylist) arraylist!!.size else 0
    }

    fun updateDataSet(arraylist: List<Album>?) {
        this.arraylist = arraylist
    }

    inner class ItemHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        var title: TextView
        var artist: TextView
        var albumArt: ImageView
        var footer: View
        override fun onClick(v: View) {
            NavigationUtils.navigateToAlbum(mContext, arraylist!![adapterPosition].id,
                    Pair(albumArt, "transition_album_art$adapterPosition"))
        }

        init {
            title = view.findViewById<View>(R.id.album_title) as TextView
            artist = view.findViewById<View>(R.id.album_artist) as TextView
            albumArt = view.findViewById<View>(R.id.album_art) as ImageView
            footer = view.findViewById(R.id.footer)
            view.setOnClickListener(this)
        }
    }

    init {
        isGrid = PreferencesUtility.getInstance(mContext).isAlbumsInGrid
    }
}