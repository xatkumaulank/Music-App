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

class RecentStore(context: Context?) {
    private var mMusicDatabase: MusicDB? = null
    fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + RecentStoreColumns.NAME + " ("
                + RecentStoreColumns.ID + " LONG NOT NULL," + RecentStoreColumns.TIMEPLAYED
                + " LONG NOT NULL);")
    }

    fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {}
    fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS " + RecentStoreColumns.NAME)
        onCreate(db)
    }

    fun addSongId(songId: Long) {
        val database = mMusicDatabase!!.writableDatabase
        database.beginTransaction()
        try {
            var mostRecentItem: Cursor? = null
            try {
                mostRecentItem = queryRecentIds("1")
                if (mostRecentItem != null && mostRecentItem.moveToFirst()) {
                    if (songId == mostRecentItem.getLong(0)) {
                        return
                    }
                }
            } finally {
                if (mostRecentItem != null) {
                    mostRecentItem.close()
                    mostRecentItem = null
                }
            }
            val values = ContentValues(2)
            values.put(RecentStoreColumns.ID, songId)
            values.put(RecentStoreColumns.TIMEPLAYED, System.currentTimeMillis())
            database.insert(RecentStoreColumns.NAME, null, values)
            var oldest: Cursor? = null
            try {
                oldest = database.query(RecentStoreColumns.NAME, arrayOf(RecentStoreColumns.TIMEPLAYED), null, null, null, null,
                        RecentStoreColumns.TIMEPLAYED + " ASC")
                if (oldest != null && oldest.count > MAX_ITEMS_IN_DB) {
                    oldest.moveToPosition(oldest.count - MAX_ITEMS_IN_DB)
                    val timeOfRecordToKeep = oldest.getLong(0)
                    database.delete(RecentStoreColumns.NAME,
                            RecentStoreColumns.TIMEPLAYED + " < ?", arrayOf(timeOfRecordToKeep.toString()))
                }
            } finally {
                if (oldest != null) {
                    oldest.close()
                    oldest = null
                }
            }
        } finally {
            database.setTransactionSuccessful()
            database.endTransaction()
        }
    }

    fun removeItem(songId: Long) {
        val database = mMusicDatabase!!.writableDatabase
        database.delete(RecentStoreColumns.NAME, RecentStoreColumns.ID + " = ?", arrayOf(songId.toString()))
    }

    fun deleteAll() {
        val database = mMusicDatabase!!.writableDatabase
        database.delete(RecentStoreColumns.NAME, null, null)
    }

    fun queryRecentIds(limit: String?): Cursor {
        val database = mMusicDatabase!!.readableDatabase
        return database.query(RecentStoreColumns.NAME, arrayOf(RecentStoreColumns.ID), null, null, null, null,
                RecentStoreColumns.TIMEPLAYED + " DESC", limit)
    }

    interface RecentStoreColumns {
        companion object {
            /* Table name */
            const val NAME = "recenthistory"

            /* Album IDs column */
            const val ID = "songid"

            /* Time played column */
            const val TIMEPLAYED = "timeplayed"
        }
    }

    companion object {
        private const val MAX_ITEMS_IN_DB = 100
        private var sInstance: RecentStore? = null
        @JvmStatic
        @Synchronized
        fun getInstance(context: Context): RecentStore? {
            if (sInstance == null) {
                sInstance = RecentStore(context.applicationContext)
            }
            return sInstance
        }
    }

    init {
        mMusicDatabase = MusicDB.getInstance(context!!)
    }
}