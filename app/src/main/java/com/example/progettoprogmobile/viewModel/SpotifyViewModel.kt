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


    // Metodo per estrarre gli ID delle tracce dalla risposta e salvarli nella lista
// Metodo per estrarre gli ID delle tracce dalla risposta e salvarli nella lista
    /* private fun extractTracks(response: TopTracksResponse) {
         trackInfo.clear() // Cancella la lista esistente prima dell'estrazione

         val tracks = response.items
         for (track in tracks) {
             val trackId = track.id
             val trackName = track.name
             val album = track.album
             val artists = track.artists
             val genres = track.genres
             val releaseDate = track.release_date
             val durationMs = track.duration_ms

             val trackItem = Track(trackName, album, artists, trackId, genres, releaseDate, durationMs)
             trackInfo.add(trackItem)
             Log.d("SpotifyRepo", "Traccia aggiunta: $trackItem")


         }
         Log.d("SpotifyRepo", "Lista di tracce: $trackInfo")
         trackInfoExtracted.postValue(true)
     }

     fun getSpecificTrackInfo(index: Int): Track? {
         Log.d("SpotifyRepo", "metodo chiamato top")
         // Verifica che l'indice sia valido
         if (index >= 0 && index < trackInfo.size) {
             val specificTrackInfo = trackInfo[index]
             Log.d("SpotifyRepo", "ID della traccia richiesta all'indice $index: $specificTrackInfo")
             return specificTrackInfo
         }
         Log.d("SpotifyRepo", "Nessun ID di traccia trovato all'indice $index.")
         return null // Restituisci null se l'indice è fuori dai limiti
     }*/
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
