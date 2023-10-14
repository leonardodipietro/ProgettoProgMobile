package com.example.progettoprogmobile.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.progettoprogmobile.R
import com.example.progettoprogmobile.model.Artist

class ArtistAdapter(private var artists: List<Artist>) : RecyclerView.Adapter<ArtistAdapter.ArtistViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtistViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_artist, parent, false)
        return ArtistViewHolder(view)
    }

    override fun onBindViewHolder(holder: ArtistViewHolder, position: Int) {
        val artist = artists[position]
        holder.bind(artist)
    }

    override fun getItemCount(): Int = artists.size

    inner class ArtistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val artistNameTextView: TextView = itemView.findViewById(R.id.nomeartistatop)
        private val artistImageView: ImageView = itemView.findViewById(R.id.imagetopartist1)

        fun bind(artist: Artist) {
            artistNameTextView.text = artist.name
            Log.d("ArtistName", "Nome Artista: ${artist.name}")
            // Verificare che la lista di immagini non sia vuota
            if (artist.images.isNotEmpty()) {
                // Utilizzare Glide per caricare l'immagine nell'ImageView
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