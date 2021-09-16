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

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class MusicDB(private val mContext: Context) : SQLiteOpenHelper(mContext, DATABASENAME, null, VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        MusicPlaybackState.getInstance(mContext).onCreate(db)
        RecentStore.getInstance(mContext).onCreate(db)
        SongPlayCount.getInstance(mContext).onCreate(db)
        SearchHistory.getInstance(mContext).onCreate(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        MusicPlaybackState.getInstance(mContext).onUpgrade(db, oldVersion, newVersion)
        RecentStore.getInstance(mContext).onUpgrade(db, oldVersion, newVersion)
        SongPlayCount.getInstance(mContext).onUpgrade(db, oldVersion, newVersion)
        SearchHistory.getInstance(mContext).onUpgrade(db, oldVersion, newVersion)
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        MusicPlaybackState.getInstance(mContext).onDowngrade(db, oldVersion, newVersion)
        RecentStore.getInstance(mContext).onDowngrade(db, oldVersion, newVersion)
        SongPlayCount.getInstance(mContext).onDowngrade(db, oldVersion, newVersion)
        SearchHistory.getInstance(mContext).onDowngrade(db, oldVersion, newVersion)
    }

    companion object {
        const val DATABASENAME = "musicdb.db"
        private const val VERSION = 4
        private var sInstance: MusicDB? = null
        @JvmStatic
        @Synchronized
        fun getInstance(context: Context): MusicDB? {
            if (sInstance == null) {
                sInstance = MusicDB(context.applicationContext)
            }
            return sInstance
        }
    }
}