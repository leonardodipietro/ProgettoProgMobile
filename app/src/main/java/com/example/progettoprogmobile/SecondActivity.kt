package com.example.progettoprogmobile

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.forEach
import androidx.navigation.Navigation
import com.google.android.material.bottomnavigation.BottomNavigationView

class SecondActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)

        // Trova il componente BottomNavigationView nel layout XML e lo assegna ad una variabile
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        // Imposta un listener per la selezione degli elementi nel BottomNavigationView
        bottomNavigationView.setOnItemSelectedListener { item ->

            // Imposta un listener per la selezione degli elementi nel BottomNavigationView
            val navController = Navigation.findNavController(this, R.id.nav_host_fragment)

            // Gestisce la selezione in base all'ID dell'elemento selezionato
            when (item.itemId) {
                R.id.home -> {
                    navController.navigate(R.id.firstFragment)// Naviga al primo fragment
                }

                R.id.cerca -> {
                    navController.navigate(R.id.secondFragment) // Naviga al secondo fragment
                }

                R.id.settings -> {
                    navController.navigate(R.id.thirdFragment) // Naviga al terzo fragment
                }
            }

            // Aggiungo un listener per i cambiamenti di destinazione
            navController.addOnDestinationChangedListener { _, destination, _ ->

                Log.d("Debug", "Destination ID: ${destination.id}")

                // Aggiorna la selezione dell'icona nella barra di navigazione in base alla destinazione
                bottomNavigationView.menu.forEach { menuItem ->
                    menuItem.isChecked = menuItem.itemId == destination.id
                }
            }
            true
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
}




/*
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
                    //NOTA BENE
                    bottomNavigationView.selectedItemId = R.id.firstFragment // Aggiorna la sezione dell'icona nella barra di navigazione

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
 */