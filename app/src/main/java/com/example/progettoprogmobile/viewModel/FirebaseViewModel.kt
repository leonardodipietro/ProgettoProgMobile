package com.example.progettoprogmobile.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.*
import com.example.progettoprogmobile.model.*
import android.util.Log
import com.google.firebase.auth.FirebaseAuth

class FirebaseViewModel : ViewModel() {

    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference

    private val user = FirebaseAuth.getInstance().currentUser
    private val userId = user?.uid
    val topTracksfromdb: MutableLiveData<List<Track>> = MutableLiveData()
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

   fun fetchTopTracksFromFirebase() {
       Log.d("FirebaseData", "Inizio recupero dati da Firebase")
       // Ottieni un riferimento al nodo "users/{userId}/tracks" con ordinamento per chiave e limitazione agli ultimi 50 elementi
       val userId = FirebaseAuth.getInstance().currentUser?.uid
       if (userId != null) {
           val query = database.child("users").child(userId).child("topTracks").orderByKey()

           query.addListenerForSingleValueEvent(object : ValueEventListener {

               override fun onDataChange(snapshot: DataSnapshot) {
                   Log.d("FirebaseData", "Metodo onDataChange chiamato")
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

                       // Aggiungi log per vedere i dati durante l'elaborazione
                       Log.d("FirebaseData", "Nome traccia: $trackName")
                       Log.d("FirebaseData", "Nome album: $albumName")
                       Log.d("FirebaseData", "Nomi artisti: $artistNames")
                   }

                   // Aggiungi log per vedere la lista di tracce completata
                   Log.d("FirebaseData", "Lista tracce completata: $tracks")

                   // Aggiorna il LiveData delle tracce nella RecyclerView
                   topTracksfromdb.postValue(tracks)
               }

               val error: MutableLiveData<Throwable> = MutableLiveData()
               override fun onCancelled(error: DatabaseError) {
                   // Gestisci l'errore in caso di problema con la lettura dal database
                   Log.e(
                       "FirebaseError",
                       "Errore durante la lettura dal database Firebase: ${error.message}"
                   )
                   this.error.postValue(error.toException())
               }
           })
       }
   }

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
   } */
}