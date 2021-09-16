package com.naman14.timber.adapters

import android.app.Activity
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.appthemeengine.Config
import com.naman14.timber.R
import com.naman14.timber.dataloaders.LastAddedLoader
import com.naman14.timber.dataloaders.PlaylistSongLoader
import com.naman14.timber.dataloaders.SongLoader
import com.naman14.timber.dataloaders.TopTracksLoader
import com.naman14.timber.models.Playlist
import com.naman14.timber.models.Song
import com.naman14.timber.utils.*
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.assist.FailReason
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener
import java.util.*

/**
 * Created by naman on 31/10/16.
 */
class PlaylistAdapter(private val mContext: Activity?, private val arraylist: MutableList<Playlist>?) : RecyclerView.Adapter<PlaylistAdapter.ItemHolder>() {
    private val isGrid: Boolean
    private val showAuto: Boolean
    private var songCountInt = 0
    private var totalRuntime: Long = 0
    private var firstAlbumID: Long = -1
    private val foregroundColor: Int
    var foregroundColors = intArrayOf(R.color.pink_transparent, R.color.green_transparent, R.color.blue_transparent, R.color.red_transparent, R.color.purple_transparent)
    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ItemHolder {
        return if (isGrid) {
            val v = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_album_grid, null)
            ItemHolder(v)
        } else {
            val v = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_album_list, null)
            ItemHolder(v)
        }
    }

    override fun onBindViewHolder(itemHolder: ItemHolder, i: Int) {
        val localItem = arraylist!![i]
        itemHolder.title.text = localItem.name
        val s = getAlbumArtUri(i, localItem.id)
        itemHolder.albumArt.tag = firstAlbumID
        ImageLoader.getInstance().displayImage(s, itemHolder.albumArt,
                DisplayImageOptions.Builder().cacheInMemory(true)
                        .showImageOnFail(R.drawable.ic_empty_music2)
                        .resetViewBeforeLoading(true)
                        .build(), object : SimpleImageLoadingListener() {
            override fun onLoadingComplete(imageUri: String, view: View, loadedImage: Bitmap) {
                if (isGrid) {
                    Palette.Builder(loadedImage).generate { palette ->
                        val swatch = palette!!.vibrantSwatch
                        if (swatch != null) {
                            val color = swatch.rgb
                            itemHolder.footer.setBackgroundColor(color)
                            val textColor = TimberUtils.getBlackWhiteColor(swatch.titleTextColor)
                            itemHolder.title.setTextColor(textColor)
                            itemHolder.artist.setTextColor(textColor)
                        } else {
                            val mutedSwatch = palette.mutedSwatch
                            if (mutedSwatch != null) {
                                val color = mutedSwatch.rgb
                                itemHolder.footer.setBackgroundColor(color)
                                val textColor = TimberUtils.getBlackWhiteColor(mutedSwatch.titleTextColor)
                                itemHolder.title.setTextColor(textColor)
                                itemHolder.artist.setTextColor(textColor)
                            }
                        }
                    }
                }
            }

            override fun onLoadingFailed(imageUri: String, view: View, failReason: FailReason) {
                if (isGrid) {
                    itemHolder.footer.setBackgroundColor(0)
                    if (mContext != null) {
                        val textColorPrimary = Config.textColorPrimary(mContext, Helpers.getATEKey(mContext))
                        itemHolder.title.setTextColor(textColorPrimary)
                        itemHolder.artist.setTextColor(textColorPrimary)
                    }
                }
            }
        })
        itemHolder.artist.text = " " + songCountInt.toString() + " " + mContext!!.getString(R.string.songs) + " - " + TimberUtils.makeShortTimeString(mContext, totalRuntime)
        if (TimberUtils.isLollipop()) itemHolder.albumArt.transitionName = "transition_album_art$i"
    }

    private fun getAlbumArtUri(position: Int, id: Long): String? {
        if (mContext != null) {
            firstAlbumID = -1
            return if (showAuto) {
                when (position) {
                    0 -> {
                        val lastAddedSongs = LastAddedLoader.getLastAddedSongs(mContext)
                        songCountInt = lastAddedSongs.size
                        totalRuntime = 0
                        for (song in lastAddedSongs) {
                            totalRuntime += (song.duration / 1000).toLong() //for some reason default playlists have songs with durations 1000x larger than they should be
                        }
                        if (songCountInt != 0) {
                            firstAlbumID = lastAddedSongs[0].albumId
                            TimberUtils.getAlbumArtUri(firstAlbumID).toString()
                        } else "nosongs"
                    }
                    1 -> {
                        val recentloader = TopTracksLoader(mContext, TopTracksLoader.QueryType.RecentSongs)
                        val recentsongs: List<Song> = SongLoader.getSongsForCursor(TopTracksLoader.cursor)
                        songCountInt = recentsongs.size
                        totalRuntime = 0
                        for (song in recentsongs) {
                            totalRuntime += (song.duration / 1000).toLong() //for some reason default playlists have songs with durations 1000x larger than they should be
                        }
                        if (songCountInt != 0) {
                            firstAlbumID = recentsongs[0].albumId
                            TimberUtils.getAlbumArtUri(firstAlbumID).toString()
                        } else "nosongs"
                    }
                    2 -> {
                        val topTracksLoader = TopTracksLoader(mContext, TopTracksLoader.QueryType.TopTracks)
                        val topsongs: List<Song> = SongLoader.getSongsForCursor(TopTracksLoader.cursor)
                        songCountInt = topsongs.size
                        totalRuntime = 0
                        for (song in topsongs) {
                            totalRuntime += (song.duration / 1000).toLong() //for some reason default playlists have songs with durations 1000x larger than they should be
                        }
                        if (songCountInt != 0) {
                            firstAlbumID = topsongs[0].albumId
                            TimberUtils.getAlbumArtUri(firstAlbumID).toString()
                        } else "nosongs"
                    }
                    else -> {
                        val playlistsongs = PlaylistSongLoader.getSongsInPlaylist(mContext, id)
                        songCountInt = playlistsongs.size
                        totalRuntime = 0
                        for (song in playlistsongs) {
                            totalRuntime += song.duration.toLong()
                        }
                        if (songCountInt != 0) {
                            firstAlbumID = playlistsongs[0].albumId
                            TimberUtils.getAlbumArtUri(firstAlbumID).toString()
                        } else "nosongs"
                    }
                }
            } else {
                val playlistsongs = PlaylistSongLoader.getSongsInPlaylist(mContext, id)
                songCountInt = playlistsongs.size
                totalRuntime = 0
                for (song in playlistsongs) {
                    totalRuntime += song.duration.toLong()
                }
                if (songCountInt != 0) {
                    firstAlbumID = playlistsongs[0].albumId
                    TimberUtils.getAlbumArtUri(firstAlbumID).toString()
                } else "nosongs"
            }
        }
        return null
    }

    override fun getItemCount(): Int {
        return arraylist?.size ?: 0
    }

    fun updateDataSet(arraylist: List<Playlist>?) {
        this.arraylist!!.clear()
        this.arraylist.addAll(arraylist!!)
        notifyDataSetChanged()
    }

    inner class ItemHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        var title: TextView
        var artist: TextView
        var albumArt: ImageView
        var footer: View
        override fun onClick(v: View) {
            NavigationUtils.navigateToPlaylistDetail(mContext, getPlaylistType(adapterPosition), albumArt.tag as Long, title.text.toString(), foregroundColor, arraylist!![adapterPosition].id, null)
        }

        init {
            title = view.findViewById<View>(R.id.album_title) as TextView
            artist = view.findViewById<View>(R.id.album_artist) as TextView
            albumArt = view.findViewById<View>(R.id.album_art) as ImageView
            footer = view.findViewById(R.id.footer)
            view.setOnClickListener(this)
        }
    }

    private fun getPlaylistType(position: Int): String {
        return if (showAuto) {
            when (position) {
                0 -> Constants.NAVIGATE_PLAYLIST_LASTADDED
                1 -> Constants.NAVIGATE_PLAYLIST_RECENT
                2 -> Constants.NAVIGATE_PLAYLIST_TOPTRACKS
                else -> Constants.NAVIGATE_PLAYLIST_USERCREATED
            }
        } else Constants.NAVIGATE_PLAYLIST_USERCREATED
    }

    init {
        isGrid = PreferencesUtility.getInstance(mContext).playlistView == Constants.PLAYLIST_VIEW_GRID
        showAuto = PreferencesUtility.getInstance(mContext).showAutoPlaylist()
        val random = Random()
        val rndInt = random.nextInt(foregroundColors.size)
        foregroundColor = foregroundColors[rndInt]
    }
}