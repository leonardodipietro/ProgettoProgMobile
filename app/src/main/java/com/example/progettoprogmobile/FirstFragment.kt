package com.example.progettoprogmobile

import android.app.AlertDialog
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
import com.example.progettoprogmobile.model.TopArtistsResponse
import com.example.progettoprogmobile.model.TopTracksResponse
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

//TODO SISTEMARE COLORE E STILE DEI BOTTONI
        spotifyViewModel = ViewModelProvider(this).get(SpotifyViewModel::class.java)
        firebaseViewModel = ViewModelProvider(this).get(FirebaseViewModel::class.java)

//        spotifyViewModel = ViewModelProvider(this)[SpotifyViewModel::class.java]
//        firebaseViewModel = ViewModelProvider(this)[FirebaseViewModel::class.java]

        val rootView = inflater.inflate(R.layout.fragment_first, container, false)
        firebaseViewModel.filter="short_term"
        val aprimenufiltraggio: Button = rootView.findViewById(R.id.apriilmenudifiltraggio)
        aprimenufiltraggio.setOnClickListener {
            // Mostra l'AlertDialog quando il pulsante viene premuto
            openfiltermenu()
        }


        val startAuthBotton:Button=rootView.findViewById(R.id.startAuthButton)
        startAuthBotton.setOnClickListener {
            startSpotifyAuthentication()
            observeToken()
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener
            token?.let { token ->

                // Richiamo i metodi per ottenere i TopTracks e TopArtists da Spotify
                getTopTracks(token, userId)
                getTopArtists(token, userId)
            } ?: run {
                Log.d("TrackGen", "Token is null")
            }

        }

        val tracksButton: Button = rootView.findViewById(R.id.gettoptrack)
        val artistsButton: Button = rootView.findViewById(R.id.gettopartist)
        tracksButton.setOnClickListener {

            artistRecyclerView.visibility = View.GONE // Nasconde la RecyclerView degli artisti
            recyclerView.visibility = View.VISIBLE // Mostra la RecyclerView delle tracce
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener
            firebaseViewModel.fetchTopTracksFromFirebase(firebaseViewModel.filter)

        }

        artistsButton.setOnClickListener {

            recyclerView.visibility = View.GONE // Nasconde la RecyclerView delle tracce
            artistRecyclerView.visibility = View.VISIBLE // Mostra la RecyclerView degli artisti
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener
            firebaseViewModel.fetchTopArtistsFromFirebase(firebaseViewModel.filter)
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
*/

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


    private fun openfiltermenu() {
        val dialogView = layoutInflater.inflate(R.layout.filter_time_alertdialog, null)


        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setCancelable(true) // Permette di chiudere il dialog toccando fuori
            .create()

        // Gestione dei click per ogni scelta
        dialogView.findViewById<Button>(R.id.seelast4weeks).setOnClickListener {
            firebaseViewModel.filter = "short_term"
            firebaseViewModel.fetchTopTracksFromFirebase(firebaseViewModel.filter)
            firebaseViewModel.fetchTopArtistsFromFirebase(firebaseViewModel.filter)
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.seelast6month).setOnClickListener {
            firebaseViewModel.filter = "medium_term"
            firebaseViewModel.fetchTopTracksFromFirebase(firebaseViewModel.filter)
            firebaseViewModel.fetchTopArtistsFromFirebase(firebaseViewModel.filter)
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.seeAlltime).setOnClickListener {
            firebaseViewModel.filter = "long_term"
            firebaseViewModel.fetchTopTracksFromFirebase(firebaseViewModel.filter)
            firebaseViewModel.fetchTopArtistsFromFirebase(firebaseViewModel.filter)
            dialog.dismiss()
        }

        dialog.show()
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
        val timeRanges = listOf("short_term", "medium_term", "long_term")

        viewLifecycleOwner.lifecycleScope.launch {
            for (timeRange in timeRanges) {
                withContext(Dispatchers.IO) {
                    spotifyViewModel.fetchTopTracks(token, timeRange)
                }

                when (timeRange) {
                    "short_term" -> {
                        spotifyViewModel.shortTermTracks.observe(viewLifecycleOwner) { response ->
                            handleResponseTrack(response, userId, timeRange)
                        }
                    }
                    "medium_term" -> {
                        spotifyViewModel.mediumTermTracks.observe(viewLifecycleOwner) { response ->
                            handleResponseTrack(response, userId, timeRange)
                        }
                    }
                    "long_term" -> {
                        spotifyViewModel.longTermTracks.observe(viewLifecycleOwner) { response ->
                            handleResponseTrack(response, userId, timeRange)
                        }
                    }
                }
            }
        }
    }

    private fun handleResponseTrack(tracksResponse: TopTracksResponse?, userId: String, timeRange: String) {
        Log.d("TOP ITEMS", "TOP ITEMS $tracksResponse")
        if (tracksResponse != null && userId != null) {
            if (tracksResponse.items.isNotEmpty()) {
                firebaseViewModel.saveTracksToMainNode(tracksResponse.items)
                firebaseViewModel.saveUserTopTracks(userId, tracksResponse.items, timeRange)
            }
        }
    }

    private fun getTopArtists(token: String, userId: String) {
        val timeRanges = listOf("short_term", "medium_term", "long_term")

        viewLifecycleOwner.lifecycleScope.launch {
            for (timeRange in timeRanges) {
                withContext(Dispatchers.IO) {
                    spotifyViewModel.fetchTopArtists(token, timeRange)
                }

                when (timeRange) {
                    "short_term" -> {
                        spotifyViewModel.shortTermArtists.observe(viewLifecycleOwner) { artistresponse ->
                            handleResponseArtists(artistresponse, userId, timeRange)
                        }
                    }
                    "medium_term" -> {
                        spotifyViewModel.mediumTermArtists.observe(viewLifecycleOwner) { artistresponse ->
                            handleResponseArtists(artistresponse, userId, timeRange)
                        }
                    }
                    "long_term" -> {
                        spotifyViewModel.longTermArtists.observe(viewLifecycleOwner) { artistresponse ->
                            handleResponseArtists(artistresponse, userId, timeRange)
                        }
                    }
                }
            }
        }
    }
    private fun handleResponseArtists(artistsResponse: TopArtistsResponse?, userId: String, timeRange: String) {
        Log.d("TOP ITEMS", "TOP ITEMS $artistsResponse")
        if (artistsResponse != null && userId != null) {
            if (artistsResponse.items.isNotEmpty()) {
                firebaseViewModel.saveArtistsToMainNode(artistsResponse.items)
                firebaseViewModel.saveUserTopArtists(userId, artistsResponse.items, timeRange)
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
/*GPT-4
User
sto facendo un applicazione mobile in kotlin che visualizza len statistiche di spotify come le toptracks, ho configurato le api e ho salvato le tracce sul database filtrate per tempo e funziona tutto, ora devo recuperare queste tracce,....................................la mia idea per recuperare le tracce filtate per tempo è quella di usare un bottone e al click ti prende le tracce e le mette nella recycler view         tracksButton.setOnClickListener {

            artistRecyclerView.visibility = View.GONE // Nasconde la RecyclerView degli artisti
            recyclerView.visibility = View.VISIBLE // Mostra la RecyclerView delle tracce
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener
            firebaseViewModel.fetchTopTracksFromFirebase()

        }in questo modo.......... fun fetchTopTracksFromFirebase() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val userTopTracksRef = database.child("users").child(userId).child("topTracks")
            userTopTracksRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val trackIds = snapshot.children.mapNotNull { it.getValue(String::class.java) }
                    retrieveTracksDetails(trackIds)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseError", "Errore durante la lettura degli IDs delle tracce dal database Firebase: ${error.message}")
                }
            })
        }
    }
con questa funzione io recuperavo le tracce e le mettevo qui dentro         // Inizializza il RecyclerView e l'adapter
        recyclerView = rootView.findViewById(R.id.recyclerViewtopbrani)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        trackAdapter = TrackAdapter(emptyList()) // Inizialmente senza tracce
        recyclerView.adapter = trackAdapter

        // Aggiorna l'adapter con i nuovi dati
        firebaseViewModel.topTracksfromdb.observe(viewLifecycleOwner) { tracks ->
            trackAdapter.submitList(tracks)
            Log.d("LISTA RECYCLER VIEW","LISTA INSERITA CON SUCCESSO")
        }.................................tuttavia questa funzione prendeva le tracce dal nodo toptracks e le buttava dentro indistintamente senza considerare i nuovi tre nodi figli di toptracks che sono longterm shortterm e mediumterm........................ora io ho implementato un bottone che mi apre una alert dialog dove l utente sceglie quale filtraggio applicare..............solo che adesso non so come posso fare per far si che quando l utente seleziona il filtraggio shortterm e poi vada a cliccare il bottone delle toptracks gli escano le topotrackshort term,,,,,,,,,,,non riewsco a collegare questo.............ovviamente deve stare tutto nello stesso fragment eh..................................
        val aprimenufiltraggio: Button = rootView.findViewById(R.id.apriilmenudifiltraggio)
        aprimenufiltraggio.setOnClickListener {
            // Mostra l'AlertDialog quando il pulsante viene premuto
            openfiltermenu()
        }questo èil codice del mio filtraggio    private fun openfiltermenu() {
        val dialogView = layoutInflater.inflate(R.layout.filter_time_alertdialog, null)

        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setCancelable(true) // Permette di chiudere il dialog toccando fuori
            .create()

        // Gestione dei click per ogni scelta
        dialogView.findViewById<Button>(R.id.seelast4weeks).setOnClickListener {

            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.seelast6month).setOnClickListener {
            // Logica per la Scelta 2
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.seeAlltime).setOnClickListener {
            // Logica per la Scelta 3
            dialog.dismiss()
        }

        dialog.show()
    }implementa la logica di cui abbiamo discusso qui dentro......ricordati che fetch toptracks è in un viewmodel*/
