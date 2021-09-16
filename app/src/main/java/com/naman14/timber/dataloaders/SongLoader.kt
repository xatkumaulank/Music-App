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
import android.media.MediaMetadataRetriever
import android.provider.BaseColumns
import android.provider.MediaStore
import android.text.TextUtils
import com.naman14.timber.models.Song
import com.naman14.timber.utils.PreferencesUtility
import java.util.*

open class SongLoader {
    companion object {
        private val sEmptyList = LongArray(0)
        @JvmStatic
        fun getSongsForCursor(cursor: Cursor?): MutableList<Song?> {
            val arrayList: MutableList<Song?> = mutableListOf()
            if (cursor != null && cursor.moveToFirst()) do {
                val id = cursor.getLong(0)
                val title = cursor.getString(1)
                val artist = cursor.getString(2)
                val album = cursor.getString(3)
                val duration = cursor.getInt(4)
                val trackNumber = cursor.getInt(5)
                val artistId = cursor.getInt(6).toLong()
                val albumId = cursor.getLong(7)
                arrayList.add(Song(id, albumId, artistId, title, artist, album, duration, trackNumber))
            } while (cursor.moveToNext())
            cursor?.close()
            return arrayList
        }

        fun getSongForCursor(cursor: Cursor?): Song {
            var song = Song()
            if (cursor != null && cursor.moveToFirst()) {
                val id = cursor.getLong(0)
                val title = cursor.getString(1)
                val artist = cursor.getString(2)
                val album = cursor.getString(3)
                val duration = cursor.getInt(4)
                val trackNumber = cursor.getInt(5)
                val artistId = cursor.getInt(6).toLong()
                val albumId = cursor.getLong(7)
                song = Song(id, albumId, artistId, title, artist, album, duration, trackNumber)
            }
            cursor?.close()
            return song
        }

        @JvmStatic
        fun getSongListForCursor(cursor: Cursor?): LongArray {
            var cursor = cursor
            if (cursor == null) {
                return sEmptyList
            }
            val len = cursor.count
            val list = LongArray(len)
            cursor.moveToFirst()
            var columnIndex = -1
            columnIndex = try {
                cursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists.Members.AUDIO_ID)
            } catch (notaplaylist: IllegalArgumentException) {
                cursor.getColumnIndexOrThrow(BaseColumns._ID)
            }
            for (i in 0 until len) {
                list[i] = cursor.getLong(columnIndex)
                cursor.moveToNext()
            }
            cursor.close()
            cursor = null
            return list
        }

        fun getSongFromPath(songPath: String, context: Context): Song {
            val cr = context.contentResolver
            val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            val selection = MediaStore.Audio.Media.DATA
            val selectionArgs = arrayOf(songPath)
            val projection = arrayOf("_id", "title", "artist", "album", "duration", "track", "artist_id", "album_id")
            val sortOrder = MediaStore.Audio.Media.TITLE + " ASC"
            val cursor = cr.query(uri, projection, "$selection=?", selectionArgs, sortOrder)
            return if (cursor != null && cursor.count > 0) {
                val song = getSongForCursor(cursor)
                cursor.close()
                song
            } else Song()
        }

        @JvmStatic
        fun getAllSongs(context: Context): MutableList<Song?> {
            return getSongsForCursor(makeSongCursor(context, null, null))
        }

        fun getSongListInFolder(context: Context, path: String): LongArray {
            val whereArgs = arrayOf("$path%")
            return getSongListForCursor(makeSongCursor(context, MediaStore.Audio.Media.DATA + " LIKE ?", whereArgs, null))
        }

        @JvmStatic
        fun getSongForID(context: Context, id: Long): Song {
            return getSongForCursor(makeSongCursor(context, "_id=$id", null))
        }

        fun searchSongs(context: Context, searchString: String, limit: Int): MutableList<Song?> {
            val result = getSongsForCursor(makeSongCursor(context, "title LIKE ?", arrayOf("$searchString%")))
            if (result.size < limit) {
                result.addAll(getSongsForCursor(makeSongCursor(context, "title LIKE ?", arrayOf("%_$searchString%"))))
            }
            return if (result.size < limit) result else result.subList(0, limit)
        }

        fun makeSongCursor(context: Context, selection: String?, paramArrayOfString: Array<String>?): Cursor {
            val songSortOrder = PreferencesUtility.getInstance(context).songSortOrder
            return makeSongCursor(context, selection, paramArrayOfString, songSortOrder)
        }

        private fun makeSongCursor(context: Context, selection: String?, paramArrayOfString: Array<String>?, sortOrder: String?): Cursor {
            var selectionStatement = "is_music=1 AND title != ''"
            if (!TextUtils.isEmpty(selection)) {
                selectionStatement = "$selectionStatement AND $selection"
            }
            return context.contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, arrayOf("_id", "title", "artist", "album", "duration", "track", "artist_id", "album_id"), selectionStatement, paramArrayOfString, sortOrder)
        }

        fun songFromFile(filePath: String?): Song {
            val mmr = MediaMetadataRetriever()
            mmr.setDataSource(filePath)
            return Song(
                    -1,
                    -1,
                    -1,
                    mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE),
                    mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST),
                    mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM), mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION).toInt(),
                    0
            )
        }
    }
}