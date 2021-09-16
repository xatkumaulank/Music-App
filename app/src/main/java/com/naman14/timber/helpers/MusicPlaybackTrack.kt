/*
* Copyright (C) 2014 The CyanogenMod Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.naman14.timber.helpers

import android.os.Parcel
import android.os.Parcelable
import com.naman14.timber.utils.TimberUtils.IdType

/**
 * This is used by the music playback service to track the music tracks it is playing
 * It has extra meta data to determine where the track came from so that we can show the appropriate
 * song playing indicator
 */
class MusicPlaybackTrack : Parcelable {
    @JvmField
    var mId: Long
    @JvmField
    var mSourceId: Long
    @JvmField
    var mSourceType: IdType
    @JvmField
    var mSourcePosition: Int

    constructor(id: Long, sourceId: Long, type: IdType, sourcePosition: Int) {
        mId = id
        mSourceId = sourceId
        mSourceType = type
        mSourcePosition = sourcePosition
    }

    constructor(`in`: Parcel) {
        mId = `in`.readLong()
        mSourceId = `in`.readLong()
        mSourceType = IdType.getTypeById(`in`.readInt())
        mSourcePosition = `in`.readInt()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(mId)
        dest.writeLong(mSourceId)
        dest.writeInt(mSourceType.mId)
        dest.writeInt(mSourcePosition)
    }

    override fun equals(o: Any?): Boolean {
        if (o is MusicPlaybackTrack) {
            val other = o as MusicPlaybackTrack?
            if (other != null) {
                return mId == other.mId && mSourceId == other.mSourceId && mSourceType == other.mSourceType && mSourcePosition == other.mSourcePosition
            }
        }
        return super.equals(o)
    }

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<MusicPlaybackTrack?> {
            override fun createFromParcel(source: Parcel): MusicPlaybackTrack {
                return MusicPlaybackTrack(source)
            }

            override fun newArray(size: Int): Array<MusicPlaybackTrack?> {
                return arrayOfNulls(size)
            }
        }
    }
}