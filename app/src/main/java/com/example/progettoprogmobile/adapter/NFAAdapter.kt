package com.example.progettoprogmobile.adapter

import android.util.Log
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.example.progettoprogmobile.model.Utente
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.progettoprogmobile.R
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.lang.Exception


class NFAAdapter (
    private val parent: ViewGroup,
    private val followClickListener: OnFollowClickListener
) : ListAdapter<Utente, NFAAdapter.ViewHolder>(UtenteDiffCallback()) {

    val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_fourth, parent, false)


    interface OnFollowClickListener {
        fun onFollowClickListener (userId: String)
    }

    private val utenteList = mutableListOf<Utente>() // Lista per tenere traccia dei follower


    fun submitUserList(userList: List<Utente>) {
        utenteList.clear()
        utenteList.addAll(userList)
        notifyDataSetChanged()
        submitList(userList)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val followersImage: ImageView = view.findViewById(R.id.followersImage)
        val followersUsername: TextView = view.findViewById(R.id.followersRequest)
        val followButton: Button = view.findViewById(R.id.followToggleButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NFAAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_notifiche_nfa, parent, false)

        // Aggiungi un log per verificare quando viene creato un ViewHolder
        Log.d("NFAAdapter", "ViewHolder creato")

        return NFAAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: NFAAdapter.ViewHolder, position: Int) {
        val utente = getItem(position)

        Log.d("NFAAdapter", "User Image URL: ${utente.userImage}")

        if (utente.userImage.isNotEmpty()) {
            Picasso.get()
                .load(utente.userImage)
                .placeholder(R.drawable.baseline_person_24)
                .into(holder.followersImage)
        }

        val message = "${utente.name} ha iniziato a seguirti"
        holder.followersUsername.text = message

        holder.followButton.setOnClickListener {
            followClickListener.onFollowClickListener(utente.userId)
        }

        // Aggiungi un log per verificare quando viene eseguito il bind del ViewHolder
        Log.d("NFAAdapter", "ViewHolder binded, Position: $position")

    }

    class UtenteDiffCallback : DiffUtil.ItemCallback<Utente>() {
        override fun areItemsTheSame(oldItem: Utente, newItem: Utente): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Utente, newItem: Utente): Boolean {
            return oldItem == newItem

        }
    }
}