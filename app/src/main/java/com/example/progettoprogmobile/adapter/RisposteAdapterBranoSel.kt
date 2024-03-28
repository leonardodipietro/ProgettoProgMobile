package com.example.progettoprogmobile.adapter
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.progettoprogmobile.BranoSelezionato
import com.example.progettoprogmobile.R
import com.example.progettoprogmobile.model.Risposta
import com.example.progettoprogmobile.model.Utente
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso


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
        val user = usersMap[risposta.userId]
        val userName = user?.name ?: "Nome non disponibile"
        val userProfileImageUrl = user?.userImage

        Log.d("RisposteAdapter", "Mostrando risposta di ${risposta.userId}: $userName")
        holder.userNameComment.text = userName
        holder.commentContent.text = risposta.answercontent

        if (userProfileImageUrl.isNullOrEmpty()) {

            holder.imgProfile.setImageResource(R.drawable.baseline_person_24)
        } else {

            Picasso.get()
                .load(userProfileImageUrl)
                .error(R.drawable.baseline_person_24) // Immagine di fallback in caso di errore nel caricamento.
                .into(holder.imgProfile)
        }
    }
        /*usersMap[risposta.userId]?.let { user ->
            txtUserName.text = user.name
            if (!user.userImage.isNullOrEmpty()) {
                Log.d("PicassoLoading", "Caricamento immagine per ${user.name} URL: ${user.userImage}")
                Picasso.get().load(user.userImage).into(imgProfile)
            } else {
                // Qui imposti un'immagine di fallback o lasci l'immagine corrente
                imgProfile.setImageResource(R.drawable.baseline_person_24) // Immagine di fallback
            }
        }*/

    override fun getItemCount(): Int = commentList.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userNameComment: TextView = itemView.findViewById(R.id.userNameComment)
        val commentContent: TextView = itemView.findViewById(R.id.commentContent)
        val imgProfile: ImageView = itemView.findViewById(R.id.imageProfileRisp)
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


        notifyDataSetChanged()
    }





    fun updateUserMap(newUsersMap: Map<String, Utente>) {
        this.usersMap = newUsersMap
        Log.d("AdapterDebugAAAAA", "Aggiornamento mappa utenti con ${newUsersMap.size} utenti.")
        notifyDataSetChanged() // Notifica che i dati sono cambiati per aggiornare la visualizzazione
    }
}

