package com.example.progettoprogmobile.viewModel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.*
import com.example.progettoprogmobile.model.*
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.content.Context
import android.os.Bundle
//import com.example.progettoprogmobile.FifthFragment
import com.example.progettoprogmobile.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.concurrent.CountDownLatch
import kotlin.coroutines.resumeWithException



class FirebaseViewModel (application: Application): AndroidViewModel(application) {

    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference
    var filter: String = "shortterm" // Default o valore iniziale

    private val user = FirebaseAuth.getInstance().currentUser
    private val userId = user?.uid
    val topTracksfromdb: MutableLiveData<List<Track>> = MutableLiveData()
    private val artistslist= mutableListOf<Artist>()
    val topArtistsfromdb: MutableLiveData<List<Artist>> = MutableLiveData()
    // Dichiarazione della variabile per il controllo dello stato di registrazione dell'utente
    private var isUserRegistered = false
    val _users= MutableLiveData<List<Utente>>()

    fun cercaUtenti(query: String) {
        Log.d("FirebaseViewModel", "cercaUtenti called with query: $query")
        val ref = FirebaseDatabase.getInstance().getReference("users")
        val risultati = mutableListOf<Utente>()
        ref.orderByChild("name").startAt(query).endAt(query + "\uf8ff")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("FirebaseViewModel", "Raw Data Snapshot: $snapshot")
                    snapshot.children.forEach {
                        Log.d("FirebaseViewModel", "Raw User Data: ${it.value}")
                        val utente = it.getValue(Utente::class.java)
                        if (utente != null) {
                            Log.d("FirebaseViewModel", "Read user name from Firebase: ${utente.name}")
                            Log.d("FirebaseViewModel", "Read ID from Firebase: ${utente.userId}")
                            risultati.add(utente)
                        } else {
                            Log.e("FirebaseViewModel", "Error reading user: $it")
                        }
                    }
                    _users.value = risultati
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseViewModel", "Error: ${error.message}")
                }
            })

    }

    fun saveUserIdToFirebase(userId: String) {
        val userRef = FirebaseDatabase.getInstance().reference.child("users").child(userId)

        val user = FirebaseAuth.getInstance().currentUser
        val userData = hashMapOf<String, Any>()

        user?.displayName?.let { userData["name"] = it }
        user?.email?.let { userData["email"] = it }
        user?.uid?.let {userData["userId"] = it }

        // Verifica se l'utente è già registrato prima di salvare le credenziali
        if (!isUserRegistered) {
            // Salva l'ID dell'utente nel database Firebase
            userRef.setValue(userData)
                .addOnSuccessListener {
                    Log.d("Firebase", "ID utente salvato su Firebase: $userData")

                    // Imposta la variabile isUserRegistered su true quando l'utente si registra per la prima volta
                    isUserRegistered = true
                }
                .addOnFailureListener {
                    Log.e(
                        "Firebase",
                        "Errore nel salvataggio dell'ID utente su Firebase: ${it.message}"
                    )
                }
        }
    }
    fun checkUserIdInFirebase(context: Context, userId: String, onComplete: (Boolean) -> Unit) {
        val userRef = FirebaseDatabase.getInstance().reference.child("users").child(userId)

        // Controlla se l'utente è già registrato nel tuo database Firebase
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val isRegistered = snapshot.exists()
                onComplete(isRegistered)
            }

            override fun onCancelled(error: DatabaseError) {
                // Gestisci l'errore in caso di problemi nella lettura dal database
                Log.e("FirebaseError", "Errore durante la lettura dal database Firebase: ${error.message}")
                onComplete(false) // Indica che non è stato possibile verificare lo stato di registrazione
            }
        })
    }

    fun saveTracksToMainNode(topTrack: List<Track>) {
        val tracksRef = FirebaseDatabase.getInstance().reference.child("tracks")

        topTrack.forEach { track ->
            val imageUrl = track.album.images.getOrNull(0)?.url ?: ""
            val artistIdsForTrackNode = track.artists.map { it.id } // Estrai solo gli ID degli artisti

            val trackData = mapOf(
                "trackName" to track.name,
                "album" to track.album.name,
                "artists" to artistIdsForTrackNode,
                "id" to track.id,
                "release_date" to track.album.releaseDate,
                "image_url" to imageUrl
            )

            tracksRef.child(track.id).setValue(trackData)
                .addOnSuccessListener {
                    Log.d("Firebase", "Traccia ${track.id} salvata su Firebase nel nodo principale.")
                    saveArtistsFromTracks(topTrack)
                }
                .addOnFailureListener {
                    Log.e("Firebase", "Errore nel salvataggio della traccia ${track.id} su Firebase: ${it.message}")
                }
        }
    }

    fun saveArtistsFromTracks(topTrack: List<Track>) {
        val artistsRef = FirebaseDatabase.getInstance().reference.child("artists")

        topTrack.flatMap { it.artists }.distinctBy { it.id }.forEach { artist ->
            artistsRef.child(artist.id).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (!dataSnapshot.exists()) {
                        val imageUrl = artist.images?.getOrNull(0)?.url ?: ""

                        val artistData = mapOf(
                            "name" to artist.name,
                            "genres" to artist.genres,
                            "id" to artist.id,
                            "image_url" to imageUrl
                        )

                        artistsRef.child(artist.id).setValue(artistData)
                            .addOnSuccessListener {
                                Log.d("Firebase", "Artista ${artist.id} salvato su Firebase nel nodo artists.")
                            }
                            .addOnFailureListener {
                                Log.e("Firebase", "Errore nel salvataggio dell'artista ${artist.id} su Firebase: ${it.message}")
                            }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Gestisci gli errori
                }
            })
        }
    }

    fun saveArtistsToMainNode(topArtists: List<Artist>) {
        val artistsRef = FirebaseDatabase.getInstance().reference.child("artists")

        topArtists.forEach { artist ->
            val imageUrl = artist.images.getOrNull(0)?.url ?: ""
            val artistData = mapOf(
                "name" to artist.name,
                "genres" to artist.genres,
                "id" to artist.id,  // Questo è l'ID fornito da Spotify

                "image_url" to imageUrl
            )

            // Usiamo l'ID dell'artista fornito da Spotify come chiave
            artistsRef.child(artist.id).setValue(artistData)
                .addOnSuccessListener {
                    Log.d("Firebase", "Artista ${artist.id} salvato su Firebase nel nodo principale.")
                }
                .addOnFailureListener {
                    Log.e("Firebase", "Errore nel salvataggio dell'artista ${artist.id} su Firebase: ${it.message}")
                }
        }
    }

    fun saveUserTopArtists(userId: String, topArtists: List<Artist>, timeRange: String = "short_term") {
        val userTopArtistsRef = FirebaseDatabase.getInstance().reference
            .child("users").child(userId).child("topArtists").child(timeRange)

        // Creiamo una lista solo degli ID degli artisti nell'ordine in cui sono stati passati
        val artistIds = topArtists.map { it.id }

        userTopArtistsRef.setValue(artistIds)
            .addOnSuccessListener {
                Log.d("Firebase", "IDs degli artisti salvati per l'utente $userId.")
            }
            .addOnFailureListener {
                Log.e("Firebase", "Errore nel salvataggio degli IDs degli artisti per l'utente $userId: ${it.message}")
            }
    }



    fun saveUserTopTracks(userId: String, topTrack: List<Track>, timeRange: String = "short_term") {
        val userTopTracksRef = FirebaseDatabase.getInstance().reference
            .child("users").child(userId).child("topTracks").child(timeRange)

        // Creiamo una lista solo degli ID delle tracce nell'ordine in cui sono state passate
        val trackIds = topTrack.map { it.id }

        userTopTracksRef.setValue(trackIds)
            .addOnSuccessListener {
                Log.d("Firebase", "IDs delle tracce salvate per l'utente $userId.")
            }
            .addOnFailureListener {
                Log.e("Firebase", "Errore nel salvataggio degli IDs delle tracce per l'utente $userId: ${it.message}")
            }
    }
    fun fetchTopTracksFromFirebase(filter:String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val userTopTracksRef = database.child("users").child(userId).child("topTracks").child(filter)
            userTopTracksRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val trackIds = snapshot.children.mapNotNull { it.getValue(String::class.java) }
                    retrieveTracksDetails(trackIds)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseError", "Errore durante la lettura degli IDs delle tracce dal database Firebase: ${error.message}")
                }
            })
        }
    }

    private fun retrieveTracksDetails(trackIds: List<String>) {
        CoroutineScope(Dispatchers.IO).launch {
            val tracksRef = database.child("tracks")
            val artistsRef = database.child("artists")
            val tracks = mutableListOf<Track>()

            val trackDetails = trackIds.map { trackId ->
                async {
                    val trackSnapshot = getSnapshotFromFirebase(tracksRef.child(trackId))
                    val trackName = trackSnapshot.child("trackName").getValue(String::class.java) ?: ""
                    val albumName = trackSnapshot.child("album").getValue(String::class.java) ?: ""
                    val imageUrl = trackSnapshot.child("image_url").getValue(String::class.java) ?: ""
                    val artistIds = trackSnapshot.child("artists").children.mapNotNull { it.getValue(String::class.java) }

                    val artistDetails = artistIds.map { artistId ->
                        async {
                            getSnapshotFromFirebase(artistsRef.child(artistId))
                        }
                    }.awaitAll()

                    val artistDetailsList = artistDetails.map { artistSnapshot ->
                        val artistName = artistSnapshot.child("name").getValue(String::class.java) ?: ""
                        val genresTypeIndicator = object : GenericTypeIndicator<List<String>>() {}
                        val genres = artistSnapshot.child("genres").getValue(genresTypeIndicator) ?: listOf()
                        val artistImageUrl = artistSnapshot.child("image_url").getValue(String::class.java) ?: ""
                        Artist(artistName, genres, artistSnapshot.key!!, listOf(Image(artistImageUrl)))
                    }

                    val album = Album(albumName, listOf(Image(imageUrl)), trackSnapshot.child("release_date").getValue(String::class.java) ?: "")
                    Track(trackName, album, artistDetailsList, trackId)
                }
            }.awaitAll()

            tracks.addAll(trackDetails)

            withContext(Dispatchers.Main) {
                topTracksfromdb.postValue(tracks)
            }
        }
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun getSnapshotFromFirebase(ref: DatabaseReference): DataSnapshot {
        return suspendCancellableCoroutine { continuation ->
            val listener = ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    continuation.resume(snapshot) {}
                }

                override fun onCancelled(error: DatabaseError) {
                    continuation.resumeWithException(Exception(error.message))
                }
            })

            continuation.invokeOnCancellation {
                ref.removeEventListener(listener as ValueEventListener)
            }
        }
    }




    fun fetchTopArtistsFromFirebase(filter:String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val userTopArtistsRef = database.child("users").child(userId).child("topArtists").child(filter)
            userTopArtistsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val artistIds = snapshot.children.mapNotNull { it.getValue(String::class.java) }
                    retrieveArtistsDetails(artistIds)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseError", "Errore durante la lettura degli IDs degli artisti dal database Firebase: ${error.message}")
                }
            })
        }
    }

    private fun retrieveArtistsDetails(artistIds: List<String>) {
        val artistsRef = database.child("artists")
        val artists = mutableListOf<Artist>()

        artistIds.forEach { id ->
            artistsRef.child(id).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val artistName = snapshot.child("name").getValue(String::class.java) ?: ""
                    val genresTypeIndicator = object : GenericTypeIndicator<List<String>>() {}
                    val genres = snapshot.child("genres").getValue(genresTypeIndicator) ?: listOf()

                    val artistId = snapshot.child("id").getValue(String::class.java) ?: ""
                    val followersTotal = snapshot.child("followers_total").getValue(Int::class.java) ?: 0
                    val imageUrl = snapshot.child("image_url").getValue(String::class.java) ?: ""

                    val artist = Artist(artistName, genres, artistId, listOf(Image(imageUrl)))
                    artists.add(artist)

                    if (artists.size == artistIds.size) {
                        topArtistsfromdb.postValue(artists)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseError", "Errore durante il recupero dei dettagli dell'artista dal database Firebase: ${error.message}")
                }
            })
        }
    }



    interface OnUserFetchedListener {
        fun onUserFetched(utente: Utente)
    }

    var onUserFetchedListener: OnUserFetchedListener? = null

    // Funzione per recuperare i dati di un utente dal database Firebase
    fun fetchUserDataFromFirebase(userId: String, listener: OnUserFetchedListener) {
        val userRef = FirebaseDatabase.getInstance().reference.child("users").child(userId)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val name = snapshot.child("name").getValue(String::class.java) ?: ""
                val userId = snapshot.child("userId").getValue(String::class.java) ?: ""

                // Crea un oggetto Utente con dati non nulli
                val utente = Utente(userId, name)

                // Passa l'oggetto Utente al listener fornito come parametro
                listener.onUserFetched(utente)
            }

            override fun onCancelled(error: DatabaseError) {
                // Gestisci l'errore durante la lettura dei dati utente
            }
        })
    }
}

/* ///////14/10/2023
 fun saveTracksToMainNode(topTrack: List<Track>) {
      val tracksRef = FirebaseDatabase.getInstance().reference.child("tracks")

      topTrack.forEachIndexed { _, track ->
          val imageUrl = track.album.images.getOrNull(0)?.url ?: ""
          val artistDataForTrackNode = track.artists.map {
              it.id to mapOf(
                  "name" to it.name,
                  "genres" to it.genres,
                  "id" to it.id,
                  "image_url" to imageUrl
              )
          }.toMap()
          val trackData = mapOf(
              "trackName" to track.name,
              "album" to track.album.name,
              //"artists" to track.artisttrack,
              //"artistIds" to track.artisttrack.map { it.id },
              "artists" to artistDataForTrackNode,
              "id" to track.id,  // Questo è l'ID fornito da Spotify
              //"genres" to track.genres,
              "release_date" to track.album.releaseDate,
              // "duration_ms" to track.durationMs,
              "image_url" to imageUrl
          )

          // Usiamo l'ID della traccia fornito da Spotify come chiave
          tracksRef.child(track.id).setValue(trackData)
              .addOnSuccessListener {
                  Log.d("Firebase", "Traccia ${track.id} salvata su Firebase nel nodo principale.")
              }
              .addOnFailureListener {
                  Log.e("Firebase", "Errore nel salvataggio della traccia ${track.id} su Firebase: ${it.message}")
              }
      }
  }*/













/*
 fun saveTracksToMainNode(topTrack: List<Track>) {
        val tracksRef = FirebaseDatabase.getInstance().reference.child("tracks")

        topTrack.forEachIndexed { _, track ->
            val imageUrl = track.album.images.getOrNull(0)?.url ?: ""
            val artistDataForTrackNode = track.artisttrack.map { it.id to it.name }.toMap()
            val trackData = mapOf(
                "trackName" to track.name,
                "album" to track.album.name,
                //"artists" to track.artisttrack,
                //"artistIds" to track.artisttrack.map { it.id },
                "artists" to artistDataForTrackNode,
                "id" to track.id,  // Questo è l'ID fornito da Spotify
                //"genres" to track.genres,
                "release_date" to track.album.releaseDate,
               // "duration_ms" to track.durationMs,
                "image_url" to imageUrl
            )

            // Usiamo l'ID della traccia fornito da Spotify come chiave
            tracksRef.child(track.id).setValue(trackData)
                .addOnSuccessListener {
                    Log.d("Firebase", "Traccia ${track.id} salvata su Firebase nel nodo principale.")
                }
                .addOnFailureListener {
                    Log.e("Firebase", "Errore nel salvataggio della traccia ${track.id} su Firebase: ${it.message}")
                }
        }
    }
    fun saveArtistsToMainNode(topArtists: List<Artist>) {
        val artistsRef = FirebaseDatabase.getInstance().reference.child("artists")

        topArtists.forEach { artist ->
            val imageUrl = artist.images.getOrNull(0)?.url ?: ""
            val artistData = mapOf(
                "name" to artist.name,
                "genres" to artist.genres,
                "id" to artist.id,  // Questo è l'ID fornito da Spotify
                "followers_total" to artist.followers.total,
                "image_url" to imageUrl
            )

            // Usiamo l'ID dell'artista fornito da Spotify come chiave
            artistsRef.child(artist.id).setValue(artistData)
                .addOnSuccessListener {
                    Log.d("Firebase", "Artista ${artist.id} salvato su Firebase nel nodo principale.")
                }
                .addOnFailureListener {
                    Log.e("Firebase", "Errore nel salvataggio dell'artista ${artist.id} su Firebase: ${it.message}")
                }
        }
    }

    fun saveUserTopArtists(userId: String, topArtists: List<Artist>, timeRange: String = "short_term") {
        val userTopArtistsRef = FirebaseDatabase.getInstance().reference
            .child("users").child(userId).child("topArtists").child(timeRange)

        // Creiamo una lista solo degli ID degli artisti nell'ordine in cui sono stati passati
        val artistIds = topArtists.map { it.id }

        userTopArtistsRef.setValue(artistIds)
            .addOnSuccessListener {
                Log.d("Firebase", "IDs degli artisti salvati per l'utente $userId.")
            }
            .addOnFailureListener {
                Log.e("Firebase", "Errore nel salvataggio degli IDs degli artisti per l'utente $userId: ${it.message}")
            }
    }



    fun saveUserTopTracks(userId: String, topTrack: List<Track>, timeRange: String = "short_term") {
        val userTopTracksRef = FirebaseDatabase.getInstance().reference
            .child("users").child(userId).child("topTracks").child(timeRange)

        // Creiamo una lista solo degli ID delle tracce nell'ordine in cui sono state passate
        val trackIds = topTrack.map { it.id }

        userTopTracksRef.setValue(trackIds)
            .addOnSuccessListener {
                Log.d("Firebase", "IDs delle tracce salvate per l'utente $userId.")
            }
            .addOnFailureListener {
                Log.e("Firebase", "Errore nel salvataggio degli IDs delle tracce per l'utente $userId: ${it.message}")
            }
    }
    fun fetchTopTracksFromFirebase(filter:String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val userTopTracksRef = database.child("users").child(userId).child("topTracks").child(filter)
            userTopTracksRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val trackIds = snapshot.children.mapNotNull { it.getValue(String::class.java) }
                    retrieveTracksDetails(trackIds)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseError", "Errore durante la lettura degli IDs delle tracce dal database Firebase: ${error.message}")
                }
            })
        }
    }

    private fun retrieveTracksDetails(trackIds: List<String>) {
        val tracksRef = database.child("tracks")
        val tracks = mutableListOf<Track>()
        var count = 0

        trackIds.forEach { id ->
            Log.d("FIREBASE VIEWMODEL","FIREBASE TRACK ID $trackIds")
            tracksRef.child(id).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val trackName = snapshot.child("trackName").getValue(String::class.java) ?: ""

                    val albumName = snapshot.child("album").getValue(String::class.java) ?: ""

                    val imageUrl = snapshot.child("image_url").getValue(String::class.java) ?: ""

                    val artistData = snapshot.child("artists").children.mapNotNull {
                        val artistId = it.key
                        val artistName = it.getValue(String::class.java) ?: ""
                        if (artistName.isNotEmpty()) SimpleArtist(artistName, artistId) else null
                    }

                    val id = snapshot.child("id").getValue(String::class.java) ?: ""
                    val releaseDate = snapshot.child("release_date").getValue(String::class.java) ?: ""


                    val album = Album(albumName, listOf(Image(imageUrl)),releaseDate)
                    val track = Track(trackName, album, artistData, id)

                    tracks.add(track)

                    count++
                    if (count == trackIds.size) {
                        topTracksfromdb.postValue(tracks)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseError", "Errore durante il recupero dei dettagli della traccia dal database Firebase: ${error.message}")
                }
            })
        }
    }
    fun fetchTopArtistsFromFirebase(filter:String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val userTopArtistsRef = database.child("users").child(userId).child("topArtists").child(filter)
            userTopArtistsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val artistIds = snapshot.children.mapNotNull { it.getValue(String::class.java) }
                    retrieveArtistsDetails(artistIds)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseError", "Errore durante la lettura degli IDs degli artisti dal database Firebase: ${error.message}")
                }
            })
        }
    }

    private fun retrieveArtistsDetails(artistIds: List<String>) {
        val artistsRef = database.child("artists")
        val artists = mutableListOf<Artist>()

        artistIds.forEach { id ->
            artistsRef.child(id).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val artistName = snapshot.child("name").getValue(String::class.java) ?: ""
                    val genresTypeIndicator = object : GenericTypeIndicator<List<String>>() {}
                    val genres = snapshot.child("genres").getValue(genresTypeIndicator) ?: listOf()

                    val artistId = snapshot.child("id").getValue(String::class.java) ?: ""
                    val followersTotal = snapshot.child("followers_total").getValue(Int::class.java) ?: 0
                    val imageUrl = snapshot.child("image_url").getValue(String::class.java) ?: ""

                    val artist = Artist(artistName, genres, artistId, Followers(followersTotal), listOf(Image(imageUrl)))
                    artists.add(artist)

                    if (artists.size == artistIds.size) {
                        topArtistsfromdb.postValue(artists)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseError", "Errore durante il recupero dei dettagli dell'artista dal database Firebase: ${error.message}")
                }
            })
        }
    }

 */

