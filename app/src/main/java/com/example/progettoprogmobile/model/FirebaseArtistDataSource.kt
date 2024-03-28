package com.example.progettoprogmobile.model

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener


class FirebaseArtistDataSource(private val artistsRef: DatabaseReference) : ArtistDataSource {
    override fun retrieveArtistById(artistId: String, onComplete: (Artist?) -> Unit, onError: (Exception) -> Unit) {
        val database = FirebaseDatabase.getInstance().reference
        val artistRef = database.child("artists").child(artistId)

        artistRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val name = dataSnapshot.child("name").getValue(String::class.java) ?: ""
                    val genres = dataSnapshot.child("genres").getValue(object : GenericTypeIndicator<List<String>>() {}) ?: listOf<String>()
                    val imageUrl = dataSnapshot.child("image_url").getValue(String::class.java) ?: ""

                    val artist = Artist(name, genres, artistId,  listOf(Image(imageUrl)))
                    onComplete(artist)
                } else {
                    onComplete(null)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

                onComplete(null)
            }
        })
    }
}

interface ArtistDataSource {
    fun retrieveArtistById(artistId: String, onComplete: (Artist?) -> Unit, onError: (Exception) -> Unit)
}