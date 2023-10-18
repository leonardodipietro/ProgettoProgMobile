package com.example.progettoprogmobile.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.progettoprogmobile.R
import com.example.progettoprogmobile.model.Utente
import android.util.Log

class UtenteAdapter(private val onUserSelected: (String) -> Unit) :
    ListAdapter<Utente, UtenteAdapter.UtenteViewHolder>(UtenteDiffCallback()) {

    // Crea una nuova istanza di UtenteViewHolder quando necessario
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UtenteViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_utente_view, parent, false)
        return UtenteViewHolder(itemView)
    }

    // Associa i dati dell'utente alla vista quando viene richiesto
    override fun onBindViewHolder(holder: UtenteViewHolder, position: Int) {
        val utente = getItem(position)
        holder.bind(utente)

        // Aggiungi un listener per l'evento di clic sulla vista dell'utente
        holder.itemView.setOnClickListener {
            val userId = utente.userId
            Log.d("UtenteAdapter", "Item clicked with userId: $userId")
            if (!userId.isNullOrEmpty()) {
                onUserSelected(userId)
            } else {
                Log.e("UtenteAdapter", "Invalid userId for item at position $position")
            }
        }
    }

    // ViewHolder per visualizzare gli elementi dell'utente nella RecyclerView
    inner class UtenteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewNome: TextView = itemView.findViewById(R.id.textViewNome)

        // Associa i dati dell'utente ai widget nella vista
        fun bind(utente: Utente) {
            textViewNome.text = utente.name
        }
    }

    // Callback per determinare se due elementi nella lista sono gli stessi (stessa identità) o contengono gli stessi dati
    private class UtenteDiffCallback : DiffUtil.ItemCallback<Utente>() {
        override fun areItemsTheSame(oldItem: Utente, newItem: Utente): Boolean {
            return oldItem.userId == newItem.userId
        }

        override fun areContentsTheSame(oldItem: Utente, newItem: Utente): Boolean {
            return oldItem == newItem
        }
    }

    // Funzione per impostare la lista di utenti nella RecyclerView
    fun setUtenti(utenti: List<Utente>) {
        submitList(utenti)
    }
}