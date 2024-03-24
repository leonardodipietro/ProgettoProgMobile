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
import com.example.progettoprogmobile.model.Album
import com.example.progettoprogmobile.model.Artist
import com.example.progettoprogmobile.model.Image
import com.example.progettoprogmobile.model.ReviewData
import com.example.progettoprogmobile.model.Track
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReviewFragmentFriend: Fragment() {

    private lateinit var reviewAdapter: ReviewAdapter
    private val database = FirebaseDatabase.getInstance()
    private lateinit var recyclerView: RecyclerView

    private lateinit var userId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_review_friend, container, false)
        val backButton: Button = rootView.findViewById(R.id.backArrow)

        userId = arguments?.getString("userId") ?: "" //Dichiarazione al livello di classe
        Log.d("FifthFragment", "User ID ricevuto per recensioni: $userId")

        backButton.setOnClickListener {
            // Azione da eseguire quando il pulsante freccia viene cliccato
            requireActivity().onBackPressed() // Torna al fragment precedente
        }

        recyclerView = rootView.findViewById(R.id.reviewRecyclerView1)
        recyclerView.layoutManager = LinearLayoutManager(context)
        reviewAdapter = ReviewAdapter(requireActivity())
        recyclerView.adapter = reviewAdapter

        reviewAdapter.setOnReviewItemClickListener(object : ReviewAdapter.OnReviewItemClickListener {
            override fun onReviewItemClick(review: ReviewData) {
                val trackId = review.trackId
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

                        for (artistId in artistIds) {
                            val artistRef = database.getReference("artists").child(artistId)
                            artistRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(artistDataSnapshot: DataSnapshot) {
                                    val artistName = artistDataSnapshot.child("name").value.toString()
                                    artistNameList.add(artistName)

                                    if (artistNameList.size == artistIds.size) {
                                        // All artist information has been retrieved
                                        val artistImages = artistNameList.map { Image(url = it) }
                                        val artist = Artist(
                                            id = artistId,
                                            name = artistNameList.joinToString(),
                                            genres = emptyList(),
                                            images = artistImages
                                        )

                                        // Create Track object
                                        val albumImage = Image(url = imageUrl)
                                        val track = Track(
                                            name = trackName,
                                            album = Album(name = albumName, images = listOf(albumImage), releaseDate = ""),
                                            artists = listOf(artist),
                                            id = trackId
                                        )

                                        // Pass data to BranoSelezionato fragment
                                        val branoSelezionatoFragment = BranoSelezionato()
                                        val bundle = Bundle().apply {
                                            putSerializable("trackDetail", track)
                                        }
                                        branoSelezionatoFragment.arguments = bundle
                                        val fragmentManager = requireActivity().supportFragmentManager
                                        fragmentManager.beginTransaction()
                                            .replace(R.id.nav_host_fragment, branoSelezionatoFragment)
                                            .addToBackStack(null)
                                            .commit()
                                    }
                                }

                                override fun onCancelled(databaseError: DatabaseError) {
                                    // Handle errors
                                }
                            })
                        }

                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Gestisci eventuali errori
                    }
                })
            }
        })

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
                                                artistNameList,
                                                trackId
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