package com.example.progettoprogmobile

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
import com.example.progettoprogmobile.model.Recensione
import com.example.progettoprogmobile.model.Track
import com.example.progettoprogmobile.model.Album
import com.example.progettoprogmobile.model.Artist
import com.example.progettoprogmobile.model.Image
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*


class FourthFragment : Fragment(),
    NotificationsAdapter.OnFollowClickListener,
    NotificationsAdapter.OnConfirmClickListener,
    NotificationsAdapter.OnDeleteClickListener {

    private val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid
    private val database = FirebaseDatabase.getInstance()
    private lateinit var recyclerView: RecyclerView
    private lateinit var notificationsAdapter: NotificationsAdapter
    private var notificationList: MutableList<NotificationItem> = mutableListOf()

    private var trackList: MutableList<Track> = mutableListOf()
    private lateinit var artistsList: MutableList<Artist>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_fourth, container, false)

        notificationList.clear()

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

        retrieveReviewsData()



        notificationsAdapter.setFollowerItemClickListener(object :
            NotificationsAdapter.FollowerViewHolder.OnClickListener {
            override fun onClick(position: Int) {
                val selectedFollower = notificationList[position] as? NotificationItem.FollowerItem
                selectedFollower?.let {
                    Log.d("MainFragment", "Follower to be passed to Follower fragment: ${it.utente}")
                    val followerSelezionatoFragment = FifthFragment()
                    val bundle = Bundle()
                    bundle.putSerializable("followerDetail", it.utente)
                    followerSelezionatoFragment.arguments = bundle
                    Log.d("MainFragment", "Follower set to FollowerSelezionato fragment: ${it.utente}")
                    val fragmentManager = requireActivity().supportFragmentManager
                    fragmentManager.beginTransaction()
                        .replace(R.id.nav_host_fragment, followerSelezionatoFragment)
                        .addToBackStack(null)
                        .commit()
                }
            }
        })


        notificationsAdapter.setRequestItemClickListener(object :
            NotificationsAdapter.RequestViewHolder.OnClickListener {
            override fun onClick(position: Int) {
                val selectedRequest = notificationList[position] as? NotificationItem.RequestItem
                selectedRequest?.let {
                    Log.d("MainFragment", "Follower to be passed to Follower fragment: ${it.utente}")
                    val followerSelezionatoFragment = FifthFragment()
                    val bundle = Bundle()
                    bundle.putSerializable("followerDetail", it.utente)
                    followerSelezionatoFragment.arguments = bundle
                    Log.d("MainFragment", "Follower set to FollowerSelezionato fragment: ${it.utente}")
                    val fragmentManager = requireActivity().supportFragmentManager
                    fragmentManager.beginTransaction()
                        .replace(R.id.nav_host_fragment, followerSelezionatoFragment)
                        .addToBackStack(null)
                        .commit()
                }
            }
        })

        notificationsAdapter.setReviewItemClickListener(object :
            NotificationsAdapter.ReviewViewHolder.OnClickListener {
            override fun onClick(position: Int) {
                val selectedReview = notificationList[position] as? NotificationItem.ReviewItem
                selectedReview?.let {
                    Log.d("MainFragment", "Track to be passed to BranoSelezionato fragment: ${it.track}")
                    val branoSelezionatoFragment = BranoSelezionato()
                    val bundle = Bundle()
                    bundle.putSerializable("trackDetail", it.track)
                    branoSelezionatoFragment.arguments = bundle
                    Log.d("MainFragment", "Track set to BranoSelezionato fragment: ${it.track}")
                    val fragmentManager = requireActivity().supportFragmentManager
                    fragmentManager.beginTransaction()
                        .replace(R.id.nav_host_fragment, branoSelezionatoFragment)
                        .addToBackStack(null)
                        .commit()
                }
            }
        })

        // Ora imposta l'adapter sulla RecyclerView
        recyclerView.adapter = notificationsAdapter

        return rootView
    }



    // Metodo per recuperare i dati dell'utente corrente
    private fun retrieveUserData() {
        val userReference = database.reference.child("users").child(currentUserId)
        userReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {

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

                Log.e("DatabaseError", "Error in retrieving data: ${databaseError.message}")

            }
        })
    }

    // Metodo per recuperare i dati di un follower specifico
    private fun retrieveFollowersData() {

        val followersReference = database.reference.child("users").child(currentUserId).child("followers")
        followersReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {

                    for (followerSnapshot in dataSnapshot.children) {
                        val followerId = followerSnapshot.key
                        if (followerId != null) {

                            retrieveFollowerData(followerId)
                        }
                    }
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
    }

    // Metodo per recuperare i dati di un follower specifico
    private fun retrieveFollowerData(followerId: String) {
        Log.d("FollowerId", "Follower ID: $followerId")

        val followerDataReference = database.reference.child("users").child(followerId)
        followerDataReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(followerDataSnapshot: DataSnapshot) {
                if (followerDataSnapshot.exists()) {

                    handleFollowerData(followerDataSnapshot)
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {

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

                }
            })
        } else {
            // Se l'utente smette di seguire, esegui le azioni necessarie
            removeUserIdFromFollowing(userId)
        }

    }

    // Metodo per aggiungere un utente alla lista "following" dell'utente corrente
    private fun addUserIdToFollowing(userId: String) {
        val followingReference = database.reference.child("users").child(currentUserId).child("following")
        followingReference.child(userId).setValue(true)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Se l'aggiunta è riuscita, aggiungi l'utente corrente alla lista "followers" dell'utente seguito
                    addCurrentUserToFollower(userId)
                } else {

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
                    // Incrementa il contatore dei follower dell'utente seguito
                    incrementFollowerCount(followedUserId)
                    // Incrementa il contatore dei following dell'utente corrente
                    incrementFollowingCount(currentUserId)
                    // Se l'aggiunta è riuscita, mostra un messaggio di successo
                    Toast.makeText(requireContext(), "Utente corrente aggiunto con successo alla lista followers dell'utente seguito", Toast.LENGTH_SHORT).show()
                } else {

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

                    removeCurrentUserFromFollower(userId)
                    Toast.makeText(requireContext(), "Utente rimosso con successo dalla lista following", Toast.LENGTH_SHORT).show()
                } else {
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
                    // Decrementa il contatore dei follower dell'utente seguito
                    decrementFollowerCount(followedUserId)
                    // Decrementa il contatore dei following dell'utente corrente
                    decrementFollowingCount(currentUserId)
                } else {
                    // Gestisci eventuali errori durante la rimozione
                    Toast.makeText(requireContext(), "Errore durante la rimozione dell'utente corrente dalla lista followers dell'utente seguito", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Metodo per incrementare il contatore dei follower di un utente
    private fun incrementFollowerCount(userId: String) {
        val userReference = database.reference.child("users").child(userId)
        userReference.child("followers counter").runTransaction(object : Transaction.Handler {
            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                val currentValue = mutableData.getValue(Int::class.java)
                mutableData.value = (currentValue ?: 0) + 1
                return Transaction.success(mutableData)
            }

            override fun onComplete(databaseError: DatabaseError?, committed: Boolean, dataSnapshot: DataSnapshot?) {
                if (!committed) {

                }
            }
        })
    }

    // Metodo per decrementare il contatore dei follower di un utente
    private fun decrementFollowerCount(userId: String) {
        val userReference = database.reference.child("users").child(userId)
        userReference.child("followers counter").runTransaction(object : Transaction.Handler {
            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                val currentValue = mutableData.getValue(Int::class.java)
                mutableData.value = (currentValue ?: 0) - 1
                return Transaction.success(mutableData)
            }

            override fun onComplete(databaseError: DatabaseError?, committed: Boolean, dataSnapshot: DataSnapshot?) {
                if (!committed) {

                }
            }
        })
    }
    // Metodo per incrementare il contatore dei following dell'utente corrente
    private fun incrementFollowingCount(userId: String) {
        val userReference = database.reference.child("users").child(userId)
        userReference.child("following counter").runTransaction(object : Transaction.Handler {
            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                val currentValue = mutableData.getValue(Int::class.java)
                mutableData.value = (currentValue ?: 0) + 1
                return Transaction.success(mutableData)
            }

            override fun onComplete(databaseError: DatabaseError?, committed: Boolean, dataSnapshot: DataSnapshot?) {
                if (!committed) {

                }
            }
        })
    }

    // Metodo per decrementare il contatore dei following dell'utente corrente
    private fun decrementFollowingCount(userId: String) {
        val userReference = database.reference.child("users").child(userId)
        userReference.child("following counter").runTransaction(object : Transaction.Handler {
            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                val currentValue = mutableData.getValue(Int::class.java)
                mutableData.value = (currentValue ?: 0) - 1
                return Transaction.success(mutableData)
            }

            override fun onComplete(databaseError: DatabaseError?, committed: Boolean, dataSnapshot: DataSnapshot?) {
                if (!committed) {

                }
            }
        })
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


                    if (followRequests.isNotEmpty()) {
                        // Recupera i dati degli utenti associati alle richieste e aggiorna la RecyclerView
                        for (userId in followRequests) {
                            retrieveUserDataForRequest(userId)
                        }

                        retrieveFollowersData()
                    }
                } else {
                    Log.d("FollowRequests", "No follow requests found")

                    retrieveFollowersData()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

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


                    Log.d("UserDataForRequest", "User Data for Request: $user")
                    notificationList.add(NotificationItem.RequestItem(user))
                    notificationsAdapter.submitList(notificationList)
                    notificationsAdapter.notifyDataSetChanged()
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("UserDataForRequest", "Error retrieving user data: ${databaseError.message}")
            }
        })
    }

    // Metodo chiamato quando si clicca su "Conferma" in una notifica
    override fun onConfirmClickListener(userId: String) {
        // Incrementa il contatore dei follower dell'utente che ha inviato la richiesta
        incrementFollowerCount(currentUserId)
        // Incrementa il contatore dei following dell'utente corrente
        incrementFollowingCount(userId)
        removeFollowRequest(userId)

        addFollower(userId)


        addUserIdToMyFollowing(userId)

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
                is NotificationItem.ReviewItem -> it.utente.userId== userId
                else ->  throw IllegalArgumentException("Tipo di item non supportato: $it")
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

                    val imageUrl = dataSnapshot.child("profile image").getValue(String::class.java)
                    val defaultImageUrl = "drawable://" + R.drawable.baseline_person_24

                    val newUser = Utente(
                        userId = userId,
                        name = dataSnapshot.child("name").getValue(String::class.java) ?: "",
                        userImage = imageUrl.takeIf { !it.isNullOrEmpty() } ?: defaultImageUrl
                    )


                    notificationList.add(NotificationItem.FollowerItem(newUser))
                    notificationsAdapter.submitList(notificationList)
                    notificationsAdapter.notifyItemInserted(notificationList.size - 1)
                    notificationsAdapter.notifyDataSetChanged()
                }
            } override fun onCancelled(databaseError: DatabaseError) {
                // Implementa l'azione da eseguire in caso di cancellazione dell'evento
                Log.e("UserDataForRequest", "Error retrieving user data: ${databaseError.message}")
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

                    requestsReference.removeValue()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

                Log.e("FollowRequests", "Error retrieving follow requests: ${databaseError.message}")
            }
        })
    }

    private fun moveRequestToFollowers(userId: String) {
        val followersReference = database.reference.child("users").child(currentUserId).child("followers")
        followersReference.child(userId).setValue(true)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    addCurrentUserToFollower(userId)
                } else {

                    Log.e("MoveRequestToFollowers", "Error moving request to followers: ${task.exception?.message}")
                }
            }
    }

    // Metodo per aggiungere un utente alla lista "following" dell'utente corrente
    private fun addUserIdToMyFollowing(userId: String) {
        val followingReference = database.reference.child("users").child(userId).child("following")
        followingReference.child(currentUserId).setValue(true)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Se l'aggiunta è riuscita, mostra un messaggio di successo
                    Toast.makeText(requireContext(), "Utente aggiunto con successo alla lista following", Toast.LENGTH_SHORT).show()
                } else {

                    Toast.makeText(requireContext(), "Errore durante l'aggiunta dell'utente alla lista following", Toast.LENGTH_SHORT).show()
                }
            }
    }


    private fun retrieveUserDataForReview(userId: String, trackId: String) {
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

                    // Recupera il nome della traccia
                    val trackName = FirebaseDatabase.getInstance().reference.child("tracks").child(trackId)
                    trackName.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(trackDataSnapshot: DataSnapshot) {
                            if (trackDataSnapshot.exists()) {
                                val trackName = trackDataSnapshot.child("trackName").value.toString()
                                // Log del nome della traccia
                                Log.d("TrackName", "Nome della traccia: $trackName")
                                val albumName = trackDataSnapshot.child("album").value.toString()
                                val imageUrl = trackDataSnapshot.child("image_url").value.toString()

                                val artistIdsSnapshot = trackDataSnapshot.child("artists")
                                val artistIds = ArrayList<String>()
                                for (artistIdSnapshot in artistIdsSnapshot.children) {
                                    val artistId = artistIdSnapshot.value.toString()
                                    artistIds.add(artistId)
                                }

                                val artistNameList = mutableListOf<String>()

                                for (artistId in artistIds) {
                                    val artistRef = database.getReference("artists").child(artistId)
                                    artistRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(artistDataSnapshot: DataSnapshot) {
                                            val artistName =
                                                artistDataSnapshot.child("name").value.toString()
                                            artistNameList.add(artistName)

                                            if (artistNameList.size == artistIds.size) {
                                                // All artist information has been retrieved
                                                val artistImages =
                                                    artistNameList.map { Image(url = it) }
                                                val artist = Artist(
                                                    id = artistId,
                                                    name = artistNameList.joinToString(),
                                                    genres = emptyList(),
                                                    images = artistImages
                                                )

                                                val albumImage = Image(url = imageUrl)
                                                val track = Track(
                                                    name = trackName,
                                                    album = Album(
                                                        name = albumName,
                                                        images = listOf(albumImage),
                                                        releaseDate = ""
                                                    ),
                                                    artists = listOf(artist),
                                                    id = trackId
                                                )

                                                notificationList.add(
                                                    NotificationItem.ReviewItem(
                                                        user,
                                                        track
                                                    )
                                                )
                                                notificationsAdapter.submitList(notificationList)
                                                notificationsAdapter.notifyDataSetChanged()

                                                Log.d(
                                                    "TrackObject",
                                                    "Track object passed to adapter: $track"
                                                )
                                            }
                                        }
                                        override fun onCancelled(databaseError: DatabaseError) {

                                            Log.e("ArtistData", "Errore nel recupero dei dati dell'artista: ${databaseError.message}")
                                        }
                                    })
                                }

                            } else {
                                Log.e("TrackName", "Nome della traccia non trovato")
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {

                            Log.e("TrackName", "Errore nel recupero del nome della traccia: ${databaseError.message}")
                        }
                    })
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

                Log.e("UserDataForReview", "Error retrieving user data: ${databaseError.message}")
            }
        })
    }

    // Metodo per recuperare le recensioni dal Realtime Database
    private fun retrieveReviewsData() {
        val reviewsReference = database.reference.child("reviews")
        reviewsReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(reviewsSnapshot: DataSnapshot) {
                if (reviewsSnapshot.exists()) {
                    // Itera sulle recensioni
                    for (reviewSnapshot in reviewsSnapshot.children) {
                        val review = reviewSnapshot.getValue(Recensione::class.java)
                        if (review != null) {
                            // Controlla se l'userId della recensione è presente nella lista "following" dell'utente corrente
                            isUserFollowing(review.userId) { isFollowing ->
                                if (isFollowing) {
                                    // Se l'utente è presente nella lista "following", recupera i dati dell'utente
                                    retrieveUserDataForReview(review.userId, review.trackId)
                                } else {
                                    // Se l'utente non è presente nella lista "following", puoi ignorare la recensione
                                    Log.d("Following", "User ${review.userId} is not following")
                                }
                            }
                        }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

                Log.e("RetrieveReviews", "Error retrieving reviews: ${databaseError.message}")
            }
        })
    }

    // Metodo per controllare se l'utente è presente nella lista "following"
    private fun isUserFollowing(userId: String, callback:(Boolean) -> Unit) {
        val followingReference = database.reference.child("users").child(currentUserId).child("following").child(userId)
        return followingReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Restituisce true se l'utente è presente nella lista "following", altrimenti restituisce false
                callback(dataSnapshot.exists())
            }

            override fun onCancelled(databaseError: DatabaseError) {

                Log.e("IsUserFollowing", "Error checking if user is following: ${databaseError.message}")
                callback(false)
            }
        })
    }
}