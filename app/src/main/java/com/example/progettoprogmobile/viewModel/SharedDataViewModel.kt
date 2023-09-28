package com.example.progettoprogmobile.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.progettoprogmobile.model.Track

class SharedDataViewModel: ViewModel() {

    private val topTracksLiveData = MutableLiveData<List<Track>>()
    fun setTopTracks(tracks: List<Track>) {
        topTracksLiveData.value = tracks
    }

    fun getTopTracks(): LiveData<List<Track>> {
        return topTracksLiveData
    }
}