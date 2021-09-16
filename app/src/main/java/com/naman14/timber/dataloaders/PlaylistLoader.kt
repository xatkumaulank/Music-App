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
package com.naman14.timber.dataloaders

import android.content.Context
import android.database.Cursor
import android.provider.BaseColumns
import android.provider.MediaStore
import android.provider.MediaStore.Audio.PlaylistsColumns
import com.naman14.timber.models.Playlist
import com.naman14.timber.utils.TimberUtils
import java.util.*

object PlaylistLoader {
    var mPlaylistList: ArrayList<Playlist>? = null
    private var mCursor: Cursor? = null
    @JvmStatic
    fun getPlaylists(context: Context, defaultIncluded: Boolean): MutableList<Playlist>? {
        mPlaylistList = ArrayList()
        if (defaultIncluded) makeDefaultPlaylists(context)
        mCursor = makePlaylistCursor(context)
        if (mCursor != null && mCursor!!.moveToFirst()) {
            do {
                val id = mCursor!!.getLong(0)
                val name = mCursor!!.getString(1)
                val songCount = TimberUtils.getSongCountForPlaylist(context, id)
                val playlist = Playlist(id, name, songCount)
                mPlaylistList!!.add(playlist)
            } while (mCursor!!.moveToNext())
        }
        if (mCursor != null) {
            mCursor!!.close()
            mCursor = null
        }
        return mPlaylistList
    }

    private fun makeDefaultPlaylists(context: Context) {
        val resources = context.resources

        /* Last added list */
        val lastAdded = Playlist(TimberUtils.PlaylistType.LastAdded.mId,
                resources.getString(TimberUtils.PlaylistType.LastAdded.mTitleId), -1)
        mPlaylistList!!.add(lastAdded)

        /* Recently Played */
        val recentlyPlayed = Playlist(TimberUtils.PlaylistType.RecentlyPlayed.mId,
                resources.getString(TimberUtils.PlaylistType.RecentlyPlayed.mTitleId), -1)
        mPlaylistList!!.add(recentlyPlayed)

        /* Top Tracks */
        val topTracks = Playlist(TimberUtils.PlaylistType.TopTracks.mId,
                resources.getString(TimberUtils.PlaylistType.TopTracks.mTitleId), -1)
        mPlaylistList!!.add(topTracks)
    }

    fun makePlaylistCursor(context: Context): Cursor {
        return context.contentResolver.query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, arrayOf(
                BaseColumns._ID,
                PlaylistsColumns.NAME
        ), null, null, MediaStore.Audio.Playlists.DEFAULT_SORT_ORDER)
    }

    fun deletePlaylists(context: Context, playlistId: Long) {
        val localUri = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI
        val localStringBuilder = StringBuilder()
        localStringBuilder.append("_id IN (")
        localStringBuilder.append(playlistId)
        localStringBuilder.append(")")
        context.contentResolver.delete(localUri, localStringBuilder.toString(), null)
    }
}