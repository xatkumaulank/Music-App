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
package com.naman14.timber.models

class Song {
    @JvmField
    val albumId: Long
    @JvmField
    val albumName: String
    val artistId: Long
    @JvmField
    val artistName: String
    @JvmField
    val duration: Int
    @JvmField
    val id: Long
    @JvmField
    val title: String
    val trackNumber: Int

    constructor() {
        id = -1
        albumId = -1
        artistId = -1
        title = ""
        artistName = ""
        albumName = ""
        duration = -1
        trackNumber = -1
    }

    constructor(_id: Long, _albumId: Long, _artistId: Long, _title: String, _artistName: String, _albumName: String, _duration: Int, _trackNumber: Int) {
        id = _id
        albumId = _albumId
        artistId = _artistId
        title = _title
        artistName = _artistName
        albumName = _albumName
        duration = _duration
        trackNumber = _trackNumber
    }
}