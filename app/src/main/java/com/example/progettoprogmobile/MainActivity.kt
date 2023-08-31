
package com.example.progettoprogmobile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.progettoprogmobile.R
import com.example.progettoprogmobile.viewModel.SpotifyViewModel
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat


import android.app.PendingIntent
import android.widget.Button
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase



class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: SpotifyViewModel

    private lateinit var database: DatabaseReference



    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) { result ->
        onSignInResult(result)
    }


    private fun createSignInIntent() {
        // [START auth_fui_create_intent]
        // Choose authentication providers
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build()

        )


        // Create and launch sign-in intent
        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .build()

        PendingIntent.getActivity(
            this, // Context
            0, // Request code
            signInIntent,
            PendingIntent.FLAG_IMMUTABLE // Add this flag
        )

        signInLauncher.launch(signInIntent)
        // [END auth_fui_create_intent]
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        result.idpResponse
        if (result.resultCode == RESULT_OK) {
            // Successfully signed in
            FirebaseAuth.getInstance().currentUser
            // ...
        } else {
            // Sign in failed. If response is null the user canceled the
            // sign-in flow using the back button. Otherwise check
            // response.getError().getErrorCode() and handle the error.
            // ...
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val emailSignInButton = findViewById<Button>(R.id.emailSignInButton) // Cambia con l'ID effettivo del tuo pulsante di accesso tramite email
        emailSignInButton.setOnClickListener {
            createSignInIntent()
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
                }
            } else {
                Log.e("TopTrackError", "Errore durante il recupero delle tracce")
            }
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
        /*
                viewModel.topTracks.observe(this, { tracksResponse ->
                    tracksResponse?.items?.forEach { track ->
                        Log.d("TopTrack", "Track Name: ${track.name}, Album: ${track.album.name}, Artists: ${track.artists.joinToString { it.name }}")
                    }
                })
                */

    }
    fun startSpotifyAuthentication(view: View) {
        val authUrl = "https://accounts.spotify.com/authorize?client_id=f81649b34ef74684b08943e7ce931d23&response_type=code&redirect_uri=myapp://callback&scope=user-read-private%20user-top-read"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(authUrl))
        signInLauncher.launch(intent)
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





















/*package com.example.progettoprogmobile

import androidx.lifecycle.ViewModelProvider
import android.content.Intent
import android.net.Uri
import android.util.Log //per il test sul Log
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.progettoprogmobile.viewModel.SpotifyViewModel
import com.example.progettoprogmobile.R
import android.view.View
class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: SpotifyViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider(this).get(SpotifyViewModel::class.java)

        handleIntent(intent)
       //l'observer è ciò che sta dentro le parantesi
        //per il token d'accesso
        viewModel.spotifyTokenResponse.observe(this, { tokenResponse ->
            if (tokenResponse?.access_token != null) {
                Log.d("SpotifyToken", "Token ottenuto: ${tokenResponse.access_token}")
            } else {
                Log.d("SpotifyToken", "Nessun token ottenuto!")
            }


        })
        viewModel.error.observe(this, { throwable ->
            Log.e("SpotifyTokenError", "Errore durante la richiesta del token", throwable)
        })

      //per le tracce
        viewModel.topTracks.observe(this, { tracksResponse ->
            tracksResponse?.items?.forEach { track ->
                Log.d("TopTrack", "Track Name: ${track.name}, Album: ${track.album.name}, Artists: ${track.artists.joinToString { it.name }}")
            }
        })




    }

    // Resto del tuo codice (es. startSpotifyAuthentication, onNewIntent, handleIntent)
   fun startSpotifyAuthentication(view: View) {
        val authUrl = "https://accounts.spotify.com/authorize?client_id=f81649b34ef74684b08943e7ce931d23&response_type=code&redirect_uri=myapp://callback&scope=user-read-private%20user-top-read"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(authUrl))
        startActivity(intent)
    }

    fun handleIntent(intent: Intent?) {

        //viene attivata dopo che viene completata l'autorizzazione da spotify e che viene quincdei completata la callback all'app

        val uri = intent?.data
        //usiamo un metodo dell oggetto Uri di android per estarre il codice dall'indirizzo (che viene consdierato una stringa)
        //myapp://callback?code=YOUR_AUTHORIZATION_CODE_HERE. che è sempre ugauale nella sintassi nell'autorizzazione qauth
        val code = uri?.getQueryParameter("code")
        if (code != null) {
            viewModel.getAccessToken(code)
        }
    }
}

*/















/*package com.example.progettoprogmobile

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.POST
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Field
import android.content.Intent
import android.net.Uri
import android.util.Log //per il test sul Log
import android.view.View


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        handleIntent(intent)

    }

    // Questo può essere un file separato, es. SpotifyModels.kt
    data class SpotifyTokenResponse(
        val access_token: String,
        val token_type: String,
        val scope: String,
        val expires_in: Int,
        val refresh_token: String?
    )

    // Questo può essere un altro file separato, es. SpotifyApi.kt
    interface SpotifyAuthService {
        @POST("api/token")
        @FormUrlEncoded
        fun getAccessToken(
            @Field("grant_type") grantType: String = "authorization_code",
            @Field("code") code: String,
            @Field("redirect_uri") redirectUri: String,
            @Field("client_id") clientId: String,
            @Field("client_secret") clientSecret: String
        ): Call<SpotifyTokenResponse>
    }
    val retrofit = Retrofit.Builder()
        .baseUrl("https://accounts.spotify.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val spotifyService: SpotifyAuthService = retrofit.create(SpotifyAuthService::class.java)

    fun startSpotifyAuthentication(view: View) {
        val authUrl = "https://accounts.spotify.com/authorize?client_id=f81649b34ef74684b08943e7ce931d23&response_type=code&redirect_uri=myapp://callback&scope=user-read-private"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(authUrl))
        startActivity(intent)
    }
    /* override fun onNewIntent(intent: Intent?) {
         super.onNewIntent(intent)
         val uri = intent?.data
         val code = uri?.getQueryParameter("code")
         if (code != null) {
             // Usa Retrofit per richiedere il token di accesso
             getAccessToken(code)
         }
     }
 */
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val uri = intent?.data
        val code = uri?.getQueryParameter("code")
        if (code != null) {
            // Usa Retrofit per richiedere il token di accesso
            getAccessToken(code)
        }
    }






    private fun getAccessToken(code: String) {
        spotifyService.getAccessToken(
            code = code,
            redirectUri = "myapp://callback",
            clientId = "f81649b34ef74684b08943e7ce931d23",
            clientSecret = "be4412d1d08645dfafdc88dc75d7b030"
        ).enqueue(object : Callback<SpotifyTokenResponse> {
            override fun onResponse(call: Call<SpotifyTokenResponse>, response: Response<SpotifyTokenResponse>) {
                val token = response.body()?.access_token
                //parte nuova
                if (token != null) {
                    Log.d("SpotifyToken", "Token ottenuto: $token")
                } else {
                    Log.d("SpotifyToken", "Nessun token ottenuto!")
                }

            }

            override fun onFailure(call: Call<SpotifyTokenResponse>, t: Throwable) {
    // Gestisci l'errore
    Log.e("SpotifyTokenError", "Errore durante la richiesta del token", t)
}
})
}


}
*/
