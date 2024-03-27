package com.example.progettoprogmobile.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.progettoprogmobile.model.Recensione
import com.example.progettoprogmobile.model.Track
import com.example.progettoprogmobile.model.Utente
import com.example.progettoprogmobile.utils.SharedEditTextVisibilityManager
import com.firebase.ui.auth.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale



//Per i test
class RecensioneViewModel @JvmOverloads constructor(
    private val db: DatabaseReference = FirebaseDatabase.getInstance().reference
) : ViewModel() {
    private var database: DatabaseReference = FirebaseDatabase.getInstance().reference
    val recensioniData: MutableLiveData<List<Recensione>> = MutableLiveData()
    var usersData: MutableLiveData<Map<String, Utente>> = MutableLiveData()
    // private lateinit var firebaseViewModel :FirebaseViewModel
    //val StatoRecensioneLiveData = MutableLiveData<Boolean>() //Per l'edit text e il bottone
   private lateinit var sharedEditTextVisibilityManager: SharedEditTextVisibilityManager


    // Chiamato normalmente nell'app
    fun init() {
        database = FirebaseDatabase.getInstance().reference

    }

    // Utilizzato solo per i test per impostare il mock
    fun initForTesting(mockDatabase: DatabaseReference) {
        database = mockDatabase
    }



    fun setSharedEditTextVisibilityManager(manager: SharedEditTextVisibilityManager) {
        this.sharedEditTextVisibilityManager = manager
    }
    fun getCurrentUserId(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }

    /*fun cercaUtenteDaRecensione(recensione: Recensione) {
        Log.d("FirebaseViewModel", "cercaUtenteDaRecensione called with userId: ${recensione.userId}")
        val ref = FirebaseDatabase.getInstance().getReference("users/${recensione.userId}")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val utente = snapshot.getValue(Utente::class.java)
                if (utente != null) {
                    // Prova a recuperare direttamente l'URL dell'immagine del profilo
                    val imageUrl = snapshot.child("profile image").getValue(String::class.java)
                    utente.userImage = imageUrl ?: "" // Aggiorna l'URL dell'immagine nel tuo oggetto Utente

                    Log.d("FirebaseViewModel", "Utente: ${utente.name}, URL Immagine: $imageUrl")

                    // Aggiorna direttamente usersData con la mappa modificata
                    usersData.value?.let { currentMap ->
                        val updatedMap = currentMap.toMutableMap()
                        updatedMap[utente.userId] = utente
                        usersData.postValue(updatedMap)
                    } ?: run {
                        usersData.postValue(mutableMapOf(utente.userId to utente))
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseViewModel", "Database error: ${error.toException()}")
            }
        })
    }
*/


    fun saveRecensione(userId: String, trackId: String, artistId: String, commentContent: String) {
        // Genera un identificativo univoco per Firebase
        val commentId = database.push().key!!
        // Ottieni la data e l'ora attuali
        val currentTimestamp = System.currentTimeMillis()
        val date = Date(currentTimestamp)
        // Formatta la data/ora per la visualizzazione
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val formattedDateTime = formatter.format(date)

        val recensione = Recensione(
            commentId = commentId,
            userId = userId,
            trackId = trackId,
            timestamp = formattedDateTime,
            content = commentContent,
            artistId = artistId
        )

        database.child("reviews").child(commentId).setValue(recensione)
            .addOnSuccessListener {
                addCommentIdToTrack(commentId, trackId)
                addCommentIdToUser(commentId, userId)
                addCommentIdToArtist(commentId, artistId)
            }
            .addOnFailureListener {
                // Gestione dell'errore
            }
      //sharedEditTextVisibilityManager.hideEditText()
    }

 /*   fun updateRecensione(commentId: String, userId: String, commentContent: String) {
        // Ottieni la data e l'ora attuali
        val currentTimestamp = System.currentTimeMillis()
        val date = Date(currentTimestamp)
        // Formatta la data/ora per la visualizzazione
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val formattedDateTime = formatter.format(date)

        database.child("reviews").child(commentId).get().addOnSuccessListener { dataSnapshot ->
            val existingReview = dataSnapshot.getValue(Recensione::class.java)
            existingReview?.let {
                val updatedReview = it.copy(content = commentContent, timestamp = formattedDateTime)

                database.child("reviews").child(commentId).setValue(updatedReview)
                    .addOnSuccessListener {
                        Log.d("Update", "Aggiornamento completato")
                    }
                    .addOnFailureListener {
                        // Gestione dell'errore
                    }
            }
        }
        sharedEditTextVisibilityManager.hideEditText()
    }
*/

    fun saveOrUpdateRecensione(userId: String, trackId: String, artistId:String, commentContent: String) {
        hasUserReviewed(trackId, userId) { existingReview ->
            if (existingReview == null) {
                //genera un identificativo univoco per firebase
                val commentId = database.push().key!!
                // Ottieni la data e l'ora attuali.
                val currentTimestamp = System.currentTimeMillis()
                val date = Date(currentTimestamp)
                // Formatta la data/ora per la visualizzazione.
                val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val formattedDateTime = formatter.format(date)


                val recensione = Recensione(
                    commentId = commentId,
                    userId = userId,
                    trackId = trackId,
                    timestamp = formattedDateTime,
                    content = commentContent,
                    artistId = artistId
                )

                database.child("reviews").child(commentId).setValue(recensione)
                    .addOnSuccessListener {
                        addCommentIdToTrack(commentId, trackId)
                        addCommentIdToUser(commentId, userId)
                        addCommentIdToArtist(commentId, artistId)
                    }
                    .addOnFailureListener {

                    }
                //StatoRecensioneLiveData.value = true se funziona tolgo
                sharedEditTextVisibilityManager.hideEditText()
            } else {
                // Ottieni la data e l'ora attuali.
                val currentTimestamp = System.currentTimeMillis()
                val date = Date(currentTimestamp)
                // Formatta la data/ora per la visualizzazione.
                val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val formattedDateTime = formatter.format(date)

                // Creazione di una copia della recensione esistente con il nuovo contenuto e la data formattata.
                val updatedReview = existingReview.copy(content = commentContent, timestamp = formattedDateTime)

                database.child("reviews").child(updatedReview.commentId).setValue(updatedReview)
                    .addOnSuccessListener {
                        Log.d("aggiornamento completato","aggiornamento vompletato")
                    }
                    .addOnFailureListener {

                    }
                sharedEditTextVisibilityManager.hideEditText()
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

    //PER AGGIORNARE IL LIVE DATA
    fun checkUserReview(trackId: String, userId: String) {
        hasUserReviewed(trackId, userId) { existingReview ->
            // Se NON esiste una recensione, l'EditText DEVE essere visibile.
            val shouldShowEditText = existingReview == null
            if(::sharedEditTextVisibilityManager.isInitialized) {
                sharedEditTextVisibilityManager.setEditTextVisibility(shouldShowEditText)
            }
        }
    }




    private fun addCommentIdToTrack(commentId: String, trackId: String) {
        database.child("tracks").child(trackId).child("comments").push().setValue(commentId)
    }
    private fun addCommentIdToArtist(commentId: String, artistId: String) {
        database.child("artists").child(artistId).child("comments").push().setValue(commentId)
    }
    private fun fetchUsers(userIds: List<String>) {
        database.child("users")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val usersMap = mutableMapOf<String, Utente>()
                    userIds.forEach { id ->
                        dataSnapshot.child(id).getValue(Utente::class.java)?.let { user ->
                            val imageUrl = dataSnapshot.child(id).child("profile image").getValue(String::class.java)
                            val updatedUser = user.copy(userImage = imageUrl ?: "") // Crea un nuovo oggetto Utente con l'URL dell'immagine aggiornato
                            Log.d("FetchUsers", "Utente: ${updatedUser.name}, URL Immagine: ${updatedUser.userImage}")
                            usersMap[id] = updatedUser // Aggiungi l'utente alla mappa
                        }
                    }
                    usersData.value = usersMap // Aggiorna LiveData con la nuova mappa
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("FetchUsers", "Errore nel database: ${databaseError.toException()}")
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
    fun retrieveTracksReviewedByArtistAndDetails(artistId: String, firebaseViewModel: FirebaseViewModel, onComplete: (List<Track>) -> Unit) {
        // Prima, chiamiamo la funzione fetchTracksReviewedByArtist per ottenere gli ID delle tracce recensite dall'artista
        fetchTracksReviewedByArtist(artistId) { trackIds ->
            // Ora abbiamo una lista di ID di tracce recensite dall'artista
            // Possiamo chiamare retrieveTracksDetails per ottenere i dettagli di queste tracce
            firebaseViewModel.retrieveTracksDetails(trackIds) { tracks ->
                // Ora abbiamo una lista di tracce con i loro dettagli
                // Puoi fare ciò che vuoi con queste tracce, ad esempio, stamparle a console
                for (track in tracks) {
                    Log.d("TrackDetails", "Nome Traccia: ${track.name}")
                    Log.d("TrackDetails", "Album: ${track.album.name}")
                    for (artist in track.artists) {
                        Log.d("TrackDetails", "Artista: ${artist.name}")
                    }
                    Log.d("TrackDetails", "-------------------")
                    onComplete?.invoke(tracks)
                }
            }
        }
    }


    fun fetchTracksReviewedByArtist(artistId: String, onComplete: (List<String>) -> Unit) {
        Log.d("TracceRecensite", "Artist ID: $artistId")

        val tracksReviewedByArtist = mutableListOf<String>()

        // Cerca tutte le recensioni associate all'artista con l'ID specificato
        database.child("reviews")
            .orderByChild("artistId")
            .equalTo(artistId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (snapshot in dataSnapshot.children) {
                        val recensione = snapshot.getValue(Recensione::class.java)
                        recensione?.let {
                            // Aggiungi l'ID della traccia alla lista se non è già presente
                            if (!tracksReviewedByArtist.contains(it.trackId)) {
                                tracksReviewedByArtist.add(it.trackId)
                            }
                        }
                        // Ora hai una lista di ID di tracce recensite dall'artista
                        // Chiama retrieveTracksDetails per ottenere i dettagli delle tracce
                    }
                    Log.d("TracceRecensite", "Prima di onComplete: $tracksReviewedByArtist")

                    // Ora hai una lista di ID di tracce recensite dall'artista
                    onComplete(tracksReviewedByArtist)
                    Log.d("TracceRecensite", "Lista di ID delle tracce recensite dall'artista: $tracksReviewedByArtist")
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("TracceRecensite", "Errore durante la ricerca delle tracce recensite: ${databaseError.message}")
                    // Gestisci l'errore qui
                }
            })
    }


    fun deleteRecensione(commentId: String, userId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val database = FirebaseDatabase.getInstance()
        val reviewsReference = database.getReference("reviews")
        val userReviewsReference = database.getReference("users").child(userId).child("reviews")

        reviewsReference.child(commentId).removeValue().addOnSuccessListener {
            userReviewsReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // Trova la chiave che corrisponde all'ID della recensione che si desidera eliminare
                    var id: String? = null
                    for (child in dataSnapshot.children) {
                        if (commentId == child.value as String) {
                            id = child.key
                            break
                        }
                    }

                    // Se trovi una corrispondenza, procedi con la rimozione
                    if (id != null) {
                        userReviewsReference.child(id).removeValue().addOnSuccessListener {
                            onSuccess() // Chiamato se la rimozione è riuscita
                        }.addOnFailureListener { e ->
                            onFailure(e) // Chiamato se c'è un errore durante la rimozione
                        }
                    } else {
                        onFailure(Exception("Recensione non trovata nel nodo dell'utente."))
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    onFailure(databaseError.toException())
                }
            })
        }.addOnFailureListener { e ->
            onFailure(e) // Chiamato se c'è un errore durante la rimozione dal nodo globale delle recensioni
        }
    }
}
