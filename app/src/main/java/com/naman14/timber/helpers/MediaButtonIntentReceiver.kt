/*
 * Copyright (C) 2007 The Android Open Source Project Licensed under the Apache
 * License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package com.naman14.timber.helpers

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Handler
import android.os.Message
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import android.util.Log
import android.view.KeyEvent
import androidx.legacy.content.WakefulBroadcastReceiver
import com.naman14.timber.MusicService
import com.naman14.timber.activities.MainActivity
import com.naman14.timber.utils.PreferencesUtility

/**
 * Used to control headset playback.
 * Single press: pause/resume
 * Double press: next track
 * Triple press: previous track
 * Long press: voice search
 */
class MediaButtonIntentReceiver : WakefulBroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val intentAction = intent.action
        if (AudioManager.ACTION_AUDIO_BECOMING_NOISY == intentAction) {
            if (PreferencesUtility.getInstance(context).pauseEnabledOnDetach()) startService(context, MusicService.CMDPAUSE)
        } else if (Intent.ACTION_MEDIA_BUTTON == intentAction) {
            val event = intent.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT) ?: return
            val keycode = event.keyCode
            val action = event.action
            val eventtime = event.eventTime
            var command: String? = null
            when (keycode) {
                KeyEvent.KEYCODE_MEDIA_STOP -> command = MusicService.CMDSTOP
                KeyEvent.KEYCODE_HEADSETHOOK, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> command = MusicService.CMDTOGGLEPAUSE
                KeyEvent.KEYCODE_MEDIA_NEXT -> command = MusicService.CMDNEXT
                KeyEvent.KEYCODE_MEDIA_PREVIOUS -> command = MusicService.CMDPREVIOUS
                KeyEvent.KEYCODE_MEDIA_PAUSE -> command = MusicService.CMDPAUSE
                KeyEvent.KEYCODE_MEDIA_PLAY -> command = MusicService.CMDPLAY
            }
            if (command != null) {
                if (action == KeyEvent.ACTION_DOWN) {
                    if (mDown) {
                        if (MusicService.CMDTOGGLEPAUSE == command || MusicService.CMDPLAY == command) {
                            if (mLastClickTime != 0L && eventtime - mLastClickTime > LONG_PRESS_DELAY) {
                                acquireWakeLockAndSendMessage(context,
                                        mHandler.obtainMessage(MSG_LONGPRESS_TIMEOUT, context), 0)
                            }
                        }
                    } else if (event.repeatCount == 0) {
                        if (keycode == KeyEvent.KEYCODE_HEADSETHOOK) {
                            if (eventtime - mLastClickTime >= DOUBLE_CLICK) {
                                mClickCounter = 0
                            }
                            mClickCounter++
                            if (DEBUG) Log.v(TAG, "Got headset click, count = " + mClickCounter)
                            mHandler.removeMessages(MSG_HEADSET_DOUBLE_CLICK_TIMEOUT)
                            val msg = mHandler.obtainMessage(
                                    MSG_HEADSET_DOUBLE_CLICK_TIMEOUT, mClickCounter, 0, context)
                            val delay = if (mClickCounter < 3) DOUBLE_CLICK.toLong() else 0.toLong()
                            if (mClickCounter >= 3) {
                                mClickCounter = 0
                            }
                            mLastClickTime = eventtime
                            acquireWakeLockAndSendMessage(context, msg, delay)
                        } else {
                            startService(context, command)
                        }
                        mLaunched = false
                        mDown = true
                    }
                } else {
                    mHandler.removeMessages(MSG_LONGPRESS_TIMEOUT)
                    mDown = false
                }
                if (isOrderedBroadcast) {
                    abortBroadcast()
                }
                releaseWakeLockIfHandlerIdle()
            }
        }
    }

    companion object {
        private const val DEBUG = false
        private const val TAG = "ButtonIntentReceiver"
        private const val MSG_LONGPRESS_TIMEOUT = 1
        private const val MSG_HEADSET_DOUBLE_CLICK_TIMEOUT = 2
        private const val LONG_PRESS_DELAY = 1000
        private const val DOUBLE_CLICK = 800
        private var mWakeLock: WakeLock? = null
        private var mClickCounter = 0
        private var mLastClickTime: Long = 0
        private var mDown = false
        private var mLaunched = false
        private val mHandler: Handler = object : Handler() {
            /**
             * {@inheritDoc}
             */
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    MSG_LONGPRESS_TIMEOUT -> {
                        if (DEBUG) Log.v(TAG, "Handling longpress timeout, launched " + mLaunched)
                        if (!mLaunched) {
                            val context = msg.obj as Context
                            val i = Intent()
                            i.setClass(context, MainActivity::class.java)
                            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                            context.startActivity(i)
                            mLaunched = true
                        }
                    }
                    MSG_HEADSET_DOUBLE_CLICK_TIMEOUT -> {
                        val clickCount = msg.arg1
                        val command: String?
                        if (DEBUG) Log.v(TAG, "Handling headset click, count = $clickCount")
                        command = when (clickCount) {
                            1 -> MusicService.CMDTOGGLEPAUSE
                            2 -> MusicService.CMDNEXT
                            3 -> MusicService.CMDPREVIOUS
                            else -> null
                        }
                        if (command != null) {
                            val context = msg.obj as Context
                            startService(context, command)
                        }
                    }
                }
                releaseWakeLockIfHandlerIdle()
            }
        }

        private fun startService(context: Context, command: String) {
            val i = Intent(context, MusicService::class.java)
            i.action = MusicService.SERVICECMD
            i.putExtra(MusicService.CMDNAME, command)
            i.putExtra(MusicService.FROM_MEDIA_BUTTON, true)
            startWakefulService(context, i)
        }

        @SuppressLint("InvalidWakeLockTag")
        private fun acquireWakeLockAndSendMessage(context: Context, msg: Message, delay: Long) {
            if (mWakeLock == null) {
                val appContext = context.applicationContext
                val pm = appContext.getSystemService(Context.POWER_SERVICE) as PowerManager
                mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"Timber headset button")
                mWakeLock!!.setReferenceCounted(false)
            }
            if (DEBUG) Log.v(TAG, "Acquiring wake lock and sending " + msg.what)
            // Make sure we don't indefinitely hold the wake lock under any circumstances
            mWakeLock!!.acquire(10000)
            mHandler.sendMessageDelayed(msg, delay)
        }

        private fun releaseWakeLockIfHandlerIdle() {
            if (mHandler.hasMessages(MSG_LONGPRESS_TIMEOUT)
                    || mHandler.hasMessages(MSG_HEADSET_DOUBLE_CLICK_TIMEOUT)) {
                if (DEBUG) Log.v(TAG, "Handler still has messages pending, not releasing wake lock")
                return
            }
            if (mWakeLock != null) {
                if (DEBUG) Log.v(TAG, "Releasing wake lock")
                mWakeLock!!.release()
                mWakeLock = null
            }
        }
    }
}