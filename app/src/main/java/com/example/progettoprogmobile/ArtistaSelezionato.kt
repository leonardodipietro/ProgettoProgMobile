package com.example.progettoprogmobile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.progettoprogmobile.adapter.ArtistaSelezionatoAdapter
import com.example.progettoprogmobile.adapter.TrackAdapter
import com.example.progettoprogmobile.model.Artist
import com.example.progettoprogmobile.model.Track
import com.example.progettoprogmobile.viewModel.FirebaseViewModel
import com.example.progettoprogmobile.viewModel.RecensioneViewModel

class ArtistaSelezionato : Fragment(), ArtistaSelezionatoAdapter.OnTrackClickListener {

    private lateinit var recensioneViewModel: RecensioneViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var firebaseViewModel: FirebaseViewModel
    private lateinit var adapter:ArtistaSelezionatoAdapter

    private lateinit var backButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inizializza firebaseViewModel
       firebaseViewModel = ViewModelProvider(this).get(FirebaseViewModel::class.java)
        recensioneViewModel = ViewModelProvider(this).get(RecensioneViewModel::class.java)
        val rootView = inflater.inflate(R.layout.artistaselezionato, container, false)
        val artist = arguments?.getSerializable("artistdetails") as? Artist
        val artistId = artist?.id ?: ""//SERVE PER PASSARE UN PARAMETRO DI DEFAULT IN CASO FOSSE NULLO
        Log.d("FragmentArtistaSelezionato", "Received track: $artist")
        val nomeartista:TextView= rootView.findViewById(R.id.nomeartistaselezionato)
        val fotoartista: ImageView = rootView.findViewById(R.id.imageartistaselezionato)
       // val genereartista: TextView=rootView.findViewById(R.id.genereartistaselezionato)
        // Impostazione delle informazioni
        artist?.let { currentArtist ->
            nomeartista.text = currentArtist.name
           // genereartista.text= currentArtist.genres.firstOrNull()
            // Impostazione dell'immagine con Glide
            val imageUrl = currentArtist.images.firstOrNull()?.url // prendo la prima immagine, se esiste
            if (imageUrl != null) {
                Glide.with(this)
                    .load(imageUrl)
                    .error(R.drawable.imgcantante)
                    .into(fotoartista)
            }

        }
        recyclerView = rootView.findViewById(R.id.recyclerBranidiArtistaRecensiti)
        adapter = ArtistaSelezionatoAdapter(emptyList(),this) // Inizializza con una lista vuota
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)
        recensioneViewModel.retrieveTracksReviewedByArtistAndDetails(artistId, firebaseViewModel) { tracks ->
            // Aggiorna l'adapter con le nuove tracce ottenute
            activity?.runOnUiThread {
                adapter.updateData(tracks)
            }

        }

        backButton = rootView.findViewById(R.id.backArrow)

        backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }

        return rootView
}

   override fun onTrackClicked(data: Any) {
        Log.d("FragmentClick", "Item clicked with data: $data")
        if (data is Track) {

            val newFragment = com.example.progettoprogmobile.BranoSelezionato()
            val bundle = Bundle()
            bundle.putSerializable("trackDetail", data)
            newFragment.arguments = bundle
            parentFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, newFragment)
                .addToBackStack(null)
                .commit()
        }
    }
}