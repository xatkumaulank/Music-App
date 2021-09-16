package com.naman14.timber.lastfmapi.models

import com.google.gson.annotations.SerializedName
import com.naman14.timber.lastfmapi.LastFmClient

/**
 * Created by christoph on 17.07.16.
 */
class UserLoginQuery(@field:SerializedName(USERNAME_NAME) var mUsername: String, @field:SerializedName(PASSWORD_NAME) var mPassword: String) {
    val signature: String
        get() = "api_key" + LastFmClient.API_KEY + "method" + Method + "password" + mPassword + "username" + mUsername + LastFmClient.API_SECRET

    companion object {
        private const val USERNAME_NAME = "username"
        private const val PASSWORD_NAME = "password"
        const val Method = "auth.getMobileSession"
    }
}