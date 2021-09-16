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
package com.naman14.timber.lastfmapi

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.naman14.timber.lastfmapi.callbacks.AlbumInfoListener
import com.naman14.timber.lastfmapi.callbacks.ArtistInfoListener
import com.naman14.timber.lastfmapi.callbacks.UserListener
import com.naman14.timber.lastfmapi.models.*
import com.naman14.timber.lastfmapi.models.LastfmUserSession.Companion.getSession
import com.naman14.timber.utils.PreferencesUtility
import retrofit.Callback
import retrofit.RetrofitError
import retrofit.client.Response
import java.io.UnsupportedEncodingException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*

class LastFmClient {
    private var mRestService: LastFmRestService? = null
    private var mUserRestService: LastFmUserRestService? = null
    private var queries: HashSet<String?>? = null
    private var isUploading = false
    private var context: Context? = null
    private var mUserSession: LastfmUserSession? = null
    fun getAlbumInfo(albumQuery: AlbumQuery, listener: AlbumInfoListener) {
        mRestService!!.getAlbumInfo(albumQuery.mArtist, albumQuery.mALbum, object : Callback<AlbumInfo> {
            override fun success(albumInfo: AlbumInfo, response: Response) {
                listener.albumInfoSuccess(albumInfo.mAlbum)
            }

            override fun failure(error: RetrofitError) {
                listener.albumInfoFailed()
                error.printStackTrace()
            }
        })
    }

    fun getArtistInfo(artistQuery: ArtistQuery, listener: ArtistInfoListener) {
        mRestService!!.getArtistInfo(artistQuery.mArtist, object : Callback<ArtistInfo> {
            override fun success(artistInfo: ArtistInfo, response: Response) {
                listener.artistInfoSucess(artistInfo.mArtist)
            }

            override fun failure(error: RetrofitError) {
                listener.artistInfoFailed()
                error.printStackTrace()
            }
        })
    }

    fun getUserLoginInfo(userLoginQuery: UserLoginQuery, listener: UserListener) {
        mUserRestService!!.getUserLoginInfo(UserLoginQuery.Method, JSON, API_KEY, generateMD5(userLoginQuery.signature), userLoginQuery.mUsername, userLoginQuery.mPassword, object : Callback<UserLoginInfo> {
            override fun success(userLoginInfo: UserLoginInfo, response: Response) {
                Log.d("Logedin", userLoginInfo.mSession!!.mToken + " " + userLoginInfo.mSession!!.mUsername)
                val extras = Bundle()
                extras.putString("lf_token", userLoginInfo.mSession!!.mToken)
                extras.putString("lf_user", userLoginInfo.mSession!!.mUsername)
                PreferencesUtility.getInstance(context).updateService(extras)
                mUserSession = userLoginInfo.mSession
                mUserSession!!.update(context!!)
                listener.userSuccess()
            }

            override fun failure(error: RetrofitError) {
                listener.userInfoFailed()
            }
        })
    }

    fun Scrobble(scrobbleQuery: ScrobbleQuery?) {
        if (mUserSession!!.isLogedin) ScrobbleUploader(scrobbleQuery)
    }

    private inner class ScrobbleUploader internal constructor(query: ScrobbleQuery?) {
        var cachedirty = false
        var newquery: ScrobbleQuery? = null
        var preferences = context!!.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        fun upload() {
            synchronized(sLock) { isUploading = true }
            var size = queries!!.size
            if (size == 0 && newquery == null) return
            //Max 50 Scrobbles per Request (restriction by LastFM)
            if (size > 50) size = 50
            if (newquery != null && size > 49) size = 49
            val currentqueries = arrayOfNulls<String>(size)
            var n = 0
            for (t in queries!!) {
                currentqueries[n++] = t
                if (n >= size) break
            }
            val fields = TreeMap<String, String?>()
            fields["method"] = ScrobbleQuery.Method
            fields["api_key"] = API_KEY
            fields["sk"] = mUserSession!!.mToken
            var i = 0
            for (squery in currentqueries) {
                val query = ScrobbleQuery(squery!!)
                fields["artist[$i]"] = query.mArtist
                fields["track[$i]"] = query.mTrack
                fields["timestamp[$i]"] = java.lang.Long.toString(query.mTimestamp)
                i++
            }
            if (newquery != null) {
                fields["artist[$i]"] = newquery!!.mArtist
                fields["track[$i]"] = newquery!!.mTrack
                fields["timestamp[$i]"] = java.lang.Long.toString(newquery!!.mTimestamp)
            }
            var sig = ""
            for ((key, value) in fields) {
                sig += key + value
            }
            sig += API_SECRET
            mUserRestService!!.getScrobbleInfo(generateMD5(sig), JSON, fields, object : Callback<ScrobbleInfo?> {
                override fun success(scrobbleInfo: ScrobbleInfo?, response: Response) {
                    synchronized(sLock) {
                        isUploading = false
                        cachedirty = true
                        if (newquery != null) newquery = null
                        for (squery in currentqueries) {
                            queries!!.remove(squery)
                        }
                        if (queries!!.size > 0) upload() else save()
                    }
                }

                override fun failure(error: RetrofitError) {
                    synchronized(sLock) {
                        isUploading = false
                        //Max 500 scrobbles in Cache
                        if (newquery != null && queries!!.size <= 500) queries!!.add(newquery.toString())
                        if (cachedirty) save()
                    }
                }
            })
        }

        fun save() {
            if (!cachedirty) return
            val editor = preferences.edit()
            editor.putStringSet(PREFERENCE_CACHE_NAME, queries)
            editor.apply()
        }

        init {
            if (queries == null) {
                queries = HashSet()
                queries!!.addAll(preferences.getStringSet(PREFERENCE_CACHE_NAME, HashSet()))
            }
            if (query != null) {
                synchronized(sLock) {
                    if (isUploading) {
                        cachedirty = true
                        queries!!.add(query.toString())
                        save()
                        return@synchronized
                    }
                }
                newquery = query
            }
            upload()
        }
    }

    fun logout() {
        mUserSession!!.mToken = null
        mUserSession!!.mUsername = null
        val preferences = context!!.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.clear()
        editor.apply()
    }

    val username: String?
        get() = if (mUserSession != null) mUserSession!!.mUsername else null

    companion object {
        //TODO update the api keys
        const val API_KEY = "62ac1851456e4558bef1c41747b1aec2"
        const val API_SECRET = "b4ae8965723d67fb18e35d207014d6f3"
        const val JSON = "json"
        const val BASE_API_URL = "http://ws.audioscrobbler.com/2.0"
        const val BASE_SECURE_API_URL = "https://ws.audioscrobbler.com/2.0"
        const val PREFERENCES_NAME = "Lastfm"
        const val PREFERENCE_CACHE_NAME = "Cache"
        private var sInstance: LastFmClient? = null
        private val sLock = Any()
        @JvmStatic
        fun getInstance(context: Context?): LastFmClient? {
            synchronized(sLock) {
                if (sInstance == null) {
                    sInstance = LastFmClient()
                    sInstance!!.context = context
                    sInstance!!.mRestService = RestServiceFactory.createStatic(context, BASE_API_URL, LastFmRestService::class.java)
                    sInstance!!.mUserRestService = RestServiceFactory.create(context, BASE_SECURE_API_URL, LastFmUserRestService::class.java)
                    sInstance!!.mUserSession = getSession(context!!)
                }
                return sInstance
            }
        }

        private fun generateMD5(`in`: String): String? {
            return try {
                val bytesOfMessage = `in`.toByteArray(charset("UTF-8"))
                val md = MessageDigest.getInstance("MD5")
                val digest = md.digest(bytesOfMessage)
                var out = ""
                for (symbol in digest) {
                    out += String.format("%02X", symbol)
                }
                out
            } catch (ignored: UnsupportedEncodingException) {
                null
            } catch (ignored: NoSuchAlgorithmException) {
                null
            }
        }
    }
}