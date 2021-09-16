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
package com.naman14.timber.activities

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.afollestad.appthemeengine.customizers.ATEActivityThemeCustomizer
import com.anjlab.android.iab.v3.BillingProcessor
import com.google.android.gms.cast.framework.media.widget.ExpandedControllerActivity
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.naman14.timber.MusicPlayer
import com.naman14.timber.R
import com.naman14.timber.activities.DonateActivity
import com.naman14.timber.cast.ExpandedControlsActivity
import com.naman14.timber.fragments.*
import com.naman14.timber.permissions.Nammu
import com.naman14.timber.permissions.PermissionCallback
import com.naman14.timber.slidinguppanel.SlidingUpPanelLayout
import com.naman14.timber.subfragments.LyricsFragment
import com.naman14.timber.utils.Constants
import com.naman14.timber.utils.Helpers
import com.naman14.timber.utils.NavigationUtils
import com.naman14.timber.utils.TimberUtils
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import java.util.*

class MainActivity : BaseActivity(), ATEActivityThemeCustomizer {
    private var panelLayout: SlidingUpPanelLayout? = null
    private var navigationView: NavigationView? = null
    private var songtitle: TextView? = null
    private var songartist: TextView? = null
    private var albumart: ImageView? = null
    private var action: String? = null
    private val navigationMap: MutableMap<String?, Runnable> = HashMap()
    private val navDrawerRunnable = Handler()
    private var runnable: Runnable? = null
    private var mDrawerLayout: DrawerLayout? = null
    private var isDarkTheme = false
    private val navigateLibrary = Runnable {
        navigationView!!.menu.findItem(R.id.nav_library).isChecked = true
        val fragment: Fragment = MainFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment).commitAllowingStateLoss()
    }
    private val navigatePlaylist = Runnable {
        navigationView!!.menu.findItem(R.id.nav_playlists).isChecked = true
        val fragment: Fragment = PlaylistFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.hide(supportFragmentManager.findFragmentById(R.id.fragment_container)!!)
        transaction.replace(R.id.fragment_container, fragment).commit()
    }
    private val navigateFolder = Runnable {
        navigationView!!.menu.findItem(R.id.nav_folders).isChecked = true
        val fragment: Fragment = FoldersFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.hide(supportFragmentManager.findFragmentById(R.id.fragment_container)!!)
        transaction.replace(R.id.fragment_container, fragment).commit()
    }
    private val navigateQueue = Runnable {
        navigationView!!.menu.findItem(R.id.nav_queue).isChecked = true
        val fragment: Fragment = QueueFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.hide(supportFragmentManager.findFragmentById(R.id.fragment_container)!!)
        transaction.replace(R.id.fragment_container, fragment).commit()
    }
    private val navigateAlbum = Runnable {
        val albumID = intent.extras.getLong(Constants.ALBUM_ID)
        val fragment: Fragment = AlbumDetailFragment.newInstance(albumID, false, null)
        val fragmentManager = supportFragmentManager
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment).commit()
    }
    private val navigateArtist = Runnable {
        val artistID = intent.extras.getLong(Constants.ARTIST_ID)
        val fragment: Fragment = ArtistDetailFragment.newInstance(artistID, false, null)
        val fragmentManager = supportFragmentManager
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment).commit()
    }
    private val navigateLyrics = Runnable {
        val fragment: Fragment = LyricsFragment()
        val fragmentManager = supportFragmentManager
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment).commit()
    }
    private val navigateNowplaying = Runnable {
        navigateLibrary.run()
        startActivity(Intent(this@MainActivity, NowPlayingActivity::class.java))
    }
    private val permissionReadstorageCallback: PermissionCallback = object : PermissionCallback {
        override fun permissionGranted() {
            loadEverything()
        }

        override fun permissionRefused() {
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        action = intent.action
        isDarkTheme = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("dark_theme", false)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        navigationMap[Constants.NAVIGATE_LIBRARY] = navigateLibrary
        navigationMap[Constants.NAVIGATE_PLAYLIST] = navigatePlaylist
        navigationMap[Constants.NAVIGATE_QUEUE] = navigateQueue
        navigationMap[Constants.NAVIGATE_NOWPLAYING] = navigateNowplaying
        navigationMap[Constants.NAVIGATE_ALBUM] = navigateAlbum
        navigationMap[Constants.NAVIGATE_ARTIST] = navigateArtist
        navigationMap[Constants.NAVIGATE_LYRICS] = navigateLyrics
        mDrawerLayout = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        panelLayout = findViewById<View>(R.id.sliding_layout) as SlidingUpPanelLayout
        navigationView = findViewById<View>(R.id.nav_view) as NavigationView
        val header = navigationView!!.inflateHeaderView(R.layout.nav_header)
        albumart = header.findViewById<View>(R.id.album_art) as ImageView
        songtitle = header.findViewById<View>(R.id.song_title) as TextView
        songartist = header.findViewById<View>(R.id.song_artist) as TextView
        setPanelSlideListeners(panelLayout!!)
        navDrawerRunnable.postDelayed({
            setupDrawerContent(navigationView)
            setupNavigationIcons(navigationView)
        }, 700)
        if (TimberUtils.isMarshmallow()) {
            checkPermissionAndThenLoad()
            //checkWritePermissions();
        } else {
            loadEverything()
        }
        addBackstackListener()
        if (Intent.ACTION_VIEW == action) {
            val handler = Handler()
            handler.postDelayed({
                MusicPlayer.clearQueue()
                MusicPlayer.openFile(intent.data.path)
                MusicPlayer.playOrPause()
                navigateNowplaying.run()
            }, 350)
        }
        if (!panelLayout!!.isPanelHidden && MusicPlayer.getTrackName() == null) {
            panelLayout!!.hidePanel()
        }
        if (playServicesAvailable) {
            val params = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT)
            params.gravity = Gravity.BOTTOM
            val contentRoot = findViewById<FrameLayout>(R.id.content_root)
            contentRoot.addView(LayoutInflater.from(this)
                    .inflate(R.layout.fragment_cast_mini_controller, null), params)
            findViewById<View>(R.id.castMiniController).setOnClickListener { startActivity(Intent(this@MainActivity, ExpandedControllerActivity::class.java)) }
        }
    }

    private fun loadEverything() {
        val navigation = navigationMap[action]
        if (navigation != null) {
            navigation.run()
        } else {
            navigateLibrary.run()
        }
        initQuickControls().execute("")
    }

    private fun checkPermissionAndThenLoad() {
        //check for permission
        if (Nammu.checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE) && Nammu.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            loadEverything()
        } else {
            if (Nammu.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Snackbar.make(panelLayout!!, "Timber will need to read external storage to display songs on your device.",
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction("OK") { Nammu.askForPermission(this@MainActivity, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), permissionReadstorageCallback) }.show()
            } else {
                Nammu.askForPermission(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), permissionReadstorageCallback)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                if (isNavigatingMain) {
                    mDrawerLayout!!.openDrawer(GravityCompat.START)
                } else super.onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (panelLayout!!.isPanelExpanded) {
            panelLayout!!.collapsePanel()
        } else if (mDrawerLayout!!.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout!!.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    private fun setupDrawerContent(navigationView: NavigationView?) {
        navigationView!!.setNavigationItemSelectedListener { menuItem ->
            updatePosition(menuItem)
            true
        }
    }

    private fun setupNavigationIcons(navigationView: NavigationView?) {

        //material-icon-lib currently doesn't work with navigationview of design support library 22.2.0+
        //set icons manually for now
        //https://github.com/code-mc/material-icon-lib/issues/15
        if (!isDarkTheme) {
            navigationView!!.menu.findItem(R.id.nav_library).setIcon(R.drawable.library_music)
            navigationView.menu.findItem(R.id.nav_playlists).setIcon(R.drawable.playlist_play)
            navigationView.menu.findItem(R.id.nav_queue).setIcon(R.drawable.music_note)
            navigationView.menu.findItem(R.id.nav_folders).setIcon(R.drawable.ic_folder_open_black_24dp)
            navigationView.menu.findItem(R.id.nav_nowplaying).setIcon(R.drawable.bookmark_music)
            navigationView.menu.findItem(R.id.nav_settings).setIcon(R.drawable.settings)
            navigationView.menu.findItem(R.id.nav_about).setIcon(R.drawable.information)
            navigationView.menu.findItem(R.id.nav_donate).setIcon(R.drawable.payment_black)
        } else {
            navigationView!!.menu.findItem(R.id.nav_library).setIcon(R.drawable.library_music_white)
            navigationView.menu.findItem(R.id.nav_playlists).setIcon(R.drawable.playlist_play_white)
            navigationView.menu.findItem(R.id.nav_queue).setIcon(R.drawable.music_note_white)
            navigationView.menu.findItem(R.id.nav_folders).setIcon(R.drawable.ic_folder_open_white_24dp)
            navigationView.menu.findItem(R.id.nav_nowplaying).setIcon(R.drawable.bookmark_music_white)
            navigationView.menu.findItem(R.id.nav_settings).setIcon(R.drawable.settings_white)
            navigationView.menu.findItem(R.id.nav_about).setIcon(R.drawable.information_white)
            navigationView.menu.findItem(R.id.nav_donate).setIcon(R.drawable.payment_white)
        }
        try {
            if (!BillingProcessor.isIabServiceAvailable(this)) {
                navigationView.menu.removeItem(R.id.nav_donate)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updatePosition(menuItem: MenuItem) {
        runnable = null
        when (menuItem.itemId) {
            R.id.nav_library -> runnable = navigateLibrary
            R.id.nav_playlists -> runnable = navigatePlaylist
            R.id.nav_folders -> runnable = navigateFolder
            R.id.nav_nowplaying -> if (castSession != null) {
                startActivity(Intent(this@MainActivity, ExpandedControlsActivity::class.java))
            } else {
                NavigationUtils.navigateToNowplaying(this@MainActivity, false)
            }
            R.id.nav_queue -> runnable = navigateQueue
            R.id.nav_settings -> NavigationUtils.navigateToSettings(this@MainActivity)
            R.id.nav_about -> {
                mDrawerLayout!!.closeDrawers()
                val handler = Handler()
                handler.postDelayed({ Helpers.showAbout(this@MainActivity) }, 350)
            }
            R.id.nav_donate -> startActivity(Intent(this@MainActivity, DonateActivity::class.java))
        }
        if (runnable != null) {
            menuItem.isChecked = true
            mDrawerLayout!!.closeDrawers()
            val handler = Handler()
            handler.postDelayed({ runnable!!.run() }, 350)
        }
    }

    fun setDetailsToHeader() {
        val name = MusicPlayer.getTrackName()
        val artist = MusicPlayer.getArtistName()
        if (name != null && artist != null) {
            songtitle!!.text = name
            songartist!!.text = artist
        }
        ImageLoader.getInstance().displayImage(TimberUtils.getAlbumArtUri(MusicPlayer.getCurrentAlbumId()).toString(), albumart,
                DisplayImageOptions.Builder().cacheInMemory(true)
                        .showImageOnFail(R.drawable.ic_empty_music2)
                        .resetViewBeforeLoading(true)
                        .build())
    }

    override fun onMetaChanged() {
        super.onMetaChanged()
        setDetailsToHeader()
        if (panelLayout!!.isPanelHidden && MusicPlayer.getTrackName() != null) {
            panelLayout!!.showPanel()
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        Nammu.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private val isNavigatingMain: Boolean
        private get() {
            val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
            return (currentFragment is MainFragment || currentFragment is QueueFragment
                    || currentFragment is PlaylistFragment || currentFragment is FoldersFragment)
        }

    private fun addBackstackListener() {
        supportFragmentManager.addOnBackStackChangedListener { supportFragmentManager.findFragmentById(R.id.fragment_container)!!.onResume() }
    }

    override fun getActivityTheme(): Int {
        return if (isDarkTheme) R.style.AppThemeNormalDark else R.style.AppThemeNormalLight
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        supportFragmentManager.findFragmentById(R.id.fragment_container)!!.onActivityResult(requestCode, resultCode, data)
    }

    override fun showCastMiniController() {
        findViewById<View>(R.id.castMiniController).visibility = View.VISIBLE
        findViewById<View>(R.id.quickcontrols_container).visibility = View.GONE
        panelLayout!!.hidePanel()
    }

    override fun hideCastMiniController() {
        findViewById<View>(R.id.castMiniController).visibility = View.GONE
        findViewById<View>(R.id.quickcontrols_container).visibility = View.VISIBLE
        panelLayout!!.showPanel()
    }
}