/*
 * Copyright (c) 2019 Naman Dwivedi.
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
 *
 */
package com.kamenrider.songlaguhenshin.ui.fragments.songs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.kamenrider.songlaguhenshin.R
import com.kamenrider.songlaguhenshin.playback.TimberMusicService.Companion.TYPE_PLAYLIST
import com.kamenrider.songlaguhenshin.constants.Constants.CATEGORY_SONG_DATA
import com.kamenrider.songlaguhenshin.databinding.FragmentCategorySongsBinding
import com.kamenrider.songlaguhenshin.extensions.addOnItemClick
import com.kamenrider.songlaguhenshin.extensions.argument
import com.kamenrider.songlaguhenshin.extensions.filter
import com.kamenrider.songlaguhenshin.extensions.getExtraBundle
import com.kamenrider.songlaguhenshin.extensions.inflateWithBinding
import com.kamenrider.songlaguhenshin.extensions.observe
import com.kamenrider.songlaguhenshin.extensions.safeActivity
import com.kamenrider.songlaguhenshin.extensions.toSongIds
import com.kamenrider.songlaguhenshin.models.CategorySongData
import com.kamenrider.songlaguhenshin.ui.adapters.SongsAdapter
import com.kamenrider.songlaguhenshin.ui.fragments.CheckSong
import com.kamenrider.songlaguhenshin.ui.fragments.base.MediaItemFragment
import com.kamenrider.songlaguhenshin.util.AutoClearedValue

class CategorySongsFragment : MediaItemFragment() {
    private lateinit var songsAdapter: SongsAdapter
    private lateinit var categorySongData: CategorySongData
    var binding by AutoClearedValue<FragmentCategorySongsBinding>(this)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        categorySongData = argument(CATEGORY_SONG_DATA)
        binding = inflater.inflateWithBinding(R.layout.fragment_category_songs, container)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.categorySongData = categorySongData

        songsAdapter = SongsAdapter(this).apply {
            popupMenuListener = mainViewModel.popupMenuListener
            if (categorySongData.type == TYPE_PLAYLIST) {
                playlistId = categorySongData.id
            }
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(safeActivity)
            adapter = songsAdapter
            addOnItemClick { position: Int, _: View ->
                val extras = getExtraBundle(songsAdapter.songs.toSongIds(), categorySongData.title)
                mainViewModel.mediaItemClicked(songsAdapter.songs[position], extras)
            }
        }

        mediaItemFragmentViewModel.mediaItems
                .filter { it.isNotEmpty() }
                .observe(this) { list ->
                    @Suppress("UNCHECKED_CAST")
                    songsAdapter.updateData(CheckSong.getValidSong(list))
                }
    }
}
