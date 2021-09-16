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
package com.naman14.timber.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.view.View
import android.widget.ImageView
import com.naman14.timber.R
import com.naman14.timber.dataloaders.AlbumLoader.getAlbum
import com.naman14.timber.lastfmapi.LastFmClient.Companion.getInstance
import com.naman14.timber.lastfmapi.callbacks.AlbumInfoListener
import com.naman14.timber.lastfmapi.models.AlbumQuery
import com.naman14.timber.lastfmapi.models.LastfmAlbum
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.assist.FailReason
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

object ImageUtils {
    private val lastfmDisplayImageOptions = DisplayImageOptions.Builder()
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .showImageOnFail(R.drawable.ic_empty_music2)
            .build()
    private val diskDisplayImageOptions = DisplayImageOptions.Builder()
            .cacheInMemory(true)
            .build()

    @JvmOverloads
    fun loadAlbumArtIntoView(albumId: Long, view: ImageView,
                             listener: ImageLoadingListener = SimpleImageLoadingListener()) {
        if (PreferencesUtility.getInstance(view.context).alwaysLoadAlbumImagesFromLastfm()) {
            loadAlbumArtFromLastfm(albumId, view, listener)
        } else {
            loadAlbumArtFromDiskWithLastfmFallback(albumId, view, listener)
        }
    }

    private fun loadAlbumArtFromDiskWithLastfmFallback(albumId: Long, view: ImageView,
                                                       listener: ImageLoadingListener) {
        ImageLoader.getInstance()
                .displayImage(TimberUtils.getAlbumArtUri(albumId).toString(),
                        view,
                        diskDisplayImageOptions,
                        object : SimpleImageLoadingListener() {
                            override fun onLoadingFailed(imageUri: String, view: View,
                                                         failReason: FailReason) {
                                loadAlbumArtFromLastfm(albumId, view as ImageView, listener)
                                listener.onLoadingFailed(imageUri, view, failReason)
                            }

                            override fun onLoadingComplete(imageUri: String, view: View, loadedImage: Bitmap) {
                                listener.onLoadingComplete(imageUri, view, loadedImage)
                            }
                        })
    }

    private fun loadAlbumArtFromLastfm(albumId: Long, albumArt: ImageView, listener: ImageLoadingListener) {
        val album = getAlbum(albumArt.context, albumId)
        getInstance(albumArt.context)
                ?.getAlbumInfo(AlbumQuery(album.title, album.artistName),
                        object : AlbumInfoListener {
                            override fun albumInfoSuccess(album: LastfmAlbum?) {
                                if (album != null) {
                                    ImageLoader.getInstance()
                                            .displayImage(album.mArtwork!![4].mUrl,
                                                    albumArt,
                                                    lastfmDisplayImageOptions, object : SimpleImageLoadingListener() {
                                                override fun onLoadingComplete(imageUri: String, view: View, loadedImage: Bitmap) {
                                                    listener.onLoadingComplete(imageUri, view, loadedImage)
                                                }

                                                override fun onLoadingFailed(imageUri: String, view: View, failReason: FailReason) {
                                                    listener.onLoadingFailed(imageUri, view, failReason)
                                                }
                                            })
                                }
                            }

                            override fun albumInfoFailed() {}
                        })
    }

    fun createBlurredImageFromBitmap(bitmap: Bitmap, context: Context, inSampleSize: Int): Drawable {
        val rs = RenderScript.create(context)
        val options = BitmapFactory.Options()
        options.inSampleSize = inSampleSize
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        val imageInByte = stream.toByteArray()
        val bis = ByteArrayInputStream(imageInByte)
        val blurTemplate = BitmapFactory.decodeStream(bis, null, options)
        val input = Allocation.createFromBitmap(rs, blurTemplate)
        val output = Allocation.createTyped(rs, input.type)
        val script = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
            ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
        } else {
            TODO("VERSION.SDK_INT < JELLY_BEAN_MR1")
        }
        script.setRadius(8f)
        script.setInput(input)
        script.forEach(output)
        output.copyTo(blurTemplate)
        return BitmapDrawable(context.resources, blurTemplate)
    }
}