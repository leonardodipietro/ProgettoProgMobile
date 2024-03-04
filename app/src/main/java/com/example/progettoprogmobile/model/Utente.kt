package com.example.progettoprogmobile.model

import java.io.Serializable

data class Utente(
    val userId: String,
    val name: String,
    val userImage: String
): Serializable {
    // Costruttore senza argomenti necessario per Firebase
    constructor() : this("", "", "")
}