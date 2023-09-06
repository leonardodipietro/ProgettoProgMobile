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
import com.example.progettoprogmobile.adapter.TrackAdapter
import com.example.progettoprogmobile.viewModel.SpotifyViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView

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

        // Trova il componente BottomNavigationView nel layout XML e lo assegna ad una variabile
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        // Imposta un listener per la selezione degli elementi nel BottomNavigationView
        bottomNavigationView.setOnItemSelectedListener { item ->
            // Gestisci la selezione in base all'ID dell'elemento selezionato
            when (item.itemId) {
                R.id.home -> {
                    // Trova il NavController e naviga al primo fragment
                    val navController = Navigation.findNavController(this, R.id.nav_host_fragment)
                    navController.navigate(R.id.firstFragment)
                    true // Indica che la selezione è stata gestita con successo
                }
                R.id.profile -> {
                    // Trova il NavController e naviga al secondo fragment
                    val navController = Navigation.findNavController(this, R.id.nav_host_fragment)
                    navController.navigate(R.id.secondFragment)
                    true // Indica che la selezione è stata gestita con successo
                }
                R.id.settings -> {
                    // Trova il NavController e naviga al terzo fragment
                    val navController = Navigation.findNavController(this, R.id.nav_host_fragment)
                    navController.navigate(R.id.thirdFragment)
                    true // Indica che la selezione è stata gestita con successo
                }
                else -> false // Gestione predefinita nel caso in cui l'ID dell'elemento non corrisponda a nessuno dei casi precedenti
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.drawer_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // Naviga al primo fragment quando l'opzione del menu è selezionata
            R.id.menu_first_fragment -> {
                val navController = Navigation.findNavController(this, R.id.nav_host_fragment)
                navController.navigate(R.id.firstFragment)
                return true
            }
            R.id.menu_second_fragment -> {
                // Naviga al secondo fragment quando l'opzione del menu è selezionata
                val navController = Navigation.findNavController(this, R.id.nav_host_fragment)
                navController.navigate(R.id.secondFragment)
                return true
            }
            R.id.menu_third_fragment -> {
                // Naviga al terzo fragment quando l'opzione del menu è selezionata
                val navController = Navigation.findNavController(this, R.id.nav_host_fragment)
                navController.navigate(R.id.thirdFragment)
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