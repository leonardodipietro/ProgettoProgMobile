package com.example.progettoprogmobile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.progettoprogmobile.adapter.FollowingFriendAdapter
import com.example.progettoprogmobile.model.Utente
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FollowingFriendFragment: Fragment() {

    private lateinit var followingFriendAdapter: FollowingFriendAdapter
    private val database = FirebaseDatabase.getInstance()
    private lateinit var recyclerView: RecyclerView
    val utenteList = mutableListOf<Utente>()
    private lateinit var userId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val rootView = inflater.inflate(R.layout.fragment_following_friend, container, false)
        val backButton: Button = rootView.findViewById(R.id.backArrow)

        userId = arguments?.getString("userId") ?: "" //Dichiarazione al livello di classe
        Log.d("FifthFragment", "User ID ricevuto per following: $userId")

        backButton.setOnClickListener {
            // Azione da eseguire quando il pulsante freccia viene cliccato
            requireActivity().onBackPressed() // Torna al fragment precedente
        }

        recyclerView = rootView.findViewById(R.id.followingRecyclerView3)
        recyclerView.layoutManager = LinearLayoutManager(context)
        followingFriendAdapter = FollowingFriendAdapter(recyclerView)
        recyclerView.adapter = followingFriendAdapter

        setupUtenteObserver()

        return rootView
    }

    private fun setupUtenteObserver() {
        val followingReference = database.getReference("users")
            .child("$userId")
            .child("following")

        followingReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                if (dataSnapshot.exists()) {
                    val followerIds = mutableListOf<String>()
                    for (followingSnapshot in dataSnapshot.children) {
                        val followingId = followingSnapshot.key
                        followingId?.let { followerIds.add(it) }
                    }

                    val usersRef = database.getReference("users")

                    for (followerId in followerIds) {
                        val userRef = usersRef.child(followerId)
                        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    val username = dataSnapshot.child("name").value.toString()
                                    val imageUrl =
                                        dataSnapshot.child("profile image").value.toString()

                                    val utente = Utente(
                                        followerId,
                                        username,
                                        imageUrl
                                    )

                                    // Aggiungi log per verificare le informazioni recuperate
                                    Log.d("UtenteInfo", "Username: $username, ImageUrl: $imageUrl")

                                    utenteList.add(utente)

                                    // Controlla se hai raccolto tutti gli utenti
                                    if (utenteList.size == followerIds.size) {
                                        followingFriendAdapter.submitUserList(utenteList)
                                    }

                                }
                            }

                            override fun onCancelled(databaseError: DatabaseError) {
                                // Gestisci eventuali errori
                            }
                        })
                    }

                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
}