package com.example.progettoprogmobile.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.progettoprogmobile.R
import com.example.progettoprogmobile.model.Recensione
import com.example.progettoprogmobile.model.Utente
import com.firebase.ui.auth.data.model.User

class RecensioniBranoSelAdapter(    private var recensioni: List<Recensione>,
                                    private var usersMap: Map<String, User> = mapOf() ) : RecyclerView.Adapter<RecensioniBranoSelAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgProfile: ImageView = view.findViewById(R.id.improfBranoSelezionato)
        val txtUserName: TextView = view.findViewById(R.id.nomeutenteBranoselezionato)
        val txtRecensione: TextView = view.findViewById(R.id.recensione1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_recensioni, parent, false)
        return ViewHolder(view)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val recensione = recensioni[position]
        val user = usersMap[recensione.userId]


    }


    override fun getItemCount(): Int = recensioni.size



}