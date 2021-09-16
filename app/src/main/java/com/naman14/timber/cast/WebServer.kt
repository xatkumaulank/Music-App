package com.naman14.timber.cast

import android.content.Context
import android.net.Uri
import com.naman14.timber.utils.Constants
import com.naman14.timber.utils.TimberUtils
import fi.iki.elonen.NanoHTTPD
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.InputStream

class WebServer(private val context: Context) : NanoHTTPD(Constants.CAST_SERVER_PORT) {
    private var songUri: Uri? = null
    private var albumArtUri: Uri? = null
    override fun serve(uri: String, method: Method,
                       header: Map<String, String>,
                       parameters: Map<String, String>,
                       files: Map<String, String>): Response {
        if (uri.contains("albumart")) {
            //serve the picture
            val albumId = parameters["id"]
            albumArtUri = TimberUtils.getAlbumArtUri(albumId!!.toLong())
            if (albumArtUri != null) {
                val mediasend = "image/jpg"
                var fisAlbumArt: InputStream? = null
                try {
                    fisAlbumArt = context.contentResolver.openInputStream(albumArtUri)
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }
                val st = Response.Status.OK

                //serve the song
                return newChunkedResponse(st, mediasend, fisAlbumArt)
            }
        } else if (uri.contains("song")) {
            val songId = parameters["id"]
            songUri = TimberUtils.getSongUri(context, songId!!.toLong())
            if (songUri != null) {
                val mediasend = "audio/mp3"
                var fisSong: FileInputStream? = null
                val song = File(songUri!!.path)
                try {
                    fisSong = FileInputStream(song)
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }
                val st = Response.Status.OK

                //serve the song
                return newFixedLengthResponse(st, mediasend, fisSong, song.length())
            }
        }
        return newFixedLengthResponse("Error")
    }
}