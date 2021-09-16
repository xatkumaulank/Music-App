/*
 * Copyright (C) 2012 Andrew Neal
 * Copyright (C) 2014 The CyanogenMod Project
 * Copyright (C) 2015 Naman Dwivedi
 * Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package com.naman14.timber.dataloaders

import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import android.provider.MediaStore.Audio.AudioColumns
import com.naman14.timber.models.Song
import com.naman14.timber.utils.PreferencesUtility
import java.util.*

object LastAddedLoader {
    private var mCursor: Cursor? = null
    @JvmStatic
    fun getLastAddedSongs(context: Context): List<Song> {
        val mSongList = ArrayList<Song>()
        mCursor = makeLastAddedCursor(context)
        if (mCursor != null && mCursor!!.moveToFirst()) {
            do {
                val id = mCursor!!.getLong(0)
                val title = mCursor!!.getString(1)
                val artist = mCursor!!.getString(2)
                val album = mCursor!!.getString(3)
                val duration = mCursor!!.getInt(4)
                val trackNumber = mCursor!!.getInt(5)
                val artistId = mCursor!!.getInt(6).toLong()
                val albumId = mCursor!!.getLong(7)
                val song = Song(id, albumId, artistId, title, artist, album, duration, trackNumber)
                mSongList.add(song)
            } while (mCursor!!.moveToNext())
        }
        if (mCursor != null) {
            mCursor!!.close()
            mCursor = null
        }
        return mSongList
    }

    fun makeLastAddedCursor(context: Context): Cursor {
        //four weeks ago
        val fourWeeksAgo = System.currentTimeMillis() / 1000 - 4 * 3600 * 24 * 7
        var cutoff = PreferencesUtility.getInstance(context).lastAddedCutoff
        // use the most recent of the two timestamps
        if (cutoff < fourWeeksAgo) {
            cutoff = fourWeeksAgo
        }
        val selection = StringBuilder()
        selection.append(AudioColumns.IS_MUSIC + "=1")
        selection.append(" AND " + AudioColumns.TITLE + " != ''")
        selection.append(" AND " + MediaStore.Audio.Media.DATE_ADDED + ">")
        selection.append(cutoff)
        return context.contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, arrayOf("_id", "title", "artist", "album", "duration", "track", "artist_id", "album_id"), selection.toString(), null, MediaStore.Audio.Media.DATE_ADDED + " DESC")
    }
}