package com.example.progettoprogmobile.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.progettoprogmobile.R
import com.example.progettoprogmobile.model.ReviewData
import com.squareup.picasso.Picasso


class ReviewAdapter (parent: ViewGroup) : ListAdapter<ReviewData, ReviewAdapter.ViewHolder>(ReviewDataDiffCallback()) {

    val view: View = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_recensioni_profilo, parent, false)

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val copertina: ImageView = view.findViewById(R.id.copertina)
        val brano: TextView = view.findViewById(R.id.brano)
        val artista: TextView = view.findViewById(R.id.artista)
        val album: TextView = view.findViewById(R.id.album)
        val recensione: TextView = view.findViewById(R.id.content)
        val timestamp: TextView = view.findViewById(R.id.timestamp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_recensioni_profilo, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val reviewData = getItem(position)

        Picasso.get().load(reviewData.image).into(holder.copertina)
        holder.brano.text = reviewData.track
        holder.artista.text = reviewData.artist.joinToString (", ")
        holder.album.text = reviewData.album
        holder.recensione.text = reviewData.recensione
        holder.timestamp.text = reviewData.timestamp.toString()
    }

    class ReviewDataDiffCallback : DiffUtil.ItemCallback<ReviewData>() {
        override fun areItemsTheSame(oldItem: ReviewData, newItem: ReviewData): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: ReviewData, newItem: ReviewData): Boolean {
            return oldItem == newItem
        }
    }
}