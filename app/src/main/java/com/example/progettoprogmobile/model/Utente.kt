package com.example.progettoprogmobile.model

data class Utente(
    val userId: String,
    val name: String
) {
    // Costruttore senza argomenti necessario per Firebase
    constructor() : this("", "")
}

/*
data class Utente(
    val userId: String = "",
    val name: String = ""
)

{
    // Costruttore senza argomenti necessario per Firebase
    constructor() : this("")
}
 */