package com.example.progettoprogmobile.api


import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.POST
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Field
import com.example.progettoprogmobile.model.SpotifyTokenResponse



class SpotifyRepository {

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://accounts.spotify.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val spotifyService: SpotifyAuthService = retrofit.create(SpotifyAuthService::class.java)

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
}