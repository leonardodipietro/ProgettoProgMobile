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
import com.example.progettoprogmobile.model.TopArtistsResponse
import android.util.Log
import com.example.progettoprogmobile.model.AddTracksBody
import com.example.progettoprogmobile.model.CreatePlaylistBody
import com.example.progettoprogmobile.model.CreatedPlaylistResponse
import retrofit2.HttpException
import retrofit2.http.Body
import retrofit2.http.Path
import retrofit2.http.Query
import java.io.IOException


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
    private val spotifyApiTrackService: SpotifyApiTrackService = apiRetrofit.create(SpotifyApiTrackService::class.java)
    private val spotifyApiArtistService: SpotifyApiArtistService = apiRetrofit.create(SpotifyApiArtistService::class.java)
    private val spotifyApiPlaylistService: SpotifyApiPlaylistService = apiRetrofit.create(SpotifyApiPlaylistService::class.java)

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

    fun getTopTracks(token: String,timeRange: String,limit: Int, callback: (response: TopTracksResponse?, error: Throwable?) -> Unit) {
        Log.d("SpotifyRepo", "Chiamata getTopTracks iniziata con il token: $token")  // Log per tracciare l'inizio della chiamata

        spotifyApiTrackService.getTopTracks("Bearer $token",timeRange,limit).enqueue(object : Callback<TopTracksResponse> {
            override fun onResponse(call: Call<TopTracksResponse>, response: Response<TopTracksResponse>) {
                if (response.isSuccessful) {
                    Log.d("SpotifyRepo", "Risposta ricevuta con successo: ")
                } else {
                    Log.d("SpotifyRepo", "Risposta con errore. Codice:  Messaggio:")
                }
                callback(response.body(), null)
            }

            override fun onFailure(call: Call<TopTracksResponse>, t: Throwable) {
                Log.e("SpotifyRepo", "Errore durante la chiamata getTopTracks", t)  // Log in caso di errore di chiamata
                callback(null, t)
            }
        })
    }

    fun getTopArtists(token: String, timeRange: String, limit: Int, callback: (response: TopArtistsResponse?, error: Throwable?) -> Unit) {
        Log.d("SpotifyRepo", "Chiamata getTopArtists iniziata con il token: $token")  // Log per tracciare l'inizio della chiamata

        spotifyApiArtistService.getTopArtists("Bearer $token", timeRange, limit).enqueue(object : Callback<TopArtistsResponse> {
            override fun onResponse(call: Call<TopArtistsResponse>, response: Response<TopArtistsResponse>) {
                if (response.isSuccessful) {
                    Log.d("SpotifyRepo", "Risposta ricevuta con successo: ${response.body()}")  // Log in caso di risposta di successo
                } else {
                    Log.d("SpotifyRepo", "Risposta con errore. Codice: ${response.code()}, Messaggio: ${response.message()}")  // Log in caso di risposta con codice d'errore
                }
                callback(response.body(), null)
            }

            override fun onFailure(call: Call<TopArtistsResponse>, t: Throwable) {
                Log.e("SpotifyRepo", "Errore durante la chiamata getTopArtists", t)  // Log in caso di errore di chiamata
                callback(null, t)
            }
        })
    }
    fun createPlaylist(token: String, body: CreatePlaylistBody, callback: (response: CreatedPlaylistResponse?, error: Throwable?) -> Unit) {
        Log.d("Spotify", "Tentativo di creazione playlist con token: $token e body: $body")

        spotifyApiPlaylistService.createPlaylist("Bearer $token", body).enqueue(object : Callback<CreatedPlaylistResponse> {
            override fun onResponse(call: Call<CreatedPlaylistResponse>, response: Response<CreatedPlaylistResponse>) {
                if (response.isSuccessful) {
                    Log.d("Spotify", "Playlist creata con successo! Response: ${response.body()}")
                    callback(response.body(), null)
                } else {
                    Log.e("Spotify", "Errore nella risposta di creazione playlist. Codice: ${response.code()} - Messaggio: ${response.message()}")
                    callback(null, Throwable("Errore nella risposta di creazione playlist. Codice: ${response.code()} - Messaggio: ${response.message()}"))
                }
            }

            override fun onFailure(call: Call<CreatedPlaylistResponse>, t: Throwable) {
                // Qui utilizziamo la logica migliorata di gestione degli errori
                when (t) {
                    is IOException -> Log.e("Spotify", "Errore di rete o API non raggiungibile", t)
                    is HttpException -> {
                        val statusCode = t.code()
                        Log.e("Spotify", "Errore nell'API Spotify, codice di stato: $statusCode", t)
                    }
                    else -> Log.e("Spotify", "Errore sconosciuto", t)
                }
                callback(null, t)
            }
        })
    }


    fun addTracksToPlaylist(token: String, playlistId: String, body: AddTracksBody, callback: (success: Boolean, error: Throwable?) -> Unit) {
        spotifyApiPlaylistService.addTracksToPlaylist("Bearer $token", playlistId, body).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    callback(true, null)
                } else {
                    // Log del corpo della risposta in caso di errore
                    val errorBody = response.errorBody()?.string()
                    Log.e("Spotify", "Errore nell'aggiungere tracce alla playlist. Codice: ${response.code()} - Errore: $errorBody")
                    callback(false, Throwable("Errore nell'aggiungere tracce alla playlist. Codice: ${response.code()} - Errore: $errorBody"))
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("Spotify", "Errore di rete o API non raggiungibile", t)
                callback(false, t)
            }
        })
    }



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

    interface SpotifyApiTrackService {
        @GET("me/top/tracks")
        fun getTopTracks(
            @Header("Authorization") authToken: String,
            @Query("time_range") timeRange:String,
            @Query("limit")limit:Int
        ): Call<TopTracksResponse>
    }

    interface SpotifyApiArtistService {
        @GET("me/top/artists")
        fun getTopArtists(
            @Header("Authorization") authToken: String,
            @Query("time_range") timeRange:String,
            @Query("limit")limit:Int
        ): Call<TopArtistsResponse>
    }

    interface SpotifyApiPlaylistService {
        @POST("me/playlists")
        fun createPlaylist(
            @Header("Authorization") authToken: String,
            @Body body: CreatePlaylistBody
        ): Call<CreatedPlaylistResponse>

        @POST("playlists/{playlist_id}/tracks")
        fun addTracksToPlaylist(
            @Header("Authorization") authToken: String,
            @Path("playlist_id") playlistId: String,
            @Body body: AddTracksBody
        ): Call<Void>
    }



}