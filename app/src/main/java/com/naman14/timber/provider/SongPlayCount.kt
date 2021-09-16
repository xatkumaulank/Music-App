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
import android.view.animation.AccelerateInterpolator
import android.view.animation.Interpolator
import java.util.*

/**
 * This database tracks the number of play counts for an individual song.  This is used to drive
 * the top played tracks as well as the playlist images
 */
class SongPlayCount(context: Context?) {
    private var mMusicDatabase: MusicDB? = null

    // number of weeks since epoch time
    private val mNumberOfWeeksSinceEpoch: Int

    // used to track if we've walkd through the db and updated all the rows
    private var mDatabaseUpdated: Boolean
    fun onCreate(db: SQLiteDatabase) {
        // create the play count table
        // WARNING: If you change the order of these columns
        // please update getColumnIndexForWeek
        val builder = StringBuilder()
        builder.append("CREATE TABLE IF NOT EXISTS ")
        builder.append(SongPlayCountColumns.NAME)
        builder.append("(")
        builder.append(SongPlayCountColumns.ID)
        builder.append(" INT UNIQUE,")
        for (i in 0 until NUM_WEEKS) {
            builder.append(getColumnNameForWeek(i))
            builder.append(" INT DEFAULT 0,")
        }
        builder.append(SongPlayCountColumns.LAST_UPDATED_WEEK_INDEX)
        builder.append(" INT NOT NULL,")
        builder.append(SongPlayCountColumns.PLAYCOUNTSCORE)
        builder.append(" REAL DEFAULT 0);")
        db.execSQL(builder.toString())
    }

    fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // No upgrade path needed yet
    }

    fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // If we ever have downgrade, drop the table to be safe
        db.execSQL("DROP TABLE IF EXISTS " + SongPlayCountColumns.NAME)
        onCreate(db)
    }

    /**
     * Increases the play count of a song by 1
     *
     * @param songId The song id to increase the play count
     */
    fun bumpSongCount(songId: Long) {
        if (songId < 0) {
            return
        }
        val database = mMusicDatabase!!.writableDatabase
        updateExistingRow(database, songId, true)
    }

    /**
     * This creates a new entry that indicates a song has been played once as well as its score
     *
     * @param database a writeable database
     * @param songId   the id of the track
     */
    private fun createNewPlayedEntry(database: SQLiteDatabase, songId: Long) {
        // no row exists, create a new one
        val newScore = getScoreMultiplierForWeek(0)
        val newPlayCount = 1
        val values = ContentValues(3)
        values.put(SongPlayCountColumns.ID, songId)
        values.put(SongPlayCountColumns.PLAYCOUNTSCORE, newScore)
        values.put(SongPlayCountColumns.LAST_UPDATED_WEEK_INDEX, mNumberOfWeeksSinceEpoch)
        values.put(getColumnNameForWeek(0), newPlayCount)
        database.insert(SongPlayCountColumns.NAME, null, values)
    }

    /**
     * This function will take a song entry and update it to the latest week and increase the count
     * for the current week by 1 if necessary
     *
     * @param database  a writeable database
     * @param id        the id of the track to bump
     * @param bumpCount whether to bump the current's week play count by 1 and adjust the score
     */
    private fun updateExistingRow(database: SQLiteDatabase, id: Long, bumpCount: Boolean) {
        val stringId = id.toString()

        // begin the transaction
        database.beginTransaction()

        // get the cursor of this content inside the transaction
        val cursor = database.query(SongPlayCountColumns.NAME, null, WHERE_ID_EQUALS, arrayOf(stringId), null, null, null)

        // if we have a result
        if (cursor != null && cursor.moveToFirst()) {
            // figure how many weeks since we last updated
            val lastUpdatedIndex = cursor.getColumnIndex(SongPlayCountColumns.LAST_UPDATED_WEEK_INDEX)
            val lastUpdatedWeek = cursor.getInt(lastUpdatedIndex)
            val weekDiff = mNumberOfWeeksSinceEpoch - lastUpdatedWeek

            // if it's more than the number of weeks we track, delete it and create a new entry
            if (Math.abs(weekDiff) >= NUM_WEEKS) {
                // this entry needs to be dropped since it is too outdated
                deleteEntry(database, stringId)
                if (bumpCount) {
                    createNewPlayedEntry(database, id)
                }
            } else if (weekDiff != 0) {
                // else, shift the weeks
                val playCounts = IntArray(NUM_WEEKS)
                if (weekDiff > 0) {
                    // time is shifted forwards
                    for (i in 0 until NUM_WEEKS - weekDiff) {
                        playCounts[i + weekDiff] = cursor.getInt(getColumnIndexForWeek(i))
                    }
                } else if (weekDiff < 0) {
                    // time is shifted backwards (by user) - nor typical behavior but we
                    // will still handle it

                    // since weekDiff is -ve, NUM_WEEKS + weekDiff is the real # of weeks we have to
                    // transfer.  Then we transfer the old week i - weekDiff to week i
                    // for example if the user shifted back 2 weeks, ie -2, then for 0 to
                    // NUM_WEEKS + (-2) we set the new week i = old week i - (-2) or i+2
                    for (i in 0 until NUM_WEEKS + weekDiff) {
                        playCounts[i] = cursor.getInt(getColumnIndexForWeek(i - weekDiff))
                    }
                }

                // bump the count
                if (bumpCount) {
                    playCounts[0]++
                }
                val score = calculateScore(playCounts)

                // if the score is non-existant, then delete it
                if (score < .01f) {
                    deleteEntry(database, stringId)
                } else {
                    // create the content values
                    val values = ContentValues(NUM_WEEKS + 2)
                    values.put(SongPlayCountColumns.LAST_UPDATED_WEEK_INDEX, mNumberOfWeeksSinceEpoch)
                    values.put(SongPlayCountColumns.PLAYCOUNTSCORE, score)
                    for (i in 0 until NUM_WEEKS) {
                        values.put(getColumnNameForWeek(i), playCounts[i])
                    }

                    // update the entry
                    database.update(SongPlayCountColumns.NAME, values, WHERE_ID_EQUALS, arrayOf(stringId))
                }
            } else if (bumpCount) {
                // else no shifting, just update the scores
                val values = ContentValues(2)

                // increase the score by a single score amount
                val scoreIndex = cursor.getColumnIndex(SongPlayCountColumns.PLAYCOUNTSCORE)
                val score = cursor.getFloat(scoreIndex) + getScoreMultiplierForWeek(0)
                values.put(SongPlayCountColumns.PLAYCOUNTSCORE, score)

                // increase the play count by 1
                values.put(getColumnNameForWeek(0), cursor.getInt(getColumnIndexForWeek(0)) + 1)

                // update the entry
                database.update(SongPlayCountColumns.NAME, values, WHERE_ID_EQUALS, arrayOf(stringId))
            }
            cursor.close()
        } else if (bumpCount) {
            // if we have no existing results, create a new one
            createNewPlayedEntry(database, id)
        }
        database.setTransactionSuccessful()
        database.endTransaction()
    }

    fun deleteAll() {
        val database = mMusicDatabase!!.writableDatabase
        database.delete(SongPlayCountColumns.NAME, null, null)
    }

    /**
     * Gets a cursor containing the top songs played.  Note this only returns songs that have been
     * played at least once in the past NUM_WEEKS
     *
     * @param numResults number of results to limit by.  If <= 0 it returns all results
     * @return the top tracks
     */
    fun getTopPlayedResults(numResults: Int): Cursor {
        updateResults()
        val database = mMusicDatabase!!.readableDatabase
        return database.query(SongPlayCountColumns.NAME, arrayOf(SongPlayCountColumns.ID),
                null, null, null, null, SongPlayCountColumns.PLAYCOUNTSCORE + " DESC",
                if (numResults <= 0) null else numResults.toString())
    }

    /**
     * Given a list of ids, it sorts the results based on the most played results
     *
     * @param ids list
     * @return sorted list - this may be smaller than the list passed in for performance reasons
     */
    fun getTopPlayedResultsForList(ids: LongArray?): LongArray? {
        val MAX_NUMBER_SONGS_TO_ANALYZE = 250
        if (ids == null || ids.size == 0) {
            return null
        }
        val uniqueIds = HashSet<Long>(ids.size)

        // create the list of ids to select against
        val selection = StringBuilder()
        selection.append(SongPlayCountColumns.ID)
        selection.append(" IN (")

        // add the first element to handle the separator case for the first element
        uniqueIds.add(ids[0])
        selection.append(ids[0])
        for (i in 1 until ids.size) {
            // if the new id doesn't exist
            if (uniqueIds.add(ids[i])) {
                // append a separator
                selection.append(",")

                // append the id
                selection.append(ids[i])

                // for performance reasons, only look at a certain number of songs
                // in case their playlist is ridiculously large
                if (uniqueIds.size >= MAX_NUMBER_SONGS_TO_ANALYZE) {
                    break
                }
            }
        }

        // close out the selection
        selection.append(")")
        val sortedList = LongArray(uniqueIds.size)

        // now query for the songs
        val database = mMusicDatabase!!.readableDatabase
        var topSongsCursor: Cursor? = null
        var idx = 0
        try {
            topSongsCursor = database.query(SongPlayCountColumns.NAME, arrayOf(SongPlayCountColumns.ID), selection.toString(), null, null,
                    null, SongPlayCountColumns.PLAYCOUNTSCORE + " DESC")
            if (topSongsCursor != null && topSongsCursor.moveToFirst()) {
                do {
                    // for each id found, add it to the list and remove it from the unique ids
                    val id = topSongsCursor.getLong(0)
                    sortedList[idx++] = id
                    uniqueIds.remove(id)
                } while (topSongsCursor.moveToNext())
            }
        } finally {
            if (topSongsCursor != null) {
                topSongsCursor.close()
                topSongsCursor = null
            }
        }

        // append the remaining items - these are songs that haven't been played recently
        val iter: Iterator<Long> = uniqueIds.iterator()
        while (iter.hasNext()) {
            sortedList[idx++] = iter.next()
        }
        return sortedList
    }

    /**
     * This updates all the results for the getTopPlayedResults so that we can get an
     * accurate list of the top played results
     */
    @Synchronized
    private fun updateResults() {
        if (mDatabaseUpdated) {
            return
        }
        val database = mMusicDatabase!!.writableDatabase
        database.beginTransaction()
        val oldestWeekWeCareAbout = mNumberOfWeeksSinceEpoch - NUM_WEEKS + 1
        // delete rows we don't care about anymore
        database.delete(SongPlayCountColumns.NAME, SongPlayCountColumns.LAST_UPDATED_WEEK_INDEX
                + " < " + oldestWeekWeCareAbout, null)

        // get the remaining rows
        var cursor = database.query(SongPlayCountColumns.NAME, arrayOf(SongPlayCountColumns.ID),
                null, null, null, null, null)
        if (cursor != null && cursor.moveToFirst()) {
            // for each row, update it
            do {
                updateExistingRow(database, cursor.getLong(0), false)
            } while (cursor.moveToNext())
            cursor.close()
            cursor = null
        }
        mDatabaseUpdated = true
        database.setTransactionSuccessful()
        database.endTransaction()
    }

    /**
     * @param songId The song Id to remove.
     */
    fun removeItem(songId: Long) {
        val database = mMusicDatabase!!.writableDatabase
        deleteEntry(database, songId.toString())
    }

    /**
     * Deletes the entry
     *
     * @param database database to use
     * @param stringId id to delete
     */
    private fun deleteEntry(database: SQLiteDatabase, stringId: String) {
        database.delete(SongPlayCountColumns.NAME, WHERE_ID_EQUALS, arrayOf(stringId))
    }

    interface SongPlayCountColumns {
        companion object {
            /* Table name */
            const val NAME = "songplaycount"

            /* Song IDs column */
            const val ID = "songid"

            /* Week Play Count */
            const val WEEK_PLAY_COUNT = "week"

            /* Weeks since Epoch */
            const val LAST_UPDATED_WEEK_INDEX = "weekindex"

            /* Play count */
            const val PLAYCOUNTSCORE = "playcountscore"
        }
    }

    companion object {
        // how many weeks worth of playback to track
        private const val NUM_WEEKS = 52
        private var sInstance: SongPlayCount? = null

        // interpolator curve applied for measuring the curve
        private val sInterpolator: Interpolator = AccelerateInterpolator(1.5f)

        // how high to multiply the interpolation curve
        private const val INTERPOLATOR_HEIGHT = 50

        // how high the base value is. The ratio of the Height to Base is what really matters
        private const val INTERPOLATOR_BASE = 25
        private const val ONE_WEEK_IN_MS = 1000 * 60 * 60 * 24 * 7
        private const val WHERE_ID_EQUALS = SongPlayCountColumns.ID + "=?"

        /**
         * @param context The [android.content.Context] to use
         * @return A new instance of this class.
         */
        @JvmStatic
        @Synchronized
        fun getInstance(context: Context): SongPlayCount? {
            if (sInstance == null) {
                sInstance = SongPlayCount(context.applicationContext)
            }
            return sInstance
        }

        /**
         * Calculates the score of the song given the play counts
         *
         * @param playCounts an array of the # of times a song has been played for each week
         * where playCounts[N] is the # of times it was played N weeks ago
         * @return the score
         */
        private fun calculateScore(playCounts: IntArray?): Float {
            if (playCounts == null) {
                return 0F
            }
            var score = 0f
            for (i in 0 until Math.min(playCounts.size, NUM_WEEKS)) {
                score += playCounts[i] * getScoreMultiplierForWeek(i)
            }
            return score
        }

        /**
         * Gets the column name for each week #
         *
         * @param week number
         * @return the column name
         */
        private fun getColumnNameForWeek(week: Int): String {
            return SongPlayCountColumns.WEEK_PLAY_COUNT + week.toString()
        }

        /**
         * Gets the score multiplier for each week
         *
         * @param week number
         * @return the multiplier to apply
         */
        private fun getScoreMultiplierForWeek(week: Int): Float {
            return (sInterpolator.getInterpolation(1 - week / NUM_WEEKS.toFloat()) * INTERPOLATOR_HEIGHT
                    + INTERPOLATOR_BASE)
        }

        /**
         * For some performance gain, return a static value for the column index for a week
         * WARNIGN: This function assumes you have selected all columns for it to work
         *
         * @param week number
         * @return column index of that week
         */
        private fun getColumnIndexForWeek(week: Int): Int {
            // ID, followed by the weeks columns
            return 1 + week
        }
    }

    /**
     * Constructor of `RecentStore`
     *
     * @param context The [android.content.Context] to use
     */
    init {
        mMusicDatabase = MusicDB.getInstance(context!!)
        val msSinceEpoch = System.currentTimeMillis()
        mNumberOfWeeksSinceEpoch = (msSinceEpoch / ONE_WEEK_IN_MS).toInt()
        mDatabaseUpdated = false
    }
}