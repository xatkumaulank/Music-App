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

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.naman14.timber.MusicPlayer
import com.naman14.timber.MusicService
import com.naman14.timber.R
import com.naman14.timber.utils.TimberUtils
import net.steamcrafted.materialiconlib.MaterialDrawableBuilder

class Timber1 : BaseNowplayingFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(
                R.layout.fragment_timber1, container, false)
        setMusicStateListener()
        setSongDetails(rootView)
        initGestures(rootView.findViewById(R.id.album_art))
        return rootView
    }

    override fun updateShuffleState() {
        if (shuffle != null && activity != null) {
            val builder = MaterialDrawableBuilder.with(activity)
                    .setIcon(MaterialDrawableBuilder.IconValue.SHUFFLE)
                    .setSizeDp(30)
            builder.setColor(TimberUtils.getBlackWhiteColor(accentColor))
            shuffle!!.setImageDrawable(builder.build())
            shuffle!!.setOnClickListener {
                val handler = Handler()
                handler.postDelayed({
                    MusicPlayer.setShuffleMode(MusicService.SHUFFLE_NORMAL)
                    MusicPlayer.next()
                    recyclerView!!.scrollToPosition(MusicPlayer.getQueuePosition())
                }, 150)
            }
        }
    }
}