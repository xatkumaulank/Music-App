/*
 * Copyright (C) 2012 Andrew Neal
 * Copyright (C) 2014 The CyanogenMod Project
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
import android.database.AbstractCursor
import android.database.Cursor
import android.os.RemoteException
import android.provider.BaseColumns
import android.provider.MediaStore
import android.provider.MediaStore.Audio.AudioColumns
import android.util.Log
import com.naman14.timber.MusicPlayer
import java.util.*

class NowPlayingCursor(private val mContext: Context) : AbstractCursor() {
    private var mNowPlaying: LongArray? = null
    private var mCursorIndexes: LongArray? = null
    private var mSize = 0
    private var mCurPos = 0
    private var mQueueCursor: Cursor? = null
    override fun getCount(): Int {
        return mSize
    }

    override fun onMove(oldPosition: Int, newPosition: Int): Boolean {
        if (oldPosition == newPosition) {
            return true
        }
        if (mNowPlaying == null || mCursorIndexes == null || newPosition >= mNowPlaying!!.size) {
            return false
        }
        val id = mNowPlaying!![newPosition]
        val cursorIndex = Arrays.binarySearch(mCursorIndexes, id)
        mQueueCursor!!.moveToPosition(cursorIndex)
        mCurPos = newPosition
        return true
    }

    override fun getString(column: Int): String {
        return try {
            mQueueCursor!!.getString(column)
        } catch (ignored: Exception) {
            onChange(true)
            ""
        }
    }

    override fun getShort(column: Int): Short {
        return mQueueCursor!!.getShort(column)
    }

    override fun getInt(column: Int): Int {
        return try {
            mQueueCursor!!.getInt(column)
        } catch (ignored: Exception) {
            onChange(true)
            0
        }
    }

    override fun getLong(column: Int): Long {
        return try {
            mQueueCursor!!.getLong(column)
        } catch (ignored: Exception) {
            onChange(true)
            0
        }
    }

    override fun getFloat(column: Int): Float {
        return mQueueCursor!!.getFloat(column)
    }

    override fun getDouble(column: Int): Double {
        return mQueueCursor!!.getDouble(column)
    }

    override fun getType(column: Int): Int {
        return mQueueCursor!!.getType(column)
    }

    override fun isNull(column: Int): Boolean {
        return mQueueCursor!!.isNull(column)
    }

    override fun getColumnNames(): Array<String> {
        return PROJECTION
    }

    override fun deactivate() {
        if (mQueueCursor != null) {
            mQueueCursor!!.deactivate()
        }
    }

    override fun requery(): Boolean {
        makeNowPlayingCursor()
        return true
    }

    override fun close() {
        try {
            if (mQueueCursor != null) {
                mQueueCursor!!.close()
                mQueueCursor = null
            }
        } catch (close: Exception) {
        }
        super.close()
    }

    private fun makeNowPlayingCursor() {
        mQueueCursor = null
        mNowPlaying = MusicPlayer.getQueue()
        Log.d("lol1", mNowPlaying.toString() + "   " + mNowPlaying!!.size)
        mSize = mNowPlaying!!.size
        if (mSize == 0) {
            return
        }
        val selection = StringBuilder()
        selection.append(MediaStore.Audio.Media._ID + " IN (")
        for (i in 0 until mSize) {
            selection.append(mNowPlaying!![i])
            if (i < mSize - 1) {
                selection.append(",")
            }
        }
        selection.append(")")
        mQueueCursor = mContext.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, PROJECTION, selection.toString(),
                null, MediaStore.Audio.Media._ID)
        if (mQueueCursor == null) {
            mSize = 0
            return
        }
        val playlistSize = mQueueCursor!!.count
        mCursorIndexes = LongArray(playlistSize)
        mQueueCursor!!.moveToFirst()
        val columnIndex = mQueueCursor!!.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
        for (i in 0 until playlistSize) {
            mCursorIndexes!![i] = mQueueCursor!!.getLong(columnIndex)
            mQueueCursor!!.moveToNext()
        }
        mQueueCursor!!.moveToFirst()
        mCurPos = -1
        var removed = 0
        for (i in mNowPlaying!!.indices.reversed()) {
            val trackId = mNowPlaying!![i]
            val cursorIndex = Arrays.binarySearch(mCursorIndexes, trackId)
            if (cursorIndex < 0) {
                removed += MusicPlayer.removeTrack(trackId)
            }
        }
        if (removed > 0) {
            mNowPlaying = MusicPlayer.getQueue()
            mSize = mNowPlaying!!.size
            if (mSize == 0) {
                mCursorIndexes = null
                return
            }
        }
    }

    fun removeItem(which: Int): Boolean {
        try {
            if (MusicPlayer.mService.removeTracks(which, which) == 0) {
                return false
            }
            var i = which
            mSize--
            while (i < mSize) {
                mNowPlaying!![i] = mNowPlaying!![i + 1]
                i++
            }
            onMove(-1, mCurPos)
        } catch (ignored: RemoteException) {
        }
        return true
    }

    companion object {
        private val PROJECTION = arrayOf(
                BaseColumns._ID,
                AudioColumns.TITLE,
                AudioColumns.ARTIST,
                AudioColumns.ALBUM_ID,
                AudioColumns.ALBUM,
                AudioColumns.DURATION,
                AudioColumns.TRACK,
                AudioColumns.ARTIST_ID,
                AudioColumns.TRACK)
    }

    init {
        makeNowPlayingCursor()
    }
}