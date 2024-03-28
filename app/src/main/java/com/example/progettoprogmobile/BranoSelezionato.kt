package com.example.progettoprogmobile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.progettoprogmobile.adapter.RecensioniBranoSelAdapter
import com.example.progettoprogmobile.adapter.RisposteAdapterBranoSel
import com.example.progettoprogmobile.model.Artist
import com.example.progettoprogmobile.model.Recensione
import com.example.progettoprogmobile.model.Track
import com.example.progettoprogmobile.utils.SharedEditTextVisibilityManager
import com.example.progettoprogmobile.viewModel.FirebaseViewModel
import com.example.progettoprogmobile.viewModel.RecensioneViewModel
import com.example.progettoprogmobile.viewModel.RisposteViewModel
import com.example.progettoprogmobile.viewModel.SharedDataViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BranoSelezionato : Fragment(), RecensioniBranoSelAdapter.OnRecensioneInteractionListener
{

    private lateinit var recensioneViewModel: RecensioneViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var firebaseViewModel: FirebaseViewModel
    private lateinit var inviarButton: Button
    private lateinit var EditText: EditText
    private lateinit var backButton: Button
    private lateinit var eliCommButderton: Button
    private var recensioneCorrenteDaModificare: Recensione? = null
    //private val sharedEditTextVisibilityManager = SharedEditTextVisibilityManager()
    private lateinit var risposteViewModel:RisposteViewModel
    private lateinit var sharedDataViewModel: SharedDataViewModel
    private var isInCommentMode = false

    private var currentTrack: Track? = null//Servono per resettare lo stato dopo la modalita commento
    private var currentArtistId: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.branoselezionato, container, false)
        initializeViewModels()
        bindingViews(rootView)
        setupRecyclerViewandObserver()
        setupBackButton()
        // observeViewModelData()

        return rootView
    }

    private fun initializeViewModels() {
        firebaseViewModel = ViewModelProvider(this).get(FirebaseViewModel::class.java)
        recensioneViewModel = ViewModelProvider(this).get(RecensioneViewModel::class.java)
        risposteViewModel = ViewModelProvider(this).get(RisposteViewModel::class.java)
        sharedDataViewModel=ViewModelProvider(this).get(SharedDataViewModel::class.java)
    }

    private fun bindingViews(rootView: View) {
        recyclerView = rootView.findViewById(R.id.recyclerBranoSelezionato)
        recyclerView.layoutManager = LinearLayoutManager(context)
        inviarButton = rootView.findViewById(R.id.inviapensiero)
        EditText = rootView.findViewById(R.id.pensieropersonale)
        backButton = rootView.findViewById(R.id.backArrow)

        val track = arguments?.getSerializable("trackDetail") as? Track
        val artistId = track?.artists?.firstOrNull()?.id ?: ""
        currentTrack = track
        currentArtistId=artistId
        setupTrackInformation(rootView, track, currentArtistId!!)

        setupSubmission()

        // Osserva il LiveData per il contenuto dell'EditText
        sharedDataViewModel.editTextContent.observe(viewLifecycleOwner) { text ->
            EditText.setText(text)
        }

        sharedDataViewModel.currentActionState.observe(viewLifecycleOwner) { state ->
            when (state) {
                ActionState.NONE -> EditText.hint = "Scrivi una recensione"
                ActionState.COMMENTING -> EditText.hint = "Scrivi un commento"
                ActionState.EDITING -> EditText.hint = "Modifica la recensione"
                else -> {}
            }
        }

    }

    private fun setupRecyclerViewandObserver() {

        val adapter = RecensioniBranoSelAdapter(emptyList(), this)
        adapter?.showComments = isInCommentMode
        if (isInCommentMode) {
            // La modalità commento è attiva
            Log.d("DDDD", "Modalità commento: attivata, mostrando i commenti")
        } else {
            // La modalità commento è disattivata
            Log.d("DDDD", "Modalità commento: disattivata, nascondendo i commenti")
        }
        val risposteAdapterBranoSel=RisposteAdapterBranoSel(emptyList(), usersMap = emptyMap(),BranoSelezionato())
        adapter.currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        recyclerView.adapter = adapter
        recensioneViewModel.recensioniData.observe(viewLifecycleOwner, Observer { recensioni ->
            adapter.updateData(recensioni, adapter.usersMap)
        })

        recensioneViewModel.usersData.observe(viewLifecycleOwner, Observer { users ->
            adapter.updateData(adapter.recensioni, users)
        })
        recensioneViewModel.flagReview.observe(viewLifecycleOwner) { hasReviewed ->
            if (hasReviewed) {

            } else {
               setupSubmission()
            }
        }

        risposteViewModel.commentiData.observe(viewLifecycleOwner, Observer { risposte ->
            // Crea o aggiorna la mappa delle risposte basata sugli ID delle recensioni
            val risposteMap = risposte.groupBy { it.commentIdfather }
            adapter.updateRisposteMap(risposteMap)
            risposteAdapterBranoSel.notifyDataSetChanged()
            risposteAdapterBranoSel.updateDataComment(risposte, adapter.usersMap)
        })

        risposteViewModel.usersData.observe(viewLifecycleOwner,Observer{ userData ->
            // Crea o aggiorna la mappa delle risposte basata sugli ID delle recensioni
            val nameCom = userData
            adapter.updateNameComment(nameCom)
            // Aggiorna la mappa degli utenti negli adapter
            Log.d("AITOOOOO", "Osservati ${userData.size} utenti.")
            risposteAdapterBranoSel.updateUserMap(userData)
        })
    }

    private fun setupBackButton() {
        backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }


    private fun setupTrackInformation(rootView: View, currentrack: Track?, artistId: String) {
        val titoloCanzone: TextView = rootView.findViewById(R.id.titolocanzone)
        val albumBranoSelezionato: TextView = rootView.findViewById(R.id.albumbranoselezionato)
        val artistaBranoSelezionato: TextView = rootView.findViewById(R.id.artistabranoselezionato)
        val imageBranoSelezionato: ImageView = rootView.findViewById(R.id.imagebranoselezionato)

        currentrack?.let { currentTrack ->
            titoloCanzone.text = currentTrack.name
            albumBranoSelezionato.text = currentTrack.album.name
            artistaBranoSelezionato.text = currentTrack.artists.firstOrNull()?.name ?: "Sconosciuto"
            val imageUrl = currentTrack.album.images.firstOrNull()?.url
            imageUrl?.let {
                Glide.with(this).load(it).into(imageBranoSelezionato)
            }
            recensioneViewModel.fetchRecensioniAndUsersForTrack(currentTrack.id)

        }

        artistaBranoSelezionato.setOnClickListener {
            firebaseViewModel.retrieveArtistById(artistId) { artist ->
                artist?.let { onNameArtistClicked(it) }
            }
        }
    }

    private fun setupSubmission() {
        val track = currentTrack
        val artistId = currentArtistId
        Log.d("DDDD", "${track?.id}")
        Log.d("DDDD", "setupSubmission: Inizio")

        if (!isInCommentMode && sharedDataViewModel.isEditingReview.value != true) {
            Log.d("DDDD", "setupSubmission: Non in modalità commento, configurazione pulsante invio")
            val userId = FirebaseAuth.getInstance().currentUser?.uid

            // Aggiungere qui l'osservazione su hasReviewedTrack
            recensioneViewModel.flagReview.observe(viewLifecycleOwner, Observer { hasReviewed ->
                if (!hasReviewed) {
                    setupButtonForSubmission(userId, track, artistId)
                } else {

                    inviarButton.setOnClickListener {
                        Toast.makeText(context, "Hai già recensito questo brano.", Toast.LENGTH_LONG).show()
                    }
                }
            })
        }
    }

    private fun setupButtonForSubmission(userId: String?, track: Track?, artistId: String?) {
        if (userId != null && track != null) {
            Log.d("DDDD", "setupSubmission: Utente e traccia verificati, procedendo con la verifica recensione")
            recensioneViewModel.hasUserReviewed(track.id, userId) { existingReview ->
                if (existingReview == null) {
                    Log.d("DDDD", "setupSubmission: Nessuna recensione esistente, configurazione per nuova recensione")
                    inviarButton.setOnClickListener {
                        val commentContent = EditText.text.toString()
                        if (commentContent.isNotBlank()) {
                            Log.d("DDDD", "setupSubmission: Contenuto valido, procedendo con l'invio della recensione")
                            recensioneViewModel.saveRecensione(userId, track.id, artistId!!, commentContent)
                            sharedDataViewModel.editTextContent.value = "" // Resetta il campo dopo l'invio
                            Log.d("DDDD", "setupSubmission: Recensione inviata con successo")
                            // Il valore di hasReviewedTrack verrà aggiornato automaticamente nel ViewModel
                        } else {
                            Log.d("DDDD", "setupSubmission: Dati non validi, invio annullato")
                        }
                    }
                } else {
                    //Toast.makeText(context, "Hai già recensito questo brano.", Toast.LENGTH_LONG).show()
                    Log.d("DDDD", "setupSubmission: Recensione esistente trovata, invio bloccato")
                }
            }
        } else {
            Log.d("DDDD", "setupSubmission: Utente o traccia non validi, azione bloccata")
        }
    }
    override fun onRecensioneDeleteClicked(commentId: String, userId: String) {
        val currentUserID = FirebaseAuth.getInstance().currentUser?.uid
        val track = currentTrack
        if (userId == currentUserID && track != null) {
            recensioneViewModel.deleteRecensione(commentId, userId, onSuccess = {
                recensioneViewModel.flagReview.value = false
                sharedDataViewModel.currentActionState.value = ActionState.NONE
                Toast.makeText(context, "Recensione eliminata.", Toast.LENGTH_LONG).show()
                sharedDataViewModel.editTextContent.value = ""

            }, onFailure = {
                Log.d("errore", "Errore nella cancellazione della recensione")
                Toast.makeText(context, "Errore nella cancellazione della recensione", Toast.LENGTH_LONG).show()
            })
        }
    }


    override fun onRecensioneModificaClicked(recensione: Recensione) {
        Log.d("DDDD", "onRecensioneModificaClicked: Inizio modifica recensione")
        sharedDataViewModel.currentActionState.value = ActionState.EDITING
        sharedDataViewModel.editTextContent.value = recensione.content
        recensioneCorrenteDaModificare = recensione

        sharedDataViewModel.isEditingReview.value = true

        Log.d("DDDD", "onRecensioneModificaClicked: Impostato stato di modifica")
        setupSubmissionForEditing(recensione)
    }

    private fun setupSubmissionForEditing(recensione: Recensione) {
        Log.d("DDDD", "setupSubmissionForEditing: Configurazione pulsante invio per modifica")
        sharedDataViewModel.editTextContent.value = ""
        inviarButton.setOnClickListener {
            // Verifica se l'utente sta effettivamente modificando
            if (sharedDataViewModel.isEditingReview.value == true) {
                val commentContent = EditText.text.toString()
                Log.d("DDDD", "setupSubmissionForEditing: Tentativo invio modifica, contenuto: $commentContent")
                recensioneViewModel.saveOrUpdateRecensione(recensione.userId, recensione.trackId, recensione.artistId, commentContent)
                sharedDataViewModel.isEditingReview.value = false
                sharedDataViewModel.editTextContent.value = "" // Resetta il campo dopo l'invio
                sharedDataViewModel.currentActionState.value = ActionState.NONE // Aggiorna lo stato a NONE
            } else {
                Log.d("DDDD", "setupSubmissionForEditing: Azione ignorata, non in modalità modifica")
            }
        }
    }

    override fun onRecensioneCommentaClicked(recensione: Recensione) {
        lifecycleScope.launch {
        isInCommentMode = !isInCommentMode // Alterna lo stato tra vero e falso
        Log.d("DDDD", "onRecensioneCommentaClicked: isInCommentMode=$isInCommentMode")

        if (isInCommentMode) {
            Log.d("DDDD", "Modalità commento: attivata")

            // Attiva la modalità commento e mostra i commenti
            (recyclerView.adapter as? RecensioniBranoSelAdapter)?.isCommentsVisible = true
            recyclerView.adapter?.notifyDataSetChanged()
            sharedDataViewModel.currentActionState.value = ActionState.COMMENTING

            sharedDataViewModel.isEditingReview.value = false // Importante: Reimposta lo stato di modifica qui
            risposteViewModel.fetchCommentfromRecensione(recensione.commentId)
            configureActionButtonForComment(recensione)

            val recensioni = async { recensioneViewModel.fetchRecensioniAndUsersForTrackSuspended(currentTrack!!.id) }
            val risposte = async { risposteViewModel.fetchCommentfromRecensioneSuspended(recensione.commentId) }
            // Attendi i risultati
            val recensioniResult = recensioni.await()
            val risposteResult = risposte.await()

            // Aggiorna i LiveData con i risultati
            withContext(Dispatchers.Main) {
                recensioneViewModel.recensioniData.value = recensioniResult
                risposteViewModel.commentiData.value = risposteResult
            }

        } else {
            Log.d("DDDD", "Modalità commento: disattivata")
            sharedDataViewModel.currentActionState.value = ActionState.NONE
            // Disattiva la modalità commento e nasconde i commenti
            (recyclerView.adapter as? RecensioniBranoSelAdapter)?.isCommentsVisible = false
            recyclerView.adapter?.notifyDataSetChanged()
            resetCommentModeUI(recensione)
            sharedDataViewModel.isEditingReview.value = false

        }
        }
    }
    private fun inviaCommento(recensione: Recensione) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val answerContent = EditText.text.toString()

        if (answerContent.isNotBlank()) {
            Log.d("DDDD", "Invio commento: Dati validi, invio in corso")
            risposteViewModel.saveRisposta(userId, recensione.commentId, answerContent)
            sharedDataViewModel.editTextContent.value = "" // Resetta il campo dopo l'invio
            risposteViewModel.fetchCommentfromRecensione(recensione.commentId)
           // resetCommentModeUI(recensione)

        } else {
            Log.d("DDDD", "Invio commento: Dati non validi, invio annullato")
        }
    }
    private fun configureActionButtonForComment(recensione: Recensione) {
        inviarButton.setOnClickListener {
            inviaCommento(recensione)
        }
    }
    private fun resetCommentModeUI(recensione: Recensione) {
        setupSubmission()
        inviarButton.text = "Invia Recensione"
        inviarButton.setOnClickListener {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            val commentContent = EditText.text.toString()
            if (userId != null && recensione!!.trackId != null && commentContent.isNotBlank()) {
                Log.d("BranoSelezionato", "Invio recensione: Dati validi, invio in corso")
                sharedDataViewModel.editTextContent.value = ""
                isInCommentMode = false
                //finalizeReviewSubmission()

            } else {
                Log.d("BranoSelezionato", "Invio recensione: Dati non validi, invio annullato")
            }
        }
    }

    fun onNameArtistClicked(artist: Artist) {
        val newFragment = ArtistaSelezionato()
        val bundle = Bundle()
        bundle.putSerializable("artistdetails", artist)
        newFragment.arguments = bundle
        parentFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, newFragment)
            .addToBackStack(null)
            .commit()
    }
}
enum class ActionState {
    NONE, COMMENTING, EDITING
}


