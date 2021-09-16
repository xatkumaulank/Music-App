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
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.naman14.timber.MusicPlayer
import com.naman14.timber.R
import com.naman14.timber.dialogs.AddPlaylistDialog
import com.naman14.timber.models.Song
import com.naman14.timber.utils.NavigationUtils
import com.naman14.timber.utils.TimberUtils

class AlbumSongsAdapter(private val mContext: Activity, private var arraylist: MutableList<Song>?, albumID: Long) : BaseSongAdapter<AlbumSongsAdapter.ItemHolder?>() {
    private val albumID: Long
    private var songIDs: LongArray
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ItemHolder {
        val v = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_album_song, null)
        return ItemHolder(v)
    }

    override fun onBindViewHolder(itemHolder: ItemHolder, i: Int) {
        val localItem = arraylist!![i]
        itemHolder.title.text = localItem.title
        itemHolder.duration.text = TimberUtils.makeShortTimeString(mContext, (localItem.duration / 1000).toLong())
        val tracknumber = localItem.trackNumber
        if (tracknumber == 0) {
            itemHolder.trackNumber.text = "-"
        } else itemHolder.trackNumber.text = tracknumber.toString()
        setOnPopupMenuListener(itemHolder, i)
    }

    private fun setOnPopupMenuListener(itemHolder: ItemHolder, position: Int) {
        itemHolder.menu.setOnClickListener { v ->
            val menu = PopupMenu(mContext, v)
            menu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.popup_song_play -> MusicPlayer.playAll(mContext, songIDs, position, -1, TimberUtils.IdType.NA, false)
                    R.id.popup_song_play_next -> {
                        val ids = LongArray(1)
                        ids[0] = arraylist!![position].id
                        MusicPlayer.playNext(mContext, ids, -1, TimberUtils.IdType.NA)
                    }
                    R.id.popup_song_goto_album -> NavigationUtils.goToAlbum(mContext, arraylist!![position].albumId)
                    R.id.popup_song_goto_artist -> NavigationUtils.goToArtist(mContext, arraylist!![position].artistId)
                    R.id.popup_song_addto_queue -> {
                        val id = LongArray(1)
                        id[0] = arraylist!![position].id
                        MusicPlayer.addToQueue(mContext, id, -1, TimberUtils.IdType.NA)
                    }
                    R.id.popup_song_addto_playlist -> AddPlaylistDialog.newInstance(arraylist!![position]).show((mContext as AppCompatActivity).supportFragmentManager, "ADD_PLAYLIST")
                    R.id.popup_song_share -> TimberUtils.shareTrack(mContext, arraylist!![position].id)
                    R.id.popup_song_delete -> {
                        val deleteIds = longArrayOf(arraylist!![position].id)
                        TimberUtils.showDeleteDialog(mContext, arraylist!![position].title, deleteIds, this@AlbumSongsAdapter, position)
                    }
                }
                false
            }
            menu.inflate(R.menu.popup_song)
            menu.show()
        }
    }

    override fun getItemCount(): Int {
        return if (null != arraylist) arraylist!!.size else 0
    }

    val songIds: LongArray
        get() {
            val ret = LongArray(itemCount)
            for (i in 0 until itemCount) {
                ret[i] = arraylist!![i].id
            }
            return ret
        }

    override fun updateDataSet(arraylist: MutableList<Song>) {
        this.arraylist = arraylist
        songIDs = songIds
    }

    override fun removeSongAt(i: Int) {
        arraylist?.removeAt(i)
    }

    inner class ItemHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        var title: TextView
        var duration: TextView
        var trackNumber: TextView
        var menu: ImageView
        override fun onClick(v: View) {
            val handler = Handler()
            handler.postDelayed({
                playAll(mContext, songIDs, adapterPosition, albumID,
                        TimberUtils.IdType.Album, false,
                        arraylist!![adapterPosition], true)
            }, 100)
        }

        init {
            title = view.findViewById<View>(R.id.song_title) as TextView
            duration = view.findViewById<View>(R.id.song_duration) as TextView
            trackNumber = view.findViewById<View>(R.id.trackNumber) as TextView
            menu = view.findViewById<View>(R.id.popup_menu) as ImageView
            view.setOnClickListener(this)
        }
    }

    init {
        songIDs = songIds
        this.albumID = albumID
    }
}