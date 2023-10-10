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

                database.child("recensioni").child(commentId).setValue(recensione)
                    .addOnSuccessListener {
                        addCommentIdToTrack(commentId, trackId)
                        addCommentIdToUser(commentId, userId)
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

    private fun addCommentIdToUser(commentId: String, userId: String) {
      database.child("users").child(userId).child("recensioni").push().setValue(commentId)


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
        database.child("recensioni")
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


}
