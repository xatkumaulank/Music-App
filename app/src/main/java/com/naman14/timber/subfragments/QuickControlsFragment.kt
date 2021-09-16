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
package com.naman14.timber.subfragments

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.TransitionDrawable
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.fragment.app.Fragment
import com.afollestad.appthemeengine.Config
import com.naman14.timber.MusicPlayer
import com.naman14.timber.R
import com.naman14.timber.activities.BaseActivity
import com.naman14.timber.listeners.MusicStateListener
import com.naman14.timber.utils.*
import com.naman14.timber.widgets.PlayPauseButton
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.assist.FailReason
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener
import net.steamcrafted.materialiconlib.MaterialIconView

class QuickControlsFragment : Fragment(), MusicStateListener {
    private var mProgress: ProgressBar? = null
    private var mSeekBar: SeekBar? = null
    private var overflowcounter = 0
    private var mPlayPause: PlayPauseButton? = null
    private var mPlayPauseExpanded: PlayPauseButton? = null
    private var mTitle: TextView? = null
    private var mTitleExpanded: TextView? = null
    private var mArtist: TextView? = null
    private var mArtistExpanded: TextView? = null
    private var mAlbumArt: ImageView? = null
    private var mBlurredArt: ImageView? = null
    private var rootView: View? = null
    private var playPauseWrapper: View? = null
    private var playPauseWrapperExpanded: View? = null
    private var previous: MaterialIconView? = null
    private var next: MaterialIconView? = null
    private var duetoplaypause = false
    private var fragmentPaused = false
    var mUpdateProgress: Runnable = object : Runnable {
        override fun run() {
            val position = MusicPlayer.position()
            mProgress!!.progress = position.toInt()
            mSeekBar!!.progress = position.toInt()
            overflowcounter--
            if (MusicPlayer.isPlaying()) {
                val delay = (1500 - position % 1000) as Int
                if (overflowcounter < 0 && !fragmentPaused) {
                    overflowcounter++
                    mProgress!!.postDelayed(this, delay.toLong())
                }
            } else mProgress!!.removeCallbacks(this)
        }
    }
    private val mPlayPauseListener = View.OnClickListener {
        duetoplaypause = true
        if (!mPlayPause!!.isPlayed) {
            mPlayPause!!.isPlayed = true
            mPlayPause!!.startAnimation()
        } else {
            mPlayPause!!.isPlayed = false
            mPlayPause!!.startAnimation()
        }
        val handler = Handler()
        handler.postDelayed({ MusicPlayer.playOrPause() }, 200)
    }
    private val mPlayPauseExpandedListener = View.OnClickListener {
        duetoplaypause = true
        if (!mPlayPauseExpanded!!.isPlayed) {
            mPlayPauseExpanded!!.isPlayed = true
            mPlayPauseExpanded!!.startAnimation()
        } else {
            mPlayPauseExpanded!!.isPlayed = false
            mPlayPauseExpanded!!.startAnimation()
        }
        val handler = Handler()
        handler.postDelayed({ MusicPlayer.playOrPause() }, 200)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_playback_controls, container, false)
        this.rootView = rootView
        mPlayPause = rootView.findViewById<View>(R.id.play_pause) as PlayPauseButton
        mPlayPauseExpanded = rootView.findViewById<View>(R.id.playpause) as PlayPauseButton
        playPauseWrapper = rootView.findViewById(R.id.play_pause_wrapper)
        playPauseWrapperExpanded = rootView.findViewById(R.id.playpausewrapper)
        playPauseWrapper?.setOnClickListener(mPlayPauseListener)
        playPauseWrapperExpanded?.setOnClickListener(mPlayPauseExpandedListener)
        mProgress = rootView.findViewById<View>(R.id.song_progress_normal) as ProgressBar
        mSeekBar = rootView.findViewById<View>(R.id.song_progress) as SeekBar
        mTitle = rootView.findViewById<View>(R.id.title) as TextView
        mArtist = rootView.findViewById<View>(R.id.artist) as TextView
        mTitleExpanded = rootView.findViewById<View>(R.id.song_title) as TextView
        mArtistExpanded = rootView.findViewById<View>(R.id.song_artist) as TextView
        mAlbumArt = rootView.findViewById<View>(R.id.album_art_nowplayingcard) as ImageView
        mBlurredArt = rootView.findViewById<View>(R.id.blurredAlbumart) as ImageView
        next = rootView.findViewById<View>(R.id.next) as MaterialIconView
        previous = rootView.findViewById<View>(R.id.previous) as MaterialIconView
        topContainer = rootView.findViewById(R.id.topContainer)
        val layoutParams = mProgress!!.layoutParams as LinearLayout.LayoutParams
        mProgress!!.measure(0, 0)
        layoutParams.setMargins(0, -(mProgress!!.measuredHeight / 2), 0, 0)
        mProgress!!.layoutParams = layoutParams
        mPlayPause!!.setColor(Config.accentColor(activity!!, Helpers.getATEKey(activity)))
        mPlayPauseExpanded!!.setColor(Color.WHITE)
        mSeekBar!!.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                if (b) {
                    MusicPlayer.seek(i.toLong())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        next!!.setOnClickListener {
            val handler = Handler()
            handler.postDelayed({ MusicPlayer.next() }, 200)
        }
        previous!!.setOnClickListener {
            val handler = Handler()
            handler.postDelayed({ MusicPlayer.previous(activity, false) }, 200)
        }
        (activity as BaseActivity?)!!.setMusicStateListenerListener(this)
        if (PreferencesUtility.getInstance(activity).isGesturesEnabled) {
            object : SlideTrackSwitcher() {
                override fun onClick() {
                    NavigationUtils.navigateToNowplaying(activity, false)
                }
            }.attach(rootView.findViewById(R.id.root_view))
        }
        return rootView
    }

    override fun onPause() {
        super.onPause()
        fragmentPaused = true
    }

    fun updateNowplayingCard() {
        mTitle!!.text = MusicPlayer.getTrackName()
        mArtist!!.text = MusicPlayer.getArtistName()
        mTitleExpanded!!.text = MusicPlayer.getTrackName()
        mArtistExpanded!!.text = MusicPlayer.getArtistName()
        if (!duetoplaypause) {
            ImageLoader.getInstance().displayImage(TimberUtils.getAlbumArtUri(MusicPlayer.getCurrentAlbumId()).toString(), mAlbumArt,
                    DisplayImageOptions.Builder().cacheInMemory(true)
                            .showImageOnFail(R.drawable.ic_empty_music2)
                            .resetViewBeforeLoading(true)
                            .build(), object : ImageLoadingListener {
                override fun onLoadingStarted(imageUri: String, view: View) {}
                override fun onLoadingFailed(imageUri: String, view: View, failReason: FailReason) {
                    val failedBitmap = ImageLoader.getInstance().loadImageSync("drawable://" + R.drawable.ic_empty_music2)
                    if (activity != null) setBlurredAlbumArt().execute(failedBitmap)
                }

                override fun onLoadingComplete(imageUri: String, view: View, loadedImage: Bitmap) {
                    if (activity != null) setBlurredAlbumArt().execute(loadedImage)
                }

                override fun onLoadingCancelled(imageUri: String, view: View) {}
            })
        }
        duetoplaypause = false
        mProgress!!.max = MusicPlayer.duration().toInt()
        mSeekBar!!.max = MusicPlayer.duration().toInt()
        mProgress!!.postDelayed(mUpdateProgress, 10)
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        topContainer = rootView!!.findViewById(R.id.topContainer)
        fragmentPaused = false
        if (mProgress != null) mProgress!!.postDelayed(mUpdateProgress, 10)
    }

    fun updateState() {
        if (MusicPlayer.isPlaying()) {
            if (!mPlayPause!!.isPlayed) {
                mPlayPause!!.isPlayed = true
                mPlayPause!!.startAnimation()
            }
            if (!mPlayPauseExpanded!!.isPlayed) {
                mPlayPauseExpanded!!.isPlayed = true
                mPlayPauseExpanded!!.startAnimation()
            }
        } else {
            if (mPlayPause!!.isPlayed) {
                mPlayPause!!.isPlayed = false
                mPlayPause!!.startAnimation()
            }
            if (mPlayPauseExpanded!!.isPlayed) {
                mPlayPauseExpanded!!.isPlayed = false
                mPlayPauseExpanded!!.startAnimation()
            }
        }
    }

    override fun restartLoader() {}
    override fun onPlaylistChanged() {}
    override fun onMetaChanged() {
        updateNowplayingCard()
        updateState()
    }

    private inner class setBlurredAlbumArt : AsyncTask<Bitmap?, Void?, Drawable?>() {
        protected override fun doInBackground(vararg loadedImage: Bitmap?): Drawable? {
            var drawable: Drawable? = null
            try {
                drawable = ImageUtils.createBlurredImageFromBitmap(loadedImage[0], activity, 6)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return drawable
        }

        override fun onPostExecute(result: Drawable?) {
            if (result != null) {
                if (mBlurredArt!!.drawable != null) {
                    val td = TransitionDrawable(arrayOf(
                            mBlurredArt!!.drawable,
                            result
                    ))
                    mBlurredArt!!.setImageDrawable(td)
                    td.startTransition(400)
                } else {
                    mBlurredArt!!.setImageDrawable(result)
                }
            }
        }

        override fun onPreExecute() {}
    }

    companion object {
        var topContainer: View? = null
    }
}