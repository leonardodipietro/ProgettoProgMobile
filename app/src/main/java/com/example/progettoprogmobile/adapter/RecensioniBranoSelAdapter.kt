package com.example.progettoprogmobile.adapter

import android.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.progettoprogmobile.BranoSelezionato
import com.example.progettoprogmobile.R
import com.example.progettoprogmobile.model.Recensione
import com.example.progettoprogmobile.model.Risposta
import com.example.progettoprogmobile.model.Utente
import com.squareup.picasso.Picasso

class RecensioniBranoSelAdapter(
    var recensioni: List<Recensione>,
    private val listener: OnRecensioneInteractionListener,
    var usersMap: Map<String, Utente> = mapOf()

) : RecyclerView.Adapter<RecensioniBranoSelAdapter.ViewHolder>() {

    var currentUserId: String? = null

    private var risposteMap: MutableMap<String, List<Risposta>> = mutableMapOf()

    var isCommentsVisible: Boolean = false
    var showComments: Boolean = false
        set(value) {
            field = value
            notifyDataSetChanged() // Notifica i cambiamenti per aggiornare la UI
        }

    interface OnRecensioneInteractionListener {
        fun onRecensioneDeleteClicked(commentId: String, userId: String)
        fun onRecensioneModificaClicked(recensione:Recensione)
        fun onRecensioneCommentaClicked(recensione: Recensione)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgProfile: ImageView = view.findViewById(R.id.improfBranoSelezionato)
        val txtUserName: TextView = view.findViewById(R.id.nomeutenteBranoselezionato)
        val txtRecensione: TextView = view.findViewById(R.id.recensione1)
         val btnDeleteRecensione: Button = view.findViewById(R.id.eliminarecensione)
         val btnModificaRecensione: Button = view.findViewById(R.id.modificarecensione)
         val btnCommentaRecensione: ImageButton = view.findViewById(R.id.commentarecensione)
        val risposteRecyclerView:RecyclerView =  view.findViewById(R.id.risposteRecyclerView)

        init {
            btnCommentaRecensione.setOnClickListener{
                Log.d("DDDD","bottone commento cliccato")

                // Controlla la visibilità corrente della RecyclerView
                if (risposteRecyclerView.visibility == View.VISIBLE) {
                    // Se è visibile, nascondila
                    risposteRecyclerView.visibility = View.GONE
                } else {
                    // Se è nascosta, mostrala
                    risposteRecyclerView.visibility = View.VISIBLE
                }

                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val recensione = recensioni[position]
                    listener.onRecensioneCommentaClicked(recensione)
                }
            }
            /*view.setOnLongClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val recensione = recensioni[position]
                    if (recensione.userId == currentUserId) {
                    val opzioni = arrayOf("Modifica", "Elimina")

                    AlertDialog.Builder(itemView.context)
                        .setTitle("Seleziona Azione")
                        .setItems(opzioni) { dialog, which ->
                            when (which) {
                                0 -> listener.onRecensioneModificaClicked(recensione)
                                1 -> listener.onRecensioneDeleteClicked(recensione.commentId, recensione.userId)
                            }
                        }.show()
                    }
                }
                true // Indica che l'evento di click è stato gestito
            }*/
            btnModificaRecensione.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val recensione = recensioni[position]
                    listener.onRecensioneModificaClicked(recensione)
                }
            }
            btnDeleteRecensione.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val recensione = recensioni[position]
                    listener.onRecensioneDeleteClicked(recensione.commentId, recensione.userId)
                }
            }
        }
        fun toggleCommentVisibility(showComments: Boolean) {
            // Aggiorna la visibilità della RecyclerView dei commenti
            risposteRecyclerView.visibility = if (showComments) View.VISIBLE else View.GONE
        }


        fun bind(recensione: Recensione,showComments: Boolean) {
            txtRecensione.text = recensione.content
            usersMap[recensione.userId]?.let { user ->
                txtUserName.text = user.name
                if (!user.userImage.isNullOrEmpty()) {
                    Log.d("PicassoLoading", "Caricamento immagine per ${user.name} URL: ${user.userImage}")
                    Picasso.get().load(user.userImage).into(imgProfile)
                } else {

                    imgProfile.setImageResource(R.drawable.baseline_person_24) // Immagine di fallback
                }
            }
            val isUserReview = recensione.userId == currentUserId
            //btnDeleteRecensione.visibility = if (isUserReview) View.VISIBLE else View.GONE
            //btnModificaRecensione.visibility = if (isUserReview) View.VISIBLE else View.GONE


            //SIRECUPERA LA LISTA DELLE RISPOSTE
            val risposte = risposteMap[recensione.commentId] ?: listOf()

            //SI CREA IL NUOVO ADAPTER
            val adapterRisposte = RisposteAdapterBranoSel(risposte,usersMap, BranoSelezionato())
            risposteRecyclerView.layoutManager = LinearLayoutManager(itemView.context)
            risposteRecyclerView.adapter = adapterRisposte
            risposteRecyclerView.visibility = if (showComments) View.VISIBLE else View.GONE

        }

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_recensioni, parent, false)
        return ViewHolder(view)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val recensione = recensioni[position]
        val risposte = risposteMap[recensione.commentId] ?: listOf()
        // Controlla se l'ID utente corrente corrisponde all'ID dell'utente della recensione
        val isUserRecensione = recensione.userId == currentUserId

        // Imposta la visibilità dei bottoni basandoti sulla condizione
        holder.btnDeleteRecensione.visibility = if (isUserRecensione) View.VISIBLE else View.GONE
        holder.btnModificaRecensione.visibility = if (isUserRecensione) View.VISIBLE else View.GONE


        holder.bind(recensione, isCommentsVisible)

        // Aggiorna l'adapter delle risposte con la nuova mappa degli utenti
        holder.risposteRecyclerView.adapter?.let { adapter ->
            if (adapter is RisposteAdapterBranoSel) {
                adapter.updateUserMap(usersMap)
            }
        }
    }
    override fun getItemCount(): Int = recensioni.size
    fun updateData(newRecensioni: List<Recensione>, newUsersMap: Map<String, Utente>) {
        recensioni = newRecensioni
        usersMap = newUsersMap
        notifyDataSetChanged()
    }
    fun updateUserMap(newUsersMap: Map<String, Utente>) {
        this.usersMap = newUsersMap
        notifyDataSetChanged()
    }

    fun updateRisposteMap(newRisposteMap: Map<String, List<Risposta>>) {
        risposteMap.clear()
        risposteMap.putAll(newRisposteMap)

        notifyDataSetChanged() // Notifica che i dati sono cambiati per ricaricare la RecyclerView
    }
    fun updateNameComment(newUsersMap: Map<String, Utente>) {
        this.usersMap = newUsersMap
        // Notifica all'adapter delle risposte di ogni recensione che ci sono stati aggiornamenti
        recensioni.forEach { recensione ->
            val risposte = risposteMap[recensione.commentId] ?: listOf()

        }
        notifyDataSetChanged() // Notifica che i dati dell'adapter principale sono cambiati
    }
}