package com.example.progettoprogmobile.model

data class ReviewData(
    val recensione: String,
    val timestamp: Long,
    val track: String,
    val album: String,
    val image: String,
    val artist: List<String>
)
