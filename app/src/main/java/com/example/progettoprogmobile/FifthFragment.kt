package com.example.progettoprogmobile

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
import com.bumptech.glide.Glide
import com.example.progettoprogmobile.adapter.TrackAdapter
import com.example.progettoprogmobile.adapter.ArtistAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import com.example.progettoprogmobile.viewModel.FirebaseViewModel
import java.util.ArrayList
import com.example.progettoprogmobile.model.Utente
import android.os.Handler
import android.os.Looper
import com.example.progettoprogmobile.model.Track
import com.example.progettoprogmobile.model.Artist
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.database.DataSnapshot

class FifthFragment : Fragment() {

    private lateinit var usernameTextView: TextView
    private lateinit var profileImageView: ImageView
    private lateinit var topTracksAdapter: TrackAdapter
    private lateinit var topArtistsAdapter: ArtistAdapter
    private val database = FirebaseDatabase.getInstance()

    private lateinit var firebaseViewModel: FirebaseViewModel
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val rootView = inflater.inflate(R.layout.fragment_fifth, container, false)
        val topTracksRecyclerView: RecyclerView = rootView.findViewById(R.id.recyclerViewUtenteTrack)
        val topArtistsRecyclerView: RecyclerView = rootView.findViewById(R.id.recyclerViewUtenteArtist)
        val backButton: Button = rootView.findViewById(R.id.backArrow)

        backButton.setOnClickListener {
            // Azione da eseguire quando il pulsante freccia viene cliccato
            requireActivity().onBackPressed() // Torna al fragment precedente
        }

        val userId: String? = arguments?.getString("userId")
        Log.d("FifthFragment", "User ID ricevuto: $userId")

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