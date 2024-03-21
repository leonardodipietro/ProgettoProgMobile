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
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
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

class BranoSelezionato : Fragment(), RecensioniBranoSelAdapter.OnRecensioneInteractionListener,
    RisposteAdapterBranoSel.OnCommentiInteractionListener
{

    private lateinit var recensioneViewModel: RecensioneViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var firebaseViewModel: FirebaseViewModel
    private lateinit var inviarButton: Button
    private lateinit var EditText: EditText
    private lateinit var backButton: Button
    private lateinit var eliCommButton: Button
    private var recensioneCorrenteDaModificare: Recensione? = null
    private val sharedEditTextVisibilityManager = SharedEditTextVisibilityManager()
    private lateinit var risposteViewModel:RisposteViewModel
    private lateinit var sharedDataViewModel: SharedDataViewModel
    private var isInCommentMode = false



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.branoselezionato, container, false)
        initializeViewModels()
        bindingViews(rootView)
        setupRecyclerView()
        setupBackButton()
        // observeViewModelData()

        return rootView
    }

    private fun initializeViewModels() {
        firebaseViewModel = ViewModelProvider(this).get(FirebaseViewModel::class.java)
        recensioneViewModel = ViewModelProvider(this).get(RecensioneViewModel::class.java)
        recensioneViewModel.setSharedEditTextVisibilityManager(sharedEditTextVisibilityManager)
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
        setupTrackInformation(rootView, track, artistId)
        setupSubmission(track, artistId)


        // Osserva il LiveData per il contenuto dell'EditText
        sharedDataViewModel.editTextContent.observe(viewLifecycleOwner) { text ->
            EditText.setText(text)
        }

        // Osserva il LiveData per lo stato corrente
        sharedDataViewModel.currentActionState.observe(viewLifecycleOwner) { state ->

        }

    }

    private fun setupRecyclerView() {

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

    /* private fun observeViewModelData() {
         sharedEditTextVisibilityManager.editTextVisibility.observe(viewLifecycleOwner) { isVisible ->
             EditText.visibility = if (isVisible) View.VISIBLE else View.GONE
             inviarButton.visibility = if (isVisible) View.VISIBLE else View.GONE
         }

     }*/

    private fun setupTrackInformation(rootView: View, track: Track?, artistId: String) {
        val titoloCanzone: TextView = rootView.findViewById(R.id.titolocanzone)
        val albumBranoSelezionato: TextView = rootView.findViewById(R.id.albumbranoselezionato)
        val artistaBranoSelezionato: TextView = rootView.findViewById(R.id.artistabranoselezionato)
        val imageBranoSelezionato: ImageView = rootView.findViewById(R.id.imagebranoselezionato)

        track?.let { currentTrack ->
            titoloCanzone.text = currentTrack.name
            albumBranoSelezionato.text = currentTrack.album.name
            artistaBranoSelezionato.text = currentTrack.artists.firstOrNull()?.name ?: "Sconosciuto"
            val imageUrl = currentTrack.album.images.firstOrNull()?.url
            imageUrl?.let {
                Glide.with(this).load(it).into(imageBranoSelezionato)
            }
            recensioneViewModel.fetchRecensioniAndUsersForTrack(currentTrack.id)
            recensioneViewModel.checkUserReview(currentTrack.id, FirebaseAuth.getInstance().currentUser?.uid ?: "")
        }

        artistaBranoSelezionato.setOnClickListener {
            firebaseViewModel.retrieveArtistById(artistId) { artist ->
                artist?.let { onNameArtistClicked(it) }
            }
        }
    }

    private fun checkUserReviewAndSetupAction(trackId: String, userId: String) {
        recensioneViewModel.hasUserReviewed(trackId, userId) { recensione ->
            if (recensione != null) {
                // L'utente ha già recensito il brano, impostiamo lo stato per permettere la modifica
                sharedDataViewModel.currentActionState.value = ActionState.EDITING
            } else {
                // Non esiste una recensione dell'utente per il brano, impostiamo lo stato per l'aggiunta
                // Se vuoi distinguere tra aggiungere una recensione e commentare, potresti dover gestire questo aspetto qui
                sharedDataViewModel.currentActionState.value = ActionState.NONE // O un altro stato appropriato
            }
            // Imposta il contenuto dell'EditText se necessario, ad esempio per la modifica
            sharedDataViewModel.editTextContent.value = recensione?.content ?: ""
        }
    }

    private fun setupSubmission(track: Track?, artistId: String) {
        Log.d("DDDD", "setupSubmission: Inizio")
        if (!isInCommentMode && sharedDataViewModel.isEditingReview.value != true) {
            Log.d("DDDD", "setupSubmission: Non in modalità commento, configurazione pulsante invio")
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null && track != null) {
                Log.d("DDDD", "setupSubmission: Utente e traccia verificati, procedendo con la verifica recensione")
                recensioneViewModel.hasUserReviewed(track.id, userId) { existingReview ->
                    if (existingReview == null) {
                        Log.d("DDDD", "setupSubmission: Nessuna recensione esistente, configurazione per nuova recensione")
                        inviarButton.setOnClickListener {
                            val commentContent = EditText.text.toString()
                            if (commentContent.isNotBlank()) {
                                Log.d("DDDD", "setupSubmission: Contenuto valido, procedendo con l'invio della recensione")
                                recensioneViewModel.saveRecensione(userId, track.id, artistId,commentContent)
                                Log.d("DDDD", "setupSubmission: Recensione inviata con successo")
                                finalizeReviewSubmission()
                            } else {
                                Log.d("DDDD", "setupSubmission: Dati non validi, invio annullato")
                            }
                        }
                    } else {
                        Log.d("DDDD", "setupSubmission: Recensione esistente trovata, invio bloccato")
                        // Qui potresti voler mostrare un messaggio all'utente che una recensione esiste già.
                    }
                }
            } else {
                Log.d("DDDD", "setupSubmission: Utente o traccia non validi, azione bloccata")
            }
        } else {
            Log.d("DDDD", "setupSubmission: Condizione non consentita per l'invio di recensioni")
            // Qui puoi gestire il caso in cui l'utente sia in modalità commento o in fase di modifica.
        }
    }




    override fun onRecensioneDeleteClicked(commentId: String, userId: String) {
        val currentUserID = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == currentUserID) {
            recensioneViewModel.deleteRecensione(commentId)
        }
    }

    override fun onEliminaClicked(answerId: String, userId: String) {
        val currentUserID = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == currentUserID&& this::risposteViewModel.isInitialized) {

            risposteViewModel.deleteRisposte(answerId)
            risposteViewModel.commentiData.observe(viewLifecycleOwner, Observer { commentiData ->

            })


        }
        else {
            Log.e("BranoSelezionato", "risposteViewModel non è stato inizializzato")
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
        inviarButton.setOnClickListener {
            // Verifica se l'utente sta effettivamente modificando
            if (sharedDataViewModel.isEditingReview.value == true) {
                val commentContent = EditText.text.toString()
                Log.d("DDDD", "setupSubmissionForEditing: Tentativo invio modifica, contenuto: $commentContent")
                recensioneViewModel.saveOrUpdateRecensione(recensione.userId, recensione.trackId, recensione.artistId, commentContent)
                // Logica di invio qui
                finalizeReviewSubmission()
            } else {
                // Altrimenti, ignora o mostra un messaggio
                Log.d("DDDD", "setupSubmissionForEditing: Azione ignorata, non in modalità modifica")
            }
        }
    }

    private fun finalizeReviewSubmission() {
        Log.d("DDDD", "finalizeReviewSubmission: Finalizzazione invio/modifica recensione")
        sharedDataViewModel.editTextContent.value = "" // Resetta il campo dopo l'invio
        sharedDataViewModel.currentActionState.value = ActionState.NONE // Aggiorna lo stato a NONE

        // Resetta il flag di modifica solo dopo l'invio di una recensione/modifica
        sharedDataViewModel.isEditingReview.value = false
        Log.d("DDDD", "finalizeReviewSubmission: Reset stato di modifica")
    }
    override fun onRecensioneCommentaClicked(recensione: Recensione) {
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
        } else {
            Log.d("DDDD", "Modalità commento: disattivata")
            // Disattiva la modalità commento e nasconde i commenti
            (recyclerView.adapter as? RecensioniBranoSelAdapter)?.isCommentsVisible = false
            recyclerView.adapter?.notifyDataSetChanged()
            resetCommentModeUI(recensione)
            sharedDataViewModel.isEditingReview.value = false // Reimposta anche qui lo stato di modifica
        }
    }
    private fun inviaCommento(recensione: Recensione) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val answerContent = EditText.text.toString()

        if (answerContent.isNotBlank()) {
            Log.d("DDDD", "Invio commento: Dati validi, invio in corso")
            risposteViewModel.saveRisposta(userId, recensione.commentId, answerContent)
            finalizeReviewSubmission()
            risposteViewModel.fetchCommentfromRecensione(recensione.commentId)
            // Aggiorna l'UI dopo l'invio del commento
            resetCommentModeUI(recensione)

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
        // Esempio di reset di UI specifiche, come nascondere la lista dei commenti se mostrata
        // ...

        // Reset del testo del pulsante e del listener per riflettere la funzione originale
        inviarButton.text = "Invia Recensione"

        // Assumi che `currentTrack` e `currentArtistId` siano disponibili a questo livello,
        // altrimenti, dovrai trovare un modo per renderli accessibili qui.
        inviarButton.setOnClickListener {
            // Assicurati di avere accesso al track e artistId correnti qui
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            val commentContent = EditText.text.toString()
            if (userId != null && recensione!!.trackId != null && commentContent.isNotBlank()) {
                Log.d("BranoSelezionato", "Invio recensione: Dati validi, invio in corso")
                recensioneViewModel.saveOrUpdateRecensione(userId, recensione!!.trackId, recensione!!.artistId, commentContent)
                sharedDataViewModel.editTextContent.value = "" // Resetta il campo dopo l'invio
                sharedDataViewModel.currentActionState.value = ActionState.NONE

                isInCommentMode = false // Assicurati che la modalità commento sia disattivata
                finalizeReviewSubmission()
                // Aggiorna l'UI dopo l'invio della recensione, se necessario
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
    NONE, COMMENTING, EDITING,AGGIORNA
}


