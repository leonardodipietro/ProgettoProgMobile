package com.example.progettoprogmobile

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.progettoprogmobile.adapter.NotificationItem
import com.example.progettoprogmobile.adapter.NotificationsAdapter
import com.example.progettoprogmobile.model.Utente
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class FourthFragment :
    Fragment(),
    NotificationsAdapter.OnFollowClickListener,
    NotificationsAdapter.OnConfirmClickListener,
    NotificationsAdapter.OnDeleteClickListener {

    private val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid
    private val database = FirebaseDatabase.getInstance()
    private lateinit var recyclerView: RecyclerView

    private var notificationList: MutableList<NotificationItem> = mutableListOf()
    private lateinit var notificationsAdapter: NotificationsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_fourth, container, false)

        recyclerView = rootView.findViewById(R.id.newFollowersRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        notificationsAdapter = NotificationsAdapter(
            requireContext(),
            recyclerView,
            this,
            this,
            this,
            database,
            currentUserId
        )

        recyclerView.adapter = notificationsAdapter

        // Recupera i dati dell'utente corrente e inizia a popolare la RecyclerView
        retrieveUserData()

        return rootView
    }

    // Metodo per recuperare i dati dell'utente corrente
    private fun retrieveUserData() {
        val userReference = database.reference.child("users").child(currentUserId)
        userReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Ottieni i dati dell'utente corrente
                    val userData = dataSnapshot.getValue(Utente::class.java)
                    if (userData != null) {
                        // Controlla le impostazioni sulla privacy dell'account per determinare cosa mostrare
                        val accountPrivacy = dataSnapshot.child("privacy").child("account")
                        val everyone = accountPrivacy.child("Everyone").getValue(Boolean::class.java)

                        // Log per vedere il valore di "everyone"
                        Log.d("UserData", "Value of 'everyone': $everyone")

                        if (everyone == true) {
                            recyclerView.visibility = View.VISIBLE
                            // Se everyone è true, sposta automaticamente gli ID da "requests" a "followers"
                            moveRequestsToFollowers()
                            // Mostra i follower
                            retrieveFollowersData()
                        } else {
                            // Nascondi la RecyclerView e mostra le richieste di follow
                            recyclerView.visibility = View.VISIBLE
                            retrieveFollowRequestsData()
                        }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Gestisci eventuali errori nel recupero dei dati dell'utente corrente
                Log.e("DatabaseError", "Error in retrieving data: ${databaseError.message}")
                // Puoi gestire l'errore mostrando un Toast o loggando l'errore.
            }
        })
    }

    // Metodo per recuperare i dati di un follower specifico
    private fun retrieveFollowersData() {
        // Recupera i dati del follower dal database
        val followersReference = database.reference.child("users").child(currentUserId).child("followers")
        followersReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Itera sui follower e recupera i loro dati
                    for (followerSnapshot in dataSnapshot.children) {
                        val followerId = followerSnapshot.key
                        if (followerId != null) {
                            // Recupera i dati del follower
                            retrieveFollowerData(followerId)
                        }
                    }
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                // Gestisci eventuali errori nel recupero dei dati
            }
        })
    }

    // Metodo per recuperare i dati di un follower specifico
    private fun retrieveFollowerData(followerId: String) {
        Log.d("FollowerId", "Follower ID: $followerId")
        // Recupera i dati del follower dal database
        val followerDataReference = database.reference.child("users").child(followerId)
        followerDataReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(followerDataSnapshot: DataSnapshot) {
                if (followerDataSnapshot.exists()) {
                    // Gestisci i dati del follower
                    handleFollowerData(followerDataSnapshot)
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                // Gestisci eventuali errori nel recupero dei dati
            }
        })
    }

    // Metodo per gestire i dati di un follower
    private fun handleFollowerData(followerDataSnapshot: DataSnapshot) {
        // Ottieni l'URL dell'immagine dal campo "profile image"
        val imageUrl = followerDataSnapshot.child("profile image").getValue(String::class.java)
        val defaultImageUrl = "drawable://" + R.drawable.baseline_person_24

        // Crea un oggetto Utente con i dati del follower
        val follower = Utente(
            userId = followerDataSnapshot.key ?: "",
            name = followerDataSnapshot.child("name").getValue(String::class.java) ?: "",
            userImage = imageUrl.takeIf { !it.isNullOrEmpty() } ?: defaultImageUrl
        )
        Log.d("FollowerData", "Follower Data: $follower")

        // Aggiungi il follower alla lista delle notifiche
        notificationList.add(NotificationItem.FollowerItem(follower))
        notificationsAdapter.submitList(notificationList)
        notificationsAdapter.notifyDataSetChanged()
    }


    // Metodo chiamato quando si clicca su "Follow" in una notifica
    override fun onFollowClickListener(userId: String, isFollowing:Boolean) {
        // Implementa la logica di gestione del click sul pulsante "Follow"
        if (isFollowing) {
            addUserIdToFollowing(userId)
            // Recupera i dati del follower appena seguito e aggiungilo alla RecyclerView delle notifiche
            val followerDataReference = database.reference.child("users").child(userId)
            followerDataReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(followerDataSnapshot: DataSnapshot) {
                    if (followerDataSnapshot.exists()) {
                        val follower = followerDataSnapshot.getValue(Utente::class.java)
                        if (follower != null) {
                            if (!notificationsAdapter.isFollowerAlreadyAdded(userId)) {
                                notificationList.add(NotificationItem.FollowerItem(follower))
                                notificationsAdapter.submitList(notificationList)
                                notificationsAdapter.notifyDataSetChanged()
                            }
                        }
                    }
                    // Imposta il pulsante di Follow come "checked" nella RecyclerView
                    notificationsAdapter.setFollowButtonChecked(userId, true)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Gestisci eventuali errori nel recupero dei dati
                }
            })
        } else {
            // Se l'utente smette di seguire, esegui le azioni necessarie
            removeUserIdFromFollowing(userId)
        }

        // Esempio di messaggio Toast per confermare il click su "Follow"
        Toast.makeText(requireContext(), "Hai cliccato su Follow per l'utente con ID: $userId", Toast.LENGTH_SHORT).show()
    }

    // Metodo per aggiungere un utente alla lista "following" dell'utente corrente
    private fun addUserIdToFollowing(userId: String) {
        val followingReference = database.reference.child("users").child(currentUserId).child("following")
        followingReference.child(userId).setValue(true)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Se l'aggiunta è riuscita, aggiungi l'utente corrente alla lista "followers" dell'utente seguito
                    addCurrentUserToFollower(userId)
                    // L'utente è stato aggiunto con successo alla lista "following"
                    Toast.makeText(requireContext(), "Utente aggiunto con successo alla lista following", Toast.LENGTH_SHORT).show()
                } else {
                    // Gestisci eventuali errori durante l'aggiunta
                    Toast.makeText(requireContext(), "Errore durante l'aggiunta dell'utente alla lista following", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Metodo per aggiungere l'utente corrente alla lista "followers" di un altro utente
    private fun addCurrentUserToFollower(followedUserId: String) {
        // Aggiungi l'ID dell'utente corrente alla lista "followers" dell'utente seguito
        val followersReference = database.reference.child("users").child(followedUserId).child("followers")
        followersReference.child(currentUserId).setValue(true)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Se l'aggiunta è riuscita, mostra un messaggio di successo
                    Toast.makeText(requireContext(), "Utente corrente aggiunto con successo alla lista followers dell'utente seguito", Toast.LENGTH_SHORT).show()
                } else {
                    // Gestisci eventuali errori durante l'aggiunta
                    Toast.makeText(requireContext(), "Errore durante l'aggiunta dell'utente corrente alla lista followers dell'utente seguito", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Metodo per rimuovere un utente dalla lista "following" dell'utente corrente
    private fun removeUserIdFromFollowing(userId: String) {
        val followingReference = database.reference.child("users").child(currentUserId).child("following")
        followingReference.child(userId).removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Se la rimozione è riuscita, rimuovi l'utente corrente dalla lista "followers" dell'utente seguito
                    removeCurrentUserFromFollower(userId)
                    // L'utente è stato rimosso con successo dalla lista "following"
                    Toast.makeText(requireContext(), "Utente rimosso con successo dalla lista following", Toast.LENGTH_SHORT).show()
                } else {
                    // Gestisci eventuali errori durante la rimozione
                    Toast.makeText(requireContext(), "Errore durante la rimozione dell'utente dalla lista following", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Metodo per rimuovere l'utente corrente dalla lista "followers" di un altro utente
    private fun removeCurrentUserFromFollower(followedUserId: String) {
        // Rimuovi l'ID dell'utente corrente dalla lista "followers" dell'utente seguito
        val followersReference = database.reference.child("users").child(followedUserId).child("followers")
        followersReference.child(currentUserId).removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Se la rimozione è riuscita, mostra un messaggio di successo
                    Toast.makeText(requireContext(), "Utente corrente rimosso con successo dalla lista followers dell'utente seguito", Toast.LENGTH_SHORT).show()
                } else {
                    // Gestisci eventuali errori durante la rimozione
                    Toast.makeText(requireContext(), "Errore durante la rimozione dell'utente corrente dalla lista followers dell'utente seguito", Toast.LENGTH_SHORT).show()
                }
            }
    }


    // Metodo per recuperare i dati delle richieste di follow
    private fun retrieveFollowRequestsData() {
        // Recupera l'elenco delle richieste di follow dal nodo "request" del database
        val requestsReference = database.reference.child("users").child(currentUserId).child("requests")
        requestsReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(requestsSnapshot: DataSnapshot) {
                if (requestsSnapshot.exists()) {
                    // Lista per mantenere traccia delle richieste di follow
                    val followRequests: MutableList<String> = mutableListOf()

                    // Itera sulle richieste di follow e recupera i dati degli utenti
                    for (requestSnapshot in requestsSnapshot.children) {
                        val requestId = requestSnapshot.key
                        if (requestId != null) {
                            followRequests.add(requestId)
                        }
                    }

                    // Verifica se ci sono richieste di follow
                    if (followRequests.isNotEmpty()) {
                        // Recupera i dati degli utenti associati alle richieste e aggiorna la RecyclerView
                        for (userId in followRequests) {
                            retrieveUserDataForRequest(userId)
                        }
                        // Aggiungi la chiamata a retrieveFollowersData() qui
                        retrieveFollowersData()
                    }
                } else {
                    Log.d("FollowRequests", "No follow requests found")

                    retrieveFollowersData()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Gestisci eventuali errori nel recupero dei dati delle richieste di follow
                Log.e("FollowRequests", "Error retrieving follow requests: ${databaseError.message}")
            }
        })
    }

    private fun retrieveUserDataForRequest(userId: String) {
        val userReference = database.reference.child("users").child(userId)
        userReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val imageUrl = dataSnapshot.child("profile image").getValue(String::class.java)
                    val defaultImageUrl = "drawable://" + R.drawable.baseline_person_24

                    val user = Utente(
                        userId = userId,
                        name = dataSnapshot.child("name").getValue(String::class.java) ?: "",
                        userImage = imageUrl.takeIf { !it.isNullOrEmpty() } ?: defaultImageUrl
                    )

                    // Log dei dati dell'utente associato alla richiesta
                    Log.d("UserDataForRequest", "User Data for Request: $user")

                    // Aggiungi l'utente associato alla richiesta alla lista delle notifiche
                    notificationList.add(NotificationItem.RequestItem(user))
                    notificationsAdapter.submitList(notificationList)
                    notificationsAdapter.notifyDataSetChanged()
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                // Gestisci eventuali errori nel recupero dei dati dell'utente associato alla richiesta
                Log.e("UserDataForRequest", "Error retrieving user data: ${databaseError.message}")
            }
        })
    }

    // Metodo chiamato quando si clicca su "Conferma" in una notifica
    override fun onConfirmClickListener(userId: String) {
        // Rimuovi la richiesta di follow
        removeFollowRequest(userId)

        // Aggiungi l'utente ai followers
        addFollower(userId)

        // Rimuovi l'utente dalla lista delle notifiche
        removeNotificationItem(userId)

        addNewFollowerNotification(userId)

    }

    // Metodo per rimuovere la richiesta di follow dal nodo "requests"
    private fun removeFollowRequest(userId: String) {
        val requestsReference = database.reference.child("users").child(currentUserId).child("requests")
        requestsReference.child(userId).removeValue()
    }

    // Metodo per aggiungere l'utente ai followers
    private fun addFollower(userId: String) {
        val followersReference = database.reference.child("users").child(currentUserId).child("followers")
        followersReference.child(userId).setValue(true)
    }

    // Metodo per rimuovere un elemento dalla lista delle notifiche
    private fun removeNotificationItem(userId: String) {
        val position = notificationList.indexOfFirst {
            when (it) {
                is NotificationItem.FollowerItem -> it.utente.userId == userId
                is NotificationItem.RequestItem -> it.utente.userId == userId
            }
        }

        if (position != -1) {
            notificationList.removeAt(position)
            notificationsAdapter.submitList(notificationList)
            notificationsAdapter.notifyItemRemoved(position)
        }
    }

    private fun addNewFollowerNotification(userId: String) {
        // Recupera i dati dell'utente associato alla richiesta di follow
        val userReference = database.reference.child("users").child(userId)
        userReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Ottieni i dati dell'utente
                    val imageUrl = dataSnapshot.child("profile image").getValue(String::class.java)
                    val defaultImageUrl = "drawable://" + R.drawable.baseline_person_24

                    // Crea un oggetto Utente con i dati recuperati
                    val newUser = Utente(
                        userId = userId,
                        name = dataSnapshot.child("name").getValue(String::class.java) ?: "",
                        userImage = imageUrl.takeIf { !it.isNullOrEmpty() } ?: defaultImageUrl
                    )

                    // Aggiungi la nuova notifica di follower
                    notificationList.add(NotificationItem.FollowerItem(newUser))
                    notificationsAdapter.submitList(notificationList)
                    notificationsAdapter.notifyItemInserted(notificationList.size - 1)
                    notificationsAdapter.notifyDataSetChanged()
                }
            } override fun onCancelled(databaseError: DatabaseError) {
                // Implementa l'azione da eseguire in caso di cancellazione dell'evento
                Log.e("UserDataForRequest", "Error retrieving user data: ${databaseError.message}")
                // Puoi mostrare un messaggio Toast o effettuare altre operazioni in risposta a questa cancellazione
            }
        })
    }

    // Metodo chiamato quando si clicca su "Elimina" in una notifica
    override fun onDeleteClickListener(userId: String) {
        val requestsReference = database.reference.child("users").child(currentUserId).child("requests")
        requestsReference.child(userId).removeValue()

        removeNotificationItem(userId)
    }


    private fun moveRequestsToFollowers() {
        val requestsReference = database.reference.child("users").child(currentUserId).child("requests")
        requestsReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(requestsSnapshot: DataSnapshot) {
                if (requestsSnapshot.exists()) {
                    // Itera sulle richieste di follow e sposta gli ID da "requests" a "followers"
                    for (requestSnapshot in requestsSnapshot.children) {
                        val requestId = requestSnapshot.key
                        if (requestId != null) {
                            moveRequestToFollowers(requestId)
                        }
                    }
                    // Dopo aver spostato gli ID, rimuovi il nodo "requests"
                    requestsReference.removeValue()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Gestisci eventuali errori nel recupero dei dati delle richieste di follow
                Log.e("FollowRequests", "Error retrieving follow requests: ${databaseError.message}")
            }
        })
    }

    private fun moveRequestToFollowers(userId: String) {
        val followersReference = database.reference.child("users").child(currentUserId).child("followers")
        followersReference.child(userId).setValue(true)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Aggiungi l'utente corrente alla lista "followers" dell'utente seguito
                    addCurrentUserToFollower(userId)
                } else {
                    // Gestisci eventuali errori durante l'aggiunta
                    Log.e("MoveRequestToFollowers", "Error moving request to followers: ${task.exception?.message}")
                }
            }
    }

}