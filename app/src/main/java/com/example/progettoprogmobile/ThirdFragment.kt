package com.example.progettoprogmobile

import android.content.Intent
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.app.AlertDialog
import androidx.navigation.Navigation
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.widget.Button
import android.widget.*
import androidx.lifecycle.ViewModelProvider
import com.example.progettoprogmobile.viewModel.FirebaseAuthViewModel

import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.IOException
import com.squareup.picasso.Picasso


class ThirdFragment : Fragment() {

    private val PICK_IMAGE_REQUEST = 1
    private lateinit var userImage: ImageView
    private lateinit var editImageButton: ImageButton
    private var imageUri: Uri? = null
    private lateinit var databaseReference: DatabaseReference
    private lateinit var storageReference: StorageReference
    private lateinit var firebaseauthviewModel: FirebaseAuthViewModel
    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val userId = currentUser?.uid


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {

        // Legge il layout XML per questo fragment
        val rootView = inflater.inflate(R.layout.fragment_third, container, false)


        //Inizializza Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("users")
        storageReference = FirebaseStorage.getInstance().getReference("immagine profilo")

        // Inizializza il ViewModel per la gestione dell'autenticazione Firebase
        firebaseauthviewModel = ViewModelProvider(this).get(FirebaseAuthViewModel::class.java)

        //Inizializza le view
        userImage = rootView.findViewById(R.id.userImage)
        editImageButton = rootView.findViewById(R.id.editImageButton)


        // Carica l'URL dell'immagine di profilo dalle SharedPreferences
        val sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val imageUrl = sharedPreferences.getString("profileImageUrl", null)


        val database: FirebaseDatabase = FirebaseDatabase.getInstance()


        // Trova i pulsanti nel layout del fragment
        val signOut = rootView.findViewById<Button>(R.id.signOut)
        val delete = rootView.findViewById<Button>(R.id.delete)

        // Imposta un click listener per il pulsante "Sign Out"
        signOut.setOnClickListener{
            firebaseauthviewModel.signOut(requireContext())
        }

        // Imposta un click listener per il pulsante "Delete"
        delete.setOnClickListener{
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


        // Mostra il nome utente
        if (userId != null) {
            displayUsername(userId, rootView)
        }

        // Gestione del cambio nome utente
        val editNameButton: ImageButton = rootView.findViewById(R.id.editNameButton)
        editNameButton.setOnClickListener {
            showEditNameDialog(rootView)
        }

        // Gestione del cambio immagine
        editImageButton.setOnClickListener {
            openFileChooser()
        }

        // Mostra l'immagine di profilo se l'URL è presente
        if (!imageUrl.isNullOrEmpty()) {
            Picasso.get().load(imageUrl).into(userImage) // Utilizza Picasso o Glide per caricare l'immagine
        }

        return rootView
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == AppCompatActivity.RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
            try {
                val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(
                    requireContext().contentResolver,
                    imageUri
                )
                userImage.setImageBitmap(bitmap)
                if (userId != null) {
                    uploadImage(userId)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    // Mostra il dialog per il cambio nome utente
    private fun showEditNameDialog(rootView: View) {
        val dialogView =
            LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_name, null)
        val builder = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle("Edit Name")

        val alertDialog = builder.create()
        val editName: EditText = dialogView.findViewById(R.id.editName)
        val saveButton = dialogView.findViewById<Button>(R.id.saveButton)
        val cancelButton = dialogView.findViewById<Button>(R.id.cancelButton)

        saveButton.setOnClickListener {
            val newName = editName.text.toString()
            if (userId != null) {
                val userRef: DatabaseReference = FirebaseDatabase.getInstance().reference.child("users").child(userId)
                userRef.child("name").setValue(newName)
                    .addOnSuccessListener {
                        val usernameTextView = rootView.findViewById<TextView>(R.id.username)
                        usernameTextView.text = newName
                        alertDialog.dismiss() // Chiudi il dialog
                        // Nome utente aggiornato con successo
                        // Puoi fare qualcosa qui se necessario
                    }
                    .addOnFailureListener { e ->
                        // Gestisci eventuali errori nell'aggiornamento del nome utente
                        Log.e("Firebase", "Errore nell'aggiornamento del nome utente: ${e.message}")
                    }
            }
            alertDialog.dismiss() // Close the dialog
        }

        cancelButton.setOnClickListener {
            alertDialog.dismiss()
        }

        alertDialog.show()
    }

    // Mostra il nome utente
    private fun displayUsername(userId: String, rootView: View) {
        val userRef: DatabaseReference = FirebaseDatabase.getInstance().reference.child("users").child(userId)

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

    //Apri il file chooser per l'immagine
    private fun openFileChooser() {
        val options = arrayOf("Scatta una foto", "Scegli dalla galleria", "Annulla")

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Scegli un'opzione")
        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> {
                    // Scatta una foto
                    val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    if (takePictureIntent.resolveActivity(requireActivity().packageManager) != null) {
                        startActivityForResult(takePictureIntent, PICK_IMAGE_REQUEST)
                    }
                }

                1 -> {
                    // Scegli dalla galleria
                    val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    intent.type = "image/*"
                    startActivityForResult(intent, PICK_IMAGE_REQUEST)
                }

                2 -> {
                    // Annulla
                    dialog.dismiss()
                }
            }
        }
        builder.show()
    }

    private fun saveProfileImageURL(imageUrl: String) {
        val sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("profileImageUrl", imageUrl)
        editor.apply()
    }

    //Upload dell'immagine
    private fun uploadImage(userId: String) {
        imageUri?.let { uri ->
            val storage = FirebaseStorage.getInstance()
            val fileReference = storageReference.child("immagine profilo/${currentUser?.uid}.jpg")

            fileReference.putFile(uri)
                .addOnSuccessListener { taskSnapshot ->
                    fileReference.downloadUrl.addOnSuccessListener { uri ->
                        val imageUrl = uri.toString()
                        // Salva l'URL dell'immagine nel database Firebase sotto il nodo dell'utente corrente
                        val userRef: DatabaseReference = FirebaseDatabase.getInstance().reference.child("users").child(userId)
                        userRef.child("profileImageUrl").setValue(imageUrl)
                            .addOnSuccessListener {
                                // URL dell'immagine di profilo salvato con successo nel database
                                saveProfileImageURL(imageUrl) // Salva l'URL nell SharedPreferences
                            }
                    }
                }
        }
    }

}