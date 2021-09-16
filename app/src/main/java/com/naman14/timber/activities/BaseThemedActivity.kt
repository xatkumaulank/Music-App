package com.naman14.timber.activities

import android.media.AudioManager
import android.os.Bundle
import com.afollestad.appthemeengine.ATEActivity
import com.naman14.timber.utils.Helpers

/**
 * Created by naman on 31/12/15.
 */
open class BaseThemedActivity : ATEActivity() {
    public override fun getATEKey(): String? {
        return Helpers.getATEKey(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //make volume keys change multimedia volume even if music is not playing now
        volumeControlStream = AudioManager.STREAM_MUSIC
    }
}