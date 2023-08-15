package com.example.progettoprogmobile.viewModel
import androidx.lifecycle.ViewModel
import com.example.progettoprogmobile.api.SpotifyRepository
import com.example.progettoprogmobile.model.SpotifyTokenResponse
import androidx.lifecycle.MutableLiveData
import com.example.progettoprogmobile.model.TopTracksResponse
import android.util.Log
class SpotifyViewModel : ViewModel() {

    private val repository = SpotifyRepository()
    val spotifyTokenResponse = MutableLiveData<SpotifyTokenResponse?>()
    val error = MutableLiveData<Throwable?>()
    val topTracks = MutableLiveData<TopTracksResponse?>()


//il code che passiamo non è il token di accesso che
    fun getAccessToken(code: String) {
        repository.getAccessToken(
            code,
            "myapp://callback",
            "f81649b34ef74684b08943e7ce931d23",
            "be4412d1d08645dfafdc88dc75d7b030"
        ) { response, error ->
            if (response != null) {
                spotifyTokenResponse.postValue(response)
            //postvalue è un metodo delle livedata che serve per informare gli observer della risposta
            } else if (error != null) {
                this.error.postValue(error)
            }
        }
    }


    fun fetchTopTracks(token: String) {
        repository.getTopTracks(token) { response, error ->
            if (response != null) {
                topTracks.postValue(response)
            } else if (error != null) {
                this.error.postValue(error)
            }
        }

    }


}