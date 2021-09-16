package com.naman14.timber.nowplaying

import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import com.naman14.timber.MusicPlayer
import com.naman14.timber.MusicService
import com.naman14.timber.R
import com.naman14.timber.dataloaders.SongLoader.Companion.getSongForID
import com.naman14.timber.utils.TimberUtils
import com.naman14.timber.widgets.CircleImageView
import net.steamcrafted.materialiconlib.MaterialDrawableBuilder

/**
 * Created by naman on 22/02/17.
 */
class Timber6 : BaseNowplayingFragment() {
    var nextSong: TextView? = null
    var nextArt: CircleImageView? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(
                R.layout.fragment_timber6, container, false)
        setMusicStateListener()
        setSongDetails(rootView)
        initGestures(rootView.findViewById(R.id.album_art))
        (rootView.findViewById<View>(R.id.song_progress) as SeekBar).progressDrawable.colorFilter = PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY)
        (rootView.findViewById<View>(R.id.song_progress) as SeekBar).thumb.colorFilter = PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
        nextSong = rootView.findViewById<View>(R.id.title_next) as TextView
        nextArt = rootView.findViewById<View>(R.id.album_art_next) as CircleImageView
        rootView.findViewById<View>(R.id.nextView).setOnClickListener { MusicPlayer.next() }
        return rootView
    }

    override fun updateShuffleState() {
        if (shuffle != null && activity != null) {
            val builder = MaterialDrawableBuilder.with(activity)
                    .setIcon(MaterialDrawableBuilder.IconValue.SHUFFLE)
                    .setSizeDp(30)
            if (MusicPlayer.getShuffleMode() == 0) {
                builder.setColor(Color.WHITE)
            } else builder.setColor(accentColor)
            shuffle!!.setImageDrawable(builder.build())
            shuffle!!.setOnClickListener {
                MusicPlayer.cycleShuffle()
                updateShuffleState()
                updateRepeatState()
            }
        }
    }

    override fun updateRepeatState() {
        if (repeat != null && activity != null) {
            val builder = MaterialDrawableBuilder.with(activity)
                    .setSizeDp(30)
            if (MusicPlayer.getRepeatMode() == 0) {
                builder.setColor(Color.WHITE)
            } else builder.setColor(accentColor)
            if (MusicPlayer.getRepeatMode() == MusicService.REPEAT_NONE) {
                builder.setIcon(MaterialDrawableBuilder.IconValue.REPEAT)
                builder.setColor(Color.WHITE)
            } else if (MusicPlayer.getRepeatMode() == MusicService.REPEAT_CURRENT) {
                builder.setIcon(MaterialDrawableBuilder.IconValue.REPEAT_ONCE)
                builder.setColor(accentColor)
            } else if (MusicPlayer.getRepeatMode() == MusicService.REPEAT_ALL) {
                builder.setColor(accentColor)
                builder.setIcon(MaterialDrawableBuilder.IconValue.REPEAT)
            }
            repeat!!.setImageDrawable(builder.build())
            repeat!!.setOnClickListener {
                MusicPlayer.cycleRepeat()
                updateRepeatState()
                updateShuffleState()
            }
        }
    }

    override fun onMetaChanged() {
        super.onMetaChanged()
        if (activity != null) {
            val nextId = MusicPlayer.getNextAudioId()
            val next = getSongForID(activity!!, nextId)
            nextSong!!.text = next.title
            nextArt!!.setImageURI(TimberUtils.getAlbumArtUri(next.albumId))
        }
    }
}