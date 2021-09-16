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
import com.naman14.timber.utils.PreferencesUtility
import com.squareup.okhttp.Cache
import com.squareup.okhttp.OkHttpClient
import retrofit.RequestInterceptor
import retrofit.RequestInterceptor.RequestFacade
import retrofit.RestAdapter
import retrofit.client.OkClient
import java.util.concurrent.TimeUnit

object RestServiceFactory {
    private const val TAG_OK_HTTP = "OkHttp"
    private const val CACHE_SIZE = (1024 * 1024).toLong()
    fun <T> createStatic(context: Context, baseUrl: String?, clazz: Class<T>?): T {
        val okHttpClient = OkHttpClient()
        okHttpClient.cache = Cache(context.applicationContext.cacheDir,
                CACHE_SIZE)
        okHttpClient.setConnectTimeout(40, TimeUnit.SECONDS)
        val interceptor: RequestInterceptor = object : RequestInterceptor {
            var prefs = PreferencesUtility.getInstance(context)
            override fun intercept(request: RequestFacade) {
                //7-days cache
                request.addHeader("Cache-Control", String.format("max-age=%d,%smax-stale=%d",
                        Integer.valueOf(60 * 60 * 24 * 7),
                        if (prefs.loadArtistAndAlbumImages()) "" else "only-if-cached,", Integer.valueOf(31536000)))
                request.addHeader("Connection", "keep-alive")
            }
        }
        val builder = RestAdapter.Builder()
                .setEndpoint(baseUrl)
                .setRequestInterceptor(interceptor)
                .setClient(OkClient(okHttpClient))
        return builder
                .build()
                .create(clazz)
    }

    fun <T> create(context: Context?, baseUrl: String?, clazz: Class<T>?): T {
        val builder = RestAdapter.Builder()
                .setEndpoint(baseUrl)
        return builder
                .build()
                .create(clazz)
    }
}