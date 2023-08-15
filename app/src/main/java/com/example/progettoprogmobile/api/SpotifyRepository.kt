package com.example.progettoprogmobile.api


import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Field
import retrofit2.http.Header
import com.example.progettoprogmobile.model.SpotifyTokenResponse
import com.example.progettoprogmobile.model.TopTracksResponse
import android.util.Log
//TODO inserire questa classe all'interno del model
class SpotifyRepository {

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://accounts.spotify.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()


    private val apiRetrofit = Retrofit.Builder()
        .baseUrl("https://api.spotify.com/v1/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val spotifyService: SpotifyAuthService = retrofit.create(SpotifyAuthService::class.java)
    private val spotifyApiService: SpotifyApiService = apiRetrofit.create(SpotifyApiService::class.java)
    fun getAccessToken(
        code: String,
        redirectUri: String,
        clientId: String,
        clientSecret: String,
        callback: (response: SpotifyTokenResponse?, error: Throwable?) -> Unit
    ) {
        spotifyService.getAccessToken("authorization_code",code, redirectUri, clientId,clientSecret ).enqueue(object : Callback<SpotifyTokenResponse> {
            override fun onResponse(call: Call<SpotifyTokenResponse>, response: Response<SpotifyTokenResponse>) {
                callback(response.body(), null)
            }

            override fun onFailure(call: Call<SpotifyTokenResponse>, t: Throwable) {
                callback(null, t)
            }
        })
    }

    fun getTopTracks(token: String, callback: (response: TopTracksResponse?, error: Throwable?) -> Unit) {
        Log.d("SpotifyRepo", "Chiamata getTopTracks iniziata con il token: $token")  // Log per tracciare l'inizio della chiamata

        spotifyApiService.getTopTracks("Bearer $token").enqueue(object : Callback<TopTracksResponse> {
            override fun onResponse(call: Call<TopTracksResponse>, response: Response<TopTracksResponse>) {
                if (response.isSuccessful) {
                    Log.d("SpotifyRepo", "Risposta ricevuta con successo: ${response.body()}")  // Log in caso di risposta di successo
                } else {
                    Log.d("SpotifyRepo", "Risposta con errore. Codice: ${response.code()}, Messaggio: ${response.message()}")  // Log in caso di risposta con codice d'errore
                }
                callback(response.body(), null)
            }

            override fun onFailure(call: Call<TopTracksResponse>, t: Throwable) {
                Log.e("SpotifyRepo", "Errore durante la chiamata getTopTracks", t)  // Log in caso di errore di chiamata
                callback(null, t)
            }
        })
    }

    /*fun getTopTracks(token: String, callback: (response: TopTracksResponse?, error: Throwable?) -> Unit) {
        spotifyService.getTopTracks("Bearer $token").enqueue(object : Callback<TopTracksResponse> {
            override fun onResponse(call: Call<TopTracksResponse>, response: Response<TopTracksResponse>) {
                callback(response.body(), null)
            }

            override fun onFailure(call: Call<TopTracksResponse>, t: Throwable) {
                callback(null, t)
            }
        })
    }
*/

    interface SpotifyAuthService {
        @POST("api/token")
        @FormUrlEncoded
        //unico metodo che usiamo per il momento a livello di api
        fun getAccessToken(
            @Field("grant_type") grantType: String = "authorization_code",
            @Field("code") code: String,
            @Field("redirect_uri") redirectUri: String,
            @Field("client_id") clientId: String,
            @Field("client_secret") clientSecret: String
        ): Call<SpotifyTokenResponse>


    }

    interface SpotifyApiService {
        @GET("me/top/tracks?time_range=short_term&limit=50")
        fun getTopTracks(@Header("Authorization") authToken: String): Call<TopTracksResponse>
    }
}
