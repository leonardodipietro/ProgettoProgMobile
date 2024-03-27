package com.example.progettoprogmobile.viewModel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.progettoprogmobile.model.Recensione
import com.example.progettoprogmobile.model.Risposta
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.progettoprogmobile.model.Utente
import com.firebase.ui.auth.data.model.User
import com.google.firebase.auth.FirebaseAuth
import java.util.concurrent.CountDownLatch

class RisposteViewModel: ViewModel() {
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference
    val commentiData: MutableLiveData<List<Risposta>> = MutableLiveData()
    val usersData: MutableLiveData<Map<String, Utente>> = MutableLiveData()
    var usersMap = mutableMapOf<String, Utente>()

    fun saveRisposta (userId: String,commentIdfather:String,answercontent:String) {

         //genera un identificativo univoco per firebase
         val answerId = database.push().key!!
         // Ottieni la data e l'ora attuali.
         val currentTimestamp = System.currentTimeMillis()
         val date = Date(currentTimestamp)
         // Formatta la data/ora per la visualizzazione.
         val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
         val formattedDateTime = formatter.format(date)

         val risposta = Risposta(
             userId = userId,
             answerId = answerId,
             commentIdfather = commentIdfather,
             timestamp = formattedDateTime,
             answercontent = answercontent,
         )
         database.child("answers").child(answerId).setValue(risposta)
             .addOnSuccessListener {
             Log.d("salvataggio commento su firebase ", "salvataggio commento su firebase con $risposta")

             }
             .addOnFailureListener {e ->
                 Log.d("salvataggio commento su firebase", "Errore nel salvataggio: ${e.message}")
             }

     }

    fun fetchCommentfromRecensione(commentIdfather:String) {
        database.child("answers")
            .orderByChild("commentIdfather")
            .equalTo(commentIdfather)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val commentiList = mutableListOf<Risposta>()
                    val userIds = mutableListOf<String>()
                    for (snapshot in dataSnapshot.children) {
                        val commento = snapshot.getValue(Risposta::class.java)
                        commento?.let {
                            commentiList.add(it)
                            userIds.add(it.userId)
                        }
                    }
                    commentiData.value = commentiList
                    Log.d("ELENCO COMMENTI ASSOCCIATI A QUEL BRANO","ELENCO : $commentiList")
                    fetchUsers(userIds)
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle error here
                }
            })
    }

    fun fetchUsers(userIds: List<String>) {
        val tempUsersMap = mutableMapOf<String, Utente>()
        var loadedUsersCount = 0 // Contatore per tracciare il numero di utenti caricati

        userIds.forEach { userId ->
            database.child("users").child(userId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val user = dataSnapshot.getValue(Utente::class.java)
                        if (user != null) {
                            tempUsersMap[userId] = user
                        }
                        synchronized(this) {
                            loadedUsersCount++
                            // Verifica se abbiamo terminato di caricare tutti gli utenti
                            if (loadedUsersCount == userIds.size) {
                                usersMap = tempUsersMap // Aggiorna la mappa principale solo quando tutti gli utenti sono stati caricati
                                usersData.value = usersMap
                            }
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Gestire l'errore qui
                    }
                })
        }
    }




}