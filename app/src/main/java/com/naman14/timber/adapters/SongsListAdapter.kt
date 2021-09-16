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

import android.graphics.Color
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
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
import com.naman14.timber.widgets.BubbleTextGetter
import com.naman14.timber.widgets.MusicVisualizer
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader

class SongsListAdapter(private val mContext: AppCompatActivity, private var arraylist: MutableList<Song?>?, private val isPlaylist: Boolean, animate: Boolean) : BaseSongAdapter<SongsListAdapter.ItemHolder?>(), BubbleTextGetter {
    var currentlyPlayingPosition = 0
    private var songIDs: LongArray
    private val animate: Boolean
    private var lastPosition = -1
    private val ateKey: String
    private var playlistId: Long = 0
    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ItemHolder {
        return if (isPlaylist) {
            val v = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_song_playlist, null)
            ItemHolder(v)
        } else {
            val v = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_song, null)
            ItemHolder(v)
        }
    }

    override fun onBindViewHolder(itemHolder: ItemHolder?, i: Int) {
        val localItem = arraylist!![i]
        if (itemHolder != null) {
            itemHolder.title.text = localItem!!.title
        }
        if (itemHolder != null) {
            if (localItem != null) {
                itemHolder.artist.text = localItem.artistName
            }
        }
        if (localItem != null) {
            if (itemHolder != null) {
                ImageLoader.getInstance().displayImage(TimberUtils.getAlbumArtUri(localItem.albumId).toString(),
                        itemHolder.albumArt, DisplayImageOptions.Builder().cacheInMemory(true)
                        .showImageOnLoading(R.drawable.ic_empty_music2)
                        .resetViewBeforeLoading(true).build())
            }
        }
        if (localItem != null) {
            if (MusicPlayer.getCurrentAudioId() == localItem.id) {
                itemHolder?.title?.setTextColor(Config.accentColor(mContext, ateKey))
                if (MusicPlayer.isPlaying()) {
                    itemHolder?.visualizer?.setColor(Config.accentColor(mContext, ateKey))
                    if (itemHolder != null) {
                        itemHolder.visualizer.visibility = View.VISIBLE
                    }
                } else {
                    itemHolder?.visualizer?.visibility = View.GONE
                }
            } else {
                itemHolder?.visualizer?.visibility = View.GONE
                if (isPlaylist) {
                    itemHolder?.title?.setTextColor(Color.WHITE)
                } else {
                    itemHolder?.title?.setTextColor(Config.textColorPrimary(mContext, ateKey))
                }
            }
        }
        if (animate && isPlaylist) {
            if (TimberUtils.isLollipop()) setAnimation(itemHolder!!.itemView, i) else {
                if (i > 10) setAnimation(itemHolder!!.itemView, i)
            }
        }
        if (itemHolder != null) {
            setOnPopupMenuListener(itemHolder, i)
        }
    }

    fun setPlaylistId(playlistId: Long) {
        this.playlistId = playlistId
    }

    override fun getItemCount(): Int {
        return if (null != arraylist) arraylist!!.size else 0
    }

    private fun setOnPopupMenuListener(itemHolder: ItemHolder, position: Int) {
        itemHolder.popupMenu.setOnClickListener { v ->
            val menu = PopupMenu(mContext, v)
            menu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.popup_song_remove_playlist -> {
                        TimberUtils.removeFromPlaylist(mContext, arraylist!![position]!!.id, playlistId)
                        removeSongAt(position)
                        notifyItemRemoved(position)
                    }
                    R.id.popup_song_play -> MusicPlayer.playAll(mContext, songIDs, position, -1, TimberUtils.IdType.NA, false)
                    R.id.popup_song_play_next -> {
                        val ids = LongArray(1)
                        ids[0] = arraylist!![position]!!.id
                        MusicPlayer.playNext(mContext, ids, -1, TimberUtils.IdType.NA)
                    }
                    R.id.popup_song_goto_album -> NavigationUtils.goToAlbum(mContext, arraylist!![position]!!.albumId)
                    R.id.popup_song_goto_artist -> NavigationUtils.goToArtist(mContext, arraylist!![position]!!.artistId)
                    R.id.popup_song_addto_queue -> {
                        val id = LongArray(1)
                        id[0] = arraylist!![position]!!.id
                        MusicPlayer.addToQueue(mContext, id, -1, TimberUtils.IdType.NA)
                    }
                    R.id.popup_song_addto_playlist -> AddPlaylistDialog.newInstance(arraylist!![position]).show(mContext.supportFragmentManager, "ADD_PLAYLIST")
                    R.id.popup_song_share -> TimberUtils.shareTrack(mContext, arraylist!![position]!!.id)
                    R.id.popup_song_delete -> {
                        val deleteIds = longArrayOf(arraylist!![position]!!.id)
                        TimberUtils.showDeleteDialog(mContext, arraylist!![position]!!.title, deleteIds, this@SongsListAdapter, position)
                    }
                }
                false
            }
            menu.inflate(R.menu.popup_song)
            menu.show()
            if (isPlaylist) menu.menu.findItem(R.id.popup_song_remove_playlist).isVisible = true
        }
    }

    val songIds: LongArray
        get() {
            val ret = LongArray(itemCount)
            for (i in 0 until itemCount) {
                ret[i] = arraylist!![i]!!.id
            }
            return ret
        }

    override fun getTextToShowInBubble(pos: Int): String {
        if (arraylist == null || arraylist!!.size == 0) return ""
        val ch = arraylist!![pos]!!.title[0]
        return if (Character.isDigit(ch)) {
            "#"
        } else ch.toString()
    }

    private fun setAnimation(viewToAnimate: View, position: Int) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition) {
            val animation = AnimationUtils.loadAnimation(mContext, R.anim.abc_slide_in_bottom)
            viewToAnimate.startAnimation(animation)
            lastPosition = position
        }
    }

    override fun updateDataSet(arraylist: MutableList<Song?>?) {
        this.arraylist = arraylist
        songIDs = songIds
    }

    inner class ItemHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        var title: TextView
        var artist: TextView
        var albumArt: ImageView
        var popupMenu: ImageView
        val visualizer: MusicVisualizer
        override fun onClick(v: View) {
            val handler = Handler()
            handler.postDelayed({
                playAll(mContext, songIDs, adapterPosition, -1,
                        TimberUtils.IdType.NA, false,
                        arraylist!![adapterPosition], false)
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
            popupMenu = view.findViewById<View>(R.id.popup_menu) as ImageView
            visualizer = view.findViewById<View>(R.id.visualizer) as MusicVisualizer
            view.setOnClickListener(this)
        }
    }

    fun getSongAt(i: Int): Song? {
        return arraylist!![i]
    }

    fun addSongTo(i: Int, song: Song?) {
        arraylist?.add(i, song)
    }

    override fun removeSongAt(i: Int) {
        arraylist?.removeAt(i)
        updateDataSet(arraylist)
    }

    init {
        songIDs = songIds
        ateKey = Helpers.getATEKey(mContext)
        this.animate = animate
    }
}