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
import com.naman14.timber.models.Song
import java.util.*

object QueueLoader {
    private var mCursor: NowPlayingCursor? = null
    @JvmStatic
    fun getQueueSongs(context: Context?): MutableList<Song> {
        val mSongList = ArrayList<Song>()
        mCursor = NowPlayingCursor(context!!)
        if (mCursor != null && mCursor!!.moveToFirst()) {
            do {
                val id = mCursor!!.getLong(0)
                val songName = mCursor!!.getString(1)
                val artist = mCursor!!.getString(2)
                val albumId = mCursor!!.getLong(3)
                val album = mCursor!!.getString(4)
                val duration = mCursor!!.getInt(5)
                val artistid = mCursor!!.getInt(7).toLong()
                val tracknumber = mCursor!!.getInt(6)
                val song = Song(id, albumId, artistid, songName, artist, album, duration, tracknumber)
                mSongList.add(song)
            } while (mCursor!!.moveToNext())
        }
        if (mCursor != null) {
            mCursor!!.close()
            mCursor = null
        }
        return mSongList
    }
}