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

class FollowingAdapter (
    private val parent: ViewGroup,
    private val followingButtonClickListener: FollowingAdapter.OnFollowingButtonClickListener
) : ListAdapter<Utente, FollowingAdapter.ViewHolder>(UtenteDiffCallback()) {

    interface OnFollowingButtonClickListener {
        fun onFollowingButtonClicked(userId: String)
    }

    val view: View = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_following, parent, false)
    private val utenteList = mutableListOf<Utente>() // Lista per tenere traccia dei follower

    fun submitUserList(userList: List<Utente>) {
        utenteList.clear()
        utenteList.addAll(userList)
        notifyDataSetChanged()
        submitList(userList)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val followingImage: ImageView = view.findViewById(R.id.followingImage)
        val followingUsername: TextView = view.findViewById(R.id.followingUsername)
        val followingButton: Button = view.findViewById(R.id.followingButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FollowingAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_following, parent, false)
        return FollowingAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: FollowingAdapter.ViewHolder, position: Int) {
        val utente = getItem(position)

        Picasso.get().load(utente.userImage).into(holder.followingImage)
        holder.followingUsername.text = utente.name
        holder.followingButton.setOnClickListener {
            followingButtonClickListener.onFollowingButtonClicked(utente.userId)
        }
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