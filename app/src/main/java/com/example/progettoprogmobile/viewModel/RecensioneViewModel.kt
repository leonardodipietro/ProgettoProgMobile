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


   /* private val _recensioniLiveData = MutableLiveData<List<Recensione>>()
    val recensioniLiveData: LiveData<List<Recensione>> = _recensioniLiveData

    private val _usersLiveData = MutableLiveData<Map<String, Utente>>()
    val usersLiveData: LiveData<Map<String, Utente>> = _usersLiveData
*/



    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference
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

                database.child("recensioni").child(commentId).setValue(recensione)
                    .addOnSuccessListener {
                        addCommentIdToTrack(commentId, trackId)
                    }
                    .addOnFailureListener {
                        // Gestisci l'errore
                    }
            } else {
                val updatedReview = existingReview.copy(content = commentContent, timestamp = System.currentTimeMillis())

                database.child("recensioni").child(updatedReview.commentId).setValue(updatedReview)
                    .addOnSuccessListener {
                        // Recensione aggiornata con successo
                    }
                    .addOnFailureListener {
                        // Gestisci l'errore
                    }
            }
        }
    }


    fun hasUserReviewed(trackId: String, userId: String, onComplete: (Recensione?) -> Unit) {
        database.child("recensioni").orderByChild("trackId").equalTo(trackId).addListenerForSingleValueEvent(object : ValueEventListener {
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

 /*   fun fetchRecensioniForTrack(trackId: String, onSuccess: (List<String>) -> Unit) {
        val trackCommentsRef = database.child("tracks").child(trackId).child("comments")

        trackCommentsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val commentIds = dataSnapshot.children.mapNotNull { it.getValue(String::class.java) }
                onSuccess(commentIds)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Gestione errori
            }
        })
    }*/





    //NUOVO
   /*fun fetchRecensioniForTrack(trackId: String) {
        val recensioniReference = database.child("recensioni")
        recensioniReference.orderByChild("trackId").equalTo(trackId)
            .addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val recensioniList = mutableListOf<Recensione>()
                    val userIds = mutableSetOf<String>()

                    for (postSnapshot in snapshot.children) {
                        val recensione = postSnapshot.getValue(Recensione::class.java)
                        recensione?.let {
                            recensioniList.add(it)
                            userIds.add(it.userId)
                        }
                    }
                    _recensioniLiveData.value = recensioniList

                    // Dopo aver ottenuto le recensioni, carica gli utenti
                    fetchUsers(userIds)
                }

                override fun onCancelled(error: DatabaseError) {
                    // Gestisci l'errore
                }
            })
    }

    private fun fetchUsers(userIds: Set<String>) {
        val usersReference = database.child("users")
        val usersMap = mutableMapOf<String, Utente>()

        // Potresti dover richiamare questo metodo per ogni ID utente.
        for (userId in userIds) {
            usersReference.child(userId).addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(Utente::class.java)
                    if (user != null) {
                        usersMap[userId] = user
                        if (usersMap.size == userIds.size) { // Se hai recuperato tutti gli utenti
                            _usersLiveData.value = usersMap
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Gestisci l'errore
                }
            })
        }



    }*/


}
