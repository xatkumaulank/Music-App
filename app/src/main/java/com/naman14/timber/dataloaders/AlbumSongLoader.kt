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
import android.provider.MediaStore
import com.naman14.timber.models.Song
import com.naman14.timber.utils.PreferencesUtility
import java.util.*

object AlbumSongLoader {
    private val sEmptyList = LongArray(0)
    @JvmStatic
    fun getSongsForAlbum(context: Context, albumID: Long): MutableList<Song> {
        val cursor = makeAlbumSongCursor(context, albumID)
        val arrayList: MutableList<Song> = mutableListOf()
        if (cursor != null && cursor.moveToFirst()) do {
            val id = cursor.getLong(0)
            val title = cursor.getString(1)
            val artist = cursor.getString(2)
            val album = cursor.getString(3)
            val duration = cursor.getInt(4)
            var trackNumber = cursor.getInt(5)
            /*This fixes bug where some track numbers displayed as 100 or 200*/while (trackNumber >= 1000) {
                trackNumber -= 1000 //When error occurs the track numbers have an extra 1000 or 2000 added, so decrease till normal.
            }
            val artistId = cursor.getInt(6).toLong()
            arrayList.add(Song(id, albumID, artistId, title, artist, album, duration, trackNumber))
        } while (cursor.moveToNext())
        if (cursor != null) cursor.close()
        return arrayList
    }

    fun makeAlbumSongCursor(context: Context, albumID: Long): Cursor {
        val contentResolver = context.contentResolver
        val albumSongSortOrder = PreferencesUtility.getInstance(context).albumSongSortOrder
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val string = "is_music=1 AND title != '' AND album_id=$albumID"
        return contentResolver.query(uri, arrayOf("_id", "title", "artist", "album", "duration", "track", "artist_id"), string, null, albumSongSortOrder)
    }
}