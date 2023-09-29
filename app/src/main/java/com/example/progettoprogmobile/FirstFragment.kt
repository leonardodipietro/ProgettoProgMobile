package com.example.progettoprogmobile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.progettoprogmobile.adapter.TrackAdapter
import com.example.progettoprogmobile.viewModel.FirebaseViewModel
import com.example.progettoprogmobile.viewModel.SpotifyViewModel
import com.example.progettoprogmobile.model.TopTracksResponse // Assicurati che il percorso sia corretto
import com.example.progettoprogmobile.model.Track
import com.google.firebase.auth.FirebaseAuth

class FirstFragment : Fragment() {
    private lateinit var firebaseViewModel: FirebaseViewModel
    private lateinit var spotifyViewModel: SpotifyViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var trackAdapter: TrackAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        spotifyViewModel = ViewModelProvider(this)[SpotifyViewModel::class.java]
        firebaseViewModel = ViewModelProvider(this)[FirebaseViewModel::class.java]
        firebaseViewModel.fetchTopTracksFromFirebase()
        val rootView = inflater.inflate(R.layout.fragment_first, container, false)

        firebaseViewModel.fetchTopTracksFromFirebase()
        val startAuthButton: Button = rootView.findViewById(R.id.startAuthButton)

        startAuthButton.setOnClickListener {
            startSpotifyAuthentication()
        }

        gestionedati()
        // Inizializza il RecyclerView e l'adapter
        recyclerView = rootView.findViewById(R.id.recyclerViewtopbrani)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        trackAdapter = TrackAdapter(emptyList()) // Inizialmente senza tracce
        recyclerView.adapter = trackAdapter

        // Aggiorna l'adapter con i nuovi dati
        firebaseViewModel.topTracksfromdb.observe(viewLifecycleOwner) { tracks ->
            trackAdapter.submitList(tracks)
            Log.d("LISTA RECYCLER VIEW", "LISTA INSERITA CON SUCCESSO")

        }

        return rootView
    }

    private fun startSpotifyAuthentication() {
        val authUrl =
            "https://accounts.spotify.com/authorize?client_id=f81649b34ef74684b08943e7ce931d23&response_type=code&redirect_uri=myapp://callback&scope=user-read-private%20user-top-read"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(authUrl))
        activity?.startActivity(intent)
    }

    private fun gestionedati() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        // Recupera le tracce più ascoltate da Spotify
        spotifyViewModel.spotifyTokenResponse.observe(viewLifecycleOwner) { tokenResponse ->
            if (tokenResponse?.access_token != null) {
                Log.d("SpotifyToken", "Token ottenuto: ${tokenResponse.access_token}")

                // Una volta ottenuto il token, recupera le tracce più ascoltate
                spotifyViewModel.fetchTopTracks(tokenResponse.access_token)
            } else {
                Log.d("SpotifyToken", "Nessun token ottenuto!")
            }
        }

        // Gestisci gli errori nella richiesta del token
        spotifyViewModel.error.observe(viewLifecycleOwner) { throwable ->
            Log.e("SpotifyTokenError", "Errore durante la richiesta del token", throwable)
        }

        // Salva le tracce nel database Firebase e recupera le tracce da Firebase
        spotifyViewModel.topTracks.observe(viewLifecycleOwner) { tracksResponse ->
            if (tracksResponse != null && userId != null) {
                if (tracksResponse.items.isNotEmpty()) {

                    firebaseViewModel.saveTracksToFirebase(userId, tracksResponse.items)
                    firebaseViewModel.fetchTopTracksFromFirebase()
                }
            }
        }
    }

    fun handleIntent(intent: Intent?) {
        Log.d("PRIMO LOG FRAGMENT", "Handling Intent: $intent")
        val uri = intent?.data
        val code = uri?.getQueryParameter("code")
        if (code != null) {
            spotifyViewModel.getAccessToken(code)
            Log.d("secondo LOG FRAGMENT", "Handling Intent: $intent")

        }
        else
            Log.d("secondo LOG FRAGMENT", "INTENT VUOTO")
    }
}