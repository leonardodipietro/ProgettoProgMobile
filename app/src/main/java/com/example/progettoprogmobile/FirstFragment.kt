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
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.progettoprogmobile.adapter.ArtistAdapter
import com.example.progettoprogmobile.adapter.ArtistGridAdapter
import com.example.progettoprogmobile.adapter.TrackGridAdapter
import com.example.progettoprogmobile.model.TopArtistsResponse
import com.example.progettoprogmobile.model.TopTracksResponse
import com.example.progettoprogmobile.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FirstFragment : Fragment(),TrackAdapter.OnTrackClickListener {
    private lateinit var firebaseViewModel: FirebaseViewModel
    private lateinit var spotifyViewModel: SpotifyViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var trackAdapter: TrackAdapter
    private lateinit var trackGridAdapter: TrackGridAdapter
    private lateinit var artistRecyclerView: RecyclerView
    private lateinit var artistAdapter: ArtistAdapter
    private lateinit var artistGridAdapter: ArtistGridAdapter
    private var token: String? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View?     {

//TODO SISTEMARE COLORE E STILE DEI BOTTONI
        val rootView = inflater.inflate(R.layout.fragment_first, container, false)

        setupViewModels()
        setupUIComponents(rootView)
        setupRecyclerViews(rootView)

        setupObservers()

        return rootView

    }
    private fun setupViewModels() {
        spotifyViewModel = ViewModelProvider(this).get(SpotifyViewModel::class.java)
        firebaseViewModel = ViewModelProvider(this).get(FirebaseViewModel::class.java)
        firebaseViewModel.filter = "short_term"
        firebaseViewModel.fetchTopTracksFromFirebase(firebaseViewModel.filter)
    }
    private fun setupUIComponents(rootView: View) {
        val aprimenufiltraggio: Button = rootView.findViewById(R.id.apriilmenudifiltraggio)
        aprimenufiltraggio.setOnClickListener { openfiltermenu() }

        val startAuthButton: Button = rootView.findViewById(R.id.startAuthButton)
        startAuthButton.setOnClickListener { handleStartAuthButtonClick() }

        val tracksButton: Button = rootView.findViewById(R.id.gettoptrack)
        val artistsButton: Button = rootView.findViewById(R.id.gettopartist)
        tracksButton.setOnClickListener { handleTracksButtonClick() }
        artistsButton.setOnClickListener { handleArtistsButtonClick() }

        val changeViewStyleButton: Button = rootView.findViewById(R.id.sceglicomevedere)
        changeViewStyleButton.setOnClickListener { openViewStyleDialog() }
    }

    private fun setupRecyclerViews(rootView: View) {
        recyclerView = rootView.findViewById(R.id.recyclerViewtopbrani)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())  // Linear view by default
        trackAdapter = TrackAdapter(emptyList(), this)
        trackGridAdapter = TrackGridAdapter(emptyList(), this)
        recyclerView.adapter = trackAdapter  // Setting linear adapter by default

        artistRecyclerView = rootView.findViewById(R.id.recyclerViewTopArtists)
        artistRecyclerView.layoutManager = LinearLayoutManager(requireContext())  // Linear view by default
        artistAdapter = ArtistAdapter(emptyList())
        artistGridAdapter = ArtistGridAdapter(emptyList())
        artistRecyclerView.adapter = artistAdapter  // Setting linear adapter by default
    }
    private fun setupObservers() {
        // Observers for tracks
        firebaseViewModel.topTracksfromdb.observe(viewLifecycleOwner) { tracks ->
            trackAdapter.submitList(tracks)
            trackGridAdapter.submitList(tracks)
            Log.d("LISTA RECYCLER VIEW", "LISTA INSERITA CON SUCCESSO")
        }

        // Observers for artists
        firebaseViewModel.topArtistsfromdb.observe(viewLifecycleOwner) { artists ->
            artistAdapter.submitList(artists)
            artistGridAdapter.submitList(artists)
            Log.d("LISTA RECYCLER VIEW", "LISTA INSERITA CON SUCCESSO")
        }
    }
    private fun handleStartAuthButtonClick() {
        startSpotifyAuthentication()
        observeToken()
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        token?.let { token ->
            getTopTracks(token, userId)
            getTopArtists(token, userId)
        } ?: run {
            Log.d("TrackGen", "Token is null")
        }
    }

    private fun handleTracksButtonClick() {
        artistRecyclerView.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        firebaseViewModel.fetchTopTracksFromFirebase(firebaseViewModel.filter)
    }

    private fun handleArtistsButtonClick() {
        recyclerView.visibility = View.GONE
        artistRecyclerView.visibility = View.VISIBLE
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        firebaseViewModel.fetchTopArtistsFromFirebase(firebaseViewModel.filter)
    }
    fun openViewStyleDialog() {
        val choices = arrayOf("Vista Lineare", "Vista a Griglia")
        AlertDialog.Builder(requireContext())
            .setTitle("Scegli Stile di Visualizzazione")
            .setItems(choices) { _, which ->
                when(which) {
                    0 -> { // Vista Lineare
                        recyclerView.adapter = trackAdapter
                        recyclerView.layoutManager = LinearLayoutManager(requireContext())

                        artistRecyclerView.adapter = artistAdapter
                        artistRecyclerView.layoutManager = LinearLayoutManager(requireContext())
                    }
                    1 -> { // Vista a Griglia
                        recyclerView.adapter = trackGridAdapter
                        recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
                        artistRecyclerView.adapter = artistGridAdapter
                        artistRecyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
                    }
                }
            }
            .show()
    }
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
        }
    }
    // Puoi aggiungere condizioni simili per gli artisti se anche loro hanno un comportamento simile

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
    private fun handleResponseArtists(artistsResponse: TopArtistsResponse?, userId: String, timeRange: String) {
        Log.d("TOP ITEMS", "TOP ITEMS $artistsResponse")
        if (artistsResponse != null && userId != null) {
            if (artistsResponse.items.isNotEmpty()) {
                firebaseViewModel.saveArtistsToMainNode(artistsResponse.items)
                firebaseViewModel.saveUserTopArtists(userId, artistsResponse.items, timeRange)
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