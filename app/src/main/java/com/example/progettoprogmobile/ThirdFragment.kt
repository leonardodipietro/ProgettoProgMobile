package com.example.progettoprogmobile

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Rect
import android.Manifest
import android.os.Bundle
import android.provider.MediaStore
import android.text.Html
import android.util.Log
import android.view.*
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.example.progettoprogmobile.viewModel.FirebaseAuthViewModel
import com.example.progettoprogmobile.viewModel.FirebaseViewModel
import com.example.progettoprogmobile.viewModel.SpotifyViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import java.io.IOException
import java.io.ByteArrayOutputStream
import com.example.progettoprogmobile.model.Track
import com.example.progettoprogmobile.viewModel.SharedDataViewModel

open class ThirdFragment : Fragment() {

    private lateinit var databaseReference: DatabaseReference
    private lateinit var storageReference: StorageReference
    private lateinit var firebaseauthviewModel: FirebaseAuthViewModel
    private lateinit var firebaseViewModel: FirebaseViewModel
    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val userId = currentUser?.uid?:""
    private lateinit var spotifyViewModel:SpotifyViewModel
    private lateinit var userImage: ImageView
    private lateinit var editImageButton: ImageButton
    private val cameraPermissionRequestCode = 1002
    private val photoRequestCode = 1
    private val photoRequestCodeFromGallery = 2
    private var token: String? = null
    private lateinit var account: TextView
    private lateinit var selectedAccountPrivacy: String
    private lateinit var accountPrivacyListView: ListView

    private lateinit var sharedPreferences: SharedPreferences
    private val sharedViewModel: SharedDataViewModel by activityViewModels()
    private val shortTermTracksShared: SharedDataViewModel by activityViewModels()
    private val mediumTermTracksShared: SharedDataViewModel by activityViewModels()
    private val longTermTracksShared: SharedDataViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val rootView = inflater.inflate(R.layout.fragment_third, container, false)
        //ottengo il token di spotify

        //Inizializza Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("users")
        storageReference = FirebaseStorage.getInstance().getReference("profile image/$userId")
        // Inizializza il ViewModel per la gestione dell'autenticazione Firebase
        firebaseauthviewModel = ViewModelProvider(this)[FirebaseAuthViewModel::class.java]
        firebaseViewModel =  ViewModelProvider(this)[FirebaseViewModel::class.java]
        sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        spotifyViewModel  = ViewModelProvider(this)[SpotifyViewModel::class.java]
        userImage = rootView.findViewById(R.id.userImage)
        editImageButton = rootView.findViewById(R.id.editImageButton)



        //var  filter
        // Gestione del cambio immagine
        editImageButton.setOnClickListener {
            openFileChooser()
        }



        // Carica l'URL dell'immagine di profilo dalle SharedPreferences
        val imageUrl = sharedPreferences.getString("profile image_$userId", null)
        // Mostra l'immagine di profilo se l'URL è presente
        if (!imageUrl.isNullOrEmpty()) {
            Picasso.get().load(imageUrl).into(userImage)
        } else {
            // Se non è presente un'immagine di profilo, mostra l'immagine di default
            userImage.setImageResource(R.drawable.default_profile_image)
        }

        val reviewTextView = rootView.findViewById<TextView>(R.id.reviewTextButton)
        reviewTextView.setOnClickListener{
            val reviewFragment = ReviewFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, reviewFragment)
                .addToBackStack(null)
                .commit()
        }
        val followersTextView = rootView.findViewById<TextView>(R.id.followersTextButton)
        followersTextView.setOnClickListener{
            val followersFragment = FollowersFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, followersFragment)
                .addToBackStack(null)
                .commit()
        }
        val followingTextView = rootView.findViewById<TextView>(R.id.followingTextButton)
        followingTextView.setOnClickListener{
            val followingFragment = FollowingFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, followingFragment)
                .addToBackStack(null)
                .commit()
        }

        // Mostra il nome utente
        displayUsername(userId, rootView)
        displayEmail(userId, rootView)

        // Gestione del cambio nome utente
        val editNameButton: ImageButton = rootView.findViewById(R.id.editNameButton)
        editNameButton.setOnClickListener {
            showEditNameDialog(requireContext(), userId, rootView)
        }



        account = rootView.findViewById(R.id.account)

        selectedAccountPrivacy = sharedPreferences.getString("selectedAccountPrivacy", "Tutti") ?: "Tutti"
        account.text = selectedAccountPrivacy

        account.setOnClickListener {
            showAPDialog()
        }

        // Trova i pulsanti nel layout del fragment
        val signOut = rootView.findViewById<Button>(R.id.signOut)
        val delete = rootView.findViewById<Button>(R.id.delete)



        signOut.setOnClickListener {
            firebaseauthviewModel.signOut(requireContext())
        }

        // Imposta un click listener per il pulsante "Delete"
        delete.setOnClickListener {
            firebaseauthviewModel.delete(requireContext())

            val databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId)

            databaseReference.removeValue()

            val reviewsReference = FirebaseDatabase.getInstance().getReference("reviews")
            val answersReference = FirebaseDatabase.getInstance().getReference("answers")
            val query = reviewsReference.orderByChild("userId").equalTo(userId)
            val queryAnswers = answersReference.orderByChild("userId").equalTo(userId)

            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (reviewSnapshot in dataSnapshot.children) {

                        reviewSnapshot.ref.removeValue().addOnSuccessListener {
                            val reviewId = reviewSnapshot.key
                            val queryAnswers = answersReference.orderByChild("reviewId").equalTo(reviewId)
                            queryAnswers.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(answerDataSnapshot: DataSnapshot) {
                                    for (answerSnapshot in answerDataSnapshot.children) {
                                        // Rimuovi questa risposta
                                        answerSnapshot.ref.removeValue()
                                    }
                                }

                                override fun onCancelled(answerDatabaseError: DatabaseError) {

                                }
                            })
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {

                }
            })

            queryAnswers.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(userAnswersDataSnapshot: DataSnapshot) {
                    for (userAnswerSnapshot in userAnswersDataSnapshot.children) {

                        userAnswerSnapshot.ref.removeValue()
                    }
                }

                override fun onCancelled(userAnswersDatabaseError: DatabaseError) {

                }
            })

            removeCurrentUserFromFollowers(userId)
            removeCurrentUserFromFollowing(userId)
            removeCurrentUserFromAllRequests(userId)

        }

        // Osserva il risultato del logout dall'account Firebase
        firebaseauthviewModel.signOutResult.observe(viewLifecycleOwner) { result ->
            if (result == FirebaseAuthViewModel.SignOutResult.SUCCESS) {
                // Se il logout è riuscito, avvia l'activity principale
                val intent = Intent(requireContext(), MainActivity::class.java)
                startActivity(intent)
            } else {

            }
        }

        firebaseauthviewModel.deleteResult.observe(viewLifecycleOwner) { result ->
            if (result == FirebaseAuthViewModel.DeleteResult.SUCCESS) {

                val intent = Intent(requireContext(), MainActivity::class.java)
                startActivity(intent)
            } else {

            }
        }


        countUserReviews(userId, FirebaseDatabase.getInstance().reference, rootView);
        countUserFollowers(userId, FirebaseDatabase.getInstance().reference, rootView);
        countUserFollowing(userId, FirebaseDatabase.getInstance().reference, rootView);

        observeToken()
        //setupButtonListeners()
        val firstPlaylistButton = rootView?.findViewById<Button>(R.id.playlist1m)
        val secondPlaylistButton = rootView?.findViewById<Button>(R.id.playlist6m)
        val thirdPlaylistButton = rootView?.findViewById<Button>(R.id.playlistalways)

        firstPlaylistButton?.setOnClickListener {
            Log.d("PlaylistButton", "First playlist button clicked")
            sharedViewModel.shortTermTracksShared.value?.items?.let { items ->
                if (items.isNullOrEmpty()) {
                    Log.d("Playlist", "La lista delle tracce è vuota")
                } else {
                    Log.d("Playlist", "Chiamata a createPlaylistFromTopTracks con ${items.size} tracce")
                    createPlaylistFromTopTracks(items)
                }
            } ?: run {
                Log.d("PlaylistButton", "shortTermTracksShared.value è null")
                Toast.makeText(requireContext(), "Errore nella creazione della playlist.", Toast.LENGTH_LONG).show()

            }
        }

        secondPlaylistButton?.setOnClickListener {
            sharedViewModel.mediumTermTracksShared.value?.items?.let { items ->
                Log.d("PlaylistButton", "First playlist button clicked")
                sharedViewModel.mediumTermTracksShared.value?.items?.let { items ->
                    if (items.isNullOrEmpty()) {
                        Log.d("Playlist", "La lista delle tracce è vuota")
                    } else {
                        Log.d("Playlist", "Chiamata a createPlaylistFromTopTracks con ${items.size} tracce")
                        createPlaylistFromTopTracks(items)
                    }
                } ?: run {
                    Log.d("PlaylistButton", "shortTermTracksShared.value è null")
                    Toast.makeText(requireContext(), "Errore nella creazione della playlist.", Toast.LENGTH_LONG).show()

                }

            }
        }

        thirdPlaylistButton?.setOnClickListener {
            sharedViewModel.longTermTracksShared.value?.items?.let { items ->
                Log.d("PlaylistButton", "Third playlist button clicked")
                sharedViewModel.longTermTracksShared.value?.items?.let { items ->
                    if (items.isNullOrEmpty()) {
                        Log.d("Playlist", "La lista delle tracce è vuota")
                    } else {
                        Log.d("Playlist", "Chiamata a createPlaylistFromTopTracks con ${items.size} tracce")
                        createPlaylistFromTopTracks(items)
                    }
                } ?: run {
                    Log.d("PlaylistButton", "shortTermTracksShared.value è null")
                    Toast.makeText(requireContext(), "Errore nella creazione della playlist.", Toast.LENGTH_LONG).show()

                }

            }
        }


        return rootView
    }

    private fun removeCurrentUserFromFollowers(userId: String) {
        val followersReference = FirebaseDatabase.getInstance().getReference("users")
        followersReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (userSnapshot in dataSnapshot.children) {
                    val otherUserId = userSnapshot.key
                    otherUserId?.let {
                        val userFollowersReference = FirebaseDatabase.getInstance().getReference("users").child(it).child("followers")
                        userFollowersReference.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()) {
                                    // L'utente corrente è un follower di questo utente
                                    userFollowersReference.child(userId).removeValue()
                                    decrementFollowerCount(it)
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {

                            }
                        })
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
    }

    private fun removeCurrentUserFromFollowing(userId: String) {
        val followersReference = FirebaseDatabase.getInstance().getReference("users")
        followersReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (userSnapshot in dataSnapshot.children) {
                    val otherUserId = userSnapshot.key
                    otherUserId?.let {
                        val userFollowingReference = FirebaseDatabase.getInstance().getReference("users").child(it).child("following")
                        userFollowingReference.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()) {
                                    // L'utente corrente è un follower di questo utente
                                    userFollowingReference.child(userId).removeValue()
                                    decrementFollowingCount(it)
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {

                            }
                        })
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
    }


    private fun decrementFollowerCount(userId: String) {
        val userReference = FirebaseDatabase.getInstance().getReference("users").child(userId)
        userReference.child("followers counter").runTransaction(object : Transaction.Handler {
            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                val currentValue = mutableData.getValue(Int::class.java)
                mutableData.value = (currentValue ?: 0) - 1
                return Transaction.success(mutableData)
            }

            override fun onComplete(databaseError: DatabaseError?, committed: Boolean, dataSnapshot: DataSnapshot?) {
                if (!committed) {

                }
            }
        })
    }

    private fun decrementFollowingCount(userId: String) {
        val userReference = FirebaseDatabase.getInstance().getReference("users").child(userId)
        userReference.child("following counter").runTransaction(object : Transaction.Handler {
            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                val currentValue = mutableData.getValue(Int::class.java)
                mutableData.value = (currentValue ?: 0) - 1
                return Transaction.success(mutableData)
            }

            override fun onComplete(databaseError: DatabaseError?, committed: Boolean, dataSnapshot: DataSnapshot?) {
                if (!committed) {

                }
            }
        })
    }

    private fun removeCurrentUserFromAllRequests(userId: String) {
        val followersReference = FirebaseDatabase.getInstance().getReference("users")
        followersReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (userSnapshot in dataSnapshot.children) {
                    val otherUserId = userSnapshot.key
                    otherUserId?.let {
                        val userRequestsReference = FirebaseDatabase.getInstance().getReference("users").child(it).child("requests")
                        userRequestsReference.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()) {

                                    userRequestsReference.child(userId).removeValue()
                                }
                            }
                            override fun onCancelled(error: DatabaseError) {

                            }
                        })
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
    }


    private fun observeToken() {
        sharedViewModel.spotifyToken.observe(viewLifecycleOwner) { accessToken ->
            accessToken?.let {

                Log.d("SpotifyToken dentro il third", "Token ottenuto: $it")
                // Chiamata per creare la playlist, o qualsiasi altra operazione che richieda il token
                token = accessToken
                Log.d("SpotifyToken dentro il third parte 2", "Token ottenuto: $token")
            } ?: run {
                Log.d("SpotifyToken", "Token non ottenuto")

            }
        }
    }
    private fun createPlaylistFromTopTracks(items: List<Track>) {
        val currentToken = token
        Log.d("SpotifyToken dentro il third parte 3", "Token ottenuto: $currentToken")
        if (currentToken != null) {
            Log.d("Playlist", "playlist chiamata nel fragment")
            val trackUris = items.map { "spotify:track:${it.id}" }
            spotifyViewModel.createSpotifyPlaylist(currentToken, trackUris)

            spotifyViewModel.playlistCreationResult.observe(viewLifecycleOwner) { success ->
                if (success) {
                    Toast.makeText(requireContext(), "Playlist creata con successo!", Toast.LENGTH_LONG).show()
                } else {

                }
            }


        } else {
            Log.e("SpotifyToken", "Token non disponibile")
            Toast.makeText(requireContext(), "Errore!!! Connettiti su spotify e riprova", Toast.LENGTH_LONG).show()
        }
    }
    private fun openFileChooser() {


        val options = arrayOf(
            Html.fromHtml("<font color='#FFFFFF'>${getString(R.string.takePicture)}</font>", Html.FROM_HTML_MODE_LEGACY),
            Html.fromHtml("<font color='#FFFFFF'>${getString(R.string.choseFromGallery)}</font>", Html.FROM_HTML_MODE_LEGACY),
            Html.fromHtml("<font color='#FFFFFF'>${getString(R.string.removeCurrentPicture)}</font>", Html.FROM_HTML_MODE_LEGACY),
            Html.fromHtml("<font color='#FFFFFF'>${getString(R.string.cancel)}</font>", Html.FROM_HTML_MODE_LEGACY)
        )

        val builder = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialogStyle)
        builder.setTitle(getString(R.string.choseAnOption))
        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> {
                    // Avvia la fotocamera
                    if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        val photoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        startActivityForResult(photoIntent, photoRequestCode)
                    } else {
                        // Non hai l'autorizzazione, richiedila all'utente
                        ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE), cameraPermissionRequestCode)
                    }

                }

                1 -> {
                    // Scegli dalla galleria
                    val galleryIntent =
                        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    galleryIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
                    startActivityForResult(galleryIntent, photoRequestCodeFromGallery)
                }

                2 -> {

                    removeCurrentPhotoFromProfile()
                }
                3 -> {

                    dialog.dismiss()
                }
            }
        }
        builder.show()
    }
    private fun saveProfileImageURL(context: Context, userId:String, imageUrl: String) {
        val sharedPreferences =
            context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("profile image_$userId", imageUrl)
        editor.apply()
    }
    fun uploadImageToFirebaseStorage(context: Context, userId: String, bitmap: Bitmap) {
        val storage = FirebaseStorage.getInstance()
        val storageReference = storage.getReference("profile image")
        val imageRef = storageReference.child("$userId.jpg")

        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        // Carica l'immagine nello storage di Firebase
        val uploadTask = imageRef.putBytes(data)


        uploadTask.addOnSuccessListener { taskSnapshot ->

            imageRef.downloadUrl.addOnSuccessListener { uri ->
                val imageUrl = uri.toString()
                saveProfileImageURL(context, userId, imageUrl)
                val userRef: DatabaseReference = FirebaseDatabase.getInstance().reference.child("users").child(userId)
                userRef.child("profile image").setValue(imageUrl)

                Log.e ("LOG UPLOAD", "Immagine caricata con successo: $imageUrl")
            }
        }.addOnFailureListener { exception ->

            Log.e ("LOG UPLOAD",  "Errore durante il caricamento dell'immagine.")
        }
    }
    private fun removeCurrentPhotoFromProfile() {
        val sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove("profile image_$userId")
        editor.apply()

        val userReference = FirebaseDatabase.getInstance().getReference("users").child(userId)
        userReference.child("profile image").removeValue().addOnSuccessListener {
            Log.d("RemovePhoto", "Riferimento all'immagine del profilo eliminato con successo dal database")


            userImage.setImageResource(R.drawable.default_profile_image)
            Toast.makeText(requireContext(), "Immagine del profilo rimossa con successo", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { exception ->

            Toast.makeText(requireContext(), "Si è verificato un errore durante la rimozione dell'immagine del profilo", Toast.LENGTH_SHORT).show()
        }
    }


    fun showEditNameDialog(context: Context, userId: String, rootView: View) {

        val dialogView =
            LayoutInflater.from(context).inflate(R.layout.dialog_edit_name, null)
        val builder =
            AlertDialog.Builder(context, R.style.CustomAlertDialogStyle).setView(dialogView).setTitle("Edit Name")
        val alertDialog = builder.create()
        val editName: EditText = dialogView.findViewById(R.id.editName)
        val saveButton = dialogView.findViewById<Button>(R.id.saveButton)
        val cancelButton = dialogView.findViewById<Button>(R.id.cancelButton)

        saveButton.setOnClickListener {
            val newName = editName.text.toString()
            val userRef: DatabaseReference =
                FirebaseDatabase.getInstance().reference.child("users").child(userId)
            userRef.child("name").setValue(newName)
                .addOnSuccessListener {
                    val usernameTextView = rootView.findViewById<TextView>(R.id.username)
                    usernameTextView.text = newName
                    alertDialog.dismiss() // Chiudi il dialog
                    // Nome utente aggiornato con successo
                    // Puoi fare qualcosa qui se necessario
                }
                .addOnFailureListener { e ->

                    Log.e("Firebase", "Errore nell'aggiornamento del nome utente: ${e.message}")
                }
            alertDialog.dismiss() // Close the dialog
        }

        cancelButton.setOnClickListener {
            alertDialog.dismiss()
        }

        alertDialog.show()
    }
    fun displayUsername(userId: String, rootView: View) {
        val userRef: DatabaseReference =
            FirebaseDatabase.getInstance().reference.child("users").child(userId)

        userRef.child("name").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val username = dataSnapshot.value.toString()
                val usernameTextView = rootView.findViewById<TextView>(R.id.username)
                usernameTextView.text = username
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("Firebase", "Errore nel recupero del nome utente: ${databaseError.message}")
            }
        })
    }


    fun displayEmail(userId: String, rootView: View) {
        val userRef: DatabaseReference =
            FirebaseDatabase.getInstance().reference.child("users").child(userId)

        userRef.child("email").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val email = dataSnapshot.value.toString()
                val emailTextView = rootView.findViewById<TextView>(R.id.email)
                emailTextView.text = email
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
    }


    private fun privacyATranslationMap(context: Context): Map<String, String> {
        return mapOf(
            context.getString(R.string.everyone) to "Everyone",
            context.getString(R.string.followers) to "Followers"
        )
    }
    fun saveSelectedAP(context: Context, accountPrivacy: String) {
        val sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("selectedAccountPrivacy", accountPrivacy)
        editor.apply()
    }
    fun updateAPOption(context: Context, userId: String, selectedROption: String) {
        val userRef: DatabaseReference = Firebase.database.reference
            .child("users")
            .child(userId)
            .child("privacy")
            .child("account")


        val privacyATranslationMap = privacyATranslationMap(context)

        val databaseKey = privacyATranslationMap[selectedROption]

        if (databaseKey != null) {

            val privacyOptions = privacyATranslationMap.values
            for (option in privacyOptions) {
                userRef.child(option).setValue(option == databaseKey)
            }
        } else {
            Log.e("Firebase", "Opzione selezionata non valida: $selectedROption")
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode==photoRequestCode) {
            if (resultCode == Activity.RESULT_OK) {
                val bp = data?.extras?.get("data") as Bitmap

                uploadImageToFirebaseStorage(requireContext(), userId, bp)
                userImage.setImageBitmap(bp)

            }
        } else if(requestCode==photoRequestCodeFromGallery){
            onActivityResultForGallery(requestCode, resultCode, data)
        }
    }
    private fun onActivityResultForGallery(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            photoRequestCodeFromGallery -> {
                if (resultCode == Activity.RESULT_OK) {
                    // Gestione per l'immagine dalla galleria
                    val selectedImageUri = data?.data
                    if (selectedImageUri != null) {
                        try {
                            val bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, selectedImageUri)

                            userImage.setImageBitmap(bitmap)

                            uploadImageToFirebaseStorage(requireContext(), userId, bitmap)
                            userImage.setImageBitmap(bitmap)
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }
    private fun showAPDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_account_privacy, null)
        accountPrivacyListView = dialogView.findViewById(R.id.accountPrivacyListView)

        val accountPrivacy = arrayOf(getString(R.string.everyone), getString(R.string.followers))

        val accountAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, accountPrivacy)
        accountPrivacyListView.adapter = accountAdapter


        val popupWindow = PopupWindow(dialogView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        accountPrivacyListView.setOnItemClickListener { _, _, position, _ ->
            selectedAccountPrivacy = accountPrivacy[position]
            saveSelectedAP(requireContext(), selectedAccountPrivacy)
            updateAPOption(requireContext(), userId, selectedAccountPrivacy)
            popupWindow.dismiss()
            account.text = selectedAccountPrivacy
        }

        dialogView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val rect = Rect()
                dialogView.getGlobalVisibleRect(rect)
                if (!rect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    popupWindow.dismiss()
                    account.performClick()
                }
            }
            true
        }

        // Visualizza il popup
        popupWindow.isOutsideTouchable = true
        popupWindow.isFocusable = true
        popupWindow.showAsDropDown(account)

        val location = IntArray(2)
        account.getLocationOnScreen(location)
        val x = location[0]
        val y = location[1] + account.height
        popupWindow.showAtLocation(account, Gravity.NO_GRAVITY, x, y)
    }


    fun countUserReviews(userId: String, databaseReference: DatabaseReference, rootView: View) {
        val reviewsReference = databaseReference.child("users").child(userId).child("reviews")

        reviewsReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val reviewCount = dataSnapshot.childrenCount.toInt()

                    saveReviewCountToUserNode(userId, reviewCount, databaseReference)

                    Log.d("CountUserReviews", "Review count for user $userId: $reviewCount")

                    val reviewNumberTextView = rootView.findViewById<TextView>(R.id.reviewNumber)
                    reviewNumberTextView.text = reviewCount.toString()
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("CountUserReviews", "Error fetching review count for user $userId: ${databaseError.message}")
                }
            })
    }
    fun countUserFollowers(userId: String, databaseReference: DatabaseReference, rootView: View) {
        val followersReference = databaseReference.child("users").child(userId).child("followers")

        followersReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val followersCount = dataSnapshot.childrenCount.toInt()

                // Ora puoi salvare il conteggio nel nodo dell'utente
                saveFollowersCountToUserNode(userId, followersCount, databaseReference)

                Log.d("CountUserFollowers", "Followers count for user $userId: $followersCount")

                val followersNumberTextView = rootView.findViewById<TextView>(R.id.followersNumber)
                followersNumberTextView.text = followersCount.toString()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("CountUserFollowers", "Error fetching followers count for user $userId: ${databaseError.message}")
            }
        })
    }
    fun countUserFollowing(userId: String, databaseReference: DatabaseReference, rootView: View) {
        val followingReference = databaseReference.child("users").child(userId).child("following")

        followingReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val followingCount = dataSnapshot.childrenCount.toInt()

                saveFollowingCountToUserNode(userId, followingCount, databaseReference)

                Log.d("CountUserFollowing", "Following count for user $userId: $followingCount")

                val followingNumberTextView = rootView.findViewById<TextView>(R.id.followingNumber)
                followingNumberTextView.text = followingCount.toString()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("CountUserFollowing", "Error fetching following count for user $userId: ${databaseError.message}")
            }
        })
    }

    private fun saveReviewCountToUserNode(userId: String, reviewCount: Int, databaseReference: DatabaseReference) {
        val userReference = databaseReference.child("users").child(userId)

        userReference.child("reviews counter").setValue(reviewCount)
    }
    private fun saveFollowersCountToUserNode(userId: String, followersCount: Int, databaseReference: DatabaseReference) {
        val userReference = databaseReference.child("users").child(userId)

        userReference.child("followers counter").setValue(followersCount)
    }
    private fun saveFollowingCountToUserNode(userId: String, followingCount: Int, databaseReference: DatabaseReference) {
        val userReference = databaseReference.child("users").child(userId)

        userReference.child("following counter").setValue(followingCount)
    }
}