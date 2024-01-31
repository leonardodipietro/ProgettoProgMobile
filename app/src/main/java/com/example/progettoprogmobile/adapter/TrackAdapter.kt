package com.example.progettoprogmobile.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.progettoprogmobile.model.Track
import com.example.progettoprogmobile.R


class TrackAdapter(private var tracks: List<Track>, private val listener: OnTrackClickListener) : RecyclerView.Adapter<TrackAdapter.TrackViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycle_view_top_brani, parent, false)
        return TrackViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        val track = tracks[position]
        Log.d("TrackAdapter", "Associazione traccia alla posizione $position: $track")
        holder.bind(track)

    }

    override fun getItemCount(): Int {
        return tracks.size
    }

    inner class TrackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val BranoTopBrani: TextView = itemView.findViewById(R.id.BranoTopbrani)
        private val ArtistaTopbrani: TextView = itemView.findViewById(R.id.ArtistaTopbrani)
        private val ALbumTopbrani: TextView = itemView.findViewById(R.id.AlbumTopbrani)
        private val albumImageView: ImageView = itemView.findViewById(R.id.imageviewtopbrani)
        fun bind(track: Track) {
            BranoTopBrani.text = track.name
            ArtistaTopbrani.text = track.artists.joinToString{it.name}
            ALbumTopbrani.text= track.album.name
            Log.d("TrackAdapter", "Album: ${track.album.name}, Images: ${track.album.images}")
            if (track.album.images.isNotEmpty()) {
                Log.d("TrackAdapter", "Album images: ${track.album.images}")
                Glide.with(itemView)
                    .load(track.album.images[0].url)
                    .into(albumImageView)
            }

            itemView.setOnClickListener {
                listener.onTrackClicked(track)
            }
        }
    }

    fun submitList(newTracks: List<Track>) {
        tracks = newTracks
        notifyDataSetChanged()
        Log.d("TrackAdapter", "submitList() chiamato con ${tracks.size} nuove tracce")
        Log.d("TrackAdapter", "Nuova lista di tracce: $tracks")
    }

    interface OnTrackClickListener {
        fun onTrackClicked(data: Any)
    }

}

