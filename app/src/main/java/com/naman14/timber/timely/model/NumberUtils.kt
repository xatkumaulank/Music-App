package com.naman14.timber.timely.model

import com.naman14.timber.timely.model.number.*
import java.security.InvalidParameterException

object NumberUtils {
    fun getControlPointsFor(start: Int): Array<FloatArray> {
        return when (start) {
            -1 -> Null.Companion.POINTS
            0 -> Zero.Companion.POINTS
            1 -> One.Companion.POINTS
            2 -> Two.Companion.POINTS
            3 -> Three.Companion.POINTS
            4 -> Four.Companion.POINTS
            5 -> Five.Companion.POINTS
            6 -> Six.Companion.POINTS
            7 -> Seven.Companion.POINTS
            8 -> Eight.Companion.POINTS
            9 -> Nine.Companion.POINTS
            else -> throw InvalidParameterException("Unsupported number requested")
        }
    }
}