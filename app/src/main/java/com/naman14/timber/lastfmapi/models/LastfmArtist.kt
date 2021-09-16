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
package com.naman14.timber.lastfmapi.models

import com.google.gson.annotations.SerializedName

class LastfmArtist {
    @SerializedName(NAME)
    var mName: String? = null

    @SerializedName(IMAGE)
    var mArtwork: List<Artwork>? = null

    @SerializedName(SIMILAR)
    var mSimilarArtist: SimilarArtist? = null

    @SerializedName(TAGS)
    var mArtistTags: ArtistTag? = null

    @SerializedName(BIO)
    var mArtistBio: ArtistBio? = null

    inner class SimilarArtist {
        @SerializedName(Companion.ARTIST)
        var mSimilarArtist: List<LastfmArtist>? = null


    }

    inner class ArtistTag {
        @SerializedName(Companion.TAG)
        var mTags: List<com.naman14.timber.lastfmapi.models.ArtistTag>? = null
    }

    companion object {
        private const val NAME = "name"
        private const val IMAGE = "image"
        private const val SIMILAR = "similar"
        private const val TAGS = "tags"
        private const val BIO = "bio"
        const val ARTIST = "artist"
        const val TAG = "tag"
    }
}