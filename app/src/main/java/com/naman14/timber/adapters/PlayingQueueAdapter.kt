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
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.appthemeengine.Config
import com.naman14.timber.MusicPlayer
import com.naman14.timber.R
import com.naman14.timber.dialogs.AddPlaylistDialog
import com.naman14.timber.models.Song
import com.naman14.timber.utils.Helpers
import com.naman14.timber.utils.NavigationUtils
import com.naman14.timber.utils.TimberUtils
import com.naman14.timber.widgets.MusicVisualizer
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader

class PlayingQueueAdapter(private val mContext: Activity, private val arraylist: MutableList<Song>?) : RecyclerView.Adapter<PlayingQueueAdapter.ItemHolder>() {
    @JvmField
    var currentlyPlayingPosition: Int
    private val ateKey: String
    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ItemHolder {
        val v = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_playing_queue, null)
        return ItemHolder(v)
    }

    override fun onBindViewHolder(itemHolder: ItemHolder, i: Int) {
        val localItem = arraylist!![i]
        itemHolder.title.text = localItem.title
        itemHolder.artist.text = localItem.artistName
        if (MusicPlayer.getCurrentAudioId() == localItem.id) {
            itemHolder.title.setTextColor(Config.accentColor(mContext, ateKey))
            if (MusicPlayer.isPlaying()) {
                itemHolder.visualizer.setColor(Config.accentColor(mContext, ateKey))
                itemHolder.visualizer.visibility = View.VISIBLE
            } else {
                itemHolder.visualizer.visibility = View.GONE
            }
        } else {
            itemHolder.title.setTextColor(Config.textColorPrimary(mContext, ateKey))
            itemHolder.visualizer.visibility = View.GONE
        }
        ImageLoader.getInstance().displayImage(TimberUtils.getAlbumArtUri(localItem.albumId).toString(),
                itemHolder.albumArt, DisplayImageOptions.Builder().cacheInMemory(true)
                .showImageOnLoading(R.drawable.ic_empty_music2).resetViewBeforeLoading(true).build())
        setOnPopupMenuListener(itemHolder, i)
    }

    private fun setOnPopupMenuListener(itemHolder: ItemHolder, position: Int) {
        itemHolder.menu.setOnClickListener { v ->
            val menu = PopupMenu(mContext, v)
            menu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.popup_song_remove_queue -> {
                        Log.v(TAG, "Removing $position")
                        MusicPlayer.removeTrackAtPosition(getSongAt(position).id, position)
                        removeSongAt(position)
                        notifyItemRemoved(position)
                    }
                    R.id.popup_song_play -> MusicPlayer.playAll(mContext, songIds, position, -1, TimberUtils.IdType.NA, false)
                    R.id.popup_song_goto_album -> NavigationUtils.goToAlbum(mContext, arraylist!![position].albumId)
                    R.id.popup_song_goto_artist -> NavigationUtils.goToArtist(mContext, arraylist!![position].artistId)
                    R.id.popup_song_addto_playlist -> AddPlaylistDialog.newInstance(arraylist!![position]).show((mContext as AppCompatActivity).supportFragmentManager, "ADD_PLAYLIST")
                }
                false
            }
            menu.inflate(R.menu.popup_playing_queue)
            menu.show()
        }
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

    fun getSongAt(i: Int): Song {
        return arraylist!![i]
    }

    fun addSongTo(i: Int, song: Song) {
        arraylist?.add(i, song)
    }

    fun removeSongAt(i: Int) {
        arraylist?.removeAt(i)
    }

    inner class ItemHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        var title: TextView
        var artist: TextView
        var albumArt: ImageView
        protected var reorder: ImageView
        var menu: ImageView
        val visualizer: MusicVisualizer
        override fun onClick(v: View) {
            val handler = Handler()
            handler.postDelayed({
                MusicPlayer.setQueuePosition(adapterPosition)
                val handler1 = Handler()
                handler1.postDelayed({
                    notifyItemChanged(currentlyPlayingPosition)
                    notifyItemChanged(adapterPosition)
                }, 50)
            }, 100)
        }

        init {
            title = view.findViewById<View>(R.id.song_title) as TextView
            artist = view.findViewById<View>(R.id.song_artist) as TextView
            albumArt = view.findViewById<View>(R.id.albumArt) as ImageView
            menu = view.findViewById<View>(R.id.popup_menu) as ImageView
            reorder = view.findViewById<View>(R.id.reorder) as ImageView
            visualizer = view.findViewById<View>(R.id.visualizer) as MusicVisualizer
            view.setOnClickListener(this)
        }
    }

    companion object {
        private const val TAG = "PlayingQueueAdapter"
    }

    init {
        currentlyPlayingPosition = MusicPlayer.getQueuePosition()
        ateKey = Helpers.getATEKey(mContext)
    }
}