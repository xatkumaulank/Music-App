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

import android.content.ContentProviderOperation
import android.content.Context
import android.content.OperationApplicationException
import android.database.Cursor
import android.os.RemoteException
import android.provider.MediaStore
import android.provider.MediaStore.Audio.AudioColumns
import android.provider.MediaStore.Audio.Playlists
import com.naman14.timber.models.Song
import java.util.*

object PlaylistSongLoader {
    private var mCursor: Cursor? = null
    private var mPlaylistID: Long = 0
    private var context: Context? = null
    @JvmStatic
    fun getSongsInPlaylist(mContext: Context?, playlistID: Long): List<Song> {
        val mSongList = ArrayList<Song>()
        context = mContext
        mPlaylistID = playlistID
        val playlistCount = countPlaylist(context, mPlaylistID)
        mCursor = makePlaylistSongCursor(context, mPlaylistID)
        if (mCursor != null) {
            var runCleanup = false
            if (mCursor!!.count != playlistCount) {
                runCleanup = true
            }
            if (!runCleanup && mCursor!!.moveToFirst()) {
                val playOrderCol = mCursor!!.getColumnIndexOrThrow(Playlists.Members.PLAY_ORDER)
                var lastPlayOrder = -1
                do {
                    val playOrder = mCursor!!.getInt(playOrderCol)
                    if (playOrder == lastPlayOrder) {
                        runCleanup = true
                        break
                    }
                    lastPlayOrder = playOrder
                } while (mCursor!!.moveToNext())
            }
            if (runCleanup) {
                cleanupPlaylist(context, mPlaylistID, mCursor)
                mCursor!!.close()
                mCursor = makePlaylistSongCursor(context, mPlaylistID)
                if (mCursor != null) {
                }
            }
        }
        if (mCursor != null && mCursor!!.moveToFirst()) {
            do {
                val id = mCursor!!.getLong(mCursor!!
                        .getColumnIndexOrThrow(Playlists.Members.AUDIO_ID))
                val songName = mCursor!!.getString(mCursor!!
                        .getColumnIndexOrThrow(AudioColumns.TITLE))
                val artist = mCursor!!.getString(mCursor!!
                        .getColumnIndexOrThrow(AudioColumns.ARTIST))
                val albumId = mCursor!!.getLong(mCursor!!
                        .getColumnIndexOrThrow(AudioColumns.ALBUM_ID))
                val artistId = mCursor!!.getLong(mCursor!!
                        .getColumnIndexOrThrow(AudioColumns.ARTIST_ID))
                val album = mCursor!!.getString(mCursor!!
                        .getColumnIndexOrThrow(AudioColumns.ALBUM))
                val duration = mCursor!!.getLong(mCursor!!
                        .getColumnIndexOrThrow(AudioColumns.DURATION))
                val durationInSecs = duration.toInt() / 1000
                val tracknumber = mCursor!!.getInt(mCursor!!
                        .getColumnIndexOrThrow(AudioColumns.TRACK))
                val song = Song(id, albumId, artistId, songName, artist, album, durationInSecs, tracknumber)
                mSongList.add(song)
            } while (mCursor!!.moveToNext())
        }
        // Close the cursor
        if (mCursor != null) {
            mCursor!!.close()
            mCursor = null
        }
        return mSongList
    }

    private fun cleanupPlaylist(context: Context?, playlistId: Long,
                                cursor: Cursor?) {
        val idCol = cursor!!.getColumnIndexOrThrow(Playlists.Members.AUDIO_ID)
        val uri = Playlists.Members.getContentUri("external", playlistId)
        val ops = ArrayList<ContentProviderOperation>()
        ops.add(ContentProviderOperation.newDelete(uri).build())
        val YIELD_FREQUENCY = 100
        if (cursor.moveToFirst() && cursor.count > 0) {
            do {
                val builder = ContentProviderOperation.newInsert(uri)
                        .withValue(Playlists.Members.PLAY_ORDER, cursor.position)
                        .withValue(Playlists.Members.AUDIO_ID, cursor.getLong(idCol))
                if ((cursor.position + 1) % YIELD_FREQUENCY == 0) {
                    builder.withYieldAllowed(true)
                }
                ops.add(builder.build())
            } while (cursor.moveToNext())
        }
        try {
            context!!.contentResolver.applyBatch(MediaStore.AUTHORITY, ops)
        } catch (e: RemoteException) {
        } catch (e: OperationApplicationException) {
        }
    }

    private fun countPlaylist(context: Context?, playlistId: Long): Int {
        var c: Cursor? = null
        try {
            c = context!!.contentResolver.query(
                    Playlists.Members.getContentUri("external", playlistId), arrayOf(
                    Playlists.Members.AUDIO_ID), null, null,
                    Playlists.Members.DEFAULT_SORT_ORDER)
            if (c != null) {
                return c.count
            }
        } finally {
            if (c != null) {
                c.close()
                c = null
            }
        }
        return 0
    }

    fun makePlaylistSongCursor(context: Context?, playlistID: Long?): Cursor {
        val mSelection = StringBuilder()
        mSelection.append(AudioColumns.IS_MUSIC + "=1")
        mSelection.append(" AND " + AudioColumns.TITLE + " != ''")
        return context!!.contentResolver.query(
                Playlists.Members.getContentUri("external", playlistID!!), arrayOf(
                Playlists.Members._ID,
                Playlists.Members.AUDIO_ID,
                AudioColumns.TITLE,
                AudioColumns.ARTIST,
                AudioColumns.ALBUM_ID,
                AudioColumns.ARTIST_ID,
                AudioColumns.ALBUM,
                AudioColumns.DURATION,
                AudioColumns.TRACK,
                Playlists.Members.PLAY_ORDER), mSelection.toString(), null,
                Playlists.Members.DEFAULT_SORT_ORDER)
    }
}