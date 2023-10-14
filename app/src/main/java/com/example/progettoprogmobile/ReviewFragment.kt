package com.example.progettoprogmobile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.progettoprogmobile.adapter.ReviewAdapter
import com.example.progettoprogmobile.model.ReviewData
import com.example.progettoprogmobile.viewModel.FirebaseViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*


class ReviewFragment: Fragment() {

    private lateinit var reviewAdapter: ReviewAdapter
    private val reviewDataList: MutableList<ReviewData> = mutableListOf()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private val database = FirebaseDatabase.getInstance()
    private val reviewsReference = database.getReference("reviews")
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val rootView = inflater.inflate(R.layout.fragment_review, container, false)
        val backButton: Button = rootView.findViewById(R.id.backArrow)

        backButton.setOnClickListener {
            // Azione da eseguire quando il pulsante freccia viene cliccato
            requireActivity().onBackPressed() // Torna al fragment precedente
        }

        recyclerView = rootView.findViewById(R.id.reviewRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        reviewAdapter = ReviewAdapter(emptyList())
        recyclerView.adapter = reviewAdapter

        retrieveReviewData()

        return rootView
    }


    private fun retrieveReviewData(): List<ReviewData> {
        // Assicurati che l'utente sia loggato prima di procedere
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val reviewsReference = database.getReference("reviews")
            reviewsReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (reviewSnapshot in dataSnapshot.children) {
                        val reviewUserId = reviewSnapshot.child("userId").value.toString()
                        Log.d("ReviewFragment", "Review User ID: $reviewUserId")

                        if (reviewUserId == userId) {
                            val trackId = reviewSnapshot.child("trackId").value.toString()
                            val content = reviewSnapshot.child("content").value.toString()
                            val timestamp = reviewSnapshot.child("timestamp").getValue(Long::class.java)?.toLong() ?: 0L

                            Log.d("ReviewFragment", "Track ID: $trackId")
                            Log.d("ReviewFragment", "Content: $content")
                            Log.d("ReviewFragment", "Timestamp: $timestamp")

                            // Ora che hai l'ID della traccia, puoi eseguire un'altra query per ottenere ulteriori informazioni sulla traccia.
                            val tracksRef = database.getReference("tracks")
                            tracksRef.child(trackId).addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(trackDataSnapshot: DataSnapshot) {
                                    val trackName = trackDataSnapshot.child("trackName").value.toString()
                                    val albumName = trackDataSnapshot.child("album").value.toString()
                                    val imageUrl = trackDataSnapshot.child("image_url").value.toString()
                                    val artistId = trackDataSnapshot.child("artists").child("0").value.toString()

                                    Log.d("ReviewFragment", "Track name: $trackName")
                                    Log.d("ReviewFragment", "Album name: $albumName")
                                    Log.d("ReviewFragment", "Image URL: $imageUrl")
                                    Log.d("ReviewFragment", "Artist ID: $artistId")

                                    val artistRef = database.getReference("artists")
                                    artistRef.child(artistId).addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(artistDataSnapshot: DataSnapshot) {
                                            val artistName = artistDataSnapshot.child("name").value.toString()

                                            val reviewData = ReviewData(
                                                content,
                                                timestamp,
                                                trackName,
                                                albumName,
                                                imageUrl,
                                                artistName
                                            )

                                            // Log per verificare i dati
                                            Log.d("ReviewFragment", "Review User ID: $reviewUserId")
                                            Log.d("ReviewFragment", "Track ID: $trackId")
                                            Log.d("ReviewFragment", "Content: $content")
                                            Log.d("ReviewFragment", "Timestamp: $timestamp")

                                            reviewDataList.add(reviewData)

                                            Log.d("ReviewFragment", "Review Data: $reviewData")
                                        }
                                        override fun onCancelled(databaseError: DatabaseError) {
                                                    // Gestisci eventuali errori
                                        }
                                    })
                                }
                                override fun onCancelled(databaseError: DatabaseError) {
                                        // Gestisci eventuali errori
                                }
                            })
                        }
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    // Gestisci eventuali errori
                }
            })
        }
        return reviewDataList
    }
}