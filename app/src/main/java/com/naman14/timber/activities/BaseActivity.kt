/*
 * Copyright (C) 2012 Andrew Neal
 * Copyright (C) 2014 The CyanogenMod Project
 * Copyright (C) 2015 Naman Dwivedi
 *
 * Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package com.naman14.timber.activities

import android.content.*
import android.media.AudioManager
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.afollestad.appthemeengine.ATE
import com.afollestad.appthemeengine.ATEActivity
import com.google.android.gms.cast.framework.*
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.naman14.timber.ITimberService
import com.naman14.timber.MusicPlayer
import com.naman14.timber.MusicPlayer.ServiceToken
import com.naman14.timber.MusicService
import com.naman14.timber.R
import com.naman14.timber.activities.BaseActivity
import com.naman14.timber.cast.SimpleSessionManagerListener
import com.naman14.timber.cast.WebServer
import com.naman14.timber.listeners.MusicStateListener
import com.naman14.timber.slidinguppanel.SlidingUpPanelLayout
import com.naman14.timber.subfragments.QuickControlsFragment
import com.naman14.timber.utils.Helpers
import com.naman14.timber.utils.NavigationUtils
import com.naman14.timber.utils.TimberUtils
import java.io.IOException
import java.lang.ref.WeakReference
import java.util.*

open class BaseActivity : ATEActivity(), ServiceConnection, MusicStateListener {
    private val mMusicStateListener = ArrayList<MusicStateListener>()
    private var mToken: ServiceToken? = null
    private var mPlaybackStatus: PlaybackStatus? = null
    var castSession: CastSession? = null
        private set
    private var mSessionManager: SessionManager? = null
    private val mSessionManagerListener: SessionManagerListener<*> = SessionManagerListenerImpl()
    private var castServer: WebServer? = null
    @JvmField
    var playServicesAvailable = false

    private inner class SessionManagerListenerImpl : SimpleSessionManagerListener() {
        override fun onSessionStarting(session: Session) {
            super.onSessionStarting(session)
            startCastServer()
        }

        override fun onSessionStarted(session: Session, sessionId: String) {
            invalidateOptionsMenu()
            castSession = mSessionManager!!.currentCastSession
            showCastMiniController()
        }

        override fun onSessionResumed(session: Session, wasSuspended: Boolean) {
            invalidateOptionsMenu()
            castSession = mSessionManager!!.currentCastSession
        }

        override fun onSessionEnded(session: Session, error: Int) {
            castSession = null
            hideCastMiniController()
            stopCastServer()
        }

        override fun onSessionResuming(session: Session, s: String) {
            super.onSessionResuming(session, s)
            startCastServer()
        }

        override fun onSessionSuspended(session: Session, i: Int) {
            super.onSessionSuspended(session, i)
            stopCastServer()
        }
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mToken = MusicPlayer.bindToService(this, this)
        mPlaybackStatus = PlaybackStatus(this)
        //make volume keys change multimedia volume even if music is not playing now
        volumeControlStream = AudioManager.STREAM_MUSIC
        try {
            playServicesAvailable = GoogleApiAvailability
                    .getInstance().isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS
        } catch (ignored: Exception) {
        }
        if (playServicesAvailable) initCast()
    }

    override fun onStart() {
        super.onStart()
        val filter = IntentFilter()
        // Play and pause changes
        filter.addAction(MusicService.PLAYSTATE_CHANGED)
        // Track changes
        filter.addAction(MusicService.META_CHANGED)
        // Update a list, probably the playlist fragment's
        filter.addAction(MusicService.REFRESH)
        // If a playlist has changed, notify us
        filter.addAction(MusicService.PLAYLIST_CHANGED)
        // If there is an error playing a track
        filter.addAction(MusicService.TRACK_ERROR)
        registerReceiver(mPlaybackStatus, filter)
    }

    override fun onStop() {
        super.onStop()
    }

    public override fun onResume() {
        if (playServicesAvailable) {
            castSession = mSessionManager!!.currentCastSession
            mSessionManager!!.addSessionManagerListener(mSessionManagerListener as SessionManagerListener<Session>?)
        }
        //For Android 8.0+: service may get destroyed if in background too long
        if (MusicPlayer.mService == null) {
            mToken = MusicPlayer.bindToService(this, this)
        }
        onMetaChanged()
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        if (playServicesAvailable) {
            mSessionManager!!.removeSessionManagerListener(mSessionManagerListener as SessionManagerListener<Session>?)
            castSession = null
        }
    }

    override fun onServiceConnected(name: ComponentName, service: IBinder) {
        MusicPlayer.mService = ITimberService.Stub.asInterface(service)
        onMetaChanged()
    }

    private fun initCast() {
        val castContext = CastContext.getSharedInstance(this)
        mSessionManager = castContext.sessionManager
    }

    override fun onServiceDisconnected(name: ComponentName) {
        MusicPlayer.mService = null
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unbind from the service
        if (mToken != null) {
            MusicPlayer.unbindFromService(mToken)
            mToken = null
        }
        try {
            unregisterReceiver(mPlaybackStatus)
        } catch (e: Throwable) {
        }
        mMusicStateListener.clear()
    }

    override fun onMetaChanged() {
        // Let the listener know to the meta chnaged
        for (listener in mMusicStateListener) {
            listener?.onMetaChanged()
        }
    }

    override fun restartLoader() {
        // Let the listener know to update a list
        for (listener in mMusicStateListener) {
            listener.restartLoader()
        }
    }

    override fun onPlaylistChanged() {
        // Let the listener know to update a list
        for (listener in mMusicStateListener) {
            listener.onPlaylistChanged()
        }
    }

    fun setMusicStateListenerListener(status: MusicStateListener?) {
        if (status === this) {
            throw UnsupportedOperationException("Override the method, don't add a listener")
        }
        if (status != null) {
            mMusicStateListener.add(status)
        }
    }

    fun removeMusicStateListenerListener(status: MusicStateListener?) {
        if (status != null) {
            mMusicStateListener.remove(status)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        menuInflater.inflate(R.menu.menu_cast, menu)
        if (playServicesAvailable) {
            CastButtonFactory.setUpMediaRouteButton(applicationContext,
                    menu,
                    R.id.media_route_menu_item)
        }
        if (!TimberUtils.hasEffectsPanel(this@BaseActivity)) {
            menu.removeItem(R.id.action_equalizer)
        }
        ATE.applyMenu(this, ateKey, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                super.onBackPressed()
                return true
            }
            R.id.action_settings -> {
                NavigationUtils.navigateToSettings(this)
                return true
            }
            R.id.action_shuffle -> {
                val handler = Handler()
                handler.postDelayed({ MusicPlayer.shuffleAll(this@BaseActivity) }, 80)
                return true
            }
            R.id.action_search -> {
                NavigationUtils.navigateToSearch(this)
                return true
            }
            R.id.action_equalizer -> {
                NavigationUtils.navigateToEqualizer(this)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    public override fun getATEKey(): String? {
        return Helpers.getATEKey(this)
    }

    fun setPanelSlideListeners(panelLayout: SlidingUpPanelLayout) {
        panelLayout.setPanelSlideListener(object : SlidingUpPanelLayout.PanelSlideListener {
            override fun onPanelSlide(panel: View, slideOffset: Float) {
                val nowPlayingCard = QuickControlsFragment.topContainer
                if (nowPlayingCard != null) nowPlayingCard.alpha = 1 - slideOffset
            }

            override fun onPanelCollapsed(panel: View) {
                val nowPlayingCard = QuickControlsFragment.topContainer
                if (nowPlayingCard != null) nowPlayingCard.alpha = 1f
            }

            override fun onPanelExpanded(panel: View) {
                val nowPlayingCard = QuickControlsFragment.topContainer
                if (nowPlayingCard != null) nowPlayingCard.alpha = 0f
            }

            override fun onPanelAnchored(panel: View) {}
            override fun onPanelHidden(panel: View) {}
        })
    }

    private class PlaybackStatus(activity: BaseActivity) : BroadcastReceiver() {
        private val mReference: WeakReference<BaseActivity>
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            val baseActivity = mReference.get()
            if (baseActivity != null) {
                if (action == MusicService.META_CHANGED) {
                    baseActivity.onMetaChanged()
                } else if (action == MusicService.PLAYSTATE_CHANGED) {
//                    baseActivity.mPlayPauseProgressButton.getPlayPauseButton().updateState();
                } else if (action == MusicService.REFRESH) {
                    baseActivity.restartLoader()
                } else if (action == MusicService.PLAYLIST_CHANGED) {
                    baseActivity.onPlaylistChanged()
                } else if (action == MusicService.TRACK_ERROR) {
                    val errorMsg = context.getString(R.string.error_playing_track)
                    Toast.makeText(baseActivity, errorMsg, Toast.LENGTH_SHORT).show()
                }
            }
        }

        init {
            mReference = WeakReference(activity)
        }
    }

    inner class initQuickControls : AsyncTask<String?, Void?, String>() {
        protected  fun doInBackground(vararg params: String): String {
            val fragment1 = QuickControlsFragment()
            val fragmentManager1 = supportFragmentManager
            fragmentManager1.beginTransaction()
                    .replace(R.id.quickcontrols_container, fragment1).commitAllowingStateLoss()
            return "Executed"
        }

        override fun onPostExecute(result: String) {}
        override fun onPreExecute() {}
    }

    open fun showCastMiniController() {
        //implement by overriding in activities
    }

    open fun hideCastMiniController() {
        //implement by overriding in activities
    }

    private fun startCastServer() {
        castServer = WebServer(this)
        try {
            castServer!!.start()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun stopCastServer() {
        if (castServer != null) {
            castServer!!.stop()
        }
    }
}