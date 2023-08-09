package com.example.progettoprogmobile

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