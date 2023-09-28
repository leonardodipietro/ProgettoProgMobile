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
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycle_view_top_brani, parent, false)
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
        private val BranoTopBrani: TextView = itemView.findViewById(R.id.BranoTopbrani)
        private val ArtistaTopbrani: TextView = itemView.findViewById(R.id.ArtistaTopbrani)
        private val ALbumTopbrani: TextView = itemView.findViewById(R.id.AlbumTopbrani)

        fun bind(track: Track) {
            BranoTopBrani.text = track.name
            ArtistaTopbrani.text = track.artists.toString()
            ALbumTopbrani.text= track.album.toString()
        }
    }

    fun submitList(newTracks: List<Track>) {
        tracks = newTracks
        notifyDataSetChanged()
    }
}
