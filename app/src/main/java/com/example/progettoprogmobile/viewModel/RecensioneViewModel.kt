package com.example.progettoprogmobile.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.progettoprogmobile.model.Recensione
import com.example.progettoprogmobile.model.Utente
import com.firebase.ui.auth.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase

class RecensioneViewModel: ViewModel() {
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference
    val recensioniData: MutableLiveData<List<Recensione>> = MutableLiveData()
    val usersData: MutableLiveData<Map<String, Utente>> = MutableLiveData()
    fun getCurrentUserId(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }

    fun saveOrUpdateRecensione(userId: String, trackId: String, commentContent: String) {
        hasUserReviewed(trackId, userId) { existingReview ->
            if (existingReview == null) {
                val commentId = database.push().key!!

                val recensione = Recensione(
                    commentId = commentId,
                    userId = userId,
                    trackId = trackId,
                    timestamp = System.currentTimeMillis(),
                    content = commentContent
                )

                database.child("reviews").child(commentId).setValue(recensione)
                    .addOnSuccessListener {
                        addCommentIdToTrack(commentId, trackId)
                        addCommentIdToUser(commentId, userId)
                    }
                    .addOnFailureListener {
                        // Gestisci l'errore
                    }
            } else {
                val updatedReview = existingReview.copy(content = commentContent, timestamp = System.currentTimeMillis())

                database.child("reviews").child(updatedReview.commentId).setValue(updatedReview)
                    .addOnSuccessListener {
                        // Recensione aggiornata con successo
                    }
                    .addOnFailureListener {
                        // Gestisci l'errore
                    }
            }
        }
    }

    private fun addCommentIdToUser(commentId: String, userId: String) {
      database.child("users").child(userId).child("reviews").push().setValue(commentId)


    }
    fun hasUserReviewed(trackId: String, userId: String, onComplete: (Recensione?) -> Unit) {
        database.child("reviews").orderByChild("trackId").equalTo(trackId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var foundRecensione: Recensione? = null
                for (snapshot in dataSnapshot.children) {
                    val review = snapshot.getValue(Recensione::class.java)
                    if (review?.userId == userId) {
                        foundRecensione = review
                        //TODO CONVERTIRE IL LOG CON UN MESSAGGIO DI ERRORE
                        Log.d("HASUSERREVIEWED","RECENSIONE GIA INSERITA")
                    }
                }
                onComplete(foundRecensione)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                onComplete(null)
            }
        })
    }


    private fun addCommentIdToTrack(commentId: String, trackId: String) {
        database.child("tracks").child(trackId).child("comments").push().setValue(commentId)
    }

    private fun fetchUsers(userIds: List<String>) {
        database.child("users")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val usersMap = mutableMapOf<String, Utente>()
                    for (id in userIds) {
                        val user = dataSnapshot.child(id).getValue(Utente::class.java)
                        user?.let { usersMap[id] = it }
                    }
                    usersData.value = usersMap
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle error here
                }
            })
    }
    fun fetchRecensioniAndUsersForTrack(trackId: String) {
        fetchRecensioniForTrack(trackId)
    }

    private fun fetchRecensioniForTrack(trackId: String) {

        database.child("reviews")
            .orderByChild("trackId")
            .equalTo(trackId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val recensioniList = mutableListOf<Recensione>()
                    val userIds = mutableListOf<String>()
                    for (snapshot in dataSnapshot.children) {
                        val recensione = snapshot.getValue(Recensione::class.java)
                        recensione?.let {
                            recensioniList.add(it)
                            userIds.add(it.userId)
                        }
                    }
                    recensioniData.value = recensioniList
                    fetchUsers(userIds)


                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle error here
                }
            })
    }

    fun fetchRecensioniForArtist(artistname: String?) {
        // Lista per contenere tutte le recensioni associate all'artista
        val recensioniList = mutableListOf<Recensione>()

        Log.d("FETCH_ARTIST", "Inizio recupero recensioni per artista con ID: $artistname")

        // Step 1: Cerca tutte le tracce associate all'artista
        database.child("tracks")
            .orderByChild("artists/0/name")
            .equalTo(artistname)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val trackIdsForArtist = mutableListOf<String>()
                    for (trackSnapshot in dataSnapshot.children) {
                        trackSnapshot.key?.let {
                            trackIdsForArtist.add(it)
                            Log.d("FETCH_ARTIST", "Trovata traccia con ID: $it per artista: $artistname")
                        } ?: run {
                            Log.w("FETCH_ARTIST", "Trovata traccia senza ID per artista: $artistname")
                        }
                    }

                    // Se non ci sono tracce per l'artista, restituisci una lista vuota
                    if (trackIdsForArtist.isEmpty()) {
                        Log.d("FETCH_ARTIST", "Nessuna traccia trovata per l'artista con ID: $artistname")
                        recensioniData.value = recensioniList
                    } else {
                        // Step 2: Con la lista delle tracce, cerca tutte le recensioni associate a queste tracce
                        var tracksProcessed = 0
                        trackIdsForArtist.forEach { trackId ->
                            database.child("reviews")
                                .orderByChild("trackId")
                                .equalTo(trackId)
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        for (reviewSnapshot in snapshot.children) {
                                            reviewSnapshot.getValue(Recensione::class.java)?.let {
                                                recensioniList.add(it)
                                                Log.d("FETCH_ARTIST", "Trovata recensione con ID: ${it.commentId} per la traccia: $trackId")
                                            }
                                        }

                                        tracksProcessed++
                                        // Se abbiamo elaborato tutte le tracce, aggiorniamo il LiveData
                                        if (tracksProcessed == trackIdsForArtist.size) {
                                            Log.d("FETCH_ARTIST", "Tutte le tracce sono state elaborate per l'artista: $artistname")
                                            recensioniData.value = recensioniList
                                            Log.d("FETCH_ARTIST", "recensioni$recensioniList ")
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        Log.e("FETCH_ARTIST", "Errore durante il recupero delle recensioni per la traccia: $trackId", error.toException())
                                    }
                                })
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("FETCH_ARTIST", "Errore durante il recupero delle tracce per l'artista: $artistname", databaseError.toException())
                }
            })
    }







}
