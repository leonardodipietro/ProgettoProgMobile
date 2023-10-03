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
import com.google.firebase.auth.FirebaseAuth
import androidx.lifecycle.lifecycleScope
import com.example.progettoprogmobile.adapter.ArtistAdapter
import com.example.progettoprogmobile.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FirstFragment : Fragment() {
    private lateinit var firebaseViewModel: FirebaseViewModel
    private lateinit var spotifyViewModel: SpotifyViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var trackAdapter: TrackAdapter
    private lateinit var artistRecyclerView: RecyclerView
    private lateinit var artistAdapter: ArtistAdapter
    private var token: String? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View?     {


        spotifyViewModel = ViewModelProvider(this).get(SpotifyViewModel::class.java)
        firebaseViewModel = ViewModelProvider(this).get(FirebaseViewModel::class.java)

//        spotifyViewModel = ViewModelProvider(this)[SpotifyViewModel::class.java]
//        firebaseViewModel = ViewModelProvider(this)[FirebaseViewModel::class.java]

        val rootView = inflater.inflate(R.layout.fragment_first, container, false)

        val startAuthBotton:Button=rootView.findViewById(R.id.startAuthButton)
        startAuthBotton.setOnClickListener {
            startSpotifyAuthentication()
            observeToken()
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener
            token?.let { token ->

                // Richiamo i metodi per ottenere i TopTracks e TopArtists da Spotify
                getTopTracks(token, userId)
                getTopArtist(token, userId)
            } ?: run {
                Log.d("TrackGen", "Token is null")
            }

        }

        val tracksButton: Button = rootView.findViewById(R.id.gettoptrack)
        val artistsButton: Button = rootView.findViewById(R.id.gettopartist)
        tracksButton.setOnClickListener {

            artistRecyclerView.visibility = View.GONE // Nasconde la RecyclerView degli artisti
            recyclerView.visibility = View.VISIBLE // Mostra la RecyclerView delle tracce
        //    firebaseViewModel.fetchTopTracksFromFirebase()

        }

        artistsButton.setOnClickListener {

            recyclerView.visibility = View.GONE // Nasconde la RecyclerView delle tracce
            artistRecyclerView.visibility = View.VISIBLE // Mostra la RecyclerView degli artisti
           // firebaseViewModel.fetchTopArtistsFromFirebase()
        }


        //gestionedati()
        // Inizializza il RecyclerView e l'adapter
      /*  recyclerView = rootView.findViewById(R.id.recyclerViewtopbrani)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        trackAdapter = TrackAdapter(emptyList()) // Inizialmente senza tracce
        recyclerView.adapter = trackAdapter

        // Aggiorna l'adapter con i nuovi dati
        firebaseViewModel.topTracksfromdb.observe(viewLifecycleOwner) { tracks ->
            trackAdapter.submitList(tracks)
            Log.d("LISTA RECYCLER VIEW","LISTA INSERITA CON SUCCESSO")
        }

        firebaseViewModel.fetchTopTracksFromFirebase()*/
        // Inizializza il RecyclerView e l'adapter
        recyclerView = rootView.findViewById(R.id.recyclerViewtopbrani)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        trackAdapter = TrackAdapter(emptyList()) // Inizialmente senza tracce
        recyclerView.adapter = trackAdapter

        // Aggiorna l'adapter con i nuovi dati
        firebaseViewModel.topTracksfromdb.observe(viewLifecycleOwner) { tracks ->
            trackAdapter.submitList(tracks)
            Log.d("LISTA RECYCLER VIEW","LISTA INSERITA CON SUCCESSO")
        }

        // Inizializza il RecyclerView e l'adapter degli artisti
        artistRecyclerView = rootView.findViewById(R.id.recyclerViewTopArtists)
        artistRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        artistAdapter = ArtistAdapter(emptyList()) // Inizialmente senza artisti
        artistRecyclerView.adapter = artistAdapter
        firebaseViewModel.topArtistsfromdb.observe(viewLifecycleOwner){ artists ->
            artistAdapter.submitList(artists)
            Log.d("LISTA RECYCLER VIEW","LISTA INSERITA CON SUCCESSO")
        }
        return rootView
    }

    private fun startSpotifyAuthentication() {
        val authUrl = "https://accounts.spotify.com/authorize?client_id=f81649b34ef74684b08943e7ce931d23&response_type=code&redirect_uri=myapp://callback&scope=user-read-private%20user-top-read"
        val intent = Intent (Intent.ACTION_VIEW, Uri.parse(authUrl))
        activity?.startActivity(intent)
    }

   private fun observeToken() {
       spotifyViewModel.spotifyTokenResponse.observe(viewLifecycleOwner) { tokenResponse ->
           tokenResponse?.access_token?.let { accessToken ->
               token = accessToken
               Log.d("SpotifyToken", "Token ottenuto: $accessToken")
           } ?: run {
               Log.d("SpotifyToken", "Token non ottenuto")
           }
       }
   }
    private fun getTopTracks(token: String, userId: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                spotifyViewModel.fetchTopTracks(token)
            }
            spotifyViewModel.fetchTopTracks(token)
            spotifyViewModel.topTracks.observe(viewLifecycleOwner) { tracksResponse ->
                Log.d("TOP ITEMS", "TOP ITEMS $tracksResponse")
                if (tracksResponse != null && userId != null) {
                    if (tracksResponse.items.isNotEmpty()) {
                        firebaseViewModel.saveTracksToMainNode( tracksResponse.items)
                        firebaseViewModel.saveUserTopTracks(userId,tracksResponse.items)
                       // firebaseViewModel.saveTrackIdsToUser(userId,tracksResponse.items)
                        //firebaseViewModel.fetchTopTracksFromFirebase()
                    }
                }
            }
        }

    }


    private fun getTopArtist(token: String,userId: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                spotifyViewModel.fetchTopArtists(token)
            }
            spotifyViewModel.fetchTopArtists(token)
            spotifyViewModel.topArtists.observe(viewLifecycleOwner) { artistsResponse ->
                // Il tuo codice per gestire gli artisti recuperati.
                Log.d("TOP ITEMS","TOP ITEMS $artistsResponse")
                if (artistsResponse != null && userId != null) {
                    if (artistsResponse.items.isNotEmpty()) {
                        Log.d("SaveArtistsDebug", "saveArtistsToFirebase sta per essere chiamato da [NomeDelMetodoODellaFunzione]")
                     firebaseViewModel.saveArtistsToMainNode(artistsResponse.items)
                        firebaseViewModel.saveUserTopArtists(userId,artistsResponse.items)


                    //   firebaseViewModel.saveArtistsToFirebase(userId, artistsResponse.items)
                        //firebaseViewModel.fetchTopArtistsFromFirebase()
                        // firebaseViewModel.getSpecificTrackInfoById("20")*/
                    }
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
/* spotifyViewModel.trackInfoExtracted.observe(viewLifecycleOwner) { extracted ->

        if (extracted == true) {
            val trackInfo = spotifyViewModel.getSpecificTrackInfo(20)
            if (trackInfo != null) {
                // Puoi accedere alle informazioni sulla traccia, ad esempio:
                val trackName = trackInfo.name
                val album = trackInfo.album
                val artists = trackInfo.artists
                val genres = trackInfo.genres
                val releaseDate = trackInfo.release_date
                val durationMs = trackInfo.duration_ms
                Log.d("TrackInfo", "Nome della traccia: $trackName")
                Log.d("TrackInfo", "Album: $album")
                Log.d("TrackInfo", "Artisti: $artists")
                Log.d("TrackInfo", "Generi: $genres")
                Log.d("TrackInfo", "Data di rilascio: $releaseDate")
                Log.d("TrackInfo", "Durata (ms): $durationMs")

                // Esegui le operazioni desiderate con queste informazioni
            } else {
                Log.d("TrackInfo", "Nessun ID di traccia trovato.")
            }
        }*/


/* private fun gestionedati() {
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
 }*/