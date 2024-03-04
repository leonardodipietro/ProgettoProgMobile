package com.example.progettoprogmobile.model

import java.io.Serializable

data class ReviewData(
    val recensione: String,
    val timestamp: String,
    val track: String,
    val album: String,
    val image: String,
    val artist: List<String>
): Serializable
