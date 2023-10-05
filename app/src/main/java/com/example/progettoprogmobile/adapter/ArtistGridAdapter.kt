package com.example.progettoprogmobile.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.progettoprogmobile.R
import com.example.progettoprogmobile.model.Artist

class ArtistGridAdapter(private var artists: List<Artist>) : RecyclerView.Adapter<ArtistGridAdapter.ArtistGridViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtistGridViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recyclerviewartistgridlayout, parent, false)
        return ArtistGridViewHolder(view)
    }

    override fun onBindViewHolder(holder: ArtistGridViewHolder, position: Int) {
        val artist = artists[position]
        holder.bind(artist)
    }

    override fun getItemCount(): Int = artists.size

    inner class ArtistGridViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val artistNameTextView: TextView = itemView.findViewById(R.id.ArtistaTopartistGrid)
        private val artistImageView: ImageView = itemView.findViewById(R.id.imageviewtopartistGrid)

        fun bind(artist: Artist) {
            artistNameTextView.text = artist.name
            if (artist.images.isNotEmpty()) {
                Glide.with(itemView)
                    .load(artist.images[0].url)
                    .into(artistImageView)
            }
        }
    }

    fun submitList(newArtists: List<Artist>) {
        artists = newArtists
        notifyDataSetChanged()
    }
}
