package com.example.progettoprogmobile.model


import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class TopTracksResponse(
    val items: List<Track>
)

data class TopArtistsResponse(
    val items: List<Artist>
)


data class Track(

    @SerializedName("name") val name: String,
    @SerializedName("album") val album: Album,
    @SerializedName("artists") val artists: List<Artist>,
    @SerializedName("id") val id: String,
  //  @SerializedName("genres") val genres: List<String>,
  //  @SerializedName("duration_ms") val durationMs: String,

):Serializable

data class Album(
    val name: String,
    val images: List<Image>,
    val releaseDate: String
)

/*data class SimpleArtist(
    @SerializedName("name") val name: String,// Solo il nome dell'artista
    @SerializedName("id") val id: String?
)*/



data class Image(
    @SerializedName("url") val url: String
)

data class Followers(
    @SerializedName("total") val total: Int
)
data class Artist(
    @SerializedName("name") val name: String,
    @SerializedName("genres") val genres: List<String>,
    @SerializedName("id") val id: String="",
    //@SerializedName("followers") val followers: Followers,
    @SerializedName("images") val images: List<Image>
):Serializable
