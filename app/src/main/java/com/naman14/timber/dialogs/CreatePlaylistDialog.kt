package com.naman14.timber.dialogs

import android.app.Dialog
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.afollestad.materialdialogs.MaterialDialog
import com.naman14.timber.MusicPlayer
import com.naman14.timber.fragments.PlaylistFragment
import com.naman14.timber.models.Song

/**
 * Created by naman on 20/12/15.
 */
class CreatePlaylistDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialDialog.Builder(activity!!).positiveText("Create").negativeText("Cancel").input("Enter playlist name", "", false) { dialog, input ->
            val songs = arguments!!.getLongArray("songs")
            val playistId = MusicPlayer.createPlaylist(activity, input.toString())
            if (playistId != -1L) {
                if (songs != null && songs.size != 0) MusicPlayer.addToPlaylist(activity, songs, playistId) else Toast.makeText(activity, "Created playlist", Toast.LENGTH_SHORT).show()
                if (parentFragment is PlaylistFragment) {
                    (parentFragment as PlaylistFragment?)!!.updatePlaylists(playistId)
                }
            } else {
                Toast.makeText(activity, "Unable to create playlist", Toast.LENGTH_SHORT).show()
            }
        }.build()
    }

    companion object {
        @JvmOverloads
        fun newInstance(song: Song? = null as Song?): CreatePlaylistDialog {
            val songs: LongArray
            if (song == null) {
                songs = LongArray(0)
            } else {
                songs = LongArray(1)
                songs[0] = song.id
            }
            return newInstance(songs)
        }

        fun newInstance(songList: LongArray?): CreatePlaylistDialog {
            val dialog = CreatePlaylistDialog()
            val bundle = Bundle()
            bundle.putLongArray("songs", songList)
            dialog.arguments = bundle
            return dialog
        }
    }
}