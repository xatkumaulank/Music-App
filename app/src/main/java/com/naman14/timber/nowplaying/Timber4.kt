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
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.TransitionDrawable
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.naman14.timber.MusicPlayer
import com.naman14.timber.MusicService
import com.naman14.timber.R
import com.naman14.timber.adapters.SlidingQueueAdapter
import com.naman14.timber.dataloaders.QueueLoader.getQueueSongs
import com.naman14.timber.utils.ImageUtils
import net.steamcrafted.materialiconlib.MaterialDrawableBuilder

class Timber4 : BaseNowplayingFragment() {
    var mBlurredArt: ImageView? = null
    var horizontalRecyclerview: RecyclerView? = null
    var horizontalAdapter: SlidingQueueAdapter? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(
                R.layout.fragment_timber4, container, false)
        setMusicStateListener()
        setSongDetails(rootView)
        mBlurredArt = rootView.findViewById<View>(R.id.album_art_blurred) as ImageView
        horizontalRecyclerview = rootView.findViewById<View>(R.id.queue_recyclerview_horizontal) as RecyclerView
        setupHorizontalQueue()
        initGestures(mBlurredArt!!)
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

    override fun doAlbumArtStuff(loadedImage: Bitmap?) {
        val blurredAlbumArt: setBlurredAlbumArt = setBlurredAlbumArt()
        blurredAlbumArt.execute(loadedImage)
    }

    private fun setupHorizontalQueue() {
        horizontalRecyclerview!!.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        horizontalAdapter = SlidingQueueAdapter(activity!!, getQueueSongs(activity))
        horizontalRecyclerview!!.adapter = horizontalAdapter
        horizontalRecyclerview!!.scrollToPosition(MusicPlayer.getQueuePosition() - 3)
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
                    td.startTransition(200)
                } else {
                    mBlurredArt!!.setImageDrawable(result)
                }
            }
        }

        override fun onPreExecute() {}
    }
}