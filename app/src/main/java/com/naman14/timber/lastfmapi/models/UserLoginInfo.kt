package com.naman14.timber.lastfmapi.models

import com.google.gson.annotations.SerializedName

/**
 * Created by christoph on 17.07.16.
 */
class UserLoginInfo {
    @JvmField
    @SerializedName(SESSION)
    var mSession: LastfmUserSession? = null

    companion object {
        private const val SESSION = "session"
    }
}