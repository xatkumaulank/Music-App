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

class Artist {
    @JvmField
    val albumCount: Int
    @JvmField
    val id: Long
    @JvmField
    val name: String
    @JvmField
    val songCount: Int

    constructor() {
        id = -1
        name = ""
        songCount = -1
        albumCount = -1
    }

    constructor(_id: Long, _name: String, _albumCount: Int, _songCount: Int) {
        id = _id
        name = _name
        songCount = _songCount
        albumCount = _albumCount
    }
}