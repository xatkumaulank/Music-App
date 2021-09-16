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
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.naman14.timber.R
import com.naman14.timber.models.Album
import com.naman14.timber.utils.ImageUtils
import com.naman14.timber.utils.NavigationUtils
import com.naman14.timber.utils.TimberUtils

class ArtistAlbumAdapter(private val mContext: Activity, private val arraylist: List<Album>?) : RecyclerView.Adapter<ArtistAlbumAdapter.ItemHolder>() {
    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ItemHolder {
        val v = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_artist_album, null)
        return ItemHolder(v)
    }

    override fun onBindViewHolder(itemHolder: ItemHolder, i: Int) {
        val localItem = arraylist!![i]
        itemHolder.title.text = localItem.title
        val songCount = TimberUtils.makeLabel(mContext, R.plurals.Nsongs, localItem.songCount)
        itemHolder.details.text = songCount
        ImageUtils.loadAlbumArtIntoView(localItem.id, itemHolder.albumArt)
        if (TimberUtils.isLollipop()) itemHolder.albumArt.transitionName = "transition_album_art$i"
    }

    override fun getItemCount(): Int {
        return arraylist?.size ?: 0
    }

    inner class ItemHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        var title: TextView
        var details: TextView
        var albumArt: ImageView
        protected var rootView: CardView
        override fun onClick(v: View) {
            NavigationUtils.navigateToAlbum(mContext, arraylist!![adapterPosition].id,
                    Pair(albumArt, "transition_album_art$adapterPosition"))
        }

        init {
            rootView = view.findViewById<View>(R.id.root_view) as CardView
            title = view.findViewById<View>(R.id.album_title) as TextView
            details = view.findViewById<View>(R.id.album_details) as TextView
            albumArt = view.findViewById<View>(R.id.album_art) as ImageView
            view.setOnClickListener(this)
        }
    }
}