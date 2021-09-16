package com.naman14.timber.adapters

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.naman14.timber.MusicPlayer
import com.naman14.timber.activities.BaseActivity
import com.naman14.timber.cast.TimberCastHelper
import com.naman14.timber.models.Song
import com.naman14.timber.utils.NavigationUtils
import com.naman14.timber.utils.TimberUtils.IdType

/**
 * Created by naman on 7/12/17.
 */
open class BaseSongAdapter<V : RecyclerView.ViewHolder?> : RecyclerView.Adapter<V?>() {


    override fun onBindViewHolder(holder: V, position: Int) {}
    override fun getItemCount(): Int {
        return 0
    }

    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }

    inner class ItemHolder(view: View?) : RecyclerView.ViewHolder(view!!)

    fun playAll(context: Activity?, list: LongArray?, position: Int,
                sourceId: Long, sourceType: IdType?,
                forceShuffle: Boolean, currentSong: Song?, navigateNowPlaying: Boolean) {
        var navigateNowPlaying = navigateNowPlaying
        if (context is BaseActivity) {
            val castSession = context.castSession
            if (castSession != null) {
                navigateNowPlaying = false
                TimberCastHelper.startCasting(castSession, currentSong)
            } else {
                MusicPlayer.playAll(context, list, position, -1, IdType.NA, false)
            }
        } else {
            MusicPlayer.playAll(context, list, position, -1, IdType.NA, false)
        }
        if (navigateNowPlaying) {
            NavigationUtils.navigateToNowplaying(context, true)
        }
    }

    open fun removeSongAt(i: Int) {}
    open fun updateDataSet(arraylist: MutableList<Song?>?) {}
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): V {
        TODO("Not yet implemented")
    }

}