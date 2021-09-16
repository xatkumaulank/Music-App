package com.naman14.timber.fragments

import android.app.Activity
import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.*
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.appthemeengine.ATE
import com.naman14.timber.R
import com.naman14.timber.adapters.FolderAdapter
import com.naman14.timber.dialogs.StorageSelectDialog
import com.naman14.timber.dialogs.StorageSelectDialog.OnDirSelectListener
import com.naman14.timber.utils.PreferencesUtility
import com.naman14.timber.widgets.DividerItemDecoration
import com.naman14.timber.widgets.FastScroller
import java.io.File

/**
 * Created by nv95 on 10.11.16.
 */
class FoldersFragment : Fragment(), OnDirSelectListener {
    private var mAdapter: FolderAdapter? = null
    private var recyclerView: RecyclerView? = null
    private var fastScroller: FastScroller? = null
    private var mProgressBar: ProgressBar? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(
                R.layout.fragment_folders, container, false)
        val toolbar = rootView.findViewById<View>(R.id.toolbar) as Toolbar
        (activity as AppCompatActivity?)!!.setSupportActionBar(toolbar)
        val ab = (activity as AppCompatActivity?)!!.supportActionBar
        ab!!.setHomeAsUpIndicator(R.drawable.ic_menu)
        ab.setDisplayHomeAsUpEnabled(true)
        ab.setTitle(R.string.folders)
        recyclerView = rootView.findViewById<View>(R.id.recyclerview) as RecyclerView
        fastScroller = rootView.findViewById<View>(R.id.fastscroller) as FastScroller
        mProgressBar = rootView.findViewById<View>(R.id.progressBar) as ProgressBar
        recyclerView!!.layoutManager = LinearLayoutManager(activity)
        if (activity != null) loadFolders().execute("")
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val dark = PreferenceManager.getDefaultSharedPreferences(activity).getBoolean("dark_theme", false)
        if (dark) {
            ATE.apply(this, "dark_theme")
        } else {
            ATE.apply(this, "light_theme")
        }
        if (mAdapter != null) {
            mAdapter!!.applyTheme(dark)
            mAdapter!!.notifyDataSetChanged()
        }
    }

    private fun setItemDecoration() {
        recyclerView!!.addItemDecoration(DividerItemDecoration(activity, DividerItemDecoration.VERTICAL_LIST))
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_folders, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_storages) {
            StorageSelectDialog(activity!!)
                    .setDirSelectListener(this)
                    .show()
        }
        return super.onOptionsItemSelected(item)
    }

    fun updateTheme() {
        val context: Context? = activity
        if (context != null) {
            val dark = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("dark_theme", false)
            mAdapter!!.applyTheme(dark)
        }
    }

    override fun onDirSelected(dir: File?) {
        mAdapter!!.updateDataSetAsync(dir!!)
    }

    private inner class loadFolders : AsyncTask<String?, Void?, String>() {
        protected override fun doInBackground(vararg params: String?): String {
            val activity: Activity? = activity
            if (activity != null) {
                mAdapter = FolderAdapter(activity, File(PreferencesUtility.getInstance(activity).lastFolder))
                updateTheme()
            }
            return "Executed"
        }

        override fun onPostExecute(result: String) {
            recyclerView!!.adapter = mAdapter
            //to add spacing between cards
            if (activity != null) {
                setItemDecoration()
            }
            mAdapter!!.notifyDataSetChanged()
            mProgressBar!!.visibility = View.GONE
            fastScroller!!.visibility = View.VISIBLE
            fastScroller!!.setRecyclerView(recyclerView)
        }

        override fun onPreExecute() {}
    }
}