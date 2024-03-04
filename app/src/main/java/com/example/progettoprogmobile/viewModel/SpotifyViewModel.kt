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
    val shortTermTracks = MutableLiveData<TopTracksResponse>()
    val mediumTermTracks = MutableLiveData<TopTracksResponse>()
    val longTermTracks = MutableLiveData<TopTracksResponse>()
    val shortTermArtists=MutableLiveData<TopArtistsResponse>()
    val mediumTermArtists=MutableLiveData<TopArtistsResponse>()
    val longTermArtists=MutableLiveData<TopArtistsResponse>()
    val topArtists= MutableLiveData<TopArtistsResponse>()
    private var database: DatabaseReference = FirebaseDatabase.getInstance().reference.child("tracks")

    private val trackInfo = mutableListOf<Track>() // Lista per conservare gli ID delle tracce
    val trackInfoExtracted = MutableLiveData<Boolean>()
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
     fun createSpotifyPlaylist(token: String,trackIds: List<String>) {
         Log.d("Playlistchiamata","playlistchiamata")
        val playlistBody = CreatePlaylistBody(name = "Mia Playlist", description = "Descrizione della mia playlist")
        val trackUris = trackIds.map { "spotify:track:$it" }
         Log.d("Playlistchiamata","playlistchiamata con $trackUris")
        repository.createPlaylist(token, playlistBody) { response, error ->
            if (response != null) {
                val playlistId = response.id
                val addTracksBody = AddTracksBody(uris = trackUris)
                repository.addTracksToPlaylist(token, playlistId, addTracksBody) { success, error ->
                    if (success) {
                        Log.d("Spotify", "Tracce aggiunte con successo alla playlist.")
                    } else {
                        Log.e("Spotify", "Errore nell'aggiungere tracce alla playlist: $error")
                    }
                }
            } else {
                Log.e("Spotify", "Errore nella creazione della playlist: $error")
            }
        }
    }
    fun fetchTopTracks(token: String, timeRange: String = "short_term") {
        repository.getTopTracks(token, timeRange, 50) { response, error ->
            if (response != null) {
                when (timeRange) {
                    "short_term" -> shortTermTracks.postValue(response)
                    "medium_term" -> mediumTermTracks.postValue(response)
                    "long_term" -> longTermTracks.postValue(response)
                }
                Log.d("FetchTopTracks", "Risposta non null ricevuta: ${response.items.size} tracce")
                topTracks.postValue(response)
                //  extractTracks(response)

            } else if (error != null) {
                Log.e("FetchTopTracks", "Errore durante la chiamata API: ${error.message}", error)
                this.error.postValue(error)
            }
        }
    }
    fun fetchTopArtists(token: String,timeRange: String="short_term") {
        repository.getTopArtists(token, timeRange,50) { response, error ->
            if (response != null) {
                when (timeRange) {
                    "short_term" -> shortTermArtists.postValue(response)
                    "medium_term" -> mediumTermArtists.postValue(response)
                    "long_term" -> longTermArtists.postValue(response)
                }
                topArtists.postValue(response)
            } else if (error != null) {
                this.error.postValue(error)
            }
        }

    }

}