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
import com.example.progettoprogmobile.adapter.FollowersFriendAdapter
import com.example.progettoprogmobile.adapter.NotificationsAdapter
import com.example.progettoprogmobile.model.Utente
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FollowersFriendFragment: Fragment() {

    private lateinit var followersFriendAdapter: FollowersFriendAdapter
    private val database = FirebaseDatabase.getInstance()
    private lateinit var recyclerView: RecyclerView
    val utenteList = mutableListOf<Utente>()
    private lateinit var userId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val rootView = inflater.inflate(R.layout.fragment_followers_friend, container, false)
        val backButton: Button = rootView.findViewById(R.id.backArrow)

        userId = arguments?.getString("userId") ?: "" //Dichiarazione al livello di classe
        Log.d("FifthFragment", "User ID ricevuto per followers: $userId")

        backButton.setOnClickListener {
            // Azione da eseguire quando il pulsante freccia viene cliccato
            requireActivity().onBackPressed() // Torna al fragment precedente
        }

        recyclerView = rootView.findViewById(R.id.followersRecyclerView2)
        recyclerView.layoutManager = LinearLayoutManager(context)
        followersFriendAdapter = FollowersFriendAdapter(recyclerView)
        recyclerView.adapter = followersFriendAdapter

        setupUtenteObserver()

        followersFriendAdapter.setFollowerItemClickListener(object :
            NotificationsAdapter.FollowerViewHolder.OnClickListener {
            override fun onClick(position: Int) {
                val selectedFollower = followersFriendAdapter.currentList[position]
                selectedFollower?.let {
                    Log.d("MainFragment", "Follower to be passed to Follower fragment: $it")
                    val followerSelezionatoFragment = FifthFragment()
                    val bundle = Bundle()
                    bundle.putSerializable("followerDetail", it)
                    followerSelezionatoFragment.arguments = bundle
                    Log.d("MainFragment", "Follower set to FollowerSelezionato fragment: $it.")
                    val fragmentManager = requireActivity().supportFragmentManager
                    fragmentManager.beginTransaction()
                        .replace(R.id.nav_host_fragment, followerSelezionatoFragment)
                        .addToBackStack(null)  // Aggiungi la transazione allo stack indietro
                        .commit()
                }
            }
        })

        return rootView
    }

    private fun setupUtenteObserver(){
        val followersReference = database.getReference("users")
            .child("$userId")
            .child("followers")

        followersReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                if (dataSnapshot.exists()) {
                    val followerIds = mutableListOf<String>()
                    for (followerSnapshot in dataSnapshot.children) {
                        val followersId = followerSnapshot.key
                        followersId?.let {followerIds.add(it)}
                    }

                    val usersRef = database.getReference("users")

                    utenteList.clear()

                    for (followerId in followerIds) {
                        val userRef = usersRef.child(followerId)
                        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    val username = dataSnapshot.child("name").value.toString()
                                    val imageUrl = dataSnapshot.child("profile image").value.toString()

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
                                        followersFriendAdapter.submitUserList(utenteList)
                                    }

                                    followersFriendAdapter.submitUserList(utenteList)


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