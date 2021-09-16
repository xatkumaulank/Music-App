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
package com.naman14.timber.subfragments

import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.naman14.timber.R
import com.naman14.timber.subfragments.ArtistTagFragment

class ArtistTagFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_artist_tag, container, false)
    }

    companion object {
        private const val ARG_PAGE_NUMBER = "pageNumber"
        fun newInstance(pageNumber: Int): ArtistTagFragment {
            val fragment = ArtistTagFragment()
            val bundle = Bundle()
            bundle.putInt(ARG_PAGE_NUMBER, pageNumber)
            fragment.arguments = bundle
            return fragment
        }
    }
}