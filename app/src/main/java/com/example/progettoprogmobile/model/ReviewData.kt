package com.example.progettoprogmobile.model

import java.sql.Timestamp


data class ReviewData(
    val recensione: String,
    val timestamp: Long,
    val track: String,
    val album: String,
    val image: String,
    val artist: String
)
