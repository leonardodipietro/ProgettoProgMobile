package com.example.progettoprogmobile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.progettoprogmobile.adapter.TrackAdapter
import com.example.progettoprogmobile.adapter.TrackGridAdapter
import com.example.progettoprogmobile.model.Artist
import com.example.progettoprogmobile.model.Track
import com.example.progettoprogmobile.viewModel.RecensioneViewModel

class ArtistaSelezionato : Fragment() {

    private lateinit var recensioneViewModel: RecensioneViewModel
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.artistaselezionato, container, false)
        val artist = arguments?.getSerializable("artistdetails") as? Artist
        Log.d("FragmentArtistaSelezionato", "Received track: $artist")
        val nomeartista:TextView= rootView.findViewById(R.id.nomeartistaselezionato)
        val fotoartista: ImageView = rootView.findViewById(R.id.imageartistaselezionato)
        val genereartista: TextView=rootView.findViewById(R.id.genereartistaselezionato)
        // Impostazione delle informazioni
        artist?.let { currentArtist ->
            nomeartista.text = currentArtist.name
            genereartista.text= currentArtist.genres.firstOrNull()
            // Impostazione dell'immagine con Glide
            val imageUrl = currentArtist.images.firstOrNull()?.url // prendo la prima immagine, se esiste
            if (imageUrl != null) {
                Glide.with(this)
                    .load(imageUrl)
                    .into(fotoartista)
            }
        }

        recensioneViewModel = ViewModelProvider(this).get(RecensioneViewModel::class.java)
        //TODO CAMBIARE NAME CON ID ANCHE NEL DB
        recensioneViewModel.fetchRecensioniForArtist(artist?.name)
        Log.d("fetchRecensiomi","Fetchrecensioni ${recensioneViewModel.fetchRecensioniForArtist(artist?.name)}")


        return rootView
}



}