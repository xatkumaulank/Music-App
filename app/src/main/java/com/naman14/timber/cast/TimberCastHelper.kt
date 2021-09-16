package com.naman14.timber.cast

import android.net.Uri
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.common.images.WebImage
import com.naman14.timber.models.Song
import com.naman14.timber.utils.Constants
import com.naman14.timber.utils.TimberUtils
import java.net.MalformedURLException
import java.net.URL

/**
 * Created by naman on 2/12/17.
 */
object TimberCastHelper {
    fun startCasting(castSession: CastSession, song: Song) {
        val ipAddress = TimberUtils.getIPAddress(true)
        val baseUrl: URL
        baseUrl = try {
            URL("http", ipAddress, Constants.CAST_SERVER_PORT, "")
        } catch (e: MalformedURLException) {
            e.printStackTrace()
            return
        }
        val songUrl = baseUrl.toString() + "/song?id=" + song.id
        val albumArtUrl = baseUrl.toString() + "/albumart?id=" + song.albumId
        val musicMetadata = MediaMetadata(MediaMetadata.MEDIA_TYPE_MUSIC_TRACK)
        musicMetadata.putString(MediaMetadata.KEY_TITLE, song.title)
        musicMetadata.putString(MediaMetadata.KEY_ARTIST, song.artistName)
        musicMetadata.putString(MediaMetadata.KEY_ALBUM_TITLE, song.albumName)
        musicMetadata.putInt(MediaMetadata.KEY_TRACK_NUMBER, song.trackNumber)
        musicMetadata.addImage(WebImage(Uri.parse(albumArtUrl)))
        try {
            val mediaInfo = MediaInfo.Builder(songUrl)
                    .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                    .setContentType("audio/mpeg")
                    .setMetadata(musicMetadata)
                    .setStreamDuration(song.duration.toLong())
                    .build()
            val remoteMediaClient = castSession.remoteMediaClient
            remoteMediaClient.load(mediaInfo, true, 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}