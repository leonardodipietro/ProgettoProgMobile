package com.example.progettoprogmobile

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Rect
import android.Manifest
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.progettoprogmobile.utils.SettingUtils
import com.example.progettoprogmobile.viewModel.FirebaseAuthViewModel
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import java.io.IOException
import java.util.Locale

open class ThirdFragment : Fragment() {


    private val settingUtils = SettingUtils

    private lateinit var databaseReference: DatabaseReference
    private lateinit var storageReference: StorageReference
    private lateinit var firebaseauthviewModel: FirebaseAuthViewModel

    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val userId = currentUser?.uid?:""

    private lateinit var userImage: ImageView
    private lateinit var editImageButton: ImageButton
    private val cameraPermissionRequestCode = 1002 // Puoi scegliere qualsiasi valore univoco

    private lateinit var language: TextView
    private lateinit var selectedLanguage: String
    private lateinit var languageListView: ListView

    private lateinit var switchNotificationButton: SwitchMaterial
    private lateinit var switchNFNButton: SwitchMaterial
    private lateinit var switchNFRButton: SwitchMaterial
    private var isNFRButtonEnabled = false
    private var isNFNButtonEnabled = false

    private lateinit var account: TextView
    private lateinit var selectedAccountPrivacy: String
    private lateinit var accountPrivacyListView: ListView

    private lateinit var review: TextView
    private lateinit var selectedReviewPrivacy: String
    private lateinit var reviewPrivacyListView: ListView

    private val photoRequestCode = 1
    private val photoRequestCodeFromGallery = 2

    private lateinit var sharedPreferences: SharedPreferences


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Legge il layout XML per questo fragment
        val rootView = inflater.inflate(R.layout.fragment_third, container, false)

        //Inizializza Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("users")
        storageReference = FirebaseStorage.getInstance().getReference("profile image/$userId")

        // Inizializza il ViewModel per la gestione dell'autenticazione Firebase
        firebaseauthviewModel = ViewModelProvider(this)[FirebaseAuthViewModel::class.java]

        //Inizializza le view
        language= rootView.findViewById(R.id.language)
        editImageButton = rootView.findViewById(R.id.editImageButton)


        switchNotificationButton = rootView.findViewById(R.id.switchNotificationButton)
        switchNFNButton = rootView.findViewById(R.id.switchNFNButton)
        switchNFRButton = rootView.findViewById(R.id.switchNFRButton)

        val database: FirebaseDatabase = FirebaseDatabase.getInstance()

        sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)


        userImage = rootView.findViewById(R.id.userImage)
        editImageButton = rootView.findViewById(R.id.editImageButton)
        // Gestione del cambio immagine
        editImageButton.setOnClickListener {
            openFileChooser()
        }

        // Carica l'URL dell'immagine di profilo dalle SharedPreferences
        val imageUrl = sharedPreferences.getString("profile image_$userId", null)
        // Mostra l'immagine di profilo se l'URL è presente
        if (!imageUrl.isNullOrEmpty()) {
            Picasso.get().load(imageUrl).into(userImage)
        }


        // Mostra il nome utente
        settingUtils.displayUsername(userId, rootView)

        // Gestione del cambio nome utente
        val editNameButton: ImageButton = rootView.findViewById(R.id.editNameButton)
        editNameButton.setOnClickListener {
            settingUtils.showEditNameDialog(requireContext(), userId, rootView)
        }


        // Leggi la lingua preferita dell'utente dalle preferenze condivise o da qualsiasi altra fonte
        selectedLanguage = sharedPreferences.getString("selectedLanguage", getString(R.string.italian)) ?: getString(R.string.italian)
        language.text=selectedLanguage

        language.setOnClickListener {
            showLanguageDialog()
        }


        val notificationEnabled = sharedPreferences.getBoolean("switchNotificationButton", false)
        switchNotificationButton.isChecked = notificationEnabled
        // Leggi lo stato di switchNFNButton dalle SharedPreferences
        val nfnEnabled = sharedPreferences.getBoolean("switchNFNButton", false)
        switchNFNButton.isChecked = nfnEnabled
        // Leggi lo stato di switchNFRButton dalle SharedPreferences
        val nfrEnabled = sharedPreferences.getBoolean("switchNFRButton", false)
        switchNFRButton.isChecked = nfrEnabled

        // Imposta lo stato iniziale di switchNFNButton in base allo stato iniziale di switchNotificationButton
        switchNFNButton.isEnabled = switchNotificationButton.isChecked
        // Imposta lo stato iniziale di switchNFNButton in base allo stato iniziale di switchNotificationButton
        switchNFRButton.isEnabled = switchNotificationButton.isChecked
        // Rileva il cambiamento di stato del pulsante switchNotificationButton
        switchNotificationButton.setOnCheckedChangeListener { _, isChecked ->
            // Abilita o disabilita switchNFNButton in base allo stato di switchNotificationButton
            switchNFNButton.isEnabled = isChecked
            // Abilita o disabilita switchNFRButton in base allo stato di switchNotificationButton
            switchNFRButton.isEnabled = isChecked

            // Verifica se sia switchNotificationButton che switchNFNButton sono attivi
            val newFollowerNotificationsEnabled = isChecked && switchNFNButton.isChecked
            // Aggiorna il valore nel database solo se entrambi i pulsanti sono attivi
            settingUtils.updateNFNSetting(userId, newFollowerNotificationsEnabled)

            // Verifica se sia switchNotificationButton che switchNFNButton sono attivi
            val newReviewNotificationsEnabled = isChecked && switchNFRButton.isChecked
            // Aggiorna il valore nel database solo se entrambi i pulsanti sono attivi
            settingUtils.updateNFRSetting(userId, newReviewNotificationsEnabled)

            // Salva lo stato di switchNotificationButton nelle SharedPreferences
            settingUtils.saveSwitchState(requireContext(),"switchNotificationButton", isChecked)
        }
        // Rileva il cambiamento di stato del pulsante switchNFNButton
        switchNFNButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                isNFNButtonEnabled = isChecked
                settingUtils.updateNFNSetting(userId, isChecked)
            } else {
                settingUtils.updateNFNSetting(userId,false)
            }
            // Salva lo stato di switchNFNButton nelle SharedPreferences
            settingUtils.saveSwitchState(requireContext(),"switchNFNButton", isChecked)
        }
        // Rileva il cambiamento di stato del pulsante switchNFRButton
        switchNFRButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                isNFRButtonEnabled = isChecked
                settingUtils.updateNFRSetting(userId, isChecked)
            } else {
                settingUtils.updateNFRSetting(userId, false)
            }
            // Salva lo stato di switchNFRButton nelle SharedPreferences
            settingUtils.saveSwitchState(requireContext(),"switchNFRButton", isChecked)
        }


        account = rootView.findViewById(R.id.account)

        selectedAccountPrivacy = sharedPreferences.getString("selectedAccountPrivacy", "Tutti") ?: "Tutti"
        account.text = selectedAccountPrivacy

        account.setOnClickListener {
            showAPDialog()
        }

        review = rootView.findViewById(R.id.review)

        selectedReviewPrivacy = sharedPreferences.getString("selectedReviewPrivacy", "Tutti") ?: "Tutti"
        review.text = selectedReviewPrivacy

        review.setOnClickListener {
            showRPDialog()
        }


        // Trova i pulsanti nel layout del fragment
        val signOut = rootView.findViewById<Button>(R.id.signOut)
        val delete = rootView.findViewById<Button>(R.id.delete)

        // Imposta un click listener per il pulsante "Sign Out"
        signOut.setOnClickListener {
            firebaseauthviewModel.signOut(requireContext())
        }

        // Imposta un click listener per il pulsante "Delete"
        delete.setOnClickListener {
            firebaseauthviewModel.delete(requireContext())

            // Ottieni un riferimento al nodo utente nel Realtime Database
            val databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId)
            // Rimuovi il nodo utente dal database in tempo reale
            databaseReference.removeValue()

            // Ora, elimina anche tutte le recensioni associate a questo utente
            val reviewsReference = FirebaseDatabase.getInstance().getReference("reviews")

            // Query per ottenere le recensioni dell'utente che stai eliminando
            val query = reviewsReference.orderByChild("userId").equalTo(userId)

            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (reviewSnapshot in dataSnapshot.children) {
                        // Rimuovi questa recensione
                        reviewSnapshot.ref.removeValue()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Gestisci eventuali errori qui
                }
            })

        }

        // Osserva il risultato del logout dall'account Firebase
        firebaseauthviewModel.signOutResult.observe(viewLifecycleOwner) { result ->
            if (result == FirebaseAuthViewModel.SignOutResult.SUCCESS) {
                // Se il logout è riuscito, avvia l'activity principale
                val intent = Intent(requireContext(), MainActivity::class.java)
                startActivity(intent)
            } else {
                // Se il logout non è riuscito, gestisce l'errore qui
            }
        }

        // Osserva il risultato dell'eliminazione dell'account Firebase
        firebaseauthviewModel.deleteResult.observe(viewLifecycleOwner) { result ->
            if (result == FirebaseAuthViewModel.DeleteResult.SUCCESS) {
                // Se l'eliminazione dell'account è riuscita, avvia l'activity principale
                val intent = Intent(requireContext(), MainActivity::class.java)
                startActivity(intent)
            } else {
                // Se l'eliminazione dell'account non è riuscita, gestisce l'errore qui
            }
        }

        return rootView
    }

    private fun openFileChooser() {
        val options = arrayOf(getString(R.string.takePicture), getString(R.string.choseFromGallery), getString(R.string.cancel))

        val builder = AlertDialog.Builder(requireContext())
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
                    // Annulla
                    dialog.dismiss()
                }
            }
        }
        builder.show()
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode==photoRequestCode) {
            if (resultCode == Activity.RESULT_OK) {
                val bp = data?.extras?.get("data") as Bitmap
                // Salva l'immagine nello storage di Firebase
                settingUtils.uploadImageToFirebaseStorage(requireContext(), userId, bp)
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
                            // Aggiorna l'ImageView con la nuova immagine
                            userImage.setImageBitmap(bitmap)
                            // Carica l'immagine su Firebase Storage
                            settingUtils.uploadImageToFirebaseStorage(requireContext(), userId, bitmap)
                            userImage.setImageBitmap(bitmap)
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }


    private fun showLanguageDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_language, null)
        languageListView = dialogView.findViewById(R.id.languageListView)

        val languageOptions = arrayOf(getString(R.string.italian),getString(R.string.english))

        // Crea un adapter per la ListView con le opzioni di privacy
        val languageAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, languageOptions)
        languageListView.adapter = languageAdapter

        // Crea una PopupWindow
        val popupWindow = PopupWindow(dialogView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        languageListView.setOnItemClickListener { _, _, position, _ ->
            when (position) {
                0 -> {
                    // L'utente ha selezionato l'opzione "Italiano"
                    // Imposta la lingua dell'app su italiano
                    val locale = Locale("Italian")
                    Locale.setDefault(locale)
                    val configuration = Configuration()
                    configuration.setLocale(locale)
                    resources.updateConfiguration(configuration, resources.displayMetrics)

                    // Salva la selezione della lingua nelle preferenze condivise
                    settingUtils.saveSelectedLanguage(requireContext(),"Italian")

                    // Chiudi la finestra di dialogo
                    popupWindow.dismiss()
                }
                1 -> {
                    // L'utente ha selezionato l'opzione "Inglese"
                    // Imposta la lingua dell'app su inglese
                    val locale = Locale("English")
                    Locale.setDefault(locale)
                    val configuration = Configuration()
                    configuration.setLocale(locale)
                    resources.updateConfiguration(configuration, resources.displayMetrics)

                    // Salva la selezione della lingua nelle preferenze condivise
                    settingUtils.saveSelectedLanguage(requireContext(),"English")

                    // Chiudi la finestra di dialogo
                    popupWindow.dismiss()
                }
            }
            // Aggiorna il testo nella TextView con la lingua selezionata
            selectedLanguage = languageOptions[position]

            // Aggiungi istruzioni di log per verificare i valori
            Log.d("LanguageDialog", "Selected language: $selectedLanguage")

            language.text = selectedLanguage
        }

        // Chiudi il popup quando viene toccato fuori
        dialogView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val rect = Rect()
                dialogView.getGlobalVisibleRect(rect)
                if (!rect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    popupWindow.dismiss()
                    language.performClick()
                }
            }
            true
        }

        // Visualizza il popup
        popupWindow.isOutsideTouchable = true
        popupWindow.isFocusable = true
        popupWindow.showAsDropDown(language)

        val location = IntArray(2)
        language.getLocationOnScreen(location)
        val x = location[0]
        val y = location[1] + language.height
        popupWindow.showAtLocation(language, Gravity.NO_GRAVITY, x, y)
    }
    private fun showAPDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_account_privacy, null)
        accountPrivacyListView = dialogView.findViewById(R.id.accountPrivacyListView)

        val accountPrivacy = arrayOf(getString(R.string.everyone), getString(R.string.followers))

        // Crea un adapter per la ListView con le opzioni di privacy
        val accountAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, accountPrivacy)
        accountPrivacyListView.adapter = accountAdapter

        // Crea una PopupWindow
        val popupWindow = PopupWindow(dialogView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        accountPrivacyListView.setOnItemClickListener { _, _, position, _ ->
            selectedAccountPrivacy = accountPrivacy[position]
            settingUtils.saveSelectedAP(requireContext(), selectedAccountPrivacy)
            settingUtils.updateAPOption(requireContext(), userId, selectedAccountPrivacy)
            popupWindow.dismiss()
            account.text = selectedAccountPrivacy
        }

        // Chiudi il popup quando viene toccato fuori
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
    private fun showRPDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_review_privacy, null)
        reviewPrivacyListView = dialogView.findViewById(R.id.reviewPrivacyListView)

        val reviewPrivacy = arrayOf(getString(R.string.everyone), getString(R.string.followers), getString(R.string.nobody))

        val reviewAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, reviewPrivacy)
        reviewPrivacyListView.adapter = reviewAdapter

        val popupWindow = PopupWindow(dialogView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        reviewPrivacyListView.setOnItemClickListener { _, _, position, _ ->
            selectedReviewPrivacy = reviewPrivacy[position]
            settingUtils.saveSelectedRP(requireContext(), selectedReviewPrivacy)
            settingUtils.updateRPOption(requireContext(), userId, selectedReviewPrivacy)
            popupWindow.dismiss()
            review.text = selectedReviewPrivacy
        }

        // Chiudi il popup quando viene toccato fuori
        dialogView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val rect = Rect()
                dialogView.getGlobalVisibleRect(rect)
                if (!rect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    popupWindow.dismiss()
                    review.performClick()
                }
            }
            true
        }

        // Visualizza il popup
        popupWindow.isOutsideTouchable = true
        popupWindow.isFocusable = true
        popupWindow.showAsDropDown(review)

        val location = IntArray(2)
        review.getLocationOnScreen(location)
        val x = location[0]
        val y = location[1] + review.height
        popupWindow.showAtLocation(review, Gravity.NO_GRAVITY, x, y)
    }
}