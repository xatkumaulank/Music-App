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
import com.naman14.timber.models.Album
import com.naman14.timber.utils.PreferencesUtility
import java.util.*

object AlbumLoader {
    fun getAlbum(cursor: Cursor?): Album {
        var album = Album()
        if (cursor != null) {
            if (cursor.moveToFirst()) album = Album(cursor.getLong(0), cursor.getString(1), cursor.getString(2), cursor.getLong(3), cursor.getInt(4), cursor.getInt(5))
        }
        cursor?.close()
        return album
    }

    private fun getAlbumsForCursor(cursor: Cursor?): MutableList<Album> {
        val arrayList: MutableList<Album> = mutableListOf()
        if (cursor != null && cursor.moveToFirst()) do {
            arrayList.add(Album(cursor.getLong(0), cursor.getString(1), cursor.getString(2), cursor.getLong(3), cursor.getInt(4), cursor.getInt(5)))
        } while (cursor.moveToNext())
        cursor?.close()
        return arrayList
    }

    @JvmStatic
    fun getAllAlbums(context: Context): List<Album> {
        return getAlbumsForCursor(makeAlbumCursor(context, null, null))
    }

    @JvmStatic
    fun getAlbum(context: Context, id: Long): Album {
        return getAlbum(makeAlbumCursor(context, "_id=?", arrayOf(id.toString())))
    }

    fun getAlbums(context: Context, paramString: String, limit: Int): List<Album> {
        val result: MutableList<Album> = getAlbumsForCursor(makeAlbumCursor(context, "album LIKE ?", arrayOf("$paramString%")))
        if (result.size < limit) {
            result.addAll(getAlbumsForCursor(makeAlbumCursor(context, "album LIKE ?", arrayOf("%_$paramString%"))))
        }
        return if (result.size < limit) result else result.subList(0, limit)
    }

    fun makeAlbumCursor(context: Context, selection: String?, paramArrayOfString: Array<String>?): Cursor {
        val albumSortOrder = PreferencesUtility.getInstance(context).albumSortOrder
        return context.contentResolver.query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, arrayOf("_id", "album", "artist", "artist_id", "numsongs", "minyear"), selection, paramArrayOfString, albumSortOrder)
    }
}