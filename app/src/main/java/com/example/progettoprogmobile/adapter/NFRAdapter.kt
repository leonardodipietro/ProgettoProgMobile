package com.example.progettoprogmobile.adapter

import android.util.Log
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


class NFRAdapter (
    private val parent: ViewGroup,
    private val confirmClickListener: OnConfirmClickListener,
    private val deleteClickListener: OnDeleteClickListener
) : ListAdapter<Utente, NFRAdapter.ViewHolder>(UtenteDiffCallback()) {

    interface OnConfirmClickListener {
        fun onConfirmClickListener (userId: String)
    }

    interface OnDeleteClickListener {
        fun onDeleteClickListener (userId: String)
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
        val confirmButton: Button = view.findViewById(R.id.confirm)
        val deleteButton: Button = view.findViewById(R.id.delete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NFRAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_notifiche_nfr, parent, false)

        // Aggiungi un log per verificare quando viene creato un ViewHolder
        Log.d("NFAAdapter", "ViewHolder creato")

        return NFRAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: NFRAdapter.ViewHolder, position: Int) {
        val utente = getItem(position)

        Picasso.get().load(utente.userImage).into(holder.followersImage)
        holder.followersUsername.text = utente.name

        holder.confirmButton.setOnClickListener {
            confirmClickListener.onConfirmClickListener(utente.userId)
        }

        holder.deleteButton.setOnClickListener {
            deleteClickListener.onDeleteClickListener(utente.userId)
        }

        // Aggiungi un log per verificare quando viene eseguito il bind del ViewHolder
        Log.d("NFRAdapter", "ViewHolder binded, Position: $position")
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