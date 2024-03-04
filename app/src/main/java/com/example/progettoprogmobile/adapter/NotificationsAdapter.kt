package com.example.progettoprogmobile.adapter

import android.util.Log
import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.example.progettoprogmobile.model.Utente
import com.example.progettoprogmobile.model.Track
import com.example.progettoprogmobile.BranoSelezionato
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ToggleButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.progettoprogmobile.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import androidx.fragment.app.FragmentActivity
import com.example.progettoprogmobile.FifthFragment


// Sealed class che rappresenta due tipi di notifiche: follower e richieste
sealed class NotificationItem {
    data class FollowerItem(val utente: Utente) : NotificationItem()
    data class RequestItem(val utente: Utente) : NotificationItem()
    data class ReviewItem (val utente: Utente, val track: Track) : NotificationItem()
}

// Adapter per la RecyclerView delle notifiche
class NotificationsAdapter (
    private val context: Context,
    val parent: ViewGroup,
    private val followClickListener: NotificationsAdapter.OnFollowClickListener,
    private val confirmClickListener: NotificationsAdapter.OnConfirmClickListener,
    private val deleteClickListener: NotificationsAdapter.OnDeleteClickListener,
    private val database: FirebaseDatabase,
    private val currentUserId: String
) : ListAdapter<NotificationItem, RecyclerView.ViewHolder>(NotificationItemDiffCallback()) {

    // View utilizzata per l'inflazione del layout del fragment (R.layout.fragment_fourth)
    val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_fourth, parent, false)


    // Interfacce per la gestione degli eventi dei pulsanti nelle notifiche
    interface OnFollowClickListener {
        fun onFollowClickListener(userId: String, isFollowing: Boolean)
    }
    interface OnConfirmClickListener {
        fun onConfirmClickListener (userId: String)
    }
    interface OnDeleteClickListener {
        fun onDeleteClickListener (userId: String)
    }

    // Lista di utenti che seguono l'utente corrente
    private val newFollowers = mutableListOf<Utente>()

    // Aggiungi una variabile per memorizzare il listener del click sull'elemento della recensione
    private var followerItemClickListener: FollowerViewHolder.OnClickListener? = null
    // Funzione per impostare il listener del click sull'elemento della recensione
    fun setFollowerItemClickListener(listener: FollowerViewHolder.OnClickListener) {
        followerItemClickListener = listener
    }

    // Aggiungi una variabile per memorizzare il listener del click sull'elemento della recensione
    private var requestItemClickListener: RequestViewHolder.OnClickListener? = null
    // Funzione per impostare il listener del click sull'elemento della recensione
    fun setRequestItemClickListener(listener: RequestViewHolder.OnClickListener) {
        requestItemClickListener = listener
    }

    // Aggiungi una variabile per memorizzare il listener del click sull'elemento della recensione
    private var reviewItemClickListener: ReviewViewHolder.OnClickListener? = null

    // Funzione per impostare il listener del click sull'elemento della recensione
    fun setReviewItemClickListener(listener: ReviewViewHolder.OnClickListener) {
        reviewItemClickListener = listener
    }

    // ViewHolder per gli elementi del tipo Follower
    class FollowerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val followersImage: ImageView = view.findViewById(R.id.followersImage)
        val followersUsername: TextView = view.findViewById(R.id.followersRequest)
        val followButton: ToggleButton = view.findViewById(R.id.followToggleButton)

        init {
            view.setOnClickListener {
                // Notifica l'evento di click all'adapter
                onClickListener?.onClick(adapterPosition)
            }
        }

        // Interfaccia per il listener del click sull'elemento della recensione
        interface OnClickListener {
            fun onClick(position: Int)
        }

        // Variabile per memorizzare il listener del click sull'elemento della recensione
        var onClickListener: OnClickListener? = null
    }

    // ViewHolder per gli elementi del tipo Request
    class RequestViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val followersImage: ImageView = view.findViewById(R.id.followersImage)
        val followersUsername: TextView = view.findViewById(R.id.followersRequest)
        val confirmButton: Button = view.findViewById(R.id.confirm)
        val deleteButton: Button = view.findViewById(R.id.delete)

        init {
            view.setOnClickListener {
                // Notifica l'evento di click all'adapter
                onClickListener?.onClick(adapterPosition)
            }
        }

        // Interfaccia per il listener del click sull'elemento della recensione
        interface OnClickListener {
            fun onClick(position: Int)
        }

        // Variabile per memorizzare il listener del click sull'elemento della recensione
        var onClickListener: OnClickListener? = null
    }

    // ViewHolder per gli elementi del tipo Review
    class ReviewViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userImage: ImageView = view.findViewById(R.id.followingImage)
        val userNameText: TextView = view.findViewById(R.id.reviewNotification)

        init {
            view.setOnClickListener {
                // Notifica l'evento di click all'adapter
                onClickListener?.onClick(adapterPosition)
            }
        }

        // Interfaccia per il listener del click sull'elemento della recensione
        interface OnClickListener {
            fun onClick(position: Int)
        }

        // Variabile per memorizzare il listener del click sull'elemento della recensione
        var onClickListener: OnClickListener? = null
    }

    // Funzione per sottomettere una nuova lista di utenti al RecyclerView
    fun submitUserList(userList: List<Utente>) {
        val followerItems = userList.map { NotificationItem.FollowerItem(it) }
        submitList(followerItems)
    }

    // Ottiene il tipo dell'elemento nella posizione specificata
    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is NotificationItem.FollowerItem -> TYPE_FOLLOWER
            is NotificationItem.RequestItem -> TYPE_REQUEST
            is NotificationItem.ReviewItem -> TYPE_REVIEW
            else -> throw IllegalArgumentException("Invalid item type at position $position")
        }
    }

    // Crea un nuovo ViewHolder in base al tipo dell'elemento
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_FOLLOWER -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_notifiche_nfa, parent, false)
                FollowerViewHolder(view)
            }
            TYPE_REQUEST -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_notifiche_nfr, parent, false)
                RequestViewHolder(view)
            }

            TYPE_REVIEW -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_notifiche_recensioni, parent, false)
                ReviewViewHolder(view)
            }

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    // Imposta lo stato del ToggleButton in base all'utente corrente sta seguendo o meno il follower
    fun setFollowButtonChecked(userId: String, isFollowing: Boolean) {
        val position = currentList.indexOfFirst {
            when (it) {
                is NotificationItem.FollowerItem -> it.utente.userId == userId
                else -> false
            }
        }

        if (position != -1) {
            // Aggiorna lo stato del ToggleButton
            notifyItemChanged(position, isFollowing)
        }
    }

    // Verifica se l'utente è già presente nella lista di nuovi follower
    fun isFollowerAlreadyAdded(followerId: String): Boolean {
        return newFollowers.any { it.userId == followerId }
    }

    // Verifica se un follower è tra i following dell'utente corrente
    private fun isFollowerInFollowing(followerId: String, onComplete: (Boolean) -> Unit) {
        val followingReference = database.reference.child("users").child(currentUserId).child("following")
        followingReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(followingSnapshot: DataSnapshot) {
                val isFollowerInFollowing = followingSnapshot.hasChild(followerId)
                onComplete(isFollowerInFollowing)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Gestisci eventuali errori nel recupero dei dati di following
                onComplete(false)
            }
        })
    }

    // Associa i dati alla vista in base al tipo di notifica
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is NotificationItem.FollowerItem -> {
                val followerViewHolder = holder as FollowerViewHolder
                val utente = item.utente

                // Carica l'immagine del follower utilizzando Picasso
                if (utente.userImage.isNotEmpty()) {
                    Picasso.get()
                        .load(utente.userImage)
                        .placeholder(R.drawable.baseline_person_24)
                        .into(holder.followersImage)
                }

                // Costruisci il messaggio della notifica
                val message = utente.name + " " + context.getString(R.string.newFollower)
                holder.followersUsername.text = message

                // Verifica se il follower è tra i following del currentUserId
                isFollowerInFollowing(utente.userId) { isFollowerInFollowing ->
                    // Aggiorna il pulsante in base allo stato di isFollowerInFollowing
                    if (holder is FollowerViewHolder) {
                        holder.followButton.isChecked = isFollowerInFollowing
                    }
                }

                // Aggiunge il listener per il pulsante di follow
                holder.followButton.setOnClickListener {
                    val isFollowing = holder.followButton.isChecked
                    if(isFollowing){
                        followClickListener.onFollowClickListener(utente.userId,isFollowing)
                    }
                }

                // Aggiunge il listener per il pulsante di follow anche per il ViewHolder
                followerViewHolder.followButton.setOnClickListener {
                    val isFollowing = followerViewHolder.followButton.isChecked
                    followClickListener.onFollowClickListener(utente.userId, isFollowing)
                }

                // Imposta il listener per il click sull'elemento della recensione
                holder.itemView.setOnClickListener {
                    Log.d("NotificationsAdapter", "Item clicked: ${utente.userId}")
                    val fragment = FifthFragment()
                    val bundle = Bundle().apply {
                        putSerializable("utenteDetail", item.utente)
                    }
                    bundle.putString("userId", item.utente.userId)
                    fragment.arguments = bundle

                    val context = holder.itemView.context
                    val fragmentTransaction = (context as FragmentActivity).supportFragmentManager.beginTransaction()
                    fragmentTransaction.replace(R.id.nav_host_fragment, fragment)
                    fragmentTransaction.addToBackStack(null)
                    fragmentTransaction.commit()
                }
            }
            is NotificationItem.RequestItem -> {
                val requestViewHolder = holder as RequestViewHolder
                val utente = item.utente

                // Carica l'immagine del follower utilizzando Picasso
                if (utente.userImage.isNotEmpty()) {
                    Picasso.get()
                        .load(utente.userImage)
                        .placeholder(R.drawable.baseline_person_24)
                        .into(holder.followersImage)
                }

                // Costruisci il messaggio della notifica
                val message = utente.name + " " + context.getString(R.string.newFRequest)
                requestViewHolder.followersUsername.text = message

                // Aggiunge il listener per il pulsante di conferma
                requestViewHolder.confirmButton.setOnClickListener {
                    confirmClickListener.onConfirmClickListener(utente.userId)
                }

                // Aggiunge il listener per il pulsante di eliminazione
                requestViewHolder.deleteButton.setOnClickListener {
                    deleteClickListener.onDeleteClickListener(utente.userId)
                }


                // Imposta il listener per il click sull'elemento della recensione
                holder.itemView.setOnClickListener {
                    val fragment = FifthFragment()
                    val bundle = Bundle().apply {
                        putSerializable("utenteDetail", item.utente)
                    }
                    bundle.putString("userId", item.utente.userId)
                    fragment.arguments = bundle

                    val context = holder.itemView.context
                    val fragmentTransaction = (context as FragmentActivity).supportFragmentManager.beginTransaction()
                    fragmentTransaction.replace(R.id.nav_host_fragment, fragment)
                    fragmentTransaction.addToBackStack(null)
                    fragmentTransaction.commit()
                }

            }
            is NotificationItem.ReviewItem -> {
                val reviewViewHolder = holder as ReviewViewHolder
                val utente = item.utente

                // Carica l'immagine dell'utente che ha scritto la recensione utilizzando Picasso
                if (utente.userImage.isNotEmpty()) {
                    Picasso.get()
                        .load(utente.userImage)
                        .placeholder(R.drawable.baseline_person_24)
                        .into(reviewViewHolder.userImage)
                }

                // Imposta il nome dell'utente come testo del nome utente
                val message = utente.name + " " + context.getString(R.string.newReviewNotification) + " " + item.track.name
                reviewViewHolder.userNameText.text = message

                // Imposta il listener per il click sull'elemento della recensione
                holder.itemView.setOnClickListener {
                    val fragment = BranoSelezionato()
                    val bundle = Bundle().apply {
                        putSerializable("trackDetail", item.track)
                    }
                    bundle.putString("trackId", item.track.id)
                    fragment.arguments = bundle

                    val context = holder.itemView.context
                    val fragmentTransaction = (context as FragmentActivity).supportFragmentManager.beginTransaction()
                    fragmentTransaction.replace(R.id.nav_host_fragment, fragment)
                    fragmentTransaction.addToBackStack(null)
                    fragmentTransaction.commit()
                }

            }
        }
    }

    companion object {
        private const val TYPE_FOLLOWER = 1
        private const val TYPE_REQUEST = 2
        private const val TYPE_REVIEW = 3
    }

    // Classe per la gestione delle differenze tra gli elementi della lista
    class NotificationItemDiffCallback : DiffUtil.ItemCallback<NotificationItem>() {
        override fun areItemsTheSame(oldItem: NotificationItem, newItem: NotificationItem): Boolean {
            // Implementa la logica di confronto degli elementi, ad esempio, usando un identificatore univoco
            return when {
                oldItem is NotificationItem.FollowerItem && newItem is NotificationItem.FollowerItem ->
                    oldItem.utente.userId == newItem.utente.userId
                oldItem is NotificationItem.RequestItem && newItem is NotificationItem.RequestItem ->
                    oldItem.utente.userId == newItem.utente.userId
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: NotificationItem, newItem: NotificationItem): Boolean {
            // Implementa la logica per verificare se il contenuto degli elementi è identico
            return oldItem == newItem
        }
    }
}