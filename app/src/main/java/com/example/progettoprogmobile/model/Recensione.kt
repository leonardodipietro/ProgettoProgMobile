package com.example.progettoprogmobile.model
data class Recensione(
    val commentId: String = "",       // ID univoco per la recensione
    val userId: String = "",          // ID dell'utente che ha lasciato la recensione
    val trackId: String = "",         // ID della canzone recensita
    val timestamp: String = "",         // Timestamp del momento in cui la recensione è stata lasciata
    val content: String = "",
    val artistId: String= ""
)
