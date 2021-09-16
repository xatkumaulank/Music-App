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
import com.naman14.timber.models.Artist
import com.naman14.timber.utils.PreferencesUtility
import java.util.*
import kotlin.collections.ArrayList

object ArtistLoader {
    fun getArtist(cursor: Cursor?): Artist {
        var artist = Artist()
        if (cursor != null) {
            if (cursor.moveToFirst()) artist = Artist(cursor.getLong(0), cursor.getString(1), cursor.getInt(2), cursor.getInt(3))
        }
        cursor?.close()
        return artist
    }

    fun getArtistsForCursor(cursor: Cursor?): MutableList<Artist> {
        val arrayList: MutableList<Artist> = mutableListOf()
        if (cursor != null && cursor.moveToFirst()) do {
            arrayList.add(Artist(cursor.getLong(0), cursor.getString(1), cursor.getInt(2), cursor.getInt(3)))
        } while (cursor.moveToNext())
        cursor?.close()
        return arrayList
    }

    @JvmStatic
    fun getAllArtists(context: Context): List<Artist> {
        return getArtistsForCursor(makeArtistCursor(context, null, null))
    }

    @JvmStatic
    fun getArtist(context: Context, id: Long): Artist {
        return getArtist(makeArtistCursor(context, "_id=?", arrayOf(id.toString())))
    }

    fun getArtists(context: Context, paramString: String, limit: Int): List<Artist> {
        val result = getArtistsForCursor(makeArtistCursor(context, "artist LIKE ?", arrayOf("$paramString%")))
        if (result.size < limit) {
            result.addAll(getArtistsForCursor(makeArtistCursor(context, "artist LIKE ?", arrayOf("%_$paramString%"))))
        }
        return if (result.size < limit) result else result.subList(0, limit)
    }

    fun makeArtistCursor(context: Context, selection: String?, paramArrayOfString: Array<String>?): Cursor {
        val artistSortOrder = PreferencesUtility.getInstance(context).artistSortOrder
        return context.contentResolver.query(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI, arrayOf("_id", "artist", "number_of_albums", "number_of_tracks"), selection, paramArrayOfString, artistSortOrder)
    }
}