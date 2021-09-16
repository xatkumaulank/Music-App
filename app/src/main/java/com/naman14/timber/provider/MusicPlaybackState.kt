/*
* Copyright (C) 2014 The CyanogenMod Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.naman14.timber.provider

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.naman14.timber.helpers.MusicPlaybackTrack
import com.naman14.timber.utils.TimberUtils
import java.util.*

/**
 * This keeps track of the music playback and history state of the playback service
 */
class MusicPlaybackState(context: Context?) {
    private var mMusicDatabase: MusicDB? = null
    fun onCreate(db: SQLiteDatabase) {
        var builder = StringBuilder()
        builder.append("CREATE TABLE IF NOT EXISTS ")
        builder.append(PlaybackQueueColumns.NAME)
        builder.append("(")
        builder.append(PlaybackQueueColumns.TRACK_ID)
        builder.append(" LONG NOT NULL,")
        builder.append(PlaybackQueueColumns.SOURCE_ID)
        builder.append(" LONG NOT NULL,")
        builder.append(PlaybackQueueColumns.SOURCE_TYPE)
        builder.append(" INT NOT NULL,")
        builder.append(PlaybackQueueColumns.SOURCE_POSITION)
        builder.append(" INT NOT NULL);")
        db.execSQL(builder.toString())
        builder = StringBuilder()
        builder.append("CREATE TABLE IF NOT EXISTS ")
        builder.append(PlaybackHistoryColumns.NAME)
        builder.append("(")
        builder.append(PlaybackHistoryColumns.POSITION)
        builder.append(" INT NOT NULL);")
        db.execSQL(builder.toString())
    }

    fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // this table was created in version 2 so call the onCreate method if we hit that scenario
        if (oldVersion < 2 && newVersion >= 2) {
            onCreate(db)
        }
    }

    fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS " + PlaybackQueueColumns.NAME)
        db.execSQL("DROP TABLE IF EXISTS " + PlaybackHistoryColumns.NAME)
        onCreate(db)
    }

    @Synchronized
    fun saveState(queue: ArrayList<MusicPlaybackTrack>,
                  history: LinkedList<Int?>?) {
        val database = mMusicDatabase!!.writableDatabase
        database.beginTransaction()
        try {
            database.delete(PlaybackQueueColumns.NAME, null, null)
            database.delete(PlaybackHistoryColumns.NAME, null, null)
            database.setTransactionSuccessful()
        } finally {
            database.endTransaction()
        }
        val NUM_PROCESS = 20
        var position = 0
        while (position < queue.size) {
            database.beginTransaction()
            try {
                var i = position
                while (i < queue.size && i < position + NUM_PROCESS) {
                    val track = queue[i]
                    val values = ContentValues(4)
                    values.put(PlaybackQueueColumns.TRACK_ID, track.mId)
                    values.put(PlaybackQueueColumns.SOURCE_ID, track.mSourceId)
                    values.put(PlaybackQueueColumns.SOURCE_TYPE, track.mSourceType.mId)
                    values.put(PlaybackQueueColumns.SOURCE_POSITION, track.mSourcePosition)
                    database.insert(PlaybackQueueColumns.NAME, null, values)
                    i++
                }
                database.setTransactionSuccessful()
            } finally {
                database.endTransaction()
                position += NUM_PROCESS
            }
        }
        if (history != null) {
            val iter: Iterator<Int?> = history.iterator()
            while (iter.hasNext()) {
                database.beginTransaction()
                try {
                    var i = 0
                    while (iter.hasNext() && i < NUM_PROCESS) {
                        val values = ContentValues(1)
                        values.put(PlaybackHistoryColumns.POSITION, iter.next())
                        database.insert(PlaybackHistoryColumns.NAME, null, values)
                        i++
                    }
                    database.setTransactionSuccessful()
                } finally {
                    database.endTransaction()
                }
            }
        }
    }

    val queue: ArrayList<MusicPlaybackTrack>
        get() {
            val results = ArrayList<MusicPlaybackTrack>()
            var cursor: Cursor? = null
            return try {
                cursor = mMusicDatabase!!.readableDatabase.query(PlaybackQueueColumns.NAME, null,
                        null, null, null, null, null)
                if (cursor != null && cursor.moveToFirst()) {
                    results.ensureCapacity(cursor.count)
                    do {
                        results.add(MusicPlaybackTrack(cursor.getLong(0), cursor.getLong(1),
                                TimberUtils.IdType.getTypeById(cursor.getInt(2)), cursor.getInt(3)))
                    } while (cursor.moveToNext())
                }
                results
            } finally {
                if (cursor != null) {
                    cursor.close()
                    cursor = null
                }
            }
        }

    fun getHistory(playlistSize: Int): LinkedList<Int> {
        val results = LinkedList<Int>()
        var cursor: Cursor? = null
        return try {
            cursor = mMusicDatabase!!.readableDatabase.query(PlaybackHistoryColumns.NAME, null,
                    null, null, null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    val pos = cursor.getInt(0)
                    if (pos >= 0 && pos < playlistSize) {
                        results.add(pos)
                    }
                } while (cursor.moveToNext())
            }
            results
        } finally {
            if (cursor != null) {
                cursor.close()
                cursor = null
            }
        }
    }

    object PlaybackQueueColumns {
        const val NAME = "playbackqueue"
        const val TRACK_ID = "trackid"
        const val SOURCE_ID = "sourceid"
        const val SOURCE_TYPE = "sourcetype"
        const val SOURCE_POSITION = "sourceposition"
    }

    object PlaybackHistoryColumns {
        const val NAME = "playbackhistory"
        const val POSITION = "position"
    }

    companion object {
        private var sInstance: MusicPlaybackState? = null
        @JvmStatic
        @Synchronized
        fun getInstance(context: Context): MusicPlaybackState? {
            if (sInstance == null) {
                sInstance = MusicPlaybackState(context.applicationContext)
            }
            return sInstance
        }
    }

    init {
        mMusicDatabase = MusicDB.getInstance(context!!)
    }
}