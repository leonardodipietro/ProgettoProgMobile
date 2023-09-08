package com.example.progettoprogmobile.model

data class TopTracksResponse(
    val items: List<Track>
)

data class TopArtistsResponse(
    val items: List<Artist>
)


data class Track(
    val name: String,
    val album: Album,
    val artists: List<Artist>
)

data class Album(
    val name: String
)

data class Artist(
    val name: String
)

