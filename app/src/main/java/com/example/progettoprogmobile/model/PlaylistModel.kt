package com.example.progettoprogmobile.model

data class CreatePlaylistBody(
    val name: String,
    val description: String? = null,
    val public: Boolean = true
)
data class CreatedPlaylistResponse(
    val id: String
)
data class AddTracksBody(
    val uris: List<String>
)
