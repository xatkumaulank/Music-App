package com.naman14.timber.cast

import com.google.android.gms.cast.framework.Session
import com.google.android.gms.cast.framework.SessionManagerListener

/**
 * Created by naman on 7/12/17.
 */
open class SimpleSessionManagerListener : SessionManagerListener<Any?> {
    override fun onSessionStarted(session: Session?, sessionId: String) {}
    override fun onSessionResumed(session: Session?, wasSuspended: Boolean) {}
    override fun onSessionEnded(session: Session?, error: Int) {}
    override fun onSessionSuspended(session: Session?, i: Int) {}
    override fun onSessionStarting(session: Session?) {}
    override fun onSessionEnding(session: Session?) {}
    override fun onSessionResuming(session: Session?, s: String) {}
    override fun onSessionResumeFailed(session: Session?, i: Int) {}
    override fun onSessionStartFailed(session: Session?, i: Int) {}
}