/*
 * Copyright (C) 2015 Naman Dwivedi
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 */
package com.naman14.timber.nowplaying

import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.appthemeengine.ATE
import com.afollestad.appthemeengine.Config
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.naman14.timber.MusicPlayer
import com.naman14.timber.MusicService
import com.naman14.timber.R
import com.naman14.timber.activities.BaseActivity
import com.naman14.timber.adapters.BaseQueueAdapter
import com.naman14.timber.adapters.SlidingQueueAdapter
import com.naman14.timber.dataloaders.QueueLoader.getQueueSongs
import com.naman14.timber.listeners.MusicStateListener
import com.naman14.timber.timely.TimelyView
import com.naman14.timber.utils.*
import com.naman14.timber.widgets.CircularSeekBar
import com.naman14.timber.widgets.CircularSeekBar.OnCircularSeekBarChangeListener
import com.naman14.timber.widgets.DividerItemDecoration
import com.naman14.timber.widgets.PlayPauseButton
import com.naman14.timber.widgets.PlayPauseDrawable
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.assist.FailReason
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener
import net.steamcrafted.materialiconlib.MaterialDrawableBuilder
import net.steamcrafted.materialiconlib.MaterialIconView
import java.security.InvalidParameterException

open class BaseNowplayingFragment : Fragment(), MusicStateListener {
    private var previous: MaterialIconView? = null
    private var next: MaterialIconView? = null
    private var mPlayPause: PlayPauseButton? = null
    private val playPauseDrawable = PlayPauseDrawable()
    private var playPauseFloating: FloatingActionButton? = null
    private var playPauseWrapper: View? = null
    private var ateKey: String? = null
    private var overflowcounter = 0
    private var songtitle: TextView? = null
    private var songalbum: TextView? = null
    private var songartist: TextView? = null
    private var songduration: TextView? = null
    private var elapsedtime: TextView? = null
    private var mProgress: SeekBar? = null
    var fragmentPaused = false
    private var mCircularProgress: CircularSeekBar? = null
    private var mAdapter: BaseQueueAdapter? = null
    private val slidingQueueAdapter: SlidingQueueAdapter? = null
    private var timelyView11: TimelyView? = null
    private var timelyView12: TimelyView? = null
    private var timelyView13: TimelyView? = null
    private var timelyView14: TimelyView? = null
    private var timelyView15: TimelyView? = null
    private var hourColon: TextView? = null
    private val timeArr = intArrayOf(0, 0, 0, 0, 0)
    private var mElapsedTimeHandler: Handler? = null
    private var duetoplaypause = false
    var albumart: ImageView? = null
    @JvmField
    var shuffle: ImageView? = null
    @JvmField
    var repeat: ImageView? = null
    @JvmField
    var accentColor = 0
    @JvmField
    open var recyclerView: RecyclerView? = null

    //seekbar
    var mUpdateProgress: Runnable? = object : Runnable {
        override fun run() {
            val position = MusicPlayer.position()
            if (mProgress != null) {
                mProgress!!.progress = position.toInt()
                if (elapsedtime != null && activity != null) elapsedtime!!.text = TimberUtils.makeShortTimeString(activity, position / 1000)
            }
            overflowcounter--
            val delay = 250 //not sure why this delay was so high before
            if (overflowcounter < 0 && !fragmentPaused) {
                overflowcounter++
                mProgress!!.postDelayed(this, delay.toLong()) //delay
            }
        }
    }

    //circular seekbar
    var mUpdateCircularProgress: Runnable? = object : Runnable {
        override fun run() {
            val position = MusicPlayer.position()
            if (mCircularProgress != null) {
                mCircularProgress!!.progress = position.toInt()
                if (elapsedtime != null && activity != null) elapsedtime!!.text = TimberUtils.makeShortTimeString(activity, position / 1000)
            }
            overflowcounter--
            if (MusicPlayer.isPlaying()) {
                val delay = (1500 - position % 1000).toInt()
                if (overflowcounter < 0 && !fragmentPaused) {
                    overflowcounter++
                    mCircularProgress!!.postDelayed(this, delay.toLong())
                }
            }
        }
    }
    var mUpdateElapsedTime: Runnable = object : Runnable {
        override fun run() {
            if (activity != null) {
                val time = TimberUtils.makeShortTimeString(activity, MusicPlayer.position() / 1000)
                if (time.length < 5) {
                    timelyView11!!.visibility = View.GONE
                    timelyView12!!.visibility = View.GONE
                    hourColon!!.visibility = View.GONE
                    tv13(time[0] - '0')
                    tv14(time[2] - '0')
                    tv15(time[3] - '0')
                } else if (time.length == 5) {
                    timelyView12!!.visibility = View.VISIBLE
                    tv12(time[0] - '0')
                    tv13(time[1] - '0')
                    tv14(time[3] - '0')
                    tv15(time[4] - '0')
                } else {
                    timelyView11!!.visibility = View.VISIBLE
                    hourColon!!.visibility = View.VISIBLE
                    tv11(time[0] - '0')
                    tv12(time[2] - '0')
                    tv13(time[3] - '0')
                    tv14(time[5] - '0')
                    tv15(time[6] - '0')
                }
                mElapsedTimeHandler!!.postDelayed(this, 600)
            }
        }
    }
    private val mButtonListener = View.OnClickListener {
        duetoplaypause = true
        if (!mPlayPause!!.isPlayed) {
            mPlayPause!!.isPlayed = true
            mPlayPause!!.startAnimation()
        } else {
            mPlayPause!!.isPlayed = false
            mPlayPause!!.startAnimation()
        }
        val handler = Handler()
        handler.postDelayed({
            MusicPlayer.playOrPause()
            if (recyclerView != null && recyclerView!!.adapter != null) recyclerView!!.adapter!!.notifyDataSetChanged()
        }, 200)
    }
    private val mFLoatingButtonListener = View.OnClickListener {
        duetoplaypause = true
        if (MusicPlayer.getCurrentTrack() == null) {
            Toast.makeText(context, getString(R.string.now_playing_no_track_selected), Toast.LENGTH_SHORT).show()
        } else {
            playPauseDrawable.transformToPlay(true)
            playPauseDrawable.transformToPause(true)
            val handler = Handler()
            handler.postDelayed({
                MusicPlayer.playOrPause()
                if (recyclerView != null && recyclerView!!.adapter != null) recyclerView!!.adapter!!.notifyDataSetChanged()
            }, 250)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ateKey = Helpers.getATEKey(activity)
        accentColor = Config.accentColor(activity!!, ateKey)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.now_playing, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_go_to_album -> NavigationUtils.goToAlbum(context, MusicPlayer.getCurrentAlbumId())
            R.id.menu_go_to_artist -> NavigationUtils.goToArtist(context, MusicPlayer.getCurrentArtistId())
            R.id.action_lyrics -> NavigationUtils.goToLyrics(context)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPause() {
        super.onPause()
        fragmentPaused = true
    }

    override fun onResume() {
        super.onResume()
        fragmentPaused = false
        if (mProgress != null) mProgress!!.postDelayed(mUpdateProgress, 10)
        if (mCircularProgress != null) mCircularProgress!!.postDelayed(mUpdateCircularProgress, 10)
    }

    fun setSongDetails(view: View) {
        albumart = view.findViewById<View>(R.id.album_art) as ImageView
        shuffle = view.findViewById<View>(R.id.shuffle) as ImageView
        repeat = view.findViewById<View>(R.id.repeat) as ImageView
        next = view.findViewById<View>(R.id.next) as MaterialIconView
        previous = view.findViewById<View>(R.id.previous) as MaterialIconView
        mPlayPause = view.findViewById<View>(R.id.playpause) as PlayPauseButton
        playPauseFloating = view.findViewById<View>(R.id.playpausefloating) as FloatingActionButton
        playPauseWrapper = view.findViewById(R.id.playpausewrapper)
        songtitle = view.findViewById<View>(R.id.song_title) as TextView
        songalbum = view.findViewById<View>(R.id.song_album) as TextView
        songartist = view.findViewById<View>(R.id.song_artist) as TextView
        songduration = view.findViewById<View>(R.id.song_duration) as TextView
        elapsedtime = view.findViewById<View>(R.id.song_elapsed_time) as TextView
        timelyView11 = view.findViewById<View>(R.id.timelyView11) as TimelyView
        timelyView12 = view.findViewById<View>(R.id.timelyView12) as TimelyView
        timelyView13 = view.findViewById<View>(R.id.timelyView13) as TimelyView
        timelyView14 = view.findViewById<View>(R.id.timelyView14) as TimelyView
        timelyView15 = view.findViewById<View>(R.id.timelyView15) as TimelyView
        hourColon = view.findViewById<View>(R.id.hour_colon) as TextView
        mProgress = view.findViewById<View>(R.id.song_progress) as SeekBar
        mCircularProgress = view.findViewById<View>(R.id.song_progress_circular) as CircularSeekBar
        recyclerView = view.findViewById<View>(R.id.queue_recyclerview) as RecyclerView
        songtitle!!.isSelected = true
        val toolbar = view.findViewById<View>(R.id.toolbar) as Toolbar
        if (toolbar != null) {
            (activity as AppCompatActivity?)!!.setSupportActionBar(toolbar)
            val ab = (activity as AppCompatActivity?)!!.supportActionBar
            ab!!.setDisplayHomeAsUpEnabled(true)
            ab.title = ""
        }
        if (mPlayPause != null && activity != null) {
            mPlayPause!!.setColor(ContextCompat.getColor(context!!, android.R.color.white))
        }
        if (playPauseFloating != null) {
            playPauseDrawable.setColorFilter(TimberUtils.getBlackWhiteColor(accentColor), PorterDuff.Mode.MULTIPLY)
            playPauseFloating!!.setImageDrawable(playPauseDrawable)
            if (MusicPlayer.isPlaying()) playPauseDrawable.transformToPause(false) else playPauseDrawable.transformToPlay(false)
        }
        if (mCircularProgress != null) {
            mCircularProgress!!.circleProgressColor = accentColor
            mCircularProgress!!.pointerColor = accentColor
            mCircularProgress!!.pointerHaloColor = accentColor
        }
        if (timelyView11 != null) {
            val time = TimberUtils.makeShortTimeString(activity, MusicPlayer.position() / 1000)
            if (time.length < 5) {
                timelyView11!!.visibility = View.GONE
                timelyView12!!.visibility = View.GONE
                hourColon!!.visibility = View.GONE
                changeDigit(timelyView13, time[0] - '0')
                changeDigit(timelyView14, time[2] - '0')
                changeDigit(timelyView15, time[3] - '0')
            } else if (time.length == 5) {
                timelyView12!!.visibility = View.VISIBLE
                changeDigit(timelyView12, time[0] - '0')
                changeDigit(timelyView13, time[1] - '0')
                changeDigit(timelyView14, time[3] - '0')
                changeDigit(timelyView15, time[4] - '0')
            } else {
                timelyView11!!.visibility = View.VISIBLE
                hourColon!!.visibility = View.VISIBLE
                changeDigit(timelyView11, time[0] - '0')
                changeDigit(timelyView12, time[2] - '0')
                changeDigit(timelyView13, time[3] - '0')
                changeDigit(timelyView14, time[5] - '0')
                changeDigit(timelyView15, time[6] - '0')
            }
        }
        setSongDetails()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (PreferenceManager.getDefaultSharedPreferences(activity).getBoolean("dark_theme", false)) {
            ATE.apply(this, "dark_theme")
        } else {
            ATE.apply(this, "light_theme")
        }
    }

    private fun setSongDetails() {
        updateSongDetails()
        if (recyclerView != null) setQueueSongs()
        setSeekBarListener()
        if (next != null) {
            next!!.setOnClickListener {
                val handler = Handler()
                handler.postDelayed({
                    MusicPlayer.next()
                    notifyPlayingDrawableChange()
                }, 200)
            }
        }
        if (previous != null) {
            previous!!.setOnClickListener {
                val handler = Handler()
                handler.postDelayed({
                    MusicPlayer.previous(activity, false)
                    notifyPlayingDrawableChange()
                }, 200)
            }
        }
        if (playPauseWrapper != null) playPauseWrapper!!.setOnClickListener(mButtonListener)
        if (playPauseFloating != null) playPauseFloating!!.setOnClickListener(mFLoatingButtonListener)
        updateShuffleState()
        updateRepeatState()
    }

    open fun updateShuffleState() {
        if (shuffle != null && activity != null) {
            val builder = MaterialDrawableBuilder.with(activity)
                    .setIcon(MaterialDrawableBuilder.IconValue.SHUFFLE)
                    .setSizeDp(30)
            if (activity != null) {
                if (MusicPlayer.getShuffleMode() == 0) {
                    builder.setColor(Config.textColorPrimary(activity!!, ateKey))
                } else builder.setColor(Config.accentColor(activity!!, ateKey))
            }
            shuffle!!.setImageDrawable(builder.build())
            shuffle!!.setOnClickListener {
                MusicPlayer.cycleShuffle()
                updateShuffleState()
                updateRepeatState()
            }
        }
    }

    open fun updateRepeatState() {
        if (repeat != null && activity != null) {
            val builder = MaterialDrawableBuilder.with(activity)
                    .setSizeDp(30)
            if (MusicPlayer.getRepeatMode() == MusicService.REPEAT_NONE) {
                builder.setIcon(MaterialDrawableBuilder.IconValue.REPEAT)
                builder.setColor(Config.textColorPrimary(activity!!, ateKey))
            } else if (MusicPlayer.getRepeatMode() == MusicService.REPEAT_CURRENT) {
                builder.setIcon(MaterialDrawableBuilder.IconValue.REPEAT_ONCE)
                builder.setColor(Config.accentColor(activity!!, ateKey))
            } else if (MusicPlayer.getRepeatMode() == MusicService.REPEAT_ALL) {
                builder.setColor(Config.accentColor(activity!!, ateKey))
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

    private fun setSeekBarListener() {
        if (mProgress != null) mProgress!!.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                if (b) {
                    MusicPlayer.seek(i.toLong())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        if (mCircularProgress != null) {
            mCircularProgress!!.setOnSeekBarChangeListener(object : OnCircularSeekBarChangeListener {
                override fun onProgressChanged(circularSeekBar: CircularSeekBar, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        MusicPlayer.seek(progress.toLong())
                    }
                }

                override fun onStopTrackingTouch(seekBar: CircularSeekBar) {}
                override fun onStartTrackingTouch(seekBar: CircularSeekBar) {}
            })
        }
    }

    fun updateSongDetails() {
        //do not reload image if it was a play/pause change
        if (!duetoplaypause) {
            if (albumart != null) {
                ImageLoader.getInstance().displayImage(TimberUtils.getAlbumArtUri(MusicPlayer.getCurrentAlbumId()).toString(), albumart,
                        DisplayImageOptions.Builder().cacheInMemory(true)
                                .showImageOnFail(R.drawable.ic_empty_music2)
                                .build(), object : SimpleImageLoadingListener() {
                    override fun onLoadingComplete(imageUri: String, view: View, loadedImage: Bitmap) {
                        doAlbumArtStuff(loadedImage)
                    }

                    override fun onLoadingFailed(imageUri: String, view: View, failReason: FailReason) {
                        val failedBitmap = ImageLoader.getInstance().loadImageSync("drawable://" + R.drawable.ic_empty_music2)
                        doAlbumArtStuff(failedBitmap)
                    }
                })
            }
            if (songtitle != null && MusicPlayer.getTrackName() != null) {
                songtitle!!.text = MusicPlayer.getTrackName()
                if (MusicPlayer.getTrackName().length <= 23) {
                    songtitle!!.textSize = 25f
                } else if (MusicPlayer.getTrackName().length >= 30) {
                    songtitle!!.textSize = 18f
                } else {
                    songtitle!!.textSize = (18 + (MusicPlayer.getTrackName().length - 24)).toFloat()
                }
                Log.v("BaseNowPlayingFrag", "Title Text Size: " + songtitle!!.textSize)
            }
            if (songartist != null) {
                songartist!!.text = MusicPlayer.getArtistName()
                songartist!!.setOnClickListener { NavigationUtils.goToArtist(context, MusicPlayer.getCurrentArtistId()) }
            }
            if (songalbum != null) songalbum!!.text = MusicPlayer.getAlbumName()
        }
        duetoplaypause = false
        if (mPlayPause != null) updatePlayPauseButton()
        if (playPauseFloating != null) updatePlayPauseFloatingButton()
        if (songduration != null && activity != null) songduration!!.text = TimberUtils.makeShortTimeString(activity, MusicPlayer.duration() / 1000)
        if (mProgress != null) {
            mProgress!!.max = MusicPlayer.duration().toInt()
            if (mUpdateProgress != null) {
                mProgress!!.removeCallbacks(mUpdateProgress)
            }
            mProgress!!.postDelayed(mUpdateProgress, 10)
        }
        if (mCircularProgress != null) {
            mCircularProgress!!.max = MusicPlayer.duration().toInt()
            if (mUpdateCircularProgress != null) {
                mCircularProgress!!.removeCallbacks(mUpdateCircularProgress)
            }
            mCircularProgress!!.postDelayed(mUpdateCircularProgress, 10)
        }
        if (timelyView11 != null) {
            mElapsedTimeHandler = Handler()
            mElapsedTimeHandler!!.postDelayed(mUpdateElapsedTime, 600)
        }
    }

    fun setQueueSongs() {
        recyclerView!!.layoutManager = LinearLayoutManager(activity)
        //load queue songs in asynctask
        if (activity != null) loadQueueSongs().execute("")
    }

    fun updatePlayPauseButton() {
        if (MusicPlayer.isPlaying()) {
            if (!mPlayPause!!.isPlayed) {
                mPlayPause!!.isPlayed = true
                mPlayPause!!.startAnimation()
            }
        } else {
            if (mPlayPause!!.isPlayed) {
                mPlayPause!!.isPlayed = false
                mPlayPause!!.startAnimation()
            }
        }
    }

    fun updatePlayPauseFloatingButton() {
        if (MusicPlayer.isPlaying()) {
            playPauseDrawable.transformToPause(false)
        } else {
            playPauseDrawable.transformToPlay(false)
        }
    }

    fun notifyPlayingDrawableChange() {
        val position = MusicPlayer.getQueuePosition()
        BaseQueueAdapter.currentlyPlayingPosition = position
    }

    override fun restartLoader() {}
    override fun onPlaylistChanged() {}
    override fun onMetaChanged() {
        updateSongDetails()
        if (recyclerView != null && recyclerView!!.adapter != null) recyclerView!!.adapter!!.notifyDataSetChanged()
    }

    fun setMusicStateListener() {
        (activity as BaseActivity?)!!.setMusicStateListenerListener(this)
    }

    open fun doAlbumArtStuff(loadedImage: Bitmap?) {}
    fun changeDigit(tv: TimelyView?, end: Int) {
        val obja = tv!!.animate(end)
        obja.duration = 400
        obja.start()
    }

    fun changeDigit(tv: TimelyView?, start: Int, end: Int) {
        try {
            val obja = tv!!.animate(start, end)
            obja.duration = 400
            obja.start()
        } catch (e: InvalidParameterException) {
            e.printStackTrace()
        }
    }

    fun tv11(a: Int) {
        if (a != timeArr[0]) {
            changeDigit(timelyView11, timeArr[0], a)
            timeArr[0] = a
        }
    }

    fun tv12(a: Int) {
        if (a != timeArr[1]) {
            changeDigit(timelyView12, timeArr[1], a)
            timeArr[1] = a
        }
    }

    fun tv13(a: Int) {
        if (a != timeArr[2]) {
            changeDigit(timelyView13, timeArr[2], a)
            timeArr[2] = a
        }
    }

    fun tv14(a: Int) {
        if (a != timeArr[3]) {
            changeDigit(timelyView14, timeArr[3], a)
            timeArr[3] = a
        }
    }

    fun tv15(a: Int) {
        if (a != timeArr[4]) {
            changeDigit(timelyView15, timeArr[4], a)
            timeArr[4] = a
        }
    }

    protected fun initGestures(v: View) {
        if (PreferencesUtility.getInstance(v.context).isGesturesEnabled) {
            object : SlideTrackSwitcher() {
                override fun onSwipeBottom() {
                    activity!!.finish()
                }
            }.attach(v)
        }
    }

    private inner class loadQueueSongs : AsyncTask<String?, Void?, String?>() {
        protected override fun doInBackground(vararg params: String?): String? {
            return if (activity != null) {
                mAdapter = BaseQueueAdapter((activity as AppCompatActivity?)!!, getQueueSongs(activity))
                "Executed"
            } else null
        }

        override fun onPostExecute(result: String?) {
            if (result != null) {
                recyclerView!!.adapter = mAdapter
                if (activity != null) recyclerView!!.addItemDecoration(DividerItemDecoration(activity, DividerItemDecoration.VERTICAL_LIST))
                recyclerView!!.scrollToPosition(MusicPlayer.getQueuePosition() - 1)
            }
        }

        override fun onPreExecute() {}
    }
}