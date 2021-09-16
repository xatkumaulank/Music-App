package com.naman14.timber.utils

import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import com.naman14.timber.MusicPlayer

/**
 * Created by nv95 on 02.11.16.
 */
open class SlideTrackSwitcher : OnTouchListener {
    private var mDetector: GestureDetector? = null
    private var mView: View? = null
    fun attach(v: View) {
        mView = v
        mDetector = GestureDetector(v.context, SwipeListener())
        v.setOnTouchListener(this)
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        return mDetector!!.onTouchEvent(event)
    }

    private inner class SwipeListener : SimpleOnGestureListener() {
        override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            var result = false
            try {
                val diffY = e2.y - e1.y
                val diffX = e2.x - e1.x
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            onSwipeRight()
                        } else {
                            onSwipeLeft()
                        }
                    }
                    result = true
                } else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY > 0) {
                        onSwipeBottom()
                    } else {
                        onSwipeTop()
                    }
                }
                result = true
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
            return result
        }

        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            MusicPlayer.playOrPause()
            return true
        }

        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            onClick()
            return super.onSingleTapConfirmed(e)
        }
    }

    fun onSwipeRight() {
        MusicPlayer.previous(mView!!.context, true)
    }

    fun onSwipeLeft() {
        MusicPlayer.next()
    }

    fun onSwipeTop() {}
    open fun onSwipeBottom() {}
    open fun onClick() {}

    companion object {
        private const val SWIPE_THRESHOLD = 200
        private const val SWIPE_VELOCITY_THRESHOLD = 100
    }
}