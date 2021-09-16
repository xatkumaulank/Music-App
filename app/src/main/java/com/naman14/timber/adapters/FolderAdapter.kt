package com.naman14.timber.adapters

import android.app.Activity
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.naman14.timber.R
import com.naman14.timber.dataloaders.FolderLoader
import com.naman14.timber.dataloaders.SongLoader
import com.naman14.timber.models.Song
import com.naman14.timber.utils.PreferencesUtility
import com.naman14.timber.utils.TimberUtils
import com.naman14.timber.widgets.BubbleTextGetter
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import java.io.File
import java.util.*

/**
 * Created by nv95 on 10.11.16.
 */
class FolderAdapter(private val mContext: Activity, root: File) : BaseSongAdapter<FolderAdapter.ItemHolder?>(), BubbleTextGetter {
    private var mFileSet: List<File>? = null
    private val mSongs: MutableList<Song>
    private var mRoot: File? = null
    private val mIcons: Array<Drawable?>
    private var mBusy = false
    fun applyTheme(dark: Boolean) {
        val cf: ColorFilter = PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
        for (d in mIcons) {
            if (dark) {
                d!!.colorFilter = cf
            } else {
                d!!.clearColorFilter()
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ItemHolder {
        val v = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_folder_list, viewGroup, false)
        return ItemHolder(v)
    }

    override fun onBindViewHolder(itemHolder: ItemHolder?, i: Int) {
        val localItem = mFileSet?.get(i)
        val song = mSongs[i]
        itemHolder!!.title.text = localItem?.name
        if (localItem!!.isDirectory) {
            itemHolder.albumArt.setImageDrawable(if (".." == localItem.name) mIcons[1] else mIcons[0])
        } else {
            ImageLoader.getInstance().displayImage(TimberUtils.getAlbumArtUri(song.albumId).toString(),
                    itemHolder.albumArt,
                    DisplayImageOptions.Builder().cacheInMemory(true).showImageOnFail(mIcons[2])
                            .resetViewBeforeLoading(true).build())
        }
    }

    override fun getItemCount(): Int {
        return mFileSet!!.size
    }

    @Deprecated("")
    fun updateDataSet(newRoot: File) {
        if (mBusy) {
            return
        }
        if (".." == newRoot.name) {
            goUp()
            return
        }
        mRoot = newRoot
        mFileSet = FolderLoader.getMediaFiles(newRoot, true)
        getSongsForFiles(mFileSet!!)
    }

    @Deprecated("")
    fun goUp(): Boolean {
        if (mRoot == null || mBusy) {
            return false
        }
        val parent = mRoot!!.parentFile
        return if (parent != null && parent.canRead()) {
            updateDataSet(parent)
            true
        } else {
            false
        }
    }

    fun goUpAsync(): Boolean {
        if (mRoot == null || mBusy) {
            return false
        }
        val parent = mRoot!!.parentFile
        return if (parent != null && parent.canRead()) {
            updateDataSetAsync(parent)
        } else {
            false
        }
    }

    fun updateDataSetAsync(newRoot: File): Boolean {
        if (mBusy) {
            return false
        }
        if (".." == newRoot.name) {
            goUpAsync()
            return false
        }
        mRoot = newRoot
        NavigateTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mRoot)
        return true
    }

    override fun getTextToShowInBubble(pos: Int): String {
        return if (mBusy || mFileSet?.size == 0) "" else try {
            val f = mFileSet?.get(pos)
            if (f!!.isDirectory) {
                f.name[0].toString()
            } else {
                Character.toString(f.name[0])
            }
        } catch (e: Exception) {
            ""
        }
    }

    private fun getSongsForFiles(files: List<File>) {
        mSongs.clear()
        for (file in files) {
            mSongs.add(SongLoader.getSongFromPath(file.absolutePath, mContext))
        }
    }

    private inner class NavigateTask : AsyncTask<File?, Void?, List<File>>() {
        override fun onPreExecute() {
            super.onPreExecute()
            mBusy = true
        }

        protected override fun doInBackground(vararg params: File?): List<File> {
            val files = FolderLoader.getMediaFiles(params[0], true)
            getSongsForFiles(files)
            return files
        }

        override fun onPostExecute(files: List<File>) {
            super.onPostExecute(files)
            mFileSet = files
            notifyDataSetChanged()
            mBusy = false
            PreferencesUtility.getInstance(mContext).storeLastFolder(mRoot!!.path)
        }

    }

    inner class ItemHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        var title: TextView
        var albumArt: ImageView
        override fun onClick(v: View) {
            if (mBusy) {
                return
            }
            val f = mFileSet?.get(adapterPosition)
            if (f != null) {
                if (f.isDirectory && updateDataSetAsync(f)) {
                    albumArt.setImageDrawable(mIcons[3])
                } else if (f.isFile) {
                    val handler = Handler()
                    handler.postDelayed({
                        var current = -1
                        val songId = SongLoader.getSongFromPath(mFileSet?.get(adapterPosition)?.absolutePath, mContext).id
                        var count = 0
                        for (song in mSongs) {
                            if (song.id != -1L) {
                                count++
                            }
                        }
                        val ret = LongArray(count)
                        var j = 0
                        for (i in 0 until itemCount) {
                            if (mSongs[i].id != -1L) {
                                ret[j] = mSongs[i].id
                                if (mSongs[i].id == songId) {
                                    current = j
                                }
                                j++
                            }
                        }
                        playAll(mContext, ret, current, -1, TimberUtils.IdType.NA,
                                false, mSongs[adapterPosition], false)
                    }, 100)
                }
            }
        }

        init {
            title = view.findViewById<View>(R.id.folder_title) as TextView
            albumArt = view.findViewById<View>(R.id.album_art) as ImageView
            view.setOnClickListener(this)
        }
    }

    init {
        mIcons = arrayOf(
                ContextCompat.getDrawable(mContext, R.drawable.ic_folder_open_black_24dp),
                ContextCompat.getDrawable(mContext, R.drawable.ic_folder_parent_dark),
                ContextCompat.getDrawable(mContext, R.drawable.ic_file_music_dark),
                ContextCompat.getDrawable(mContext, R.drawable.ic_timer_wait)
        )
        mSongs = ArrayList()
        updateDataSet(root)
    }
}