package com.example.progettoprogmobile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.widget.SearchView
import com.example.progettoprogmobile.viewModel.FirebaseViewModel
import android.util.Log
import com.example.progettoprogmobile.adapter.UtenteAdapter
import com.example.progettoprogmobile.model.Utente

class SecondFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var firebaseViewModel: FirebaseViewModel
    private lateinit var utenteAdapter: UtenteAdapter
    var utente: Utente? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val rootView = inflater.inflate(R.layout.fragment_second, container, false)

        recyclerView = rootView.findViewById(R.id.recyclerViewsearchuser)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Inizializza l'adapter senza dati
        utenteAdapter = UtenteAdapter { userId ->
            Log.d("CallingCode", "Value of userId: $userId")
            onUserSelected(userId)
        }

        recyclerView.adapter = utenteAdapter

        firebaseViewModel = ViewModelProvider(this)[FirebaseViewModel::class.java]

        // Osserva i cambiamenti nella lista di utenti nel ViewModel
        firebaseViewModel._users.observe(viewLifecycleOwner, Observer { users ->
            Log.d("SecondFragment", "Received users: $users")
            // Aggiorna la lista di utenti nel tuo adattatore e quindi nella RecyclerView
            utenteAdapter.setUtenti(users)
            // Notifica all'adapter che i dati sono stati aggiornati
            utenteAdapter.notifyDataSetChanged()
        })

        val searchView: SearchView = rootView.findViewById(R.id.searchviewuser)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrEmpty()) {
                    firebaseViewModel.cercaUtenti(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Gestisci la ricerca in tempo reale qui
                if (!newText.isNullOrEmpty()) {
                    firebaseViewModel.cercaUtenti(newText)
                }
                return true
            }
        })
        return rootView
    }

    // Quando un utente seleziona un altro utente nella tua RecyclerView
    fun onUserSelected(userId: String) {
        Log.d("SecondFragment", "onUserSelected called with userId: $userId")

        val backStackEntryCountBefore = requireActivity().supportFragmentManager.backStackEntryCount
        Log.d("BackStackEntryCount", "BackStackEntryCount Before: $backStackEntryCountBefore")

        if (!userId.isNullOrEmpty()) {
            // Chiamare la funzione per ottenere i dati dell'utente
            firebaseViewModel.fetchUserDataFromFirebase(userId, object : FirebaseViewModel.OnUserFetchedListener {
                override fun onUserFetched(utente: Utente) {
                    // Esegui le azioni desiderate con l'oggetto Utente ricevuto
                    Log.d("SecondFragment", "User selected: ${utente.name}, ID: ${utente.userId}")

                    // Passare l'oggetto Utente al tuo FifthFragment
                    val bundle = Bundle().apply {
                        putString("userId", utente.userId)
                    }
                    val fifthFragment = FifthFragment()
                    fifthFragment.arguments = bundle
                    //val transaction = requireActivity().supportFragmentManager.beginTransaction()

                    parentFragmentManager.beginTransaction()
                        .replace(R.id.nav_host_fragment, fifthFragment) //fragment_container_second
                        .addToBackStack(null) // Rimuovi il fragment precedente dallo stack
                        .commit()
                }
            })
            val backStackEntryCountAfter = requireActivity().supportFragmentManager.backStackEntryCount
            Log.d("BackStackEntryCount", "BackStackEntryCount After: $backStackEntryCountAfter")
        } else {
            Log.e("SecondFragment", "Invalid userId: $userId")
        }

        // Imposta l'utente selezionato nel ViewModel
        firebaseViewModel.setSelectedUser(utente)
        Log.d("SecondFragment", "Utente impostato nel ViewModel: $utente")
    }
}