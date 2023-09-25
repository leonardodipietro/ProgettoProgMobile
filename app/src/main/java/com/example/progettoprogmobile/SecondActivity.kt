package com.example.progettoprogmobile
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.example.progettoprogmobile.adapter.TrackAdapter
import com.example.progettoprogmobile.viewModel.FirebaseAuthViewModel
import com.example.progettoprogmobile.viewModel.FirebaseViewModel
import com.example.progettoprogmobile.viewModel.SharedDataViewModel
import com.example.progettoprogmobile.viewModel.SpotifyViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView

class SecondActivity : AppCompatActivity() {

    private lateinit var trackAdapter: TrackAdapter
    private lateinit var firebaseViewModel: FirebaseViewModel
    private lateinit var firebaseauthviewModel: FirebaseAuthViewModel
    private lateinit var spotifyViewModel: SpotifyViewModel
    private lateinit var sharedViewModel: SharedDataViewModel
    private val fragmentManager: FragmentManager = supportFragmentManager
    private lateinit var currentFragment: Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)


        // Inizializza la ViewModel
        firebaseauthviewModel = ViewModelProvider(this)[FirebaseAuthViewModel::class.java]
        spotifyViewModel = ViewModelProvider(this)[SpotifyViewModel::class.java]
        firebaseViewModel = ViewModelProvider(this)[FirebaseViewModel::class.java]
        sharedViewModel = ViewModelProvider(this)[SharedDataViewModel::class.java]



        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navControllerHome = navHostFragment.navController






        // Verifica se l'activity è stata avviata per la prima volta (savedInstanceState == null)
        // e se è così, sostituisci il contenuto con il tuo fragment iniziale (FirstFragment)
        if (savedInstanceState == null) {
            val fragment = TrackGen()
            fragmentManager.beginTransaction()
                .add(R.id.nav_host_fragment, fragment, "trackGen")
                .commit()
            currentFragment = fragment
        }


        // Trova il componente BottomNavigationView nel layout XML e lo assegna ad una variabile
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)


        // Imposta un listener per la selezione degli elementi nel BottomNavigationView
        bottomNavigationView.setOnItemSelectedListener { item ->
            val transaction: FragmentTransaction = fragmentManager.beginTransaction()

            // Nascondi il fragment corrente
            transaction.hide(currentFragment)

            // Gestisci la selezione in base all'ID dell'elemento selezionato
            when (item.itemId) {
                R.id.home -> {
                    // Se il fragment è già stato creato, mostra semplicemente il fragment
                    val fragment = fragmentManager.findFragmentByTag("firstFragment")
                    if (fragment != null) {
                        transaction.show(fragment)
                        currentFragment = fragment
                    } else {
                        // Altrimenti, crea un nuovo fragment e aggiungilo
                        val newFragment = FirstFragment()
                        transaction.add(R.id.nav_host_fragment, newFragment, "firstFragment")
                        currentFragment = newFragment
                    }
                }
                R.id.profile -> {
                    val fragment = fragmentManager.findFragmentByTag("secondFragment")
                    if (fragment != null) {
                        transaction.show(fragment)
                        currentFragment = fragment
                    } else {
                        val newFragment = SecondFragment()
                        transaction.add(R.id.nav_host_fragment, newFragment, "secondFragment")
                        currentFragment = newFragment
                    }
                }
                R.id.settings -> {
                    val fragment = fragmentManager.findFragmentByTag("thirdFragment")
                    if (fragment != null) {
                        transaction.show(fragment)
                        currentFragment = fragment
                    } else {
                        val newFragment = ThirdFragment()
                        transaction.add(R.id.nav_host_fragment, newFragment, "thirdFragment")
                        currentFragment = newFragment
                    }
                }
            }
            transaction.commit()
            true // Indica che la selezione è stata gestita con successo
        }


    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.d("PRIMO LOG ACTIVITY", "ONNEWINTENTCHIAMATA")
        // Verifica se l'Intent contiene dati
        if (intent != null && intent.data != null) {
            Log.d("SECONDO LOG ACTIVITY", "Intent contain data:  $intent")
            // Passa l'Intent ricevuto al tuo Fragment corrente
            val currentFragment = fragmentManager.findFragmentByTag("trackGen")
            Log.d("TERZO LOG ACTIVITY", "Intent contain data:  ${R.id.trackGen}")
            Log.d("QUARTO LOG ACTIVITY", "Intent contain data:  $currentFragment")
            if (currentFragment is TrackGen) {
                currentFragment.handleIntent(intent)
            }
        } else {
            Log.d("SECONDO LOG ACTIVITY", "Intent does not contain data")
        }
    }
}



    //SEMMAI SI RIUSA QUESTA PARTE SUCCESSIVAMENTE
   /* override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.drawer_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val transaction: FragmentTransaction = fragmentManager.beginTransaction()

        when (item.itemId) {
            // Naviga al primo fragment quando l'opzione del menu è selezionata
            R.id.menu_first_fragment -> {
                val fragment = fragmentManager.findFragmentByTag("firstFragment")
                if (fragment != null) {
                    transaction.show(fragment)
                    currentFragment = fragment
                } else {
                    val newFragment = FirstFragment()
                    transaction.add(R.id.nav_host_fragment, newFragment, "firstFragment")
                    currentFragment = newFragment
                }
            }
            R.id.menu_second_fragment -> {
                val fragment = fragmentManager.findFragmentByTag("secondFragment")
                if (fragment != null) {
                    transaction.show(fragment)
                    currentFragment = fragment
                } else {
                    val newFragment = SecondFragment()
                    transaction.add(R.id.nav_host_fragment, newFragment, "secondFragment")
                    currentFragment = newFragment
                }
            }
            R.id.menu_third_fragment -> {
                val fragment = fragmentManager.findFragmentByTag("thirdFragment")
                if (fragment != null) {
                    transaction.show(fragment)
                    currentFragment = fragment
                } else {
                    val newFragment = ThirdFragment()
                    transaction.add(R.id.nav_host_fragment, newFragment, "thirdFragment")
                    currentFragment = newFragment
                }
            }
            else -> return super.onOptionsItemSelected(item)
        }
        transaction.commit()
        return true
    }
*/








/*   spotifyviewModel.spotifyTokenResponse.observe(this) { tokenResponse ->
       if (tokenResponse?.access_token != null) {
           Log.d("SpotifyToken", "Token ottenuto: ${tokenResponse.access_token}")

           // Una volta ottenuto il token, recupera le tracce più ascoltate
           spotifyviewModel.fetchTopTracks(tokenResponse.access_token)
       } else {
           Log.d("SpotifyToken", "Nessun token ottenuto!")
       }
   }

   spotifyviewModel.error.observe(this) { throwable ->
       Log.e("SpotifyTokenError", "Errore durante la richiesta del token", throwable)
   }

*/
/*
        spotifyviewModel.topTracks.observe(this) { tracksResponse ->
            if (tracksResponse != null && userId != null) {
                // Utilizza il FirebaseViewModel per salvare le tracce nel database Firebase
                firebaseViewModel.saveTracksToFirebase(userId, tracksResponse.items)
                firebaseViewModel.fetchTopTracksFromFirebase()
            }
        } */

// Dopo aver chiamato fetchTopTracksFromFirebase, configura la RecyclerView e osserva topTracksfromdb
