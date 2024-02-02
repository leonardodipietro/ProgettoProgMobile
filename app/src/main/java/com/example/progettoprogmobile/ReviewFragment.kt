package com.example.progettoprogmobile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.progettoprogmobile.adapter.ReviewAdapter
import com.example.progettoprogmobile.model.ReviewData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*



class ReviewFragment: Fragment() {

    private lateinit var reviewAdapter: ReviewAdapter
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private val database = FirebaseDatabase.getInstance()
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
        reviewAdapter = ReviewAdapter(recyclerView)
        recyclerView.adapter = reviewAdapter

        setupReviewDataObserver()

        return rootView
    }

    private fun setupReviewDataObserver() {
        val reviewsReference = database.getReference("reviews")
        val query = reviewsReference.orderByChild("userId").equalTo(userId)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val reviewDataList = mutableListOf<ReviewData>()

                for (reviewSnapshot in dataSnapshot.children) {
                    val trackId = reviewSnapshot.child("trackId").value.toString()
                    val content = reviewSnapshot.child("content").value.toString()
                    val timestamp = reviewSnapshot.child("timestamp").value.toString()

                    val tracksRef = database.getReference("tracks").child(trackId)
                    tracksRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(trackDataSnapshot: DataSnapshot) {
                            val trackName = trackDataSnapshot.child("trackName").value.toString()
                            val albumName = trackDataSnapshot.child("album").value.toString()
                            val imageUrl = trackDataSnapshot.child("image_url").value.toString()
                            val artistIdsSnapshot = trackDataSnapshot.child("artists")
                            val artistIds = ArrayList<String>()
                            for (artistIdSnapshot in artistIdsSnapshot.children) {
                                val artistId = artistIdSnapshot.value.toString()
                                artistIds.add(artistId)
                            }

                            val artistNameList = mutableListOf<String>()

                            for(artistId in artistIds) {
                                val artistRef = database.getReference("artists").child(artistId)
                                artistRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(artistDataSnapshot: DataSnapshot) {
                                        val artistName = artistDataSnapshot.child("name").value.toString()
                                        artistNameList.add(artistName)
                                        if (artistNameList.size == artistIds.size) {
                                            val reviewData = ReviewData(
                                                content,
                                                timestamp,
                                                trackName,
                                                albumName,
                                                imageUrl,
                                                artistNameList
                                            )
                                            reviewDataList.add(reviewData)

                                            // Se hai raccolto tutte le recensioni, aggiorna la RecyclerView
                                            if (reviewDataList.size == dataSnapshot.childrenCount.toInt()) {
                                                reviewAdapter.submitList(reviewDataList)
                                            }
                                        }
                                    }

                                    override fun onCancelled(databaseError: DatabaseError) {
                                        // Gestisci eventuali errori
                                    }
                                })
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            // Gestisci eventuali errori
                        }
                    })
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Gestisci eventuali errori
            }
        })
    }
}