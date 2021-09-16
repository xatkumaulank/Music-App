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
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.naman14.timber.MusicPlayer
import com.naman14.timber.MusicService
import com.naman14.timber.R
import com.naman14.timber.adapters.SlidingQueueAdapter
import com.naman14.timber.dataloaders.QueueLoader.getQueueSongs
import com.naman14.timber.utils.ImageUtils
import net.steamcrafted.materialiconlib.MaterialDrawableBuilder

/**
 * Created by naman on 22/02/17.
 */
class Timber5 : BaseNowplayingFragment() {
    var mBlurredArt: ImageView? = null
    override var recyclerView: RecyclerView? = null
    var adapter: SlidingQueueAdapter? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(
                R.layout.fragment_timber5, container, false)
        setMusicStateListener()
        setSongDetails(rootView)
        mBlurredArt = rootView.findViewById<View>(R.id.album_art_blurred) as ImageView
        recyclerView = rootView.findViewById<View>(R.id.queue_recyclerview_horizontal) as RecyclerView
        initGestures(mBlurredArt!!)
        setupSlidingQueue()
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

    private fun setupSlidingQueue() {
        recyclerView!!.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        adapter = SlidingQueueAdapter((activity as AppCompatActivity?)!!, getQueueSongs(activity))
        recyclerView!!.adapter = adapter
        recyclerView!!.scrollToPosition(MusicPlayer.getQueuePosition() - 3)
    }

    private inner class setBlurredAlbumArt : AsyncTask<Bitmap?, Void?, Drawable?>() {
        protected override fun doInBackground(vararg loadedImage: Bitmap?): Drawable? {
            var drawable: Drawable? = null
            try {
                drawable = ImageUtils.createBlurredImageFromBitmap(loadedImage[0], activity, 12)
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