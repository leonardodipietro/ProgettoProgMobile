package com.example.progettoprogmobile.viewModel
import androidx.lifecycle.ViewModel
import com.example.progettoprogmobile.api.SpotifyRepository
import com.example.progettoprogmobile.model.SpotifyTokenResponse
import androidx.lifecycle.MutableLiveData
import com.example.progettoprogmobile.model.TopTracksResponse
import com.google.firebase.database.*
import com.example.progettoprogmobile.model.*
import android.util.Log
import com.google.firebase.auth.FirebaseAuth

//TODO POTREBBE ESSERE NECCESARIO CREARE SUDDIVIDERE QUESTO VIEW MODEL IN UNO PER LA GESTIONE DELLE TRACCE DI SPOTIFY E UNO PER I DATI DEL DB
class SpotifyViewModel : ViewModel() {

    private val repository = SpotifyRepository()
    val spotifyTokenResponse = MutableLiveData<SpotifyTokenResponse?>()
    val error = MutableLiveData<Throwable?>()
    val topTracks = MutableLiveData<TopTracksResponse?>()
    private var database: DatabaseReference = FirebaseDatabase.getInstance().reference.child("tracks")

    private val user = FirebaseAuth.getInstance().currentUser
    private val userId = user?.uid // Ottieni l'ID dell'utente

    init {
        // Inizializza il database Firebase solo se userId non è nullo
        userId?.let { saveUserIdToFirebase(it) }
    }

    private fun saveUserIdToFirebase(userId: String) {
        val userRef = FirebaseDatabase.getInstance().reference.child("users").child(userId)

        val user = FirebaseAuth.getInstance().currentUser
        val userData = hashMapOf<String, Any>()

        user?.displayName?.let { userData["name"] = it }
        user?.email?.let { userData["email"] = it }


        // Salva l'ID dell'utente nel database Firebase
        userRef.setValue(userData)
            .addOnSuccessListener {
                Log.d("Firebase", "ID utente salvato su Firebase: $userData")
            }
            .addOnFailureListener {
                Log.e(
                    "Firebase",
                    "Errore nel salvataggio dell'ID utente su Firebase: ${it.message}"
                )
            }
    }


    /*
   fun initDatabase(databaseReference: DatabaseReference) {
        database = databaseReference
    }
*/


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

    fun saveTracksToFirebase(userId: String, topTrack: List<Track>) {
        val userRef = FirebaseDatabase.getInstance().reference.child("users").child(userId)

        topTrack.forEachIndexed { _, track ->
            val trackData = mapOf(
                "trackName" to track.name,
                "album" to track.album.name,
                "artists" to track.artists.joinToString { it.name }
            )
            val trackKey = userRef.child("topTracks").push().key

            if (trackKey != null) {
                val update = mapOf<String, Any>("topTracks/$trackKey" to trackData)
                userRef.updateChildren(update)
                    .addOnSuccessListener {
                        Log.d("Firebase", "Traccia salvata su Firebase per l'utente: $userId")
                    }
                    .addOnFailureListener {
                        Log.e("Firebase", "Errore nel salvataggio della traccia su Firebase: ${it.message}")
                    }
            } else {
                Log.e("Firebase", "Errore nel generare una chiave univoca per la traccia.")
            }
        }
    }


/*
        fun fetchTopTracksFromFirebase() {
            // Ottieni un riferimento al nodo "tracks" con ordinamento per chiave e limitazione agli ultimi 50 elementi
            val query = database.child("tracks").orderByKey().limitToLast(50)
            //listener per l evento di recupero dati
            query.addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    val tracks = mutableListOf<Track>()

                    for (trackSnapshot in snapshot.children) {
                        val trackName =
                            trackSnapshot.child("trackName").getValue(String::class.java) ?: ""
                        val albumName =
                            trackSnapshot.child("album").getValue(String::class.java) ?: ""
                        val artistNames =
                            trackSnapshot.child("artists").getValue(String::class.java) ?: ""

                        val artists = artistNames.split(",").map { Artist(it.trim()) }
                        val track = Track(trackName, Album(albumName), artists)
                        tracks.add(track)
                    }
                    // Aggiorna il LiveData delle tracce nella RecyclerView
                    topTracks.postValue(TopTracksResponse(tracks))
                }

                override fun onCancelled(error: DatabaseError) {
                    // Gestisci l'errore in caso di problema con la lettura dal database
                    this@SpotifyViewModel.error.postValue(error.toException())
                }
            })
        }
    */

    fun fetchTopTracksFromFirebase() {
        // Ottieni un riferimento al nodo "tracks" con ordinamento per chiave e limitazione agli ultimi 50 elementi
        val query = database.child("tracks").orderByKey().limitToLast(50)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val tracks = mutableListOf<Track>()

                for (trackSnapshot in snapshot.children) {
                    val trackName =
                        trackSnapshot.child("trackName").getValue(String::class.java) ?: ""
                    val albumName = trackSnapshot.child("album").getValue(String::class.java) ?: ""
                    val artistNames =
                        trackSnapshot.child("artists").getValue(String::class.java) ?: ""

                    val artists = artistNames.split(",").map { Artist(it.trim()) }
                    val track = Track(trackName, Album(albumName), artists)
                    tracks.add(track)
                }

                // Aggiorna il LiveData delle tracce nella RecyclerView
                topTracks.postValue(TopTracksResponse(tracks))
            }

            override fun onCancelled(error: DatabaseError) {
                // Gestisci l'errore in caso di problema con la lettura dal database
                this@SpotifyViewModel.error.postValue(error.toException())
            }
        })
    }

}