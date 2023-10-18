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
import com.squareup.picasso.Picasso


class FollowersAdapter (
    private val parent: ViewGroup,
    private val removeFollowerClickListener: OnRemoveFollowerClickListener
) : ListAdapter<Utente, FollowersAdapter.ViewHolder>(UtenteDiffCallback()) {

    interface OnRemoveFollowerClickListener {
        fun onRemoveFollowerClicked(userId: String)
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
        val followersUsername: TextView = view.findViewById(R.id.followersUsername)
        val followersRemove: Button = view.findViewById(R.id.followersRemove)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FollowersAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_followers, parent, false)

        // Aggiungi un log per verificare quando viene creato un ViewHolder
        Log.d("FollowersAdapter", "ViewHolder creato")

        return FollowersAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: FollowersAdapter.ViewHolder, position: Int) {
        val utente = getItem(position)

        Picasso.get().load(utente.userImage).into(holder.followersImage)
        holder.followersUsername.text = utente.name

        holder.followersRemove.setOnClickListener {
            removeFollowerClickListener.onRemoveFollowerClicked(utente.userId)
        }

        // Aggiungi un log per verificare quando viene eseguito il bind del ViewHolder
        Log.d("FollowersAdapter", "ViewHolder binded, Position: $position")

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