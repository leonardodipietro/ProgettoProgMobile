package com.example.progettoprogmobile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.progettoprogmobile.model.Track
import com.example.progettoprogmobile.viewModel.FirebaseViewModel
import com.example.progettoprogmobile.viewModel.RecensioneViewModel
import com.bumptech.glide.Glide
import com.example.progettoprogmobile.adapter.RecensioniBranoSelAdapter
import com.example.progettoprogmobile.model.Artist
import com.google.firebase.auth.FirebaseAuth

class BranoSelezionato : Fragment() {

    private lateinit var recensioneViewModel: RecensioneViewModel
    private lateinit var recyclerView:RecyclerView
    private lateinit var firebaseViewModel: FirebaseViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val rootView = inflater.inflate(R.layout.branoselezionato, container, false)
        firebaseViewModel=ViewModelProvider(this).get(FirebaseViewModel::class.java)
        recyclerView = rootView.findViewById(R.id.recyclerBranoSelezionato)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = RecensioniBranoSelAdapter(emptyList())
        val track = arguments?.getSerializable("trackDetail") as? Track
        Log.d("FragmentBranoSelezionato", "Received track: $track")
        val artistId = track?.artists?.firstOrNull()?.id ?: ""
        // Binding delle viste
        val titoloCanzone: TextView = rootView.findViewById(R.id.titolocanzone)
        val albumBranoSelezionato: TextView = rootView.findViewById(R.id.albumbranoselezionato)
        val artistaBranoSelezionato: TextView = rootView.findViewById(R.id.artistabranoselezionato)
        val imageBranoSelezionato: ImageView = rootView.findViewById(R.id.imagebranoselezionato)

        val recensioneEditText: EditText = rootView.findViewById(R.id.recensionepersonale)
        val inviarRecensioneButton: Button = rootView.findViewById(R.id.inviarrecensione)

        // ViewModel
        recensioneViewModel = ViewModelProvider(this).get(RecensioneViewModel::class.java)

        inviarRecensioneButton.setOnClickListener {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null && track != null) {
                val commentContent = recensioneEditText.text.toString()
                if (commentContent.isNotBlank()) {
                    recensioneViewModel.saveOrUpdateRecensione(userId, track.id,artistId, commentContent)
                }
            } else {
                // Gestisci il caso in cui l'utente non sia loggato o non ci sia un brano selezionato
            }
        }


        // Impostazione delle informazioni
        track?.let { currentTrack ->
            titoloCanzone.text = currentTrack.name
            albumBranoSelezionato.text = currentTrack.album.name
            artistaBranoSelezionato.text = currentTrack.artists.firstOrNull()?.name ?: "Sconosciuto" // prendo il primo artista, se esiste
            // recensioneViewModel.fetchRecensioniForTrack(track.id)
            recensioneViewModel.fetchRecensioniAndUsersForTrack(currentTrack.id)
            // Impostazione dell'immagine con Glide
            val imageUrl = currentTrack.album.images.firstOrNull()?.url // prendo la prima immagine, se esiste
            if (imageUrl != null) {
                Glide.with(this)
                    .load(imageUrl)
                    .into(imageBranoSelezionato)
            }
        }

        artistaBranoSelezionato.setOnClickListener {
            firebaseViewModel.retrieveArtistById(artistId) { artist ->
                if (artist != null) {
                    onNameArtistClicked(artist)
                }
            }
        }
        setupRecyclerView(rootView)

        return rootView
    }


    private fun setupRecyclerView(rootView: View) {
        recyclerView = rootView.findViewById(R.id.recyclerBranoSelezionato)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = RecensioniBranoSelAdapter(emptyList())

        recensioneViewModel.recensioniData.observe(viewLifecycleOwner, Observer { recensioni ->
            val adapter = recyclerView.adapter as? RecensioniBranoSelAdapter
            adapter?.recensioni = recensioni
            adapter?.notifyDataSetChanged()
        })

        recensioneViewModel.usersData.observe(viewLifecycleOwner, Observer { users ->
            val adapter = recyclerView.adapter as? RecensioniBranoSelAdapter
            adapter?.usersMap = users
            adapter?.notifyDataSetChanged()
        })
    }
    fun onNameArtistClicked(data: Artist) {
        Log.d("FragmentClick", "Item clicked with data: $data")
        if (data is Artist) {
            // Qui naviga verso il nuovo fragment, puoi passare "data" come argomento se necessario
            val newFragment = com.example.progettoprogmobile.ArtistaSelezionato()
            val bundle = Bundle()
            bundle.putSerializable("artistdetails", data)
            newFragment.arguments = bundle
            parentFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, newFragment)
                .addToBackStack(null)
                .commit()
        }
    }

}