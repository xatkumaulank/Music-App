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

import android.os.Build
import android.view.View
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.AnimationUtils
import android.view.animation.Interpolator
import androidx.core.view.ViewCompat
import androidx.core.view.ViewPropertyAnimatorListener
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.naman14.timber.R

object FabAnimationUtils {
    private const val DEFAULT_DURATION = 200L
    private val FAST_OUT_SLOW_IN_INTERPOLATOR: Interpolator = FastOutSlowInInterpolator()
    @JvmOverloads
    fun scaleIn(fab: View, duration: Long = DEFAULT_DURATION, callback: ScaleCallback? = null) {
        fab.visibility = View.VISIBLE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            ViewCompat.animate(fab)
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .alpha(1.0f)
                    .setDuration(duration)
                    .setInterpolator(FAST_OUT_SLOW_IN_INTERPOLATOR)
                    .withLayer()
                    .setListener(object : ViewPropertyAnimatorListener {
                        override fun onAnimationStart(view: View) {
                            callback?.onAnimationStart()
                        }

                        override fun onAnimationCancel(view: View) {}
                        override fun onAnimationEnd(view: View) {
                            view.visibility = View.VISIBLE
                            callback?.onAnimationEnd()
                        }
                    }).start()
        } else {
            val anim = AnimationUtils.loadAnimation(fab.context, R.anim.design_fab_out)
            anim.duration = duration
            anim.interpolator = FAST_OUT_SLOW_IN_INTERPOLATOR
            anim.setAnimationListener(object : AnimationListener {
                override fun onAnimationStart(animation: Animation) {
                    callback?.onAnimationStart()
                }

                override fun onAnimationEnd(animation: Animation) {
                    fab.visibility = View.VISIBLE
                    callback?.onAnimationEnd()
                }

                override fun onAnimationRepeat(animation: Animation) {
                    //
                }
            })
            fab.startAnimation(anim)
        }
    }

    fun scaleOut(fab: View, callback: ScaleCallback?) {
        scaleOut(fab, DEFAULT_DURATION, callback)
    }

    @JvmOverloads
    fun scaleOut(fab: View, duration: Long = DEFAULT_DURATION, callback: ScaleCallback? = null) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            ViewCompat.animate(fab)
                    .scaleX(0.0f)
                    .scaleY(0.0f).alpha(0.0f)
                    .setInterpolator(FAST_OUT_SLOW_IN_INTERPOLATOR)
                    .setDuration(duration)
                    .withLayer()
                    .setListener(object : ViewPropertyAnimatorListener {
                        override fun onAnimationStart(view: View) {
                            callback?.onAnimationStart()
                        }

                        override fun onAnimationCancel(view: View) {}
                        override fun onAnimationEnd(view: View) {
                            view.visibility = View.INVISIBLE
                            callback?.onAnimationEnd()
                        }
                    }).start()
        } else {
            val anim = AnimationUtils.loadAnimation(fab.context, R.anim.design_fab_out)
            anim.interpolator = FAST_OUT_SLOW_IN_INTERPOLATOR
            anim.duration = duration
            anim.setAnimationListener(object : AnimationListener {
                override fun onAnimationStart(animation: Animation) {
                    callback?.onAnimationStart()
                }

                override fun onAnimationEnd(animation: Animation) {
                    fab.visibility = View.INVISIBLE
                    callback?.onAnimationEnd()
                }

                override fun onAnimationRepeat(animation: Animation) {
                    //
                }
            })
            fab.startAnimation(anim)
        }
    }

    interface ScaleCallback {
        fun onAnimationStart()
        fun onAnimationEnd()
    }
}