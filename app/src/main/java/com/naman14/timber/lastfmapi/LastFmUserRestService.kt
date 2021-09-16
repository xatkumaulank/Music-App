package com.naman14.timber.lastfmapi

import com.naman14.timber.lastfmapi.models.ScrobbleInfo
import com.naman14.timber.lastfmapi.models.UserLoginInfo
import retrofit.Callback
import retrofit.http.Field
import retrofit.http.FieldMap
import retrofit.http.FormUrlEncoded
import retrofit.http.POST

/**
 * Created by christoph on 17.07.16.
 */
interface LastFmUserRestService {
    @POST(BASE)
    @FormUrlEncoded
    fun getUserLoginInfo(@Field("method") method: String?, @Field("format") format: String?, @Field("api_key") apikey: String?, @Field("api_sig") apisig: String?, @Field("username") username: String?, @Field("password") password: String?, callback: Callback<UserLoginInfo?>?)

    @POST(BASE)
    @FormUrlEncoded
    fun getScrobbleInfo(@Field("api_sig") apisig: String?, @Field("format") format: String?, @FieldMap fields: Map<String?, String?>?, callback: Callback<ScrobbleInfo?>?)

    companion object {
        const val BASE = "/"
    }
}