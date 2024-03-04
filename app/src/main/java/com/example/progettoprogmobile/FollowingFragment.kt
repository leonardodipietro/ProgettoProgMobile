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
import com.example.progettoprogmobile.adapter.FollowingAdapter
import com.example.progettoprogmobile.adapter.NotificationsAdapter
import com.example.progettoprogmobile.model.Utente
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FollowingFragment: Fragment(), FollowingAdapter.OnFollowingButtonClickListener {

    private lateinit var followingAdapter: FollowingAdapter
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    private val database = FirebaseDatabase.getInstance()
    private lateinit var recyclerView: RecyclerView
    val utenteList = mutableListOf<Utente>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val rootView = inflater.inflate(R.layout.fragment_following, container, false)
        val backButton: Button = rootView.findViewById(R.id.backArrow)

        backButton.setOnClickListener {
            // Azione da eseguire quando il pulsante freccia viene cliccato
            requireActivity().onBackPressed() // Torna al fragment precedente
        }

        recyclerView = rootView.findViewById(R.id.followingRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        followingAdapter = FollowingAdapter(recyclerView, this)
        recyclerView.adapter = followingAdapter

        setupUtenteObserver()

        followingAdapter.setFollowingItemClickListener(object :
            NotificationsAdapter.RequestViewHolder.OnClickListener {
            override fun onClick(position: Int) {
                val selectedFollower = followingAdapter.currentList[position]
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
        val followingReference = database.getReference("users")
            .child("$currentUserId")
            .child("following")

        followingReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                if (dataSnapshot.exists()) {
                    val followerIds = mutableListOf<String>()
                    for (followingSnapshot in dataSnapshot.children) {
                        val followingId = followingSnapshot.key
                        followingId?.let {followerIds.add(it)}
                    }

                    val usersRef = database.getReference("users")

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
                                        followingAdapter.submitUserList(utenteList)
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

    override fun onFollowingButtonClicked(userId:String) {
        if (currentUserId != null) {
            val usersRef = database.getReference("users")
            val currentFollowingRef = usersRef.child(currentUserId).child("following").child(userId)
            val userFollowersRef = usersRef.child(userId).child("followers").child(currentUserId)

            currentFollowingRef.removeValue().addOnSuccessListener {
                userFollowersRef.removeValue().addOnSuccessListener {
                    val position = findUserPosition(userId)
                    if (position != -1) {
                        utenteList.removeAt(position)
                        followingAdapter.submitUserList(utenteList)
                    }
                    showToast("Utente rimosso con successo")
                }.addOnFailureListener { e ->
                    showToast("Errore nella rimozione dell'utente")
                }
            }.addOnFailureListener { e ->
                showToast("Errore nella rimozione dell'utente")
            }
        }
    }

    private fun showToast(message: String) {
        val toast = Toast.makeText(context, message, Toast.LENGTH_SHORT)
        toast.show()
    }

    private fun findUserPosition(userId: String): Int {
        for (i in 0 until utenteList.size) {
            if (utenteList[i].userId == userId) {
                return i
            }
        }
        return -1
    }
}