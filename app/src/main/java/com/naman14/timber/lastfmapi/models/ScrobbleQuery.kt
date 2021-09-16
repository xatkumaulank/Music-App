package com.naman14.timber.lastfmapi.models

import com.google.gson.annotations.SerializedName
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.net.URLEncoder

/**
 * Created by christoph on 17.07.16.
 */
class ScrobbleQuery {
    @JvmField
    @SerializedName(ARTIST_NAME)
    var mArtist: String? = null

    @JvmField
    @SerializedName(TRACK_NAME)
    var mTrack: String? = null

    @JvmField
    @SerializedName(TIMESTAMP_NAME)
    var mTimestamp: Long = 0

    constructor(`in`: String) {
        val arr = `in`.split(",").toTypedArray()
        try {
            mArtist = URLDecoder.decode(arr[0], "UTF-8")
            mTrack = URLDecoder.decode(arr[1], "UTF-8")
            mTimestamp = arr[2].toLong(16)
        } catch (ignored: UnsupportedEncodingException) {
        }
    }

    constructor(artist: String?, track: String?, timestamp: Long) {
        mArtist = artist
        mTrack = track
        mTimestamp = timestamp
    }

    override fun toString(): String {
        return try {
            URLEncoder.encode(mArtist, "UTF-8") + ',' + URLEncoder.encode(mTrack, "UTF-8") + ',' + java.lang.Long.toHexString(mTimestamp)
        } catch (ignored: UnsupportedEncodingException) {
            ""
        }
    }

    companion object {
        private const val ARTIST_NAME = "artist"
        private const val TRACK_NAME = "track"
        private const val TIMESTAMP_NAME = "timestamp"
        const val Method = "track.scrobble"
    }
}