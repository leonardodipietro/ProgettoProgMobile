package com.example.progettoprogmobile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.progettoprogmobile.R
import com.example.progettoprogmobile.adapter.TrackAdapter
import com.example.progettoprogmobile.viewModel.SpotifyViewModel
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class SecondActivity : AppCompatActivity() {

    private lateinit var viewModel: SpotifyViewModel
    // private lateinit var database: DatabaseReference
    private lateinit var recyclerView: RecyclerView
    private lateinit var trackAdapter: TrackAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)


        val startAuthButton = findViewById<Button>(R.id.startAuthButton)
        startAuthButton.setOnClickListener {
            startSpotifyAuthentication()
        }

        /*  FirebaseApp.initializeApp(this)
          database = FirebaseDatabase.getInstance().reference.child("tracks") */
        viewModel = ViewModelProvider(this)[SpotifyViewModel::class.java]
        //   viewModel.initDatabase(database) // Inizializza il database nel ViewModel
        handleIntent(intent)

        viewModel.spotifyTokenResponse.observe(this) { tokenResponse ->
            if (tokenResponse?.access_token != null) {
                Log.d("SpotifyToken", "Token ottenuto: ${tokenResponse.access_token}")

                // Una volta ottenuto il token, recupera le tracce più ascoltate
                viewModel.fetchTopTracks(tokenResponse.access_token)
            } else {
                Log.d("SpotifyToken", "Nessun token ottenuto!")
            }
        }

        viewModel.error.observe(this) { throwable ->
            Log.e("SpotifyTokenError", "Errore durante la richiesta del token", throwable)
        }

        // Recupera le ultime 50 tracce dal database Firebase e visualizzale nella RecyclerView
        viewModel.fetchTopTracksFromFirebase()

        recyclerView = findViewById(R.id.recyclerViewtopbrani)
        recyclerView.layoutManager = LinearLayoutManager(this)
        trackAdapter = TrackAdapter(emptyList()) // Inizialmente senza tracce
        recyclerView.adapter = trackAdapter
        viewModel.topTracks.observe(this) { tracksResponse ->
            if (tracksResponse != null) {
                // Aggiorna l'adapter con le tracce recuperate dal database Firebase
                trackAdapter = TrackAdapter(tracksResponse.items)
                recyclerView.adapter = trackAdapter
                trackAdapter.submitList(tracksResponse.items)

                // Salvare le tracce nel database Firebase
                tracksResponse.items.forEach { track ->
                    viewModel.saveTrackToFirebase(track)
                }
            } else {
                Log.e("TopTrackError", "Errore durante il recupero delle tracce dal database Firebase")
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.drawer_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_first_fragment -> {
                val navController = Navigation.findNavController(this, R.id.nav_host_fragment)
                navController.navigate(R.id.firstFragment)
                return true
            }
            R.id.menu_second_fragment -> {
                val navController = Navigation.findNavController(this, R.id.nav_host_fragment)
                navController.navigate(R.id.secondFragment)
                return true
            }
            R.id.menu_button_1 -> {
                startSpotifyAuthentication()
                return true
            }
            R.id.menu_button_2 -> {
                // Esegui l'azione relativa al Button 2
                // Ad esempio, avvia un'altra azione desiderata
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun startSpotifyAuthentication() {
        val authUrl = "https://accounts.spotify.com/authorize?client_id=f81649b34ef74684b08943e7ce931d23&response_type=code&redirect_uri=myapp://callback&scope=user-read-private%20user-top-read"
        val intent = Intent (Intent.ACTION_VIEW, Uri.parse(authUrl))
        startActivity(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }
    private fun handleIntent(intent: Intent?) {
        val uri = intent?.data
        val code = uri?.getQueryParameter("code")
        if (code != null) {
            viewModel.getAccessToken(code)
        }
    }
}