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
import android.graphics.Rect
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.naman14.timber.MusicPlayer
import com.naman14.timber.R
import com.naman14.timber.dataloaders.ArtistAlbumLoader
import com.naman14.timber.dialogs.AddPlaylistDialog
import com.naman14.timber.models.Song
import com.naman14.timber.utils.NavigationUtils
import com.naman14.timber.utils.TimberUtils
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import java.util.*

class ArtistSongAdapter(private val mContext: Activity, private var arraylist: MutableList<Song>, private val artistID: Long) : BaseSongAdapter<ArtistSongAdapter.ItemHolder?>() {
    private var songIDs: LongArray
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ItemHolder {
        return if (viewType == 0) {
            val v0 = LayoutInflater.from(viewGroup.context).inflate(R.layout.artist_detail_albums_header, null)
            ItemHolder(v0)
        } else {
            val v2 = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_artist_song, null)
            ItemHolder(v2)
        }
    }

    override fun onBindViewHolder(itemHolder: ItemHolder, i: Int) {
        if (getItemViewType(i) == 0) {
            //nothing
            setUpAlbums(itemHolder.albumsRecyclerView)
        } else {
            val localItem = arraylist[i]
            itemHolder.title.text = localItem.title
            itemHolder.album.text = localItem.albumName
            ImageLoader.getInstance().displayImage(TimberUtils.getAlbumArtUri(localItem.albumId).toString(),
                    itemHolder.albumArt, DisplayImageOptions.Builder()
                    .cacheInMemory(true).showImageOnLoading(R.drawable.ic_empty_music2).resetViewBeforeLoading(true).build())
            setOnPopupMenuListener(itemHolder, i - 1)
        }
    }

    override fun onViewRecycled(itemHolder: ItemHolder) {
        if (itemHolder.itemViewType == 0) clearExtraSpacingBetweenCards(itemHolder.albumsRecyclerView)
    }

    override fun getItemCount(): Int {
        return if (null != arraylist) arraylist.size else 0
    }

    private fun setOnPopupMenuListener(itemHolder: ItemHolder, position: Int) {
        itemHolder.menu.setOnClickListener { v ->
            val menu = PopupMenu(mContext, v)
            menu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.popup_song_play -> MusicPlayer.playAll(mContext, songIDs, position + 1, -1, TimberUtils.IdType.NA, false)
                    R.id.popup_song_play_next -> {
                        val ids = LongArray(1)
                        ids[0] = arraylist[position + 1].id
                        MusicPlayer.playNext(mContext, ids, -1, TimberUtils.IdType.NA)
                    }
                    R.id.popup_song_goto_album -> NavigationUtils.goToAlbum(mContext, arraylist[position + 1].albumId)
                    R.id.popup_song_goto_artist -> NavigationUtils.goToArtist(mContext, arraylist[position + 1].artistId)
                    R.id.popup_song_addto_queue -> {
                        val id = LongArray(1)
                        id[0] = arraylist[position + 1].id
                        MusicPlayer.addToQueue(mContext, id, -1, TimberUtils.IdType.NA)
                    }
                    R.id.popup_song_addto_playlist -> AddPlaylistDialog.newInstance(arraylist[position + 1]).show((mContext as AppCompatActivity).supportFragmentManager, "ADD_PLAYLIST")
                    R.id.popup_song_share -> TimberUtils.shareTrack(mContext, arraylist[position + 1].id)
                    R.id.popup_song_delete -> {
                        val deleteIds = longArrayOf(arraylist[position + 1].id)
                        TimberUtils.showDeleteDialog(mContext, arraylist[position + 1].title, deleteIds, this@ArtistSongAdapter, position + 1)
                    }
                }
                false
            }
            menu.inflate(R.menu.popup_song)
            menu.show()
        }
    }

    private fun setUpAlbums(albumsRecyclerview: RecyclerView) {
        albumsRecyclerview.layoutManager = LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false)
        albumsRecyclerview.setHasFixedSize(true)

        //to add spacing between cards
        val spacingInPixels = mContext.resources.getDimensionPixelSize(R.dimen.spacing_card)
        albumsRecyclerview.addItemDecoration(SpacesItemDecoration(spacingInPixels))
        albumsRecyclerview.isNestedScrollingEnabled = false
        val mAlbumAdapter = ArtistAlbumAdapter(mContext, ArtistAlbumLoader.getAlbumsForArtist(mContext, artistID))
        albumsRecyclerview.adapter = mAlbumAdapter
    }

    private fun clearExtraSpacingBetweenCards(albumsRecyclerview: RecyclerView) {
        //to clear any extra spacing between cards
        val spacingInPixelstoClear = -mContext.resources.getDimensionPixelSize(R.dimen.spacing_card)
        albumsRecyclerview.addItemDecoration(SpacesItemDecoration(spacingInPixelstoClear))
    }

    //actualArraylist.remove(0);
    val songIds: LongArray
        get() {
            val actualArraylist: List<Song> = ArrayList(arraylist)
            //actualArraylist.remove(0);
            val ret = LongArray(actualArraylist.size)
            for (i in actualArraylist.indices) {
                ret[i] = actualArraylist[i].id
            }
            return ret
        }

    override fun removeSongAt(i: Int) {
        arraylist.removeAt(i)
        updateDataSet(arraylist)
    }

    override fun updateDataSet(arraylist: MutableList<Song>) {
        this.arraylist = arraylist
        songIDs = songIds
    }

    override fun getItemViewType(position: Int): Int {
        val viewType: Int
        viewType = if (position == 0) {
            0
        } else 1
        return viewType
    }

    inner class ItemHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        var title: TextView
        var album: TextView
        var albumArt: ImageView
        var menu: ImageView
        var albumsRecyclerView: RecyclerView
        override fun onClick(v: View) {
            val handler = Handler()
            handler.postDelayed({
                playAll(mContext, songIDs, adapterPosition, artistID,
                        TimberUtils.IdType.Artist, false,
                        arraylist[adapterPosition], true)
            }, 100)
        }

        init {
            albumsRecyclerView = view.findViewById<View>(R.id.recycler_view_album) as RecyclerView
            title = view.findViewById<View>(R.id.song_title) as TextView
            album = view.findViewById<View>(R.id.song_album) as TextView
            albumArt = view.findViewById<View>(R.id.albumArt) as ImageView
            menu = view.findViewById<View>(R.id.popup_menu) as ImageView
            view.setOnClickListener(this)
        }
    }

    inner class SpacesItemDecoration(private val space: Int) : ItemDecoration() {
        override fun getItemOffsets(outRect: Rect, view: View,
                                    parent: RecyclerView, state: RecyclerView.State) {

            //the padding from left
            outRect.left = space
        }
    }

    init {
        songIDs = songIds
    }
}