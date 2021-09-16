package com.naman14.timber.utils

import android.R
import android.annotation.TargetApi
import android.app.Activity
import android.app.ActivityManager.TaskDescription
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.view.View
import android.view.ViewGroup
import androidx.drawerlayout.widget.DrawerLayout
import com.afollestad.appthemeengine.Config
import com.afollestad.appthemeengine.util.Util
import com.google.android.material.floatingactionbutton.FloatingActionButton

/**
 * Created by naman on 02/01/16.
 */
object ATEUtils {
    fun setStatusBarColor(activity: Activity, key: String?, color: Int) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val window = activity.window
                if (Config.coloredStatusBar(activity, key)) window.statusBarColor = getStatusBarColor(color) else window.statusBarColor = Color.BLACK
                if (Config.coloredNavigationBar(activity, key)) window.navigationBarColor = color else window.navigationBarColor = Color.BLACK
                applyTaskDescription(activity, key, color)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val decorView = activity.window.decorView
                val lightStatusMode = Config.lightStatusBarMode(activity, key)
                var lightStatusEnabled = false
                when (lightStatusMode) {
                    Config.LIGHT_STATUS_BAR_OFF -> {
                    }
                    Config.LIGHT_STATUS_BAR_ON -> lightStatusEnabled = true
                    Config.LIGHT_STATUS_BAR_AUTO -> lightStatusEnabled = Util.isColorLight(color)
                    else -> {
                    }
                }
                val systemUiVisibility = decorView.systemUiVisibility
                if (lightStatusEnabled) {
                    decorView.systemUiVisibility = systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                } else {
                    decorView.systemUiVisibility = systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val color2 = if (Config.coloredStatusBar(activity, key)) Color.TRANSPARENT else Color.BLACK
                activity.window.statusBarColor = color2
            }
            if (Config.coloredStatusBar(activity, key)) ((activity.findViewById<View>(R.id.content) as ViewGroup).getChildAt(0) as DrawerLayout).setStatusBarBackgroundColor(getStatusBarColor(color))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun applyTaskDescription(activity: Activity, key: String?, color: Int) {
        // Sets color of entry in the system recents page
        try {
            val td = TaskDescription(
                    activity.title as String,
                    (activity.applicationInfo.loadIcon(activity.packageManager) as BitmapDrawable).bitmap,
                    color)
            activity.setTaskDescription(td)
        } catch (ignored: Exception) {
        }
    }

    fun getStatusBarColor(primaryColor: Int): Int {
        val arrayOfFloat = FloatArray(3)
        Color.colorToHSV(primaryColor, arrayOfFloat)
        arrayOfFloat[2] *= 0.9f
        return Color.HSVToColor(arrayOfFloat)
    }

    fun setFabBackgroundTint(fab: FloatingActionButton, color: Int) {
        val fabColorStateList = ColorStateList(arrayOf(intArrayOf()), intArrayOf(
                color))
        fab.backgroundTintList = fabColorStateList
    }
}