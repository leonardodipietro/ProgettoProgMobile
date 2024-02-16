package com.example.progettoprogmobile

import android.app.AlertDialog
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
import com.example.progettoprogmobile.adapter.ArtistAdapter
import com.example.progettoprogmobile.adapter.TrackGridAdapter
import com.example.progettoprogmobile.adapter.TrackAdapter
import com.example.progettoprogmobile.adapter.ArtistGridAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import com.example.progettoprogmobile.viewModel.FirebaseViewModel
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import com.example.progettoprogmobile.model.Artist
import com.example.progettoprogmobile.model.Track
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.database.DatabaseReference


class FifthFragment : Fragment(),TrackAdapter.OnTrackClickListener,
    ArtistAdapter.OnArtistClickListener {

    private lateinit var usernameTextView: TextView
    private lateinit var profileImageView: ImageView

    private lateinit var trackGridAdapter: TrackGridAdapter
    private lateinit var trackAdapter: TrackAdapter
    private lateinit var artistGridAdapter: ArtistGridAdapter
    private lateinit var artistAdapter: ArtistAdapter

    private lateinit var topTracksRecyclerView: RecyclerView
    private lateinit var topArtistsRecyclerView: RecyclerView

    private lateinit var btnTopBrani: Button
    private lateinit var btnTopArtisti: Button
    private lateinit var clessidraButton: Button
    private lateinit var vistaButton: Button
    private lateinit var backButton: Button
    private lateinit var followButton: Button

    private val database = FirebaseDatabase.getInstance()
    private lateinit var firebaseViewModel: FirebaseViewModel
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage

    private lateinit var userId: String
    private val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

    private lateinit var sharedPreferences: SharedPreferences

    private var isFollowing = false
    private var isRequestSent = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val rootView = inflater.inflate(R.layout.fragment_fifth, container, false)

        initializeViews(rootView)
        initializeViewModelAndAdapters()
        configureRecyclerViews()
        setButtonClickListeners()

        setupObservers()

        userId = arguments?.getString("userId") ?: "" //Dichiarazione al livello di classe
        //val userId: String? = arguments?.getString("userId")
        Log.d("FifthFragment", "User ID ricevuto: $userId")

        sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        countUserReviews(userId, FirebaseDatabase.getInstance().reference, rootView);
        countUserFollowers(userId, FirebaseDatabase.getInstance().reference, rootView);
        countUserFollowing(userId, FirebaseDatabase.getInstance().reference, rootView);

        // Recupera lo stile di visualizzazione
        val viewStyle = sharedPreferences.getString("viewStyle", "lineare")
        Log.d("ViewStyle", "Stile di visualizzazione recuperato: $viewStyle")
        when (viewStyle) {
            "lineare" -> {
                Log.d("ViewStyle", "Impostazione della vista lineare")
                topTracksRecyclerView.adapter = trackAdapter
                topTracksRecyclerView.layoutManager = LinearLayoutManager(requireContext())

                topArtistsRecyclerView.adapter = artistAdapter
                topArtistsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

                saveViewStyle(requireContext(), "lineare")
            }
            "griglia" -> {
                Log.d("ViewStyle", "Impostazione della vista a griglia")
                topTracksRecyclerView.adapter = trackGridAdapter
                topTracksRecyclerView.layoutManager = GridLayoutManager(requireContext(), 3)

                topArtistsRecyclerView.adapter = artistGridAdapter
                topArtistsRecyclerView.layoutManager = GridLayoutManager(requireContext(), 3)

                saveViewStyle(requireContext(), "griglia")
            }
        }

        // Recupera il filtro temporale
        val timeFilter = sharedPreferences.getString("timeFilter", "short_term") ?: "short_term"
        Log.d("TimeFilter", "Filtro temporale recuperato: $timeFilter")
        firebaseViewModel.filter = timeFilter

        firebaseViewModel.fetchTopTracksForUser(userId, timeFilter) { tracks ->
            requireActivity().runOnUiThread {
                if (isLinearViewSelected()) {
                    trackAdapter.submitList(tracks)
                } else {
                    trackGridAdapter.submitList(tracks)
                }
            }
        }

        firebaseViewModel.fetchTopArtistsForUser(userId, timeFilter) { artists ->
            requireActivity().runOnUiThread {
                if (isLinearViewSelected()) {
                    artistAdapter.submitList(artists)
                } else {
                    artistGridAdapter.submitList(artists)
                }
            }
        }

        when (sharedPreferences.getString("currentView", "brani")) {
            "brani" -> handleTracksButtonClick()
            "artisti" -> handleArtistsButtonClick()
        }

        // Chiamata per ottenere i dati e aggiornare lo stato del pulsante
        updateFollowButtonState {
            Log.d("onCreateView", "isFollowing: $isFollowing, isRequestSent: $isRequestSent")
            // Configura il listener per il click sul pulsante
            followButton.setOnClickListener {
                // In base allo stato corrente, esegui l'azione appropriata
                when {
                    isRequestSent -> {
                        Log.d("FollowButton", "Canceling follow request")
                        cancelFollowRequest() // Se la richiesta è stata inviata, annulla la richiesta
                    }
                    isFollowing -> {
                        Log.d("FollowButton", "Unfollowing user")
                        unfollowUser() // Se stai già seguendo l'utente, smetti di seguirlo
                    }
                    else -> {
                        Log.d("FollowButton", "Sending follow request")
                        sendFollowRequest() // Altrimenti, invia una richiesta di seguimento
                    }
                }
                // Dopo aver eseguito l'azione, salva lo stato del pulsante nelle preferenze condivise
                saveButtonState(isFollowing, isRequestSent)
            }
        }

        // Inizializza Firebase
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()

        // Ottieni il riferimento al nodo utente nel database Firebase
        val userReference = database.reference.child("users").child(userId ?: "")

        val reviewTextView = rootView.findViewById<TextView>(R.id.contatoreRecensioni)
        reviewTextView.setOnClickListener{
            checkPrivacySettings { _, followersPrivacy ->
                Log.d("reviewTextView", "followersPrivacy: $followersPrivacy, isFollowing: $isFollowing")
                if (!followersPrivacy) {
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
                } else if (followersPrivacy && isFollowing) {
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
                } else {
                    // Disabilita il click sui contatori
                    reviewTextView.isClickable = false
                    Log.d("ReviewFragment", "Il clic sui contatori è stato disabilitato.")
                }
            }
        }

        val followersTextView = rootView.findViewById<TextView>(R.id.contatoreFollowers)
        followersTextView.setOnClickListener{
            checkPrivacySettings { _, followersPrivacy ->
                Log.d("followersTextView", "followersPrivacy: $followersPrivacy, isFollowing: $isFollowing")
                if (!followersPrivacy) {
                    val followersFragmentFriend = FollowersFriendFragment()
                    val bundle = Bundle().apply {
                        putString("userId", userId)
                    }
                    followersFragmentFriend.arguments = bundle
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.nav_host_fragment, followersFragmentFriend)
                        .addToBackStack(null)
                        .commit()
                } else if (followersPrivacy && isFollowing) {
                    val followersFragmentFriend = FollowersFriendFragment()
                    val bundle = Bundle().apply {
                        putString("userId", userId)
                    }
                    followersFragmentFriend.arguments = bundle
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.nav_host_fragment, followersFragmentFriend)
                        .addToBackStack(null)
                        .commit()
                } else {
                    // Disabilita il click sui contatori
                    reviewTextView.isClickable = false
                }
            }
        }

        val followingTextView = rootView.findViewById<TextView>(R.id.contatoreFollowing)
        followingTextView.setOnClickListener{
            checkPrivacySettings { _, followersPrivacy ->
                Log.d("followingTextView", "followersPrivacy: $followersPrivacy, isFollowing: $isFollowing")
                if (!followersPrivacy) {
                    val followingFragmentFriend = FollowingFriendFragment()
                    val bundle = Bundle().apply {
                        putString("userId", userId)
                    }
                    followingFragmentFriend.arguments = bundle
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.nav_host_fragment, followingFragmentFriend)
                        .addToBackStack(null)
                        .commit()
                } else if (followersPrivacy && isFollowing) {
                    val followingFragmentFriend = FollowingFriendFragment()
                    val bundle = Bundle().apply {
                        putString("userId", userId)
                    }
                    followingFragmentFriend.arguments = bundle
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.nav_host_fragment, followingFragmentFriend)
                        .addToBackStack(null)
                        .commit()
                } else {
                    // Disabilita il click sui contatori
                    reviewTextView.isClickable = false
                }
            }
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

    // Configurazioni
    private fun initializeViews(rootView: View) {
        topTracksRecyclerView = rootView.findViewById(R.id.recyclerViewUtenteTrack)
        topArtistsRecyclerView = rootView.findViewById(R.id.recyclerViewUtenteArtist)

        btnTopBrani = rootView.findViewById(R.id.btn_topBrani)
        btnTopArtisti = rootView.findViewById(R.id.btn_topArtisti)

        followButton = rootView.findViewById(R.id.followButton)
        backButton = rootView.findViewById(R.id.backArrow)

        clessidraButton = rootView.findViewById(R.id.clessidra)
        vistaButton = rootView.findViewById(R.id.vista)

        usernameTextView = rootView.findViewById(R.id.usernameHeader)
        profileImageView = rootView.findViewById(R.id.userProfileImage)
    }

    private fun initializeViewModelAndAdapters() {
        // Inizializza FirebaseViewModel
        firebaseViewModel = ViewModelProvider(this).get(FirebaseViewModel::class.java)

        // Inizializzazione degli adapter delle recyclerView
        trackGridAdapter = TrackGridAdapter(emptyList(), this)
        artistGridAdapter = ArtistGridAdapter(emptyList(), this)
        trackAdapter = TrackAdapter(emptyList(), this)
        artistAdapter = ArtistAdapter(emptyList(), this)
    }

    private fun configureRecyclerViews() {
        // Configurazione iniziale delle RecyclerView
        topTracksRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        topTracksRecyclerView.adapter = trackAdapter

        topArtistsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        topArtistsRecyclerView.adapter = artistAdapter
    }

    private fun setButtonClickListeners() {
        // Imposta i click listener per i pulsanti
        btnTopBrani.setOnClickListener { handleTracksButtonClick() }
        btnTopArtisti.setOnClickListener { handleArtistsButtonClick() }
        clessidraButton.setOnClickListener { openfiltermenu() }
        vistaButton.setOnClickListener { openViewStyleDialog() }
        backButton.setOnClickListener {
            requireActivity().onBackPressed() // Torna al fragment precedente
        }
    }


    // Top brani e artisti
    private fun saveTimeFilter(context: Context, timeFilter: String) {
        val sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("timeFilter", timeFilter)
        editor.apply()
    }

    private fun saveViewStyle(context: Context, viewStyle: String) {
        val sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("viewStyle", viewStyle)
        editor.apply()
    }

    private fun saveCurrentView(context: Context, view: String) {
        //Vale per la scelta tra Artisti e Brani
        val sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("currentView", view)
        editor.apply()
    }

    // Fa si che le RecyclerView vengano aggiornate automaticamente quando i dati su Firebase cambiano
    private fun setupObservers() {
        // Observers for tracks
        firebaseViewModel.topTracksfromdb.observe(viewLifecycleOwner) { tracks ->
            trackAdapter.submitList(tracks)
            trackGridAdapter.submitList(tracks)
            Log.d("setupObservers", "LISTA TRACCE INSERITA CON SUCCESSO")
        }

        // Observers for artists
        firebaseViewModel.topArtistsfromdb.observe(viewLifecycleOwner) { artists ->
            artistAdapter.submitList(artists)
            artistGridAdapter.submitList(artists)
            Log.d("setupObservers", "LISTA ARTISTI INSERITA CON SUCCESSO")
        }
    }

    // Gestione pulsante TopBrani
    private fun handleTracksButtonClick() {
        topArtistsRecyclerView.visibility = View.GONE
        topTracksRecyclerView.visibility = View.VISIBLE

        firebaseViewModel.fetchTopTracksForUser(userId, firebaseViewModel.filter) { tracks ->

            requireActivity().runOnUiThread {
                if (isLinearViewSelected()) {
                    trackAdapter.submitList(tracks)
                } else {
                    trackGridAdapter.submitList(tracks)
                }
            }
            saveCurrentView(requireContext(), "brani")
        }
    }

    // Gestione pulsante TopArtisti
    private fun handleArtistsButtonClick() {
        topTracksRecyclerView.visibility = View.GONE
        topArtistsRecyclerView.visibility = View.VISIBLE

        firebaseViewModel.fetchTopArtistsForUser(userId, firebaseViewModel.filter) { artists ->

            requireActivity().runOnUiThread {
                if (isLinearViewSelected()) {
                    artistAdapter.submitList(artists)
                } else {
                    artistGridAdapter.submitList(artists)
                }
            }
            saveCurrentView(requireContext(), "artisti")
        }
    }

    private fun isLinearViewSelected(): Boolean {
        val viewStyle = sharedPreferences.getString("viewStyle", "lineare")
        return viewStyle == "lineare"
    }

    // Gestione pulsante della vista
    private fun openViewStyleDialog() {
        val choices = arrayOf("Vista Lineare", "Vista a Griglia")
        AlertDialog.Builder(requireContext())
            .setTitle("Scegli Stile di Visualizzazione")
            .setItems(choices) { _, which ->
                when(which) {
                    0 -> { // Vista Lineare
                        topTracksRecyclerView.adapter = trackAdapter
                        topTracksRecyclerView.layoutManager = LinearLayoutManager(requireContext())

                        topArtistsRecyclerView.adapter = artistAdapter
                        topArtistsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

                        saveViewStyle(requireContext(), "lineare")
                    }
                    1 -> { // Vista a Griglia
                        topTracksRecyclerView.adapter = trackGridAdapter
                        topTracksRecyclerView.layoutManager = GridLayoutManager(requireContext(), 3)

                        topArtistsRecyclerView.adapter = artistGridAdapter
                        topArtistsRecyclerView.layoutManager = GridLayoutManager(requireContext(), 3)

                        saveViewStyle(requireContext(), "griglia")
                    }
                }
                // Ricarica i dati dopo aver cambiato la visualizzazione
                val timeFilter = sharedPreferences.getString("timeFilter", "short_term") ?: "short_term"
                firebaseViewModel.filter = timeFilter

                firebaseViewModel.fetchTopTracksForUser(userId, timeFilter) { tracks ->
                    requireActivity().runOnUiThread {
                        if (isLinearViewSelected()) {
                            trackAdapter.submitList(tracks)
                        } else {
                            trackGridAdapter.submitList(tracks)
                        }
                    }
                }

                firebaseViewModel.fetchTopArtistsForUser(userId, timeFilter) { artists ->
                    requireActivity().runOnUiThread {
                        if (isLinearViewSelected()) {
                            artistAdapter.submitList(artists)
                        } else {
                            artistGridAdapter.submitList(artists)
                        }
                    }
                }
            }
            .show()
    }

    // Quando un brano viene cliccato naviga a BranoSelezionato
    override fun onTrackClicked(data: Any) {
        Log.d("FragmentClick", "Item clicked with data: $data")
        if (data is Track) {
            // Qui naviga verso il nuovo fragment, puoi passare "data" come argomento se necessario
            val newFragment = com.example.progettoprogmobile.BranoSelezionato()
            val bundle = Bundle()
            bundle.putSerializable("trackDetail", data)
            newFragment.arguments = bundle
            parentFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, newFragment)
                .addToBackStack(null)
                .commit()

            // Salva lo stato corrente come "brani"
            saveCurrentView(requireContext(), "brani")
        }
    }

    // Quando un brano viene cliccato naviga a ArtistaSelezionato
    override fun onArtistClicked(data: Any) {
        Log.d("FragmentClick", "Item clicked with data: $data")
        if (data is Artist) {
            // Qui naviga verso il nuovo fragment, puoi passare "data" come argomento se necessario
            val newFragment = com.example.progettoprogmobile.ArtistaSelezionato()
            val bundle = Bundle()
            bundle.putSerializable("artistdetails", data)
            newFragment.arguments = bundle
            parentFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, newFragment)
                .addToBackStack(null)
                .commit()
        }
    }

    // Gestione pulsante Clessidra
    private fun openfiltermenu() {
        val dialogView = layoutInflater.inflate(R.layout.filter_time_alertdialog, null)


        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setCancelable(true) // Permette di chiudere il dialog toccando fuori
            .create()

        // Gestione dei click per ogni scelta
        dialogView.findViewById<Button>(R.id.seelast4weeks).setOnClickListener {
            firebaseViewModel.filter = "short_term"

            firebaseViewModel.fetchTopTracksForUser(userId, firebaseViewModel.filter) { tracks ->
                saveTimeFilter(requireContext(), "short_term")
                dialog.dismiss()
                requireActivity().runOnUiThread {
                    if (isLinearViewSelected()) {
                        trackAdapter.submitList(tracks)
                    } else {
                        trackGridAdapter.submitList(tracks)
                    }
                }
            }

            firebaseViewModel.fetchTopArtistsForUser(userId, firebaseViewModel.filter) { artists ->
                saveTimeFilter(requireContext(), "short_term")
                dialog.dismiss()
                requireActivity().runOnUiThread {
                    if (isLinearViewSelected()) {
                        artistAdapter.submitList(artists)
                    } else {
                        artistGridAdapter.submitList(artists)
                    }
                }
            }
        }

        dialogView.findViewById<Button>(R.id.seelast6month).setOnClickListener {
            firebaseViewModel.filter = "medium_term"

            firebaseViewModel.fetchTopTracksForUser(userId, firebaseViewModel.filter) { tracks ->
                saveTimeFilter(requireContext(), "medium_term")
                dialog.dismiss()
                requireActivity().runOnUiThread {
                    if (isLinearViewSelected()) {
                        trackAdapter.submitList(tracks)
                    } else {
                        trackGridAdapter.submitList(tracks)
                    }
                }
            }

            firebaseViewModel.fetchTopArtistsForUser(userId, firebaseViewModel.filter) { artists ->
                saveTimeFilter(requireContext(), "medium_term")
                dialog.dismiss()
                requireActivity().runOnUiThread {
                    if (isLinearViewSelected()) {
                        artistAdapter.submitList(artists)
                    } else {
                        artistGridAdapter.submitList(artists)
                    }
                }
            }
        }

        dialogView.findViewById<Button>(R.id.seeAlltime).setOnClickListener {
            firebaseViewModel.filter = "long_term"

            firebaseViewModel.fetchTopTracksForUser(userId, firebaseViewModel.filter) { tracks ->
                saveTimeFilter(requireContext(), "long_term")
                dialog.dismiss()
                requireActivity().runOnUiThread {
                    if (isLinearViewSelected()) {
                        trackAdapter.submitList(tracks)
                    } else {
                        trackGridAdapter.submitList(tracks)
                    }
                }
            }

            firebaseViewModel.fetchTopArtistsForUser(userId, firebaseViewModel.filter) { artists ->
                saveTimeFilter(requireContext(), "long_term")
                dialog.dismiss()
                requireActivity().runOnUiThread {
                    if (isLinearViewSelected()) {
                        artistAdapter.submitList(artists)
                    } else {
                        artistGridAdapter.submitList(artists)
                    }
                }
            }
        }

        dialog.show()
    }


    // Contatore recensioni
    private fun countUserReviews(userId: String, databaseReference: DatabaseReference, rootView: View) {
        val reviewsReference = databaseReference.child("users").child(userId).child("reviews counter")

        // Aggiorna la TextView con il conteggio delle recensioni
        val reviewNumberTextView = rootView.findViewById<TextView>(R.id.reviews)
        // Aggiungi un listener per leggere il valore dal database
        reviewsReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Ottieni il valore dallo snapshot
                val reviewCount = dataSnapshot.value as? Long ?: 0
                // Aggiorna la TextView con il conteggio delle recensioni
                reviewNumberTextView.text = reviewCount.toString()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Gestisci eventuali errori
                Log.e("Firebase", "Error fetching review count: ${databaseError.message}")
            }
        })
    }

    // Contatore followers
    private fun countUserFollowers(userId: String, databaseReference: DatabaseReference, rootView: View) {
        val followersReference = databaseReference.child("users").child(userId).child("followers counter")

        // Aggiorna la TextView con il conteggio dei followers
        val followersNumberTextView = rootView.findViewById<TextView>(R.id.followers)
        // Aggiungi un listener per leggere il valore dal database
        followersReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Ottieni il valore dallo snapshot
                val reviewCount = dataSnapshot.value as? Long ?: 0
                // Aggiorna la TextView con il conteggio delle recensioni
                followersNumberTextView.text = reviewCount.toString()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Gestisci eventuali errori
                Log.e("Firebase", "Error fetching review count: ${databaseError.message}")
            }
        })
    }

    // Contatore following
    private fun countUserFollowing(userId: String, databaseReference: DatabaseReference, rootView: View) {
        val followingReference = databaseReference.child("users").child(userId).child("following counter")

        // Aggiorna la TextView con il conteggio delle recensioni
        val followingNumberTextView = rootView.findViewById<TextView>(R.id.following)
        // Aggiungi un listener per leggere il valore dal database
        followingReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Ottieni il valore dallo snapshot
                val reviewCount = dataSnapshot.value as? Long ?: 0
                // Aggiorna la TextView con il conteggio delle recensioni
                followingNumberTextView.text = reviewCount.toString()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Gestisci eventuali errori
                Log.e("Firebase", "Error fetching review count: ${databaseError.message}")
            }
        })
    }


    // Bottone segui
    // Metodo per controllare le impostazioni di privacy e impostare isFollowButtonClickable
    private fun checkPrivacySettings(callback: (everyonePrivacy: Boolean, followersPrivacy: Boolean) -> Unit) {
        val userReference = FirebaseDatabase.getInstance().reference.child("users").child(userId ?: "").child("privacy").child("account")

        userReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val everyonePrivacy = dataSnapshot.child("Everyone").getValue(Boolean::class.java)
                val followersPrivacy = dataSnapshot.child("Followers").getValue(Boolean::class.java)

                Log.d("checkPrivacySettings", "everyonePrivacy: $everyonePrivacy, followersPrivacy: $followersPrivacy")

                callback(everyonePrivacy ?: false, followersPrivacy ?: false)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Gestisci eventuali errori di lettura dal database
                Log.e("Firebase", "Errore durante il recupero delle impostazioni di privacy: ${databaseError.message}")
            }
        })
    }

    // Metodo per salvare lo stato del pulsante nelle preferenze condivise
    private fun saveButtonState(isFollowing: Boolean, isRequestSent: Boolean) {
        val sharedPreferences = requireContext().getSharedPreferences("ButtonStatePrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("isFollowing", isFollowing)
        editor.putBoolean("isRequestSent", isRequestSent)
        editor.apply()

        Log.d("saveButtonState", "isFollowing saved: $isFollowing")
    }

    // Metodo per aggiornare il testo del pulsante in base allo stato corrente
    private fun updateFollowButton(isFollowing: Boolean, isRequestSent: Boolean) {
        Log.d("updateFollowButton", "isFollowing: $isFollowing")
        when {
            isRequestSent -> followButton.text = "Richiesta inviata"
            isFollowing -> followButton.text = "Segui già"
            else -> followButton.text = "Segui"
        }
    }

    // Metodo per controllare se l'utente sta già seguendo l'utente target
    private fun updateFollowButtonState(callback: () -> Unit) {
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid!!

        // Ottieni il riferimento al nodo dell'utente target nel database Firebase
        val userReference = FirebaseDatabase.getInstance().reference.child("users").child(userId ?: "")

        userReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val followersSnapshot = dataSnapshot.child("followers")
                val requestsSnapshot = dataSnapshot.child("requests")

                val isFollowing = followersSnapshot.hasChild(currentUserUid)
                val isRequestSent = requestsSnapshot.hasChild(currentUserUid)

                Log.d("updateFollowButtonState", "isFollowing: $isFollowing, isRequestSent: $isRequestSent")
                // Dato che seguo già esce giustamente true

                // Aggiornamento dei valori isFollowing e isRequestSent
                this@FifthFragment.isFollowing = isFollowing
                this@FifthFragment.isRequestSent = isRequestSent

                updateFollowButton(isFollowing, isRequestSent)
                Log.d("dopoFollowButtonState", "isFollowing: $isFollowing")
                // Qui è ancora true e va bene

                saveButtonState(isFollowing, isRequestSent)
                Log.d("dopo2FollowButtonState", "isFollowing: $isFollowing")

                callback()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Gestisci eventuali errori di lettura dal database
                Log.e("Firebase", "Error updating follow button state: ${databaseError.message}")
            }
        })
    }

    // Metodo per inviare una richiesta di seguimento
    private fun sendFollowRequest() {
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid!!

        // Verifica lo stato della privacy
        checkPrivacySettings { everyonePrivacy, followersPrivacy ->
            if (followersPrivacy) {
                // La privacy è impostata su "followers", quindi invia una richiesta di seguimento
                val userReference = FirebaseDatabase.getInstance().reference.child("users").child(userId ?: "")
                userReference.child("requests").child(currentUserUid).setValue(true)
                    .addOnSuccessListener {
                        // Operazione di scrittura completata con successo
                        isRequestSent = true
                        updateFollowButton(isFollowing, isRequestSent)
                        // Aggiorna il pulsante nello stato delle preferenze condivise
                        saveButtonState(isFollowing, isRequestSent)
                    }
                    .addOnFailureListener { exception ->
                        // Gestisci eventuali errori
                        Log.e("Firebase", "Error sending follow request: ${exception.message}")
                    }
            } else {
                // La privacy non è impostata su "followers", quindi inizia a seguire l'utente direttamente
                startFollowingUser()
            }
        }
    }

    // Metodo per annullare una richiesta di seguimento
    private fun cancelFollowRequest() {

        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid!!

        // Cancella la richiesta di seguimento dal database Firebase
        val userReference = FirebaseDatabase.getInstance().reference.child("users").child(userId ?: "")
        userReference.child("requests").child(currentUserUid).removeValue()
            .addOnSuccessListener {
                // Operazione di cancellazione completata con successo
                isRequestSent = false
                updateFollowButton(isFollowing, isRequestSent)
                // Aggiorna il pulsante nello stato delle preferenze condivise
                saveButtonState(isFollowing, isRequestSent)
            }
            .addOnFailureListener { exception ->
                // Gestisci eventuali errori
                Log.e("Firebase", "Error canceling follow request: ${exception.message}")
            }
    }

    // Metodo per iniziare a seguire un utente
    private fun startFollowingUser() {
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid!!

        // Aggiungi l'utente alla lista dei seguaci dell'utente target nel database Firebase
        val userReference = FirebaseDatabase.getInstance().reference.child("users").child(userId ?: "")
        userReference.child("followers").child(currentUserUid).setValue(true)
            .addOnSuccessListener {
                // Operazione di aggiunta completata con successo
                isFollowing = true
                updateFollowButton(isFollowing, isRequestSent)
                // Aggiorna il pulsante nello stato delle preferenze condivise
                saveButtonState(isFollowing, isRequestSent)
            }
            .addOnFailureListener { exception ->
                // Gestisci eventuali errori
                Log.e("Firebase", "Error starting to follow user: ${exception.message}")
            }
    }

    // Metodo per eseguire l'azione di smettere di seguire un utente
    private fun unfollowUser() {
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid!!
        Log.d("UnfollowUser", "Current user ID: $currentUserUid, Target user ID: $userId")

        // Rimuovi l'utente corrente dalla lista dei seguaci dell'altro utente
        val otherUserReference = FirebaseDatabase.getInstance().reference.child("users").child(userId ?: "")
        otherUserReference.child("followers").child(currentUserUid).removeValue()
            .addOnSuccessListener {
                // Operazione di rimozione completata con successo per l'altro utente
                Log.d("UnfollowUser", "Successfully removed current user from other user's followers")
                isFollowing = false
                updateFollowButton(isFollowing, isRequestSent)
                // Aggiorna il pulsante nello stato delle preferenze condivise
                saveButtonState(isFollowing, isRequestSent)
            }
            .addOnFailureListener { exception ->
                // Gestisci eventuali errori
                Log.e("Firebase", "Error removing current user from other user's followers: ${exception.message}")
            }

        // Rimuovi l'altro utente dalla lista dei tuoi seguaci
        val currentUserReference = FirebaseDatabase.getInstance().reference.child("users").child(currentUserUid)
        currentUserReference.child("following").child(userId ?: "").removeValue()
            .addOnSuccessListener {
                // Operazione di rimozione completata con successo per l'utente corrente
                Log.d("UnfollowUser", "Successfully removed other user from current user's following")
            }
            .addOnFailureListener { exception ->
                // Gestisci eventuali errori
                Log.e("Firebase", "Error removing other user from current user's following: ${exception.message}")
            }
    }

    // Nella funzione onPause() del tuo fragment
    override fun onPause() {
        super.onPause()
        // Quando il fragment va in pausa, salva lo stato del pulsante nelle preferenze condivise
        saveButtonState(isFollowing, isRequestSent)
    }
}