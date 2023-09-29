package com.example.progettoprogmobile.viewModel
import android.os.Parcel
import android.os.Parcelable
import androidx.lifecycle.ViewModel
import com.example.progettoprogmobile.api.SpotifyRepository
import com.example.progettoprogmobile.model.SpotifyTokenResponse
import androidx.lifecycle.MutableLiveData
import com.example.progettoprogmobile.model.TopTracksResponse
import com.google.firebase.database.*
import com.example.progettoprogmobile.model.*
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import java.io.Serializable

class SpotifyViewModel : ViewModel(){

    private val repository = SpotifyRepository()
    val spotifyTokenResponse = MutableLiveData<SpotifyTokenResponse?>()
    val error = MutableLiveData<Throwable?>()
    val topTracks = MutableLiveData<TopTracksResponse?>()
    val topArtists= MutableLiveData<TopArtistsResponse>()
    private var database: DatabaseReference = FirebaseDatabase.getInstance().reference.child("tracks")



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
        repository.getTopTracks(token, "short_term",50) { response, error ->
            if (response != null) {
                topTracks.postValue(response)
                Log.d("SpotifyViewModel", "Tracce ottenute con successo: ${response.items}")
            } else if (error != null) {
                this.error.postValue(error)
                Log.e("SpotifyViewModel", "Errore nel recupero delle tracce da Spotify", error)
            }
        }

    }

    fun fetchTopArtists(token: String) {
        repository.getTopArtists(token, "short_term",50) { response, error ->
            if (response != null) {
                topArtists.postValue(response)
            } else if (error != null) {
                this.error.postValue(error)
            }
        }

    }




}