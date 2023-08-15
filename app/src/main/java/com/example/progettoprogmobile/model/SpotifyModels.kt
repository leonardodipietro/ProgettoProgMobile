package com.example.progettoprogmobile.model

data class TopTracksResponse(
    val items: List<TrackItem>
)

data class TrackItem(
    val id: String,
    val name: String,
    val album: Album,
    val artists: List<Artist>
)

data class Album(
    val id: String,
    val name: String
)

data class Artist(
    val id: String,
    val name: String
)
