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
import com.naman14.timber.lastfmapi.LastFmClient
import com.naman14.timber.lastfmapi.callbacks.ArtistInfoListener
import com.naman14.timber.lastfmapi.models.ArtistQuery
import com.naman14.timber.lastfmapi.models.LastfmArtist
import com.naman14.timber.models.Album
import com.naman14.timber.models.Artist
import com.naman14.timber.models.Song
import com.naman14.timber.utils.NavigationUtils
import com.naman14.timber.utils.TimberUtils
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer

class SearchAdapter(private val mContext: Activity) : BaseSongAdapter<SearchAdapter.ItemHolder?>() {
    private var searchResults: List<*> = emptyList()
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ItemHolder {
        return when (viewType) {
            0 -> {
                val v0 = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_song, null)
                ItemHolder(v0)
            }
            1 -> {
                val v1 = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_album_search, null)
                ItemHolder(v1)
            }
            2 -> {
                val v2 = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_artist, null)
                ItemHolder(v2)
            }
            10 -> {
                val v10 = LayoutInflater.from(viewGroup.context).inflate(R.layout.search_section_header, null)
                ItemHolder(v10)
            }
            else -> {
                val v3 = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_song, null)
                ItemHolder(v3)
            }
        }
    }

    override fun onBindViewHolder(itemHolder: ItemHolder?, i: Int) {
        when (getItemViewType(i)) {
            0 -> {
                val song = searchResults[i] as Song
                if (itemHolder != null) {
                    itemHolder.title.text = song.title
                }
                if (itemHolder != null) {
                    itemHolder.songartist.text = song.albumName
                }
                ImageLoader.getInstance().displayImage(TimberUtils.getAlbumArtUri(song.albumId).toString(), itemHolder?.albumArt,
                        DisplayImageOptions.Builder().cacheInMemory(true)
                                .cacheOnDisk(true)
                                .showImageOnFail(R.drawable.ic_empty_music2)
                                .resetViewBeforeLoading(true)
                                .displayer(FadeInBitmapDisplayer(400))
                                .build())
                setOnPopupMenuListener(itemHolder!!, i)
            }
            1 -> {
                val album = searchResults[i] as Album
                if (itemHolder != null) {
                    itemHolder.albumtitle.text = album.title
                }
                if (itemHolder != null) {
                    itemHolder.albumartist.text = album.artistName
                }
                ImageLoader.getInstance().displayImage(TimberUtils.getAlbumArtUri(album.id).toString(), itemHolder?.albumArt,
                        DisplayImageOptions.Builder().cacheInMemory(true)
                                .cacheOnDisk(true)
                                .showImageOnFail(R.drawable.ic_empty_music2)
                                .resetViewBeforeLoading(true)
                                .displayer(FadeInBitmapDisplayer(400))
                                .build())
            }
            2 -> {
                val artist = searchResults[i] as Artist
                if (itemHolder != null) {
                    itemHolder.artisttitle.text = artist.name
                }
                val albumNmber = TimberUtils.makeLabel(mContext, R.plurals.Nalbums, artist.albumCount)
                val songCount = TimberUtils.makeLabel(mContext, R.plurals.Nsongs, artist.songCount)
                if (itemHolder != null) {
                    itemHolder.albumsongcount.text = TimberUtils.makeCombinedString(mContext, albumNmber, songCount)
                }
                LastFmClient.getInstance(mContext).getArtistInfo(ArtistQuery(artist.name), object : ArtistInfoListener {
                    override fun artistInfoSucess(artist: LastfmArtist) {
                        if (itemHolder != null) {
                            if (itemHolder.artistImage != null) {
                                ImageLoader.getInstance().displayImage(artist.mArtwork[1].mUrl, itemHolder.artistImage,
                                        DisplayImageOptions.Builder().cacheInMemory(true)
                                                .cacheOnDisk(true)
                                                .showImageOnFail(R.drawable.ic_empty_music2)
                                                .resetViewBeforeLoading(true)
                                                .displayer(FadeInBitmapDisplayer(400))
                                                .build())
                            }
                        }
                    }

                    override fun artistInfoFailed() {}
                })
            }
            10 -> if (itemHolder != null) {
                itemHolder.sectionHeader.text = searchResults[i] as String?
            }
            3 -> {
            }
        }
    }

    override fun onViewRecycled(itemHolder: ItemHolder) {}
    override fun getItemCount(): Int {
        return searchResults.size
    }

    private fun setOnPopupMenuListener(itemHolder: ItemHolder, position: Int) {
        itemHolder.menu.setOnClickListener { v ->
            val menu = PopupMenu(mContext, v)
            menu.setOnMenuItemClickListener { item ->
                val song = LongArray(1)
                song[0] = (searchResults[position] as Song).id
                when (item.itemId) {
                    R.id.popup_song_play -> MusicPlayer.playAll(mContext, song, 0, -1, TimberUtils.IdType.NA, false)
                    R.id.popup_song_play_next -> MusicPlayer.playNext(mContext, song, -1, TimberUtils.IdType.NA)
                    R.id.popup_song_goto_album -> NavigationUtils.navigateToAlbum(mContext, (searchResults[position] as Song).albumId, null)
                    R.id.popup_song_goto_artist -> NavigationUtils.navigateToArtist(mContext, (searchResults[position] as Song).artistId, null)
                    R.id.popup_song_addto_queue -> MusicPlayer.addToQueue(mContext, song, -1, TimberUtils.IdType.NA)
                    R.id.popup_song_addto_playlist -> AddPlaylistDialog.newInstance(searchResults[position] as Song?).show((mContext as AppCompatActivity).supportFragmentManager, "ADD_PLAYLIST")
                }
                false
            }
            menu.inflate(R.menu.popup_song)
            //Hide these because they aren't implemented
            menu.menu.findItem(R.id.popup_song_delete).isVisible = false
            menu.menu.findItem(R.id.popup_song_share).isVisible = false
            menu.show()
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (searchResults[position] is Song) return 0
        if (searchResults[position] is Album) return 1
        if (searchResults[position] is Artist) return 2
        return if (searchResults[position] is String) 10 else 3
    }

    fun updateSearchResults(searchResults: List<*>) {
        this.searchResults = searchResults
    }

    inner class ItemHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        var title: TextView
        var songartist: TextView
        var albumtitle: TextView
        var artisttitle: TextView
        var albumartist: TextView
        var albumsongcount: TextView
        var sectionHeader: TextView
        var albumArt: ImageView
        var artistImage: ImageView?
        var menu: ImageView
        override fun onClick(v: View) {
            when (itemViewType) {
                0 -> {
                    val handler = Handler()
                    handler.postDelayed({
                        val ret = LongArray(1)
                        ret[0] = (searchResults[adapterPosition] as Song).id
                        playAll(mContext, ret, 0, -1, TimberUtils.IdType.NA,
                                false, searchResults[adapterPosition] as Song?, false)
                    }, 100)
                }
                1 -> NavigationUtils.goToAlbum(mContext, (searchResults[adapterPosition] as Album).id)
                2 -> NavigationUtils.goToArtist(mContext, (searchResults[adapterPosition] as Artist).id)
                3 -> {
                }
                10 -> {
                }
            }
        }

        init {
            title = view.findViewById<View>(R.id.song_title) as TextView
            songartist = view.findViewById<View>(R.id.song_artist) as TextView
            albumsongcount = view.findViewById<View>(R.id.album_song_count) as TextView
            artisttitle = view.findViewById<View>(R.id.artist_name) as TextView
            albumtitle = view.findViewById<View>(R.id.album_title) as TextView
            albumartist = view.findViewById<View>(R.id.album_artist) as TextView
            albumArt = view.findViewById<View>(R.id.albumArt) as ImageView
            artistImage = view.findViewById<View>(R.id.artistImage) as ImageView
            menu = view.findViewById<View>(R.id.popup_menu) as ImageView
            sectionHeader = view.findViewById<View>(R.id.section_header) as TextView
            view.setOnClickListener(this)
        }
    }
}