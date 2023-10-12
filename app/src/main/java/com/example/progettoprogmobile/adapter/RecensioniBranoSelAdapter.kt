package com.example.progettoprogmobile.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.progettoprogmobile.R
import com.example.progettoprogmobile.model.Recensione
import com.example.progettoprogmobile.model.Utente


class RecensioniBranoSelAdapter(     var recensioni: List<Recensione>,
                                     var usersMap: Map<String, Utente> = mapOf() ) : RecyclerView.Adapter<RecensioniBranoSelAdapter.ViewHolder>() {

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

        holder.txtRecensione.text = recensione.content // supponendo che Recensione abbia un campo 'content' per il contenuto della recensione

//        user?.let {
//            holder.txtUserName.text = it.name
//            Glide.with(holder.itemView.context)
//                .load(it.images) // assumendo che l'oggetto utente abbia un campo 'profileImage' per l'URL dell'immagine
//                .into(holder.imgProfile)
//        }
    }


    override fun getItemCount(): Int = recensioni.size



}