package com.example.progettoprogmobile.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.progettoprogmobile.R
import com.example.progettoprogmobile.model.Track

class TrackGridAdapter(private var tracks: List<Track>, private val listener: TrackAdapter.OnTrackClickListener) : RecyclerView.Adapter<TrackGridAdapter.TrackGridViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackGridViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recyclerviewbranigridlayout, parent, false)
        return TrackGridViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrackGridViewHolder, position: Int) {
        val track = tracks[position]
        holder.bind(track)
    }

    override fun getItemCount(): Int {
        return tracks.size
    }

    inner class TrackGridViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val BranoTopBraniGrid: TextView = itemView.findViewById(R.id.BranoTopbraniGrid)
        private val AlbumInfoGrid: TextView = itemView.findViewById(R.id.AlbumInfoGrid)
        private val ArtistInfoGrid: TextView = itemView.findViewById(R.id.ArtistInfoGrid)
        private val albumImageViewGrid: ImageView = itemView.findViewById(R.id.imageviewtopbraniGrid)

        fun bind(track: Track) {
            BranoTopBraniGrid.text = track.name
            AlbumInfoGrid.text = track.album.name
            ArtistInfoGrid.text = track.artists.joinToString { it.name }
            if (track.album.images.isNotEmpty()) {
                Glide.with(itemView)
                    .load(track.album.images[0].url)
                    .into(albumImageViewGrid)
            }
            itemView.setOnClickListener {
                listener.onTrackClicked(track)
            }

        }
    }

    interface OnTrackClickListener {
        fun onTrackClicked(data: Track)
    }

    fun submitList(newTracks: List<Track>) {
        tracks = newTracks
        notifyDataSetChanged()
    }
}
