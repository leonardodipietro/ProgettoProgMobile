package com.example.progettoprogmobile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.progettoprogmobile.viewModel.SpotifyViewModel
import androidx.recyclerview.widget.RecyclerView
import com.example.progettoprogmobile.adapter.TrackAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.progettoprogmobile.viewModel.FirebaseAuthViewModel
import com.example.progettoprogmobile.viewModel.FirebaseViewModel
import com.example.progettoprogmobile.viewModel.SharedDataViewModel
import com.google.firebase.auth.FirebaseAuth

class SecondActivity : AppCompatActivity() {


    // private lateinit var database: DatabaseReference
    private lateinit var recyclerView: RecyclerView
    private lateinit var trackAdapter: TrackAdapter
    private lateinit var firebaseViewModel:FirebaseViewModel
    private lateinit var firebaseauthviewModel: FirebaseAuthViewModel
    private lateinit var spotifyViewModel: SpotifyViewModel
    private lateinit var sharedViewModel: SharedDataViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)
        val signOut = findViewById<Button>(R.id.signOut)
        val delete = findViewById<Button>(R.id.delete)

        // Inizializza la ViewModel
        firebaseauthviewModel = ViewModelProvider(this)[FirebaseAuthViewModel::class.java]
        spotifyViewModel = ViewModelProvider(this)[SpotifyViewModel::class.java]
        firebaseViewModel = ViewModelProvider(this)[FirebaseViewModel::class.java]
        sharedViewModel = ViewModelProvider(this)[SharedDataViewModel::class.java]

        if (savedInstanceState == null) {
            // Verifica che non sia un ripristino da stato salvato
            val fragment = FirstFragment() // Crea un'istanza del tuo fragment
            supportFragmentManager.beginTransaction()
                .replace(R.id.firstFragment, fragment) // Sostituisci il contenuto con il tuo fragment
                .commit()
        }




        signOut.setOnClickListener{
            firebaseauthviewModel.signOut(applicationContext)
        }

        delete.setOnClickListener{
            firebaseauthviewModel.delete(applicationContext)
        }


        val userId = FirebaseAuth.getInstance().currentUser?.uid
        // Recupera le ultime 50 tracce dal database Firebase e visualizzale nella RecyclerView
        firebaseViewModel.fetchTopTracksFromFirebase()

        firebaseauthviewModel.signOutResult.observe(this) { result ->
            if (result == FirebaseAuthViewModel.SignOutResult.SUCCESS) {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            } else {
                //logout non riuscito
            }
        }

        firebaseauthviewModel.deleteResult.observe(this) { result ->
            if (result == FirebaseAuthViewModel.DeleteResult.SUCCESS) {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            } else {
                //eliminazione dell'account non riuscita
            }
        }


        // Trova il componente BottomNavigationView nel layout XML e lo assegna ad una variabile
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        // Imposta un listener per la selezione degli elementi nel BottomNavigationView
        bottomNavigationView.setOnItemSelectedListener { item ->
            // Gestisci la selezione in base all'ID dell'elemento selezionato
            when (item.itemId) {
                R.id.home -> {
                    // Trova il NavController e naviga al primo fragment
                    val navController = Navigation.findNavController(this, R.id.nav_host_fragment)
                    navController.navigate(R.id.firstFragment)
                    true // Indica che la selezione è stata gestita con successo
                }
                R.id.profile -> {
                    // Trova il NavController e naviga al secondo fragment
                    val navController = Navigation.findNavController(this, R.id.nav_host_fragment)
                    navController.navigate(R.id.secondFragment)
                    true // Indica che la selezione è stata gestita con successo
                }
                R.id.settings -> {
                    // Trova il NavController e naviga al terzo fragment
                    val navController = Navigation.findNavController(this, R.id.nav_host_fragment)
                    navController.navigate(R.id.thirdFragment)
                    true // Indica che la selezione è stata gestita con successo
                }
                else -> false // Gestione predefinita nel caso in cui l'ID dell'elemento non corrisponda a nessuno dei casi precedenti
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.drawer_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // Naviga al primo fragment quando l'opzione del menu è selezionata
            R.id.menu_first_fragment -> {
                val navController = Navigation.findNavController(this, R.id.nav_host_fragment)
                navController.navigate(R.id.firstFragment)
                return true
            }
            R.id.menu_second_fragment -> {
                // Naviga al secondo fragment quando l'opzione del menu è selezionata
                val navController = Navigation.findNavController(this, R.id.nav_host_fragment)
                navController.navigate(R.id.secondFragment)
                return true
            }
            R.id.menu_third_fragment -> {
                // Naviga al terzo fragment quando l'opzione del menu è selezionata
                val navController = Navigation.findNavController(this, R.id.nav_host_fragment)
                navController.navigate(R.id.thirdFragment)
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }


    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.d("PRIMO LOG ACTIVITY", "ONNEWINTENTCHIAMATA")
        // Verifica se l'Intent contiene dati
        if (intent != null && intent.data != null) {
            Log.d("SECONDO LOG ACTIVITY", "Intent contain data:  $intent")
            // Passa l'Intent ricevuto al tuo Fragment corrente
            val currentFragment = supportFragmentManager.findFragmentById(R.id.firstFragment)
            Log.d("TERZO LOG ACTIVITY", "Intent contain data:  ${R.id.firstFragment}")
            Log.d("QUARTO LOG ACTIVITY", "Intent contain data:  $currentFragment")
            if (currentFragment is FirstFragment) {
                currentFragment.handleIntent(intent)

            }
        } else {
            Log.d("SECONDO LOG ACTIVITY", "Intent does not contain data")
        }
    }




}


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
