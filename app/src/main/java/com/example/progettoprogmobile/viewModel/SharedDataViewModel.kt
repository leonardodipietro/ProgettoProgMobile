package com.example.progettoprogmobile.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.progettoprogmobile.ActionState
import com.example.progettoprogmobile.model.Track
import com.example.progettoprogmobile.model.Utente

class SharedDataViewModel: ViewModel() {

    private val topTracksLiveData = MutableLiveData<List<Track>>()
    /*fun setTopTracks(tracks: List<Track>) {
        topTracksLiveData.value = tracks
    }

    fun getTopTracks(): LiveData<List<Track>> {
        return topTracksLiveData
    }*/

    val editTextContent = MutableLiveData<String?>()
    val currentActionState = MutableLiveData<ActionState>()
    val commentJustSubmitted = MutableLiveData<Boolean>().apply { value = false }
    val isEditingReview = MutableLiveData<Boolean>(false)
    val usersMap: MutableLiveData<Map<String, Utente>> = MutableLiveData()
}