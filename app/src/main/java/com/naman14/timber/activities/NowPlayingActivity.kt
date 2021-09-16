package com.naman14.timber.activities

import android.graphics.Color
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.annotation.StyleRes
import com.afollestad.appthemeengine.Config
import com.afollestad.appthemeengine.customizers.ATEActivityThemeCustomizer
import com.afollestad.appthemeengine.customizers.ATEStatusBarCustomizer
import com.afollestad.appthemeengine.customizers.ATEToolbarCustomizer
import com.naman14.timber.R
import com.naman14.timber.utils.Constants
import com.naman14.timber.utils.NavigationUtils
import com.naman14.timber.utils.PreferencesUtility

/**
 * Created by naman on 01/01/16.
 */
class NowPlayingActivity : BaseActivity(), ATEActivityThemeCustomizer, ATEToolbarCustomizer, ATEStatusBarCustomizer {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nowplaying)
        val prefs = getSharedPreferences(Constants.FRAGMENT_ID, MODE_PRIVATE)
        val fragmentID = prefs.getString(Constants.NOWPLAYING_FRAGMENT_ID, Constants.TIMBER3)
        val fragment = NavigationUtils.getFragmentForNowplayingID(fragmentID)
        val fragmentManager = supportFragmentManager
        fragmentManager.beginTransaction()
                .replace(R.id.container, fragment).commit()
    }

    @StyleRes
    override fun getActivityTheme(): Int {
        return if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("dark_theme", false)) R.style.AppTheme_FullScreen_Dark else R.style.AppTheme_FullScreen_Light
    }

    override fun getLightToolbarMode(): Int {
        return Config.LIGHT_TOOLBAR_AUTO
    }

    override fun getLightStatusBarMode(): Int {
        return Config.LIGHT_STATUS_BAR_OFF
    }

    override fun getToolbarColor(): Int {
        return Color.TRANSPARENT
    }

    override fun getStatusBarColor(): Int {
        return Color.TRANSPARENT
    }

    override fun onResume() {
        super.onResume()
        if (PreferencesUtility.getInstance(this).didNowplayingThemeChanged()) {
            PreferencesUtility.getInstance(this).setNowPlayingThemeChanged(false)
            recreate()
        }
    }
}