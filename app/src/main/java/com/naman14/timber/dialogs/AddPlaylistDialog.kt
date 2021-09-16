package com.naman14.timber.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.MaterialDialog.ListCallback
import com.naman14.timber.MusicPlayer
import com.naman14.timber.dataloaders.PlaylistLoader.getPlaylists
import com.naman14.timber.models.Song

/**
 * Created by naman on 20/12/15.
 */
class AddPlaylistDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val playlists = getPlaylists(activity!!, false)
        val chars = arrayOfNulls<CharSequence>(playlists!!.size + 1)
        chars[0] = "Create new playlist"
        for (i in playlists.indices) {
            chars[i + 1] = playlists[i].name
        }
        return MaterialDialog.Builder(activity!!).title("Add to playlist").items(*chars).itemsCallback(ListCallback { dialog, itemView, which, text ->
            val songs = arguments!!.getLongArray("songs")
            if (which == 0) {
                CreatePlaylistDialog.newInstance(songs).show(activity!!.supportFragmentManager, "CREATE_PLAYLIST")
                return@ListCallback
            }
            MusicPlayer.addToPlaylist(activity, songs, playlists[which - 1].id)
            dialog.dismiss()
        }).build()
    }

    companion object {
        fun newInstance(song: Song): AddPlaylistDialog {
            val songs = LongArray(1)
            songs[0] = song.id
            return newInstance(songs)
        }

        @JvmStatic
        fun newInstance(songList: LongArray?): AddPlaylistDialog {
            val dialog = AddPlaylistDialog()
            val bundle = Bundle()
            bundle.putLongArray("songs", songList)
            dialog.arguments = bundle
            return dialog
        }
    }
}