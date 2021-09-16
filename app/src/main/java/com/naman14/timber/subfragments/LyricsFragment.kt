package com.naman14.timber.subfragments

import android.content.CursorLoader
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.naman14.timber.MusicPlayer
import com.naman14.timber.R
import com.naman14.timber.utils.LyricsExtractor
import com.naman14.timber.utils.LyricsLoader
import retrofit.Callback
import retrofit.RetrofitError
import retrofit.client.Response
import java.io.File

/**
 * Created by christoph on 10.12.16.
 */
class LyricsFragment : Fragment() {
    private var lyrics: String? = null
    private var toolbar: Toolbar? = null
    private var rootView: View? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_lyrics, container, false)
        toolbar = rootView?.findViewById<View>(R.id.toolbar) as Toolbar
        setupToolbar()
        loadLyrics()
        return rootView
    }

    private fun loadLyrics() {
        val lyricsView = rootView!!.findViewById<View>(R.id.lyrics)
        val poweredbyTextView = lyricsView.findViewById<View>(R.id.lyrics_makeitpersonal) as TextView
        poweredbyTextView.visibility = View.GONE
        val lyricsTextView = lyricsView.findViewById<View>(R.id.lyrics_text) as TextView
        lyricsTextView.text = getString(R.string.lyrics_loading)
        val filename = getRealPathFromURI(Uri.parse(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.toString() + "/" + MusicPlayer.getCurrentAudioId()))
        if (filename != null && lyrics == null) {
            lyrics = LyricsExtractor.getLyrics(File(filename))
        }
        if (lyrics != null) {
            lyricsTextView.text = lyrics
        } else {
            var artist = MusicPlayer.getArtistName()
            if (artist != null) {
                val i = artist.lastIndexOf(" feat")
                if (i != -1) {
                    artist = artist.substring(0, i)
                }
                LyricsLoader.getInstance(this.context).getLyrics(artist, MusicPlayer.getTrackName(), object : Callback<String> {
                    override fun success(s: String, response: Response) {
                        lyrics = s
                        if (s == "Sorry, We don't have lyrics for this song yet.\n") {
                            lyricsTextView.setText(R.string.no_lyrics)
                        } else {
                            lyricsTextView.text = s
                            poweredbyTextView.visibility = View.VISIBLE
                        }
                    }

                    override fun failure(error: RetrofitError) {
                        lyricsTextView.setText(R.string.no_lyrics)
                    }
                })
            } else {
                lyricsTextView.setText(R.string.no_lyrics)
            }
        }
    }

    private fun setupToolbar() {
        (activity as AppCompatActivity?)!!.setSupportActionBar(toolbar)
        val ab = (activity as AppCompatActivity?)!!.supportActionBar
        ab!!.setDisplayHomeAsUpEnabled(true)
        if (MusicPlayer.getTrackName() != null) {
            ab.title = MusicPlayer.getTrackName()
        }
    }

    override fun onResume() {
        super.onResume()
        toolbar!!.setBackgroundColor(Color.TRANSPARENT)
    }

    private fun getRealPathFromURI(contentUri: Uri): String {
        val proj = arrayOf(MediaStore.Audio.Media.DATA)
        val loader = CursorLoader(this.context, contentUri, proj, null, null, null)
        val cursor = loader.loadInBackground()
        val column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
        cursor.moveToFirst()
        val result = cursor.getString(column_index)
        cursor.close()
        return result
    }
}