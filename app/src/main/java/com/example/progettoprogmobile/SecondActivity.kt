package com.example.progettoprogmobile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.lifecycle.ViewModelProvider
import com.example.progettoprogmobile.viewModel.FirebaseViewModel
import com.google.firebase.auth.FirebaseAuth

class SecondActivity : AppCompatActivity() {

    private val fragmentManager: FragmentManager = supportFragmentManager
    private lateinit var currentFragment: Fragment
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var firebaseViewModel: FirebaseViewModel // Dichiarazione del tuo ViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_second)

        // Inizializza il tuo ViewModel
        firebaseViewModel = ViewModelProvider(this).get(FirebaseViewModel::class.java)

        // Verifica se l'activity è stata avviata per la prima volta (savedInstanceState == null)
        // e se è così, sostituisci il contenuto con il tuo fragment iniziale (FirstFragment)
        if (savedInstanceState == null) {
            val fragment = FirstFragment()
            fragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, fragment, "firstFragment")
                .commit()
            currentFragment = fragment
        }

        // Trova il componente BottomNavigationView nel layout XML e lo assegna ad una variabile
        bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        // Imposta un listener per la selezione degli elementi nel BottomNavigationView
        bottomNavigationView.setOnItemSelectedListener { item ->
            val transaction: FragmentTransaction = fragmentManager.beginTransaction()

            // Rimuovi il fragment corrente
            transaction.remove(currentFragment)


            // Gestisci la selezione in base all'ID dell'elemento selezionato
            when (item.itemId) {
                R.id.home -> {
                    // Crea un nuovo fragment e sostituiscilo
                    val newFragment = FirstFragment()
                    transaction.replace(R.id.nav_host_fragment, newFragment, "firstFragment")
                    currentFragment = newFragment
                }
                R.id.cerca -> {
                    val newFragment = SecondFragment()
                    transaction.replace(R.id.nav_host_fragment, newFragment, "secondFragment")
                    currentFragment = newFragment
                }
                R.id.settings -> {
                    val newFragment = ThirdFragment()
                    transaction.replace(R.id.nav_host_fragment, newFragment, "thirdFragment")
                    currentFragment = newFragment
                }
                R.id.notification -> {
                    val newFragment = FourthFragment()
                    transaction.replace(R.id.nav_host_fragment, newFragment, "fourthFragment")
                    currentFragment = newFragment
                }
            }
            transaction.commit()
            true // Indica che la selezione è stata gestita con successo
        }

        // Verifica se l'utente è registrato e salva le credenziali se necessario
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            // Utilizza il FirebaseViewModel per verificare lo stato di registrazione
            firebaseViewModel.checkUserIdInFirebase(this, userId) { isRegistered ->
                if (!isRegistered) {
                    // L'utente non è registrato, quindi salva le sue credenziali
                    firebaseViewModel.saveUserIdToFirebase(userId)

                    // Aggiorna la UI o esegui altre azioni necessarie
                }
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.drawer_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val transaction: FragmentTransaction = fragmentManager.beginTransaction()

        // Rimuovi il fragment corrente
        transaction.remove(currentFragment)

        when (item.itemId) {
            R.id.menu_first_fragment -> {
                val newFragment = FirstFragment()
                transaction.replace(R.id.nav_host_fragment, newFragment, "firstFragment")
                currentFragment = newFragment
                bottomNavigationView.selectedItemId = R.id.home
            }
            R.id.menu_second_fragment -> {
                val newFragment = SecondFragment()
                transaction.replace(R.id.nav_host_fragment, newFragment, "secondFragment")
                currentFragment = newFragment
                bottomNavigationView.selectedItemId = R.id.cerca

            }
            R.id.menu_third_fragment -> {
                val newFragment = ThirdFragment()
                transaction.replace(R.id.nav_host_fragment, newFragment, "thirdFragment")
                currentFragment = newFragment
                bottomNavigationView.selectedItemId = R.id.settings
            }
            R.id.menu_fourth_fragment -> {
                val newFragment = ThirdFragment()
                transaction.replace(R.id.nav_host_fragment, newFragment, "fourthFragment")
                currentFragment = newFragment
                bottomNavigationView.selectedItemId = R.id.notification
            }
            else -> return super.onOptionsItemSelected(item)
        }
        transaction.commit()
        return true
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.d("PRIMO LOG ACTIVITY", "ONNEWINTENTCHIAMATA")
        // Verifica se l'Intent contiene dati
        if (intent != null && intent.data != null) {
            Log.d("SECONDO LOG ACTIVITY", "Intent contain data:  $intent")
            // Passa l'Intent ricevuto al tuo Fragment corrente
            val currentFragment = fragmentManager.findFragmentByTag("firstFragment")
            Log.d("TERZO LOG ACTIVITY", "Intent contain data:  ${R.id.firstFragment}")
            Log.d("QUARTO LOG ACTIVITY", "Intent contain data:  $currentFragment")
            if (currentFragment is FirstFragment) {
                currentFragment.handleIntent(intent)
            } else {
                Log.d("QUINTO LOG ACTIVITY", "NIENTE")
            }
        } else {
            Log.d("SECONDO LOG ACTIVITY", "Intent does not contain data")
        }
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }
    }
}