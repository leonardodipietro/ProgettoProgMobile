package com.example.progettoprogmobile.model

data class Risposta (
    val commentIdfather: String = "",
    val answerId: String= "", // ID univoco per la risposta
    val userId: String = "",   // ID dell'utente che ha risposto
    val timestamp: String = "",  // Timestamp del momento in cui la recensione è stata lasciata
    val answercontent: String = ""
)