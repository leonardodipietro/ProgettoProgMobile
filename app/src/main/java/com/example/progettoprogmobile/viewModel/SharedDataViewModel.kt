package com.example.progettoprogmobile.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.progettoprogmobile.ActionState
import com.example.progettoprogmobile.model.TopTracksResponse
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

    val commentJustSubmitted = MutableLiveData<Boolean>().apply { value = false }
    val isEditingReview = MutableLiveData<Boolean>(false)
    val usersMap: MutableLiveData<Map<String, Utente>> = MutableLiveData()

    val shortTermTracksShared = MutableLiveData<TopTracksResponse>()
    val mediumTermTracksShared = MutableLiveData<TopTracksResponse>()
    val longTermTracksShared = MutableLiveData<TopTracksResponse>()

    val spotifyToken: MutableLiveData<String?> = MutableLiveData()



    val currentActionState = MutableLiveData<ActionState>().apply { value = ActionState.NONE }

    fun updateShortTermTracks(data: TopTracksResponse) {
        shortTermTracksShared.value = data
    }

    fun updateMediumTermTracks(data: TopTracksResponse) {
        mediumTermTracksShared.value = data
    }

    fun updateLongTermTracks(data: TopTracksResponse) {
        longTermTracksShared.value = data
    }

    fun updateToken(token: String?) {
        spotifyToken.value = token
    }


}