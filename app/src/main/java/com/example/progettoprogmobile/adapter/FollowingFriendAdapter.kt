package com.example.progettoprogmobile.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.progettoprogmobile.R
import com.example.progettoprogmobile.model.Utente
import com.squareup.picasso.Picasso

class FollowingFriendAdapter (private val parent: ViewGroup) : ListAdapter<Utente, FollowingFriendAdapter.ViewHolder>(UtenteDiffCallback()) {

    val view: View = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_followers_friend, parent, false)
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
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FollowingFriendAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_followers_friend, parent, false)
        return FollowingFriendAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: FollowingFriendAdapter.ViewHolder, position: Int) {
        val utente = getItem(position)

        Picasso.get().load(utente.userImage).into(holder.followersImage)
        holder.followersUsername.text = utente.name
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