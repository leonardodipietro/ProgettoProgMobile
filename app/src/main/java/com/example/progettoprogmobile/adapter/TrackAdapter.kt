package com.example.progettoprogmobile.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.progettoprogmobile.model.Track
import com.example.progettoprogmobile.R

class TrackAdapter(private var tracks: List<Track>) : RecyclerView.Adapter<TrackAdapter.TrackViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_toptrack, parent, false)
        return TrackViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        val track = tracks[position]
        holder.bind(track)
    }

    override fun getItemCount(): Int {
        return tracks.size
    }

    inner class TrackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val trackNameTextView: TextView = itemView.findViewById(R.id.trackNameTextView)
        private val albumAndArtistsTextView: TextView = itemView.findViewById(R.id.albumAndArtistsTextView)
//TODO INSERIRE VISUALIZZAZIONE DEI BRANI NELLA RECYCLER
        fun bind(track: Track) {
            trackNameTextView.text = track.name
            val artistNames = track.artists.joinToString(", ") { it.name }
            albumAndArtistsTextView.text = "${track.album.name} - $artistNames"
        }
    }

    fun submitList(newTracks: List<Track>) {
        tracks = newTracks
        notifyDataSetChanged()
    }
}
