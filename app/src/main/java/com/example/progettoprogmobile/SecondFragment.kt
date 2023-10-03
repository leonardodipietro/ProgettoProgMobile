package com.example.progettoprogmobile

import androidx.navigation.Navigation
import android.widget.TextView
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import UtenteAdapter
import android.util.Log
import android.widget.Button
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView;
import com.example.progettoprogmobile.viewModel.FirebaseViewModel
import com.example.progettoprogmobile.model.Utente
import com.example.progettoprogmobile.viewModel.SharedDataViewModel
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
class SecondFragment : Fragment() {

    private lateinit var firebaseViewModel: FirebaseViewModel
    private lateinit var utenteAdapter:UtenteAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {

        //val rootView = inflater.inflate(R.layout.fragment_second, container, false)
        firebaseViewModel = ViewModelProvider(this)[FirebaseViewModel::class.java]
        val rootView = inflater.inflate(R.layout.fragment_second, container, false)
        val recyclerView: RecyclerView = rootView.findViewById(R.id.recyclerViewsearchuser)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        utenteAdapter = UtenteAdapter()
        recyclerView.adapter = utenteAdapter
        // Qui stai osservando i cambiamenti nella lista di utenti (_users) nel ViewModel
        // e ogni volta che la lista cambia, aggiorni la lista dell'Adapter e fai log
        firebaseViewModel._users.observe(viewLifecycleOwner, Observer { users ->
            // Log per visualizzare quante utenti sono stati ricevuti dal ViewModel
            Log.d("SecondFragment", "Received ${users.size} users from ViewModel")

            // Aggiorni la lista dell'Adapter con la nuova lista di utenti
            utenteAdapter.submitList(users)
        })

        val searchView: androidx.appcompat.widget.SearchView = rootView.findViewById(R.id.searchviewuser)
        searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if(!query.isNullOrEmpty()) {
                    firebaseViewModel.cercaUtenti(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })
        return rootView
    }

}