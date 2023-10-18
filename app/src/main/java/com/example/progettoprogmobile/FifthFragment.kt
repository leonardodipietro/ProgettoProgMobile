package com.example.progettoprogmobile

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.example.progettoprogmobile.adapter.TrackAdapter
import com.example.progettoprogmobile.adapter.ArtistAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import com.example.progettoprogmobile.viewModel.FirebaseViewModel
import android.content.SharedPreferences
import java.util.ArrayList
import com.example.progettoprogmobile.model.Utente
import android.os.Handler
import android.os.Looper
import com.example.progettoprogmobile.adapter.UtenteAdapter
import com.example.progettoprogmobile.model.Track
import com.example.progettoprogmobile.model.Artist
import com.example.progettoprogmobile.utils.SettingUtils
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class FifthFragment : Fragment() {

    private lateinit var usernameTextView: TextView
    private lateinit var profileImageView: ImageView
    private lateinit var topTracksAdapter: TrackAdapter
    private lateinit var topArtistsAdapter: ArtistAdapter
    private val database = FirebaseDatabase.getInstance()

    private lateinit var firebaseViewModel: FirebaseViewModel
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage

    private lateinit var followButton: Button
    private var staSeguendoUtente = false
    private lateinit var userId: String
    private var databaseReference = FirebaseDatabase.getInstance().reference
    private val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

    private enum class ButtonState {
        ATTENDI_CONFERMA,
        INIZIA_A_SEGUIRE,
        SMETTI_DI_SEGUIRE
    }

    private var currentState = ButtonState.ATTENDI_CONFERMA
    private var isRequestAccepted = false
    private lateinit var sharedPreferences: SharedPreferences
    private var currentUserHasSentRequest = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val rootView = inflater.inflate(R.layout.fragment_fifth, container, false)
        val topTracksRecyclerView: RecyclerView = rootView.findViewById(R.id.recyclerViewUtenteTrack)
        val topArtistsRecyclerView: RecyclerView = rootView.findViewById(R.id.recyclerViewUtenteArtist)
        val backButton: Button = rootView.findViewById(R.id.backArrow)
        followButton = rootView.findViewById(R.id.addFriendButton)

        userId = arguments?.getString("userId") ?: "" //Dichiarazione al livello di classe
        //val userId: String? = arguments?.getString("userId")
        Log.d("FifthFragment", "User ID ricevuto: $userId")

        // Controlla se currentUserUid(io) sono dentro nodo Followers di userId(Amico)
        val followersRef = databaseReference
            .child("users")
            .child(userId)
            .child("followers")

        // Crea una query per trovare il nodo figlio con valore uguale a currentUserUid
        val followersCheck = followersRef.orderByValue().equalTo(true)

        // Controlla se userId(Amico) ha il nodo Followers true o false
        val privacyAmico = databaseReference.child("users")
            .child(userId)
            .child("privacy")
            .child("account")
            .child("Followers")

        privacyAmico.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val privacyAmicoValue = dataSnapshot.getValue(Boolean::class.java)
                Log.d("MyTag", "Valore di privacyAmicoValue: $privacyAmicoValue")

                // Utilizza la query per ottenere i dati da followersCheck
                followersCheck.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        // Loop attraverso i risultati della query
                        for (childSnapshot in dataSnapshot.children) {
                            val followersCheckValue = childSnapshot.getValue(Boolean::class.java)
                            Log.d("MyTag", "Valore di followersCheckValue: $followersCheckValue")

                            // Esegui le azioni desiderate se il valore è true
                            if (privacyAmicoValue == true && followersCheckValue == true) {
                                // Il valore di "Followers" è true
                                Log.d("MyTag", "Followers è true.")
                                // Esegui le azioni desiderate qui
                            } else {
                                // Il valore di "Followers" non è true
                                Log.d("MyTag", "Followers non è true.")
                            }
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Gestisci eventuali errori qui
                        Log.e(
                            "MyTag",
                            "Errore nel recupero del valore di Followers: " + databaseError.message
                        )
                    }
                })
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Gestisci eventuali errori qui per privacyAmico
                Log.e(
                    "MyTag",
                    "Errore nel recupero del valore di privacyAmico: " + databaseError.message
                )
            }
        })

        // Azione da eseguire quando il pulsante freccia viene cliccato
        backButton.setOnClickListener {
            requireActivity().onBackPressed() // Torna al fragment precedente
        }

        followButton.setOnClickListener {

            val privacyAmico = databaseReference.child("users")
                .child(userId)
                .child("privacy")
                .child("account")
                .child("Followers")

            privacyAmico.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val privacyAmicoValue = dataSnapshot.getValue(Boolean::class.java) ?: false

                    if(privacyAmicoValue) {
                        when (currentState) {
                            ButtonState.ATTENDI_CONFERMA -> {
                                // Esegui l'azione per la richiesta di amicizia
                                // Cambia lo stato del pulsante se la richiesta è stata accettata
                                currentState = ButtonState.INIZIA_A_SEGUIRE
                                // Aggiorna l'aspetto del pulsante
                                aggiornaAspettoPulsante()
                                // Esegui l'azione per inviare la richiesta di amicizia
                                inviaRichiestaAmicizia()
                            }

                            ButtonState.INIZIA_A_SEGUIRE -> {
                                // Esegui l'azione per smettere di seguire
                                currentState = ButtonState.SMETTI_DI_SEGUIRE
                                // Aggiorna l'aspetto del pulsante
                                aggiornaAspettoPulsante()
                                // Esegui l'azione per iniziare a seguire
                                iniziaASeguire()
                            }

                            ButtonState.SMETTI_DI_SEGUIRE -> {
                                // Esegui l'azione per smettere di seguire
                                currentState = ButtonState.INIZIA_A_SEGUIRE
                                // Aggiorna l'aspetto del pulsante
                                aggiornaAspettoPulsante()
                                // Esegui l'azione per smettere di seguire
                                smettiDiSeguire()
                            }
                        }
                    } else {
                        // Se Followers è false, esegui l'azione per inviare la richiesta di amicizia
                        currentState = ButtonState.INIZIA_A_SEGUIRE
                        inviaRichiestaAmicizia()
                    }
                    // Aggiorna l'aspetto del pulsante
                    aggiornaAspettoPulsante()
                }
                override fun onCancelled(error: DatabaseError) {
                    // Gestisci eventuali errori nel recupero del valore di Followers
                    Log.e("MyTag", "Errore nel recupero del valore di Followers: " + error.message)
                }
            })
        }

        // Inizializza le SharedPreferences
        sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE)

        // Carica lo stato del bottone dalle SharedPreferences, utilizza false come valore di default
        isRequestAccepted = sharedPreferences.getBoolean("isRequestAccepted", false)
        aggiornaAspettoPulsante()

        // Inizializza Firebase
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()

        // Inizializza FirebaseViewModel
        firebaseViewModel = ViewModelProvider(this).get(FirebaseViewModel::class.java)

        // Inizializza le views
        usernameTextView = rootView.findViewById(R.id.usernameHeader)
        profileImageView = rootView.findViewById(R.id.userProfileImage)

        // Ottieni il riferimento al nodo utente nel database Firebase
        val userReference = database.reference.child("users").child(userId ?: "")

        val reviewTextView = rootView.findViewById<TextView>(R.id.contatoreRecensioni)
        reviewTextView.setOnClickListener{
            val reviewFragmentFriend = ReviewFragmentFriend()
            // Passare l'oggetto Utente al tuo ReviewFragmentFriend
            val bundle = Bundle().apply {
                putString("userId", userId)
            }
            reviewFragmentFriend.arguments = bundle
            parentFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, reviewFragmentFriend)
                .addToBackStack(null)
                .commit()
        }

        val followersTextView = rootView.findViewById<TextView>(R.id.contatoreFollowers)
        followersTextView.setOnClickListener{
            val followersFragmentFriend = FollowersFriendFragment()
            val bundle = Bundle().apply {
                putString("userId", userId)
            }
            followersFragmentFriend.arguments = bundle
            parentFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, followersFragmentFriend)
                .addToBackStack(null)
                .commit()
        }

        val followingTextView = rootView.findViewById<TextView>(R.id.contatoreFollowing)
        followingTextView.setOnClickListener{
            val followingFragmentFriend = FollowingFriendFragment()
            val bundle = Bundle().apply {
                putString("userId", userId)
            }
            followingFragmentFriend.arguments = bundle
            parentFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, followingFragmentFriend)
                .addToBackStack(null)
                .commit()
        }


        // Ottieni il nome utente dell'utente attuale da Firebase
        userReference.child("name").get().addOnSuccessListener { dataSnapshot ->
            val username = dataSnapshot.value as? String
            username?.let {
                usernameTextView.text = it
                Log.d("FifthFragment", "Name: $it")
            }
        }.addOnFailureListener { exception ->
            // Gestisci l'errore
            Log.e("FifthFragment", "Errore nel recupero del nome utente: ${exception.message}")
        }

        // Ottieni l'URL dell'immagine del profilo dell'utente attuale da Firebase Storage
        userReference.child("profile image").get().addOnSuccessListener { dataSnapshot ->
            val profileImageUrl = dataSnapshot.value as? String
            profileImageUrl?.let {
                // Carica l'immagine del profilo utilizzando Glide o un'altra libreria di immagini
                Handler(Looper.getMainLooper()).post {
                    Picasso.get().load(it).into(profileImageView) //Glide.with(this).load(it).into(profileImageView)
                    Log.d("FifthFragment", "Profile Image URL: $it")
                }
            }
        }.addOnFailureListener { exception ->
            // Gestisci l'errore
            Log.e("FifthFragment", "Errore nel recupero dell'URL dell'immagine del profilo: ${exception.message}")
        }
        return rootView
    }

    private fun inviaRichiestaAmicizia() {
        // Implementa l'azione per inviare una richiesta di amicizia
        if (currentUserUid != null) {
            val requestsRef = databaseReference
                .child("users")
                .child(userId)
                .child("requests")

            // Aggiungi l'utente corrente all'elenco delle richieste nell'utente da seguire
            requestsRef.child(currentUserUid).setValue(true)
                .addOnSuccessListener {
                    Log.d("Firebase", "Richiesta di amicizia inviata all'utente con ID: $userId")
                    currentUserHasSentRequest = true
                    // Imposta lo stato della richiesta come non accettata
                    isRequestAccepted = false
                    // Salva lo stato del bottone nelle SharedPreferences
                    saveButtonState()
                    // Aggiorna lo stato del pulsante dopo l'invio della richiesta
                    aggiornaAspettoPulsante()
                }
                .addOnFailureListener {
                    // Gestisci eventuali errori durante l'invio della richiesta di amicizia
                    Log.e("Firebase", "Errore durante l'invio della richiesta di amicizia: $it")
                }
        }
    }

    private fun saveButtonState() {
        // Salva lo stato del bottone nelle SharedPreferences
        val editor = sharedPreferences.edit()
        editor.putBoolean("isRequestAccepted", isRequestAccepted)
        editor.apply()
    }

    // Funzioni per seguire e smettere di seguire
    private fun iniziaASeguire() {
        if (isRequestAccepted && currentUserUid != null) {

            val usersReference = databaseReference.child("users")
            val requestsRef = usersReference.child(userId).child("requests").child(currentUserUid)
            // La richiesta di amicizia è stata accettata, rimuovi l'utente corrente dalle richieste pendenti
            requestsRef.removeValue()
                .addOnSuccessListener {
                    Log.d("Firebase", "Richiesta di amicizia accettata. Richiesta rimossa.")
                }
                .addOnFailureListener {
                    // Gestisci eventuali errori durante la rimozione della richiesta di amicizia
                    Log.e("Firebase", "Errore durante la rimozione della richiesta di amicizia: $it")
                }

            // Esegui l'azione per iniziare a seguire
            databaseReference.child("users").child(userId).child("followers").child(currentUserUid).setValue(true)
                .addOnSuccessListener {
                    staSeguendoUtente = true
                    aggiornaAspettoPulsante()

                    // Imposta lo stato della richiesta come accettata
                    isRequestAccepted = true

                    // Salva lo stato del bottone nelle SharedPreferences
                    saveButtonState()

                    Log.d("Firebase", "Hai iniziato a seguire l'utente con ID: $userId")
                }
                .addOnFailureListener {
                    // Gestisci eventuali errori durante l'aggiunta dell'utente come seguace
                    Log.e("Firebase", "Errore durante l'aggiunta dell'utente come seguace: $it")
                }
            databaseReference.child("users").child(currentUserUid).child("following").child(userId).setValue(true)
        } else {
            // La richiesta di amicizia non è stata ancora accettata, mostra un messaggio all'utente
            Log.d("Firebase", "La richiesta di amicizia è ancora in attesa di accettazione.")
            // Puoi anche mostrare un toast o un avviso per informare l'utente
        }
    }

    private fun smettiDiSeguire() {
        if (currentUserUid != null) {
            val usersReference = databaseReference.child("users")
            val requestsRef = usersReference.child(userId).child("requests").child(currentUserUid)
            // Rimuovi l'utente corrente dalle richieste pendenti
            requestsRef.removeValue()
                .addOnSuccessListener {
                    Log.d("Firebase", "Richiesta di amicizia accettata. Richiesta rimossa.")
                }
                .addOnFailureListener {
                    // Gestisci eventuali errori durante la rimozione della richiesta di amicizia
                    Log.e("Firebase", "Errore durante la rimozione della richiesta di amicizia: $it")
                }

            // Rimuovi l'utente corrente dall'elenco dei seguaci dell'utente da smettere di seguire
            databaseReference.child("users").child(userId).child("followers").child(currentUserUid).removeValue()
                .addOnSuccessListener {
                    staSeguendoUtente = false
                    aggiornaAspettoPulsante()
                    Log.d("Firebase", "Hai smesso di seguire l'utente con ID: $userId")
                }
                .addOnFailureListener {
                    // Gestisci eventuali errori durante la rimozione dell'utente come seguace
                    Log.e("Firebase", "Errore durante la rimozione dell'utente come seguace: $it")
                }
            databaseReference.child("users").child(currentUserUid).child("following").child(userId).removeValue()
        }
    }

    private fun aggiornaAspettoPulsante() {

        //isRequestAccepted = currentUserHasSentRequest // Aggiorna isRequestAccepted

        if (isRequestAccepted) {
            followButton.text = "Following"
            followButton.setBackgroundResource(R.drawable.unfollow_button_background)
        } else if (currentUserHasSentRequest) {
            followButton.text = "Request Sent"
            followButton.setBackgroundResource(R.drawable.pending_button_background)
        } else {
            followButton.text = "Follow"
            followButton.setBackgroundResource(R.drawable.follow_button_background)
        }
    }
}

/*
// Crea un elenco vuoto di oggetti Track e Artist
        val topTracksList: ArrayList<String> = ArrayList()
        val topArtistsList: ArrayList<String> = ArrayList()

// Crea gli adapter e passa un'istanza del listener
        topTracksAdapter = TrackAdapter(emptyList(), object : TrackAdapter.OnTrackClickListener {
            override fun onTrackClicked(data: Any) {
                // Gestisci l'evento di clic sulla traccia
            }
        })

        topArtistsAdapter = ArtistAdapter(emptyList())


        // Inizializza gli adapter con una lista vuota
//        topTracksAdapter = TrackAdapter(emptyList())
//        topArtistsAdapter = ArtistAdapter(emptyList())

        // Verifica se gli adapter sono nulli prima di assegnarli alla RecyclerView
        if (topTracksAdapter != null) {
            topTracksRecyclerView.adapter = topTracksAdapter
            topTracksRecyclerView.layoutManager = LinearLayoutManager(context)
        }

        if (topArtistsAdapter != null) {
            topArtistsRecyclerView.adapter = topArtistsAdapter
            topArtistsRecyclerView.layoutManager = LinearLayoutManager(context)
        }

        // Richiama fetchUserDataFromFirebase passando un listener
        firebaseViewModel.fetchUserDataFromFirebase(userId ?: "", object : FirebaseViewModel.OnUserFetchedListener {
            override fun onUserFetched(utente: Utente) {
                // Esegui le azioni desiderate con l'oggetto Utente ricevuto
                // Ad esempio, puoi aggiornare direttamente le tue views qui

                // Aggiorna i dati negli adapter e notifica i cambiamenti
//                topTracksAdapter.submitList(utente.topTracks)
//                topArtistsAdapter.submitList(utente.topArtists)

                // Notifica all'adapter che i dati sono stati aggiornati
                topTracksAdapter.notifyDataSetChanged()
                topArtistsAdapter.notifyDataSetChanged()
            }
        })

// Riferimento al nodo delle top track nel database Firebase
        val topTracksReference = database.reference.child("top_tracks")

        // Ottieni la lista delle top track da Firebase
        topTracksReference.get().addOnSuccessListener { dataSnapshot ->
            // Itera attraverso i dati e ottieni le top track
            for (trackSnapshot in dataSnapshot.children) {
                val trackName = trackSnapshot.child("track_name").getValue(String::class.java)
                // Aggiungi il nome della top track alla lista
                trackName?.let {
                    topTracksList.add(it)
                }
                Log.d("FifthFragment", "Top Tracks List: $topTracksList")
            }
            // Notifica all'adapter che i dati sono stati aggiornati
            topTracksAdapter.notifyDataSetChanged()

            // Ottieni la RecyclerView per le top track
            val topTracksRecyclerView: RecyclerView = rootView.findViewById(R.id.recyclerViewUtenteTrack)
            topTracksRecyclerView.adapter = topTracksAdapter
            topTracksRecyclerView.layoutManager = LinearLayoutManager(context)
        }

        // Riferimento al nodo delle top artist nel database Firebase
        val topArtistsReference = database.reference.child("top_artists")

        // Ottieni la lista delle top artist da Firebase
        topArtistsReference.get().addOnSuccessListener { dataSnapshot ->
            // Itera attraverso i dati e ottieni le top artist
            for (artistSnapshot in dataSnapshot.children) {
                val artistName = artistSnapshot.child("artist_name").getValue(String::class.java)
                // Aggiungi il nome della top artist alla lista
                artistName?.let {
                    topArtistsList.add(it)
                }
                Log.d("FifthFragment", "Top Artists List: $topArtistsList")
            }
            // Notifica all'adapter che i dati sono stati aggiornati
            topArtistsAdapter.notifyDataSetChanged()

            // Ottieni la RecyclerView per gli artisti
            val topArtistsRecyclerView: RecyclerView = rootView.findViewById(R.id.recyclerViewUtenteArtist)
            topArtistsRecyclerView.adapter = topArtistsAdapter
            topArtistsRecyclerView.layoutManager = LinearLayoutManager(context)
        }
 */