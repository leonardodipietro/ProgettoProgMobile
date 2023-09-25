package com.example.progettoprogmobile


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.progettoprogmobile.adapter.TrackAdapter
import com.example.progettoprogmobile.viewModel.FirebaseViewModel
import com.example.progettoprogmobile.viewModel.SharedDataViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.example.progettoprogmobile.SecondActivity

class FirstFragment : Fragment() {

    private lateinit var firebaseViewModel: FirebaseViewModel
    private lateinit var trackAdapter: TrackAdapter
    private lateinit var dataviewmodel: SharedDataViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        dataviewmodel = ViewModelProvider(this)[SharedDataViewModel::class.java]
        val rootView = inflater.inflate(R.layout.fragment_first, container, false)
        lateinit var navControllerStats: NavController

        val navHostFragment = childFragmentManager.findFragmentById(R.id.nav_host_fragment_stats) as NavHostFragment
        val navController = navHostFragment.navController
        val bottomNavigationView = rootView.findViewById<BottomNavigationView>(R.id.bottomNavigationStatsGen)

        bottomNavigationView?.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_track_gen -> {
                    // Naviga al fragment delle tracce
                    navController.navigate(R.id.trackGen)
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.menu_artist_gen -> {
                    // Naviga al fragment degli artisti
                    navController.navigate(R.id.artistGen)
                    return@setOnNavigationItemSelectedListener true
                }
                else -> false
            }
        }

        return rootView
    }
}










    /* ONCREATE
 firebaseViewModel.fetchTopTracksFromFirebase()
 val startAuthButton: Button = rootView.findViewById(R.id.startAuthButton)

  startAuthButton.setOnClickListener {
       startSpotifyAuthentication()

   }

   gestionedati()


   // Inizializza il RecyclerView e l'adapter
   recyclerView = rootView.findViewById(R.id.recyclerViewtopbrani)
   recyclerView.layoutManager = LinearLayoutManager(requireContext())
   trackAdapter = TrackAdapter(emptyList()) // Inizialmente senza tracce
   recyclerView.adapter = trackAdapter

   // Aggiorna l'adapter con i nuovi dati
   firebaseViewModel.topTracksfromdb.observe(viewLifecycleOwner) { tracks ->
       trackAdapter.submitList(tracks)
   } */


  /*  private fun gestionedati() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        // Recupera le tracce più ascoltate da Spotify
        spotifyViewModel.spotifyTokenResponse.observe(viewLifecycleOwner) { tokenResponse ->
            if (tokenResponse?.access_token != null) {
                Log.d("SpotifyToken", "Token ottenuto: ${tokenResponse.access_token}")

                // Una volta ottenuto il token, recupera le tracce più ascoltate
                spotifyViewModel.fetchTopTracks(tokenResponse.access_token)
            } else {
                Log.d("SpotifyToken", "Nessun token ottenuto!")
            }
        }

        // Gestisci gli errori nella richiesta del token
        spotifyViewModel.error.observe(viewLifecycleOwner) { throwable ->
            Log.e("SpotifyTokenError", "Errore durante la richiesta del token", throwable)
        }

        // Salva le tracce nel database Firebase e recupera le tracce da Firebase
        spotifyViewModel.topTracks.observe(viewLifecycleOwner) { tracksResponse ->
            if (tracksResponse != null && userId != null) {
                if (tracksResponse.items.isNotEmpty()) {
                    Log.e("futa", "NUMERO TRACCE: ${tracksResponse.items}")
                    firebaseViewModel.saveTracksToFirebase(userId, tracksResponse.items)
                    firebaseViewModel.fetchTopTracksFromFirebase()
                }
            }
        }


    }



    override fun onResume() {
        super.onResume()



    }

    override fun onPause() {
        super.onPause()
        Log.d("Debug", "Fragment paused") // Aggiungi questo log
    }

   private fun startSpotifyAuthentication() {
        val authUrl = "https://accounts.spotify.com/authorize?client_id=f81649b34ef74684b08943e7ce931d23&response_type=code&redirect_uri=myapp://callback&scope=user-read-private%20user-top-read"
        val intent = Intent (Intent.ACTION_VIEW, Uri.parse(authUrl))
        activity?.startActivity(intent)
    }
    fun handleIntent(intent: Intent?) {
        Log.d("PRIMO LOG FRAGMENT", "Handling Intent: $intent")
        val uri = intent?.data
        val code = uri?.getQueryParameter("code")
        if (code != null) {
            spotifyViewModel.getAccessToken(code)
            Log.d("secondo LOG FRAGMENT", "Handling Intent: $intent")

        }
        else
            Log.d("secondo LOG FRAGMENT", "INTENT VUOTO")
    }

   */







/*
        val nextButton1 = rootView.findViewById<Button>(R.id.nextButton1) //per ottenere il Button dal layout
        nextButton1.setOnClickListener { //setOnClickListener con una lambda che contiene l'azione da eseguire quando l'elemento viene cliccato
            val navController = Navigation.findNavController(requireView())
            navController.navigate(R.id.navigateToSecondFragment)
        }

        val nextButton3 = rootView.findViewById<Button>(R.id.nextButton3) //per ottenere il Button dal layout
        nextButton3.setOnClickListener { //setOnClickListener con una lambda che contiene l'azione da eseguire quando l'elemento viene cliccato
            val navController = Navigation.findNavController(requireView())
            navController.navigate(R.id.navigateToThirdFragment)
        }
*/




