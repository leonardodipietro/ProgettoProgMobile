package com.example.progettoprogmobile.model



data class SpotifyTokenResponse (
    val access_token: String,//token effettivo
    private val token_type: String,//con spotify sarà sempre di tipo bearer
    private val scope: String, //dati a cui puoi accedere con il token
    private val expires_in: Int, //tempo di durata del token
    private val refresh_token: String?,
)