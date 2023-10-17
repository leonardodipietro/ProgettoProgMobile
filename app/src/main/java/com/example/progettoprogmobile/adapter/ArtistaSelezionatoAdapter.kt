package com.example.progettoprogmobile.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.progettoprogmobile.ArtistaSelezionato
import com.example.progettoprogmobile.R
import com.example.progettoprogmobile.model.Track

class ArtistaSelezionatoAdapter(private var tracks: List<Track>, private val listener: ArtistaSelezionato) : RecyclerView.Adapter<ArtistaSelezionatoAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        Log.d("ArtistaSelezionatoAdapter", "onCreateViewHolder")
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_artistaselezionato, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.d("ArtistaSelezionatoAdapter", "onBindViewHolder: position $position")
        val track = tracks[position]
        holder.bind(track)
    }

    override fun getItemCount(): Int {
        Log.d("ArtistaSelezionatoAdapter", "getItemCount: ${tracks.size}")
        return tracks.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(track: Track) {
            Log.d("ArtistaSelezionatoAdapter", "bind: track ${track.name}")
            // Inserisci qui la logica per impostare i dati nelle viste dell'elemento
            val nomeArtista = itemView.findViewById<TextView>(R.id.Branolistaartistselezionato)
            val album = itemView.findViewById<TextView>(R.id.Albumlistaartistaselezionato)
            val albumImageView: ImageView = itemView.findViewById(R.id.imageviewlistaartistselezionato)

            nomeArtista.text = track.name
            album.text = track.album.name
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

    fun updateData(newTracks: List<Track>) {
        Log.d("ArtistaSelezionatoAdapter", "updateData: Aggiornamento dati con ${newTracks.size} nuove tracce")
        tracks = newTracks // Aggiungi i nuovi dati
        notifyDataSetChanged() // Notifica all'adapter che i dati sono stati modificati
    }

    interface OnTrackClickListener {
        fun onTrackClicked(data: Any)
    }
}
