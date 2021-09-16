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
import com.naman14.timber.dataloaders.AlbumLoader.getAllAlbums
import com.naman14.timber.models.Album
import java.util.*

object ArtistAlbumLoader {
    @JvmStatic
    fun getAlbumsForArtist(context: Context?, artistID: Long): ArrayList<Album>? {
        if (artistID == -1L) return null
        val allAlbums = getAllAlbums(context!!)
        val artistAlbums = ArrayList<Album>()
        for (album in allAlbums) {
            if (album.artistId == artistID) {
                artistAlbums.add(album)
            }
        }
        return artistAlbums
    }
}