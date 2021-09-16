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

object ArtistSongLoader {
    @JvmStatic
    fun getSongsForArtist(context: Context, artistID: Long): MutableList<Song> {
        val cursor = makeArtistSongCursor(context, artistID)
        val songsList: MutableList<Song> = mutableListOf()
        if (cursor != null && cursor.moveToFirst()) do {
            val id = cursor.getLong(0)
            val title = cursor.getString(1)
            val artist = cursor.getString(2)
            val album = cursor.getString(3)
            val duration = cursor.getInt(4)
            val trackNumber = cursor.getInt(5)
            val albumId = cursor.getInt(6).toLong()
            val artistId = artistID
            songsList.add(Song(id, albumId, artistID, title, artist, album, duration, trackNumber))
        } while (cursor.moveToNext())
        if (cursor != null) cursor.close()
        return songsList
    }

    fun makeArtistSongCursor(context: Context, artistID: Long): Cursor {
        val contentResolver = context.contentResolver
        val artistSongSortOrder = PreferencesUtility.getInstance(context).artistSongSortOrder
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val string = "is_music=1 AND title != '' AND artist_id=$artistID"
        return contentResolver.query(uri, arrayOf("_id", "title", "artist", "album", "duration", "track", "album_id"), string, null, artistSongSortOrder)
    }
}