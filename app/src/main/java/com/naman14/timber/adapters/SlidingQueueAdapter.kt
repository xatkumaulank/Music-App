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
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.naman14.timber.MusicPlayer
import com.naman14.timber.R
import com.naman14.timber.models.Song
import com.naman14.timber.utils.TimberUtils
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader

class SlidingQueueAdapter(private val mContext: Activity, private val arraylist: List<Song>?) : RecyclerView.Adapter<SlidingQueueAdapter.ItemHolder>() {
    private var lastPosition = -1
    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ItemHolder {
        val v = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_song_sliding_queue, null)
        return ItemHolder(v)
    }

    override fun onBindViewHolder(itemHolder: ItemHolder, i: Int) {

//        setAnimation(itemHolder.itemView, i);
        val localItem = arraylist!![i]
        ImageLoader.getInstance().displayImage(TimberUtils.getAlbumArtUri(localItem.albumId).toString(),
                itemHolder.albumArt, DisplayImageOptions.Builder().cacheInMemory(true)
                .showImageOnLoading(R.drawable.ic_empty_music2).resetViewBeforeLoading(true).build())
    }

    override fun getItemCount(): Int {
        return arraylist?.size ?: 0
    }

    val songIds: LongArray
        get() {
            val ret = LongArray(itemCount)
            for (i in 0 until itemCount) {
                ret[i] = arraylist!![i].id
            }
            return ret
        }

    private fun setAnimation(viewToAnimate: View, position: Int) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition) {
            val animation = AnimationUtils.loadAnimation(mContext, R.anim.scale)
            viewToAnimate.startAnimation(animation)
            lastPosition = position
        }
    }

    inner class ItemHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        var albumArt: ImageView
        override fun onClick(v: View) {
            val handler = Handler()
            handler.postDelayed({
                MusicPlayer.setQueuePosition(adapterPosition)
                val handler1 = Handler()
                handler1.postDelayed({
                    notifyItemChanged(currentlyPlayingPosition)
                    notifyItemChanged(adapterPosition)
                    val handler2 = Handler()
                    handler2.postDelayed({ }, 50)
                }, 50)
            }, 100)
        }

        init {
            albumArt = view.findViewById<View>(R.id.album_art) as ImageView
            view.setOnClickListener(this)
        }
    }

    companion object {
        var currentlyPlayingPosition: Int = 0
    }

    init {
        currentlyPlayingPosition = MusicPlayer.getQueuePosition()
    }
}