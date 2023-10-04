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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


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

        topTrack.forEachIndexed { _, track ->
            val imageUrl = track.album.images.getOrNull(0)?.url ?: ""
            val trackData = mapOf(
                "trackName" to track.name,
                "album" to track.album.name,
                "artists" to track.artisttrack,
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

                    val artistNames = snapshot.child("artists").children.mapNotNull {
                        it.child("name").getValue(String::class.java)
                    }
                    val artistsList = artistNames.map { SimpleArtist(it) }
                    val id = snapshot.child("id").getValue(String::class.java) ?: ""
                    val releaseDate = snapshot.child("release_date").getValue(String::class.java) ?: ""


                    val album = Album(albumName, listOf(Image(imageUrl)),releaseDate)
                    val track = Track(trackName, album, artistsList, id)

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

}


   /* fun fetchTopTracksFromFirebase() {
        Log.d("FirebaseData", "Inizio recupero dati da Firebase")
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val query = database.child("users").child(userId).child("topTracks").orderByKey()
            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("FirebaseData", "Metodo onDataChange chiamato")
                    val tracks = mutableListOf<Track>()
                    for (trackSnapshot in snapshot.children) {
                        val trackName = trackSnapshot.child("trackName").getValue(String::class.java) ?: ""
                        val albumName = trackSnapshot.child("album").getValue(String::class.java) ?: ""
                        val imageUrl = trackSnapshot.child("image_url").getValue(String::class.java) ?: ""
                        val artistList = mutableListOf<SimpleArtist>()
                        val artistsSnapshot = trackSnapshot.child("artists")
                        for (artistSnapshot in artistsSnapshot.children) {
                            val artistName = artistSnapshot.child("name").getValue(String::class.java) ?: ""
                            artistList.add(SimpleArtist(artistName))
                        }
                        val id = trackSnapshot.child("id").getValue(String::class.java) ?: ""
                       // val genres = trackSnapshot.child("genres").getValue(String::class.java) ?: ""
                        val releaseDate = trackSnapshot.child("release_date").getValue(String::class.java) ?: ""
                       // val durationMs = trackSnapshot.child("duration_ms").getValue(String::class.java) ?: ""
                        val album = Album(albumName, listOf(Image(imageUrl)))
                        val track = Track(trackName, album, artistList, id,  releaseDate, )
                        tracks.add(track)
                        Log.d("FIREBASE VIEWMODEL","FIREBASE $tracks")
                    }
                    topTracksfromdb.postValue(tracks)
                }
                val error: MutableLiveData<Throwable> = MutableLiveData()
                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseError", "Errore durante la lettura dal database Firebase: ${error.message}")
                    this.error.postValue(error.toException())
                }
            })
        }
    }































 /*   fun saveTracksToMainNode(topTrack: List<Track>) {
        val tracksRef = FirebaseDatabase.getInstance().reference.child("tracks")
        topTrack.forEachIndexed { _, track ->
            val imageUrl = track.album.images.getOrNull(0)?.url ?: ""
            val trackData = mapOf(
                "trackName" to track.name,
                "album" to track.album.name,
                "artists" to track.artisttrack,
                "id" to track.id,
                "genres" to track.genres,
                "release_date" to track.releaseDate,
                "duration_ms" to track.durationMs,
                "image_url" to imageUrl
            )
            val trackKey = tracksRef.push().key
            if (trackKey != null) {
                val update = mapOf<String, Any>("$trackKey" to trackData)
                tracksRef.updateChildren(update)
                    .addOnSuccessListener {
                        Log.d("Firebase", "Traccia salvata su Firebase nel nodo principale.")
                    }
                    .addOnFailureListener {
                        Log.e("Firebase", "Errore nel salvataggio della traccia su Firebase: ${it.message}")
                    }
            } else {
                Log.e("Firebase", "Errore nel generare una chiave univoca per la traccia.")
            }
        }
    }


    fun saveTrackIdsToUser(userId: String, topTrack: List<Track>) {
        val userRef = FirebaseDatabase.getInstance().reference.child("users").child(userId)
        val trackIds = topTrack.map { it.id }

        userRef.child("userTracks").setValue(trackIds)
            .addOnSuccessListener {
                Log.d("Firebase", "IDs delle tracce salvate su Firebase per l'utente: $userId")
            }
            .addOnFailureListener {
                Log.e("Firebase", "Errore nel salvataggio degli IDs delle tracce su Firebase per l'utente $userId: ${it.message}")
            }
    }
*/
    /*fun saveTracksToFirebase(userId: String, topTrack: List<Track>) {
        val userRef = FirebaseDatabase.getInstance().reference.child("users").child(userId)
        topTrack.forEachIndexed { _, track ->
            val imageUrl = track.album.images.getOrNull(0)?.url ?: ""
            val trackData = mapOf(
                "trackName" to track.name,
                "album" to track.album.name,
                "artists" to track.artisttrack,
                "id" to track.id,
                "genres" to track.genres,
                "release_date" to track.releaseDate,
                "duration_ms" to track.durationMs,
                "image_url" to imageUrl
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
    }*/
  /*  fun fetchTopTracksFromFirebase() {
        Log.d("FirebaseData", "Inizio recupero dati da Firebase")
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val query = database.child("users").child(userId).child("topTracks").orderByKey()
            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("FirebaseData", "Metodo onDataChange chiamato")
                    val tracks = mutableListOf<Track>()
                    for (trackSnapshot in snapshot.children) {
                        val trackName = trackSnapshot.child("trackName").getValue(String::class.java) ?: ""
                        val albumName = trackSnapshot.child("album").getValue(String::class.java) ?: ""
                        val imageUrl = trackSnapshot.child("image_url").getValue(String::class.java) ?: ""
                        val artistList = mutableListOf<SimpleArtist>()
                        val artistsSnapshot = trackSnapshot.child("artists")
                        for (artistSnapshot in artistsSnapshot.children) {
                            val artistName = artistSnapshot.child("name").getValue(String::class.java) ?: ""
                            artistList.add(SimpleArtist(artistName))
                        }
                        val id = trackSnapshot.child("id").getValue(String::class.java) ?: ""
                        val genres = trackSnapshot.child("genres").getValue(String::class.java) ?: ""
                        val releaseDate = trackSnapshot.child("release_date").getValue(String::class.java) ?: ""
                        val durationMs = trackSnapshot.child("duration_ms").getValue(String::class.java) ?: ""
                        val album = Album(albumName, listOf(Image(imageUrl)))
                        val track = Track(trackName, album, artistList, id, genres, releaseDate, durationMs)
                        tracks.add(track)
                        Log.d("FIREBASE VIEWMODEL","FIREBASE $tracks")
                    }
                    topTracksfromdb.postValue(tracks)
                }
                val error: MutableLiveData<Throwable> = MutableLiveData()
                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseError", "Errore durante la lettura dal database Firebase: ${error.message}")
                    this.error.postValue(error.toException())
                }
            })
        }
    }

    fun saveArtistsToFirebase(userId: String, topArtist: List<Artist>) {
        Log.d("SaveArtistsDebug", "Inizio del metodo saveArtistsToFirebase")

        val userRef = FirebaseDatabase.getInstance().reference.child("users").child(userId)

        topArtist.forEachIndexed { index, artist ->
            Log.d("SaveArtistsDebug", "Salvataggio dell'artista $index: ${artist.name}")

            val imageUrl = artist.images.getOrNull(0)?.url ?: ""
            val artistData = mapOf(
                "artistname" to artist.name,
                "id" to artist.id,
                "genres" to artist.genres,
                "followers" to artist.followers.total,
                "image_url" to imageUrl
            )

            val artistKey = userRef.child("topArtists").push().key
            if (artistKey != null) {
                val update = mapOf<String, Any>("topArtists/$artistKey" to artistData)
                userRef.updateChildren(update)
                    .addOnSuccessListener {
                        Log.d("SaveArtistsDebug", "Artista salvato con successo: ${artist.name}")
                    }
                    .addOnFailureListener {
                        Log.e("SaveArtistsDebug", "Errore nel salvataggio dell'artista ${artist.name}: ${it.message}")
                    }
            } else {
                Log.e("SaveArtistsDebug", "Errore nel generare una chiave univoca per l'artista ${artist.name}.")
            }
        }
    }*/

   /* fun getSpecificArtistInfoByName(name: String): Artist? {
        // Cerca la traccia con l'ID specificato
        val specificArtistInfo=artistslist.find { it.name == name }
        if (specificArtistInfo != null) {
            Log.d("DatabaseViewModel", "Track info for ID $name")
            return specificArtistInfo // Restituisci la traccia se l'ID è valido
        }
        Log.d("DatabaseViewModel", "No track found for ID $name.")
        return null // Restituisci null se l'ID non è valido
    }

    fun getSpecificTrackInfoById(id: String): Track? {
        // Cerca la traccia con l'ID specificato
        val specificTrackInfo = tracks.find { it.id == id }
        if (specificTrackInfo != null) {
            Log.d("DatabaseViewModel", "Track info for ID $id: $specificTrackInfo")
            return specificTrackInfo // Restituisci la traccia se l'ID è valido
        }
        Log.d("DatabaseViewModel", "No track found for ID $id.")
        return null // Restituisci null se l'ID non è valido
    }
*/
   /* fun fetchTopArtistsFromFirebase() {
        Log.d("FirebaseData", "Inizio recupero dati artisti da Firebase")
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val query = database.child("users").child(userId).child("topArtists").orderByKey()
            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("FirebaseData", "Metodo onDataChange per artisti chiamato")
                    val artists = mutableListOf<Artist>()
                    for (artistSnapshot in snapshot.children) {
                        val artistName = artistSnapshot.child("artistname").getValue(String::class.java) ?: ""
                        Log.d("FirebaseData", "Nome dell'artista recuperato: $artistName")
                        val imageUrl = artistSnapshot.child("image_url").getValue(String::class.java) ?: ""
                        val id = artistSnapshot.child("id").getValue(String::class.java) ?: ""
                        val genres = artistSnapshot.child("genres").children.map { it.getValue(String::class.java) ?: "" }
                        val followersTotal = artistSnapshot.child("followers").getValue(Int::class.java) ?: 0

                        val followers = Followers(followersTotal)
                        val images = listOf(Image(imageUrl))
                        val artist = Artist(artistName, genres, id, followers, images)

                        artists.add(artist)
                        Log.d("FIREBASE VIEWMODEL","FIREBASE $artists")
                    }

                    topArtistsfromdb.postValue(artists)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseError", "Errore durante la lettura degli artisti dal database Firebase: ${error.message}")
                    // Gestisci l'errore come preferisci, per esempio con un altro MutableLiveData
                }
            })
        }
    }

}*/

   /* fun deleteUserDataFromFirebase(userId: String) {
       val userRef = FirebaseDatabase.getInstance().reference.child("users").child(userId)

       // Rimuovi tutti i dati dell'utente, inclusi i dati delle tracce
       userRef.removeValue()
           .addOnSuccessListener {
               Log.d("Firebase", "Dati dell'utente eliminati con successo da Firebase: $userId")
           }
           .addOnFailureListener {
               Log.e("Firebase", "Errore nell'eliminazione dei dati dell'utente da Firebase: ${it.message}")
           }
   }*/
   */
