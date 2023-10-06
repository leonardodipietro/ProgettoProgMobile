package com.example.progettoprogmobile

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Rect
import android.Manifest
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.progettoprogmobile.utils.SettingUtils
import com.example.progettoprogmobile.viewModel.FirebaseAuthViewModel
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import java.io.File
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
    private var imageUri: Uri? = null
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


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Legge il layout XML per questo fragment
        val rootView = inflater.inflate(R.layout.fragment_third, container, false)

        //Inizializza Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("users")
        storageReference = FirebaseStorage.getInstance().getReference("profile image")

        // Inizializza il ViewModel per la gestione dell'autenticazione Firebase
        firebaseauthviewModel = ViewModelProvider(this)[FirebaseAuthViewModel::class.java]

        //Inizializza le view
        language= rootView.findViewById(R.id.language)
        userImage = rootView.findViewById(R.id.userImage)
        editImageButton = rootView.findViewById(R.id.editImageButton)


        switchNotificationButton = rootView.findViewById(R.id.switchNotificationButton)
        switchNFNButton = rootView.findViewById(R.id.switchNFNButton)
        switchNFRButton = rootView.findViewById(R.id.switchNFRButton)

        val database: FirebaseDatabase = FirebaseDatabase.getInstance()

        val sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)


        // Carica l'URL dell'immagine di profilo dalle SharedPreferences
        val imageUrl = sharedPreferences.getString("profile image_$userId", null)

        // Mostra l'immagine di profilo se l'URL è presente
        if (!imageUrl.isNullOrEmpty()) {
            Picasso.get().load(imageUrl).into(userImage)
        }
        // Gestione del cambio immagine
        editImageButton.setOnClickListener {
            openFileChooser()
        }

        // Mostra l'immagine di profilo se l'URL è presente
        if (!imageUrl.isNullOrEmpty()) {
            Picasso.get().load(imageUrl).into(userImage) // Utilizza Picasso per caricare l'immagine
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

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            val data: Intent? = result.data
            if (data != null && data.data != null) {
                imageUri = data.data
                try {
                    if (imageUri != null) {
                        Glide.with(this)
                            .load(imageUri)
                            .into(userImage)
                        uploadImage(userId)
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }
    private fun openFileChooser() {
        val options = arrayOf(getString(R.string.takePicture), getString(R.string.choseFromGallery), getString(R.string.cancel))

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.choseAnOption))
        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> {
                    // Scatta una foto
                    if (ContextCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        // Hai l'autorizzazione, puoi avviare l'attività di scatto foto
                        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        if (takePictureIntent.resolveActivity(requireActivity().packageManager) != null) {
                            // Crea un file temporaneo in cui verrà salvata l'immagine catturata
                            val photoFile: File? = try {
                                settingUtils.createImageFile(requireContext())
                            } catch (ex: IOException) {
                                // Gestire l'errore qui se necessario
                                ex.printStackTrace() // Aggiungi questa linea per registrare l'errore
                                null
                            }
                            // Se il file temporaneo è stato creato con successo, avvia l'attività di scatto foto
                            photoFile?.also {
                                val photoURI: Uri = FileProvider.getUriForFile(
                                    requireContext(),
                                    "com.example.progettoprogmobile.fileprovider",
                                    it
                                )
                                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)

                                pickImageLauncher.launch(takePictureIntent)

                                // Qui puoi chiamare la funzione per salvare l'immagine direttamente nello storage Firebase
                                // Assicurati di passare il percorso del file temporaneo come parametro
                                uploadImageToFirebaseStorage( userId, photoFile)
                            }
                        }
                    } else {
                        // Non hai l'autorizzazione, richiedila all'utente
                        ActivityCompat.requestPermissions(
                            requireActivity(),
                            arrayOf(
                                Manifest.permission.CAMERA,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                            ),
                            cameraPermissionRequestCode
                        )
                    }
                }

                1 -> {
                    // Scegli dalla galleria
                    val intent =
                        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
                    pickImageLauncher.launch(intent)
                }

                2 -> {
                    // Annulla
                    dialog.dismiss()
                }
            }
        }
        builder.show()
    }
    private fun uploadImage(userId: String) {
        imageUri?.let { uri ->
            val storage = FirebaseStorage.getInstance()
            val fileReference = storageReference.child("$userId.jpg")

            fileReference.putFile(uri)
                .addOnSuccessListener {
                    fileReference.downloadUrl.addOnSuccessListener { uri ->
                        val imageUrl = uri.toString()
                        // Salva l'URL dell'immagine nel database Firebase sotto il nodo dell'utente corrente
                        val userRef: DatabaseReference =
                            FirebaseDatabase.getInstance().reference.child("users").child(userId)
                        userRef.child("profile image").setValue(imageUrl)
                            .addOnSuccessListener {
                                // URL dell'immagine di profilo salvato con successo nel database
                                settingUtils.saveProfileImageURL(requireContext(),userId, imageUrl) // Salva l'URL nell SharedPreferences
                            }
                    }
                }
        }
    }
    private fun uploadImageToFirebaseStorage(userId: String, photoFile: File) {
        val storage = FirebaseStorage.getInstance()
        val storageReference =
            storage.getReference("profile image") // Riferimento al tuo nodo "immagine profilo"

        val fileReference = storageReference.child("$userId.jpg")

        val uri = Uri.fromFile(photoFile)

        fileReference.putFile(uri)
            .addOnSuccessListener {
                fileReference.downloadUrl.addOnSuccessListener { uri ->
                    val imageUrl = uri.toString()
                    // Salva l'URL dell'immagine nel database Firebase sotto il nodo "immagine profilo"
                    val userRef: DatabaseReference =
                        FirebaseDatabase.getInstance().reference.child("users").child(userId)
                    userRef.child("profile image").setValue(imageUrl)
                        .addOnSuccessListener {
                            // URL dell'immagine di profilo salvato con successo nel database
                            settingUtils.saveProfileImageURL(requireContext(), userId, imageUrl) // Salva l'URL nell SharedPreferences
                        }
                }
            }
            .addOnFailureListener { e ->
                // Gestisci eventuali errori nell'upload dell'immagine
                Log.e("Firebase", "Errore nell'upload dell'immagine: ${e.message}")
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
            settingUtils.saveSelectedAP(requireContext(), selectedReviewPrivacy)
            settingUtils.updateAPOption(requireContext(), userId, selectedReviewPrivacy)
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