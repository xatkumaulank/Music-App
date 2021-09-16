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
package com.naman14.timber.dataloaders

import android.content.Context
import android.database.Cursor
import android.provider.BaseColumns
import com.naman14.timber.provider.RecentStore
import com.naman14.timber.provider.SongPlayCount

class TopTracksLoader(private var mContext: Context, protected var mQueryType: QueryType) : SongLoader() {
    enum class QueryType {
        TopTracks, RecentSongs
    }

    companion object {
        protected var mQueryType: QueryType? = null
        private val mContext: Context? = null
        const val NUMBER_OF_SONGS = 99
        @JvmStatic
        val cursor: Cursor?
            get() {
                var retCursor: SortedCursor? = null
                if (Companion.mQueryType == QueryType.TopTracks) {
                    retCursor = makeTopTracksCursor(Companion.mContext)
                } else if (Companion.mQueryType == QueryType.RecentSongs) {
                    retCursor = makeRecentTracksCursor(Companion.mContext)
                }
                if (retCursor != null) {
                    val missingIds = retCursor.missingIds
                    if (missingIds != null && missingIds.size > 0) {
                        for (id in missingIds) {
                            if (Companion.mQueryType == QueryType.TopTracks) {
                                SongPlayCount.getInstance(Companion.mContext).removeItem(id)
                            } else if (Companion.mQueryType == QueryType.RecentSongs) {
                                RecentStore.getInstance(Companion.mContext).removeItem(id)
                            }
                        }
                    }
                }
                return retCursor
            }

        fun makeTopTracksCursor(context: Context?): SortedCursor? {
            var songs = SongPlayCount.getInstance(context).getTopPlayedResults(NUMBER_OF_SONGS)
            return try {
                makeSortedCursor(context, songs,
                        songs!!.getColumnIndex(SongPlayCount.SongPlayCountColumns.ID))
            } finally {
                if (songs != null) {
                    songs.close()
                    songs = null
                }
            }
        }

        fun makeRecentTracksCursor(context: Context?): SortedCursor? {
            var songs = RecentStore.getInstance(context).queryRecentIds(null)
            return try {
                makeSortedCursor(context, songs,
                        songs!!.getColumnIndex(SongPlayCount.SongPlayCountColumns.ID))
            } finally {
                if (songs != null) {
                    songs.close()
                    songs = null
                }
            }
        }

        fun makeSortedCursor(context: Context?, cursor: Cursor?,
                             idColumn: Int): SortedCursor? {
            if (cursor != null && cursor.moveToFirst()) {
                val selection = StringBuilder()
                selection.append(BaseColumns._ID)
                selection.append(" IN (")
                val order = LongArray(cursor.count)
                var id = cursor.getLong(idColumn)
                selection.append(id)
                order[cursor.position] = id
                while (cursor.moveToNext()) {
                    selection.append(",")
                    id = cursor.getLong(idColumn)
                    order[cursor.position] = id
                    selection.append(id.toString())
                }
                selection.append(")")
                val songCursor = makeSongCursor(context!!, selection.toString(), null)
                if (songCursor != null) {
                    return SortedCursor(songCursor, order, BaseColumns._ID, null)
                }
            }
            return null
        }
    }
}