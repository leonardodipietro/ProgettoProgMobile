package com.example.progettoprogmobile.adapter
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.progettoprogmobile.BranoSelezionato
import com.example.progettoprogmobile.R
import com.example.progettoprogmobile.model.Risposta
import com.example.progettoprogmobile.model.Utente
import com.google.firebase.database.FirebaseDatabase


class RisposteAdapterBranoSel(
    private var commentList: List<Risposta>,
    private var usersMap: Map<String, Utente> = emptyMap(),
    private val listener: BranoSelezionato
) : RecyclerView.Adapter<RisposteAdapterBranoSel.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_commenti, parent, false)
        return ViewHolder(itemView)
    }
    interface OnCommentiInteractionListener {
        fun onEliminaClicked(answerId:String,userId:String)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val risposta = commentList[position]
        val userName = usersMap[risposta.userId]?.name ?: "Nome non disponibile"

        Log.d("RisposteAdapter", "Mostrando risposta di ${risposta.userId}: $userName")
        holder.userNameComment.text = userName
        holder.commentContent.text = risposta.answercontent

    }

    override fun getItemCount(): Int = commentList.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userNameComment: TextView = itemView.findViewById(R.id.userNameComment)
        val commentContent: TextView = itemView.findViewById(R.id.commentContent)
        private val btnEliminaCommento: Button = itemView.findViewById(R.id.eliminacommento)

        init {
            btnEliminaCommento.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val commento = commentList[position]
                    //listener.onEliminaClicked(commento.answerId, commento.userId)
                    val commentReference = FirebaseDatabase.getInstance().getReference("answers")
                    commentReference.child(commento.answerId).removeValue()

                        .addOnSuccessListener {
                            //POTREBBE ESSERE TOLTO VEDIAMO
                            //sharedEditTextVisibilityManager.setEditTextVisibility(isVisible = true)
                            Log.d("COMMENTTO ELIMINATO","COMMENTO ELIMINATO")
                        }
                        .addOnFailureListener { e ->
                            Log.d("qualcosa è andato storto","qualcosa è andato storto")
                        }
                }
            }
        }
    }

    fun updateDataComment(newCommentList: List<Risposta>, newUserMap: Map<String, Utente>) {
        this.commentList = newCommentList
        this.usersMap = newUserMap
        // Aggiorna la mappa delle risposte con le nuove risposte

        notifyDataSetChanged()
    }





    fun updateUserMap(newUsersMap: Map<String, Utente>) {
        this.usersMap = newUsersMap
        Log.d("AdapterDebugAAAAA", "Aggiornamento mappa utenti con ${newUsersMap.size} utenti.")
        notifyDataSetChanged() // Notifica che i dati sono cambiati per aggiornare la visualizzazione
    }
}

