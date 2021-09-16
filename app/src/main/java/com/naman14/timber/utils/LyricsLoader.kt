package com.naman14.timber.utils

import android.content.Context
import com.squareup.okhttp.Cache
import com.squareup.okhttp.OkHttpClient
import retrofit.Callback
import retrofit.RequestInterceptor
import retrofit.RestAdapter
import retrofit.client.OkClient
import retrofit.converter.ConversionException
import retrofit.converter.Converter
import retrofit.http.GET
import retrofit.http.Headers
import retrofit.http.Query
import retrofit.mime.TypedInput
import retrofit.mime.TypedOutput
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit

/**
 * Created by Christoph Walcher on 03.12.16.
 */
class LyricsLoader private constructor(con: Context) {
    private val service: LyricsRestService
    fun getLyrics(artist: String?, title: String?, callback: Callback<String?>?) {
        service.getLyrics(artist, title, callback)
    }

    private interface LyricsRestService {
        @Headers("Cache-Control: public")
        @GET("/lyrics")
        fun getLyrics(@Query("artist") artist: String?, @Query("title") title: String?, callback: Callback<String?>?)
    }

    companion object {
        private var instance: LyricsLoader? = null
        private const val BASE_API_URL = "https://makeitpersonal.co"
        private const val CACHE_SIZE = (1024 * 1024).toLong()
        fun getInstance(con: Context): LyricsLoader? {
            if (instance == null) instance = LyricsLoader(con)
            return instance
        }
    }

    init {
        val okHttpClient = OkHttpClient()
        okHttpClient.cache = Cache(con.applicationContext.cacheDir,
                CACHE_SIZE)
        okHttpClient.setConnectTimeout(20, TimeUnit.SECONDS)
        val interceptor = RequestInterceptor { request -> //7-days cache
            request.addHeader("Cache-Control", String.format("max-age=%d,max-stale=%d", Integer.valueOf(60 * 60 * 24 * 7), Integer.valueOf(31536000)))
        }
        val builder = RestAdapter.Builder()
                .setEndpoint(BASE_API_URL)
                .setRequestInterceptor(interceptor)
                .setConverter(object : Converter {
                    @Throws(ConversionException::class)
                    override fun fromBody(arg0: TypedInput, arg1: Type): Any? {
                        return try {
                            var br: BufferedReader? = null
                            val sb = StringBuilder()
                            var line: String?
                            br = BufferedReader(InputStreamReader(arg0.`in`()))
                            while (br.readLine().also { line = it } != null) {
                                sb.append(line)
                                sb.append('\n')
                            }
                            sb.toString()
                        } catch (e: IOException) {
                            e.printStackTrace()
                            null
                        }
                    }

                    override fun toBody(arg0: Any): TypedOutput? {
                        return null
                    }
                })
                .setClient(OkClient(okHttpClient))
        service = builder
                .build()
                .create(LyricsRestService::class.java)
    }
}