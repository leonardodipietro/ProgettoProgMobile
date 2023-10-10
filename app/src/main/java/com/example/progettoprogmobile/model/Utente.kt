package com.example.progettoprogmobile.model

import com.google.gson.annotations.SerializedName

data class Utente(
    val name:String?=null,
    val images: List<ImageProfile>?=null ,
)
data class ImageProfile(
    @SerializedName("url") val url: String
)