package com.example.progettoprogmobile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.progettoprogmobile.viewModel.SpotifyViewModel
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase




class SecondActivity : AppCompatActivity() {

    private lateinit var viewModel: SpotifyViewModel
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)

        val startAuthButton = findViewById<Button>(R.id.startAuthButton)
        startAuthButton.setOnClickListener {
            startSpotifyAuthentication()
        }


        FirebaseApp.initializeApp(this)
        database = FirebaseDatabase.getInstance().reference.child("tracks")
        viewModel = ViewModelProvider(this)[SpotifyViewModel::class.java]

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


        viewModel.topTracks.observe(this) { tracksResponse ->
            if (tracksResponse != null) {
                tracksResponse.items.forEach { track ->
                    Log.d(
                        "TopTrack",
                        "Track Name: ${track.name}, Album: ${track.album.name}, Artists: ${track.artists.joinToString { it.name }}"
                    )

                    // Chiamare la funzione saveTrackToFirebase per salvare la traccia nel database
                    val trackInfo = Track(track.name, Album(track.album.name), track.artists.map { Artist(it.name) })
                    saveTrackToFirebase(trackInfo)
                }
            } else {
                Log.e("TopTrackError", "Errore durante il recupero delle tracce")
            }
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
    private fun saveTrackToFirebase(track: Track) {
        val trackData = hashMapOf(
            "trackName" to track.name,
            "album" to track.album.name,
            "artists" to track.artists.joinToString { it.name }
        )

        // Carica i dati nel database Firebase
        val newTrackRef = database.push()
        newTrackRef.setValue(trackData)
            .addOnSuccessListener {
                Log.d("Firebase", "Dati traccia salvati su Firebase: $trackData")
            }
            .addOnFailureListener {
                Log.e("Firebase", "Errore nel salvataggio dati traccia su Firebase: ${it.message}")
            }
    }

}

data class Track(
    val name: String,
    val album: Album,
    val artists: List<Artist>
)

data class Album(
    val name: String
)

data class Artist(
    val name: String
)
