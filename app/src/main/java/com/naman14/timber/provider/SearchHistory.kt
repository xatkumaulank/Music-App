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
import java.util.*

class SearchHistory(context: Context?) {
    private var mMusicDatabase: MusicDB? = null
    fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + SearchHistoryColumns.NAME + " ("
                + SearchHistoryColumns.SEARCHSTRING + " STRING NOT NULL,"
                + SearchHistoryColumns.TIMESEARCHED + " LONG NOT NULL);")
    }

    fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {}
    fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS " + SearchHistoryColumns.NAME)
        onCreate(db)
    }

    fun addSearchString(searchString: String?) {
        if (searchString == null) {
            return
        }
        val trimmedString = searchString.trim { it <= ' ' }
        if (trimmedString.isEmpty()) {
            return
        }
        val database = mMusicDatabase!!.writableDatabase
        database.beginTransaction()
        try {
            database.delete(SearchHistoryColumns.NAME,
                    SearchHistoryColumns.SEARCHSTRING + " = ? COLLATE NOCASE", arrayOf(trimmedString))
            val values = ContentValues(2)
            values.put(SearchHistoryColumns.SEARCHSTRING, trimmedString)
            values.put(SearchHistoryColumns.TIMESEARCHED, System.currentTimeMillis())
            database.insert(SearchHistoryColumns.NAME, null, values)
            var oldest: Cursor? = null
            try {
                database.query(SearchHistoryColumns.NAME, arrayOf(SearchHistoryColumns.TIMESEARCHED), null, null, null, null,
                        SearchHistoryColumns.TIMESEARCHED + " ASC")
                if (oldest != null && oldest.count > MAX_ITEMS_IN_DB) {
                    oldest.moveToPosition(oldest.count - MAX_ITEMS_IN_DB)
                    val timeOfRecordToKeep = oldest.getLong(0)
                    database.delete(SearchHistoryColumns.NAME,
                            SearchHistoryColumns.TIMESEARCHED + " < ?", arrayOf(timeOfRecordToKeep.toString()))
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

    fun queryRecentSearches(limit: String?): Cursor {
        val database = mMusicDatabase!!.readableDatabase
        return database.query(SearchHistoryColumns.NAME, arrayOf(SearchHistoryColumns.SEARCHSTRING), null, null, null, null,
                SearchHistoryColumns.TIMESEARCHED + " DESC", limit)
    }

    val recentSearches: ArrayList<String>
        get() {
            var searches: Cursor? = queryRecentSearches(MAX_ITEMS_IN_DB.toString())
            val results = ArrayList<String>(MAX_ITEMS_IN_DB)
            try {
                if (searches != null && searches.moveToFirst()) {
                    val colIdx = searches.getColumnIndex(SearchHistoryColumns.SEARCHSTRING)
                    do {
                        results.add(searches.getString(colIdx))
                    } while (searches.moveToNext())
                }
            } finally {
                if (searches != null) {
                    searches.close()
                    searches = null
                }
            }
            return results
        }

    interface SearchHistoryColumns {
        companion object {
            /* Table name */
            const val NAME = "searchhistory"

            /* What was searched */
            const val SEARCHSTRING = "searchstring"

            /* Time of search */
            const val TIMESEARCHED = "timesearched"
        }
    }

    companion object {
        private const val MAX_ITEMS_IN_DB = 25
        private var sInstance: SearchHistory? = null
        @Synchronized
        fun getInstance(context: Context): SearchHistory? {
            if (sInstance == null) {
                sInstance = SearchHistory(context.applicationContext)
            }
            return sInstance
        }
    }

    init {
        mMusicDatabase = MusicDB.getInstance(context!!)
    }
}