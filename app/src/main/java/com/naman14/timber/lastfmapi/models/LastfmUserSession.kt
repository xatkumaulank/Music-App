package com.naman14.timber.lastfmapi.models

import android.content.Context
import com.google.gson.annotations.SerializedName
import com.naman14.timber.lastfmapi.LastFmClient

/**
 * Created by christoph on 17.07.16.
 */
class LastfmUserSession {
    val isLogedin: Boolean
        get() = session!!.mToken != null && session!!.mUsername != null

    fun update(context: Context) {
        val preferences = context.getSharedPreferences(LastFmClient.PREFERENCES_NAME, Context.MODE_PRIVATE)
        val editor = preferences.edit()
        if (mToken == null || mUsername == null) {
            editor.clear()
        } else {
            editor.putString(TOKEN, mToken)
            editor.putString(USERNAME, mUsername)
        }
        editor.apply()
    }

    @JvmField
    @SerializedName(USERNAME)
    var mUsername: String? = null

    @JvmField
    @SerializedName(TOKEN)
    var mToken: String? = null

    companion object {
        private const val USERNAME = "name"
        private const val TOKEN = "key"
        private var session: LastfmUserSession? = null
        @JvmStatic
        fun getSession(context: Context): LastfmUserSession? {
            if (session != null) return session
            val preferences = context.getSharedPreferences(LastFmClient.PREFERENCES_NAME, Context.MODE_PRIVATE)
            session = LastfmUserSession()
            session!!.mToken = preferences.getString(TOKEN, null)
            session!!.mUsername = preferences.getString(USERNAME, null)
            return session
        }
    }
}