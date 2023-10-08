package com.example.progettoprogmobile.utils

import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
import com.google.firebase.ktx.Firebase
import android.util.Log
import android.view.View
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.example.progettoprogmobile.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream



class SettingUtils{
    companion object {
        fun showEditNameDialog(context: Context, userId: String, rootView: View) {

            val dialogView =
                LayoutInflater.from(context).inflate(R.layout.dialog_edit_name, null)
            val builder =
                AlertDialog.Builder(context).setView(dialogView).setTitle("Edit Name")
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
                        // Gestisci eventuali errori nell'aggiornamento del nome utente
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

            // Converte la Bitmap in un array di byte
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()

            // Carica l'immagine nello storage di Firebase
            val uploadTask = imageRef.putBytes(data)

            // Aggiungi un listener per gestire l'avanzamento dell'upload
            uploadTask.addOnSuccessListener { taskSnapshot ->
                // L'upload è stato completato con successo
                // Puoi ottenere l'URL dell'immagine caricata
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    val imageUrl = uri.toString()
                    saveProfileImageURL(context, userId, imageUrl)
                    val userRef: DatabaseReference = FirebaseDatabase.getInstance().reference.child("users").child(userId)
                    userRef.child("profile image").setValue(imageUrl)

                    Log.e ("LOG UPLOAD", "Immagine caricata con successo: $imageUrl")
                }
            }.addOnFailureListener { exception ->
                // Si è verificato un errore durante l'upload
                // Gestisci l'errore come preferisci
                Log.e ("LOG UPLOAD",  "Errore durante il caricamento dell'immagine.")
            }
        }


        fun saveSelectedLanguage(context: Context, languageCode: String) {
            val sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString("selectedLanguage", languageCode)
            editor.apply()
        }

        fun updateNFNSetting(userId: String, enableNotifications: Boolean) {
            val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().reference
            val userRef: DatabaseReference = databaseReference
                .child("users")
                .child(userId) // Sostituisci con il percorso appropriato nel tuo database

            val notificationRef: DatabaseReference = userRef.child("notification")
            notificationRef.child("new followers").setValue(enableNotifications)
                .addOnSuccessListener {
                    // Il valore è stato aggiornato con successo nel database
                    // Puoi fare qualcosa qui se necessario
                }
                .addOnFailureListener { e ->
                    // Gestisci eventuali errori nell'aggiornamento del valore
                    Log.e("Firebase", "Errore nell'aggiornamento del valore: ${e.message}")
                }
        }
        fun saveSwitchState(context: Context, key: String, value: Boolean) {
            val sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putBoolean(key, value)
            editor.apply()
        }
        fun updateNFRSetting(userId: String, enableNotifications: Boolean) {
            val userRef: DatabaseReference = FirebaseDatabase.getInstance()
                .reference
                .child("users")
                .child(userId)
                .child("notification") // Sostituisci "notifiche" con il tuo percorso nel database

            userRef.child("new following reviews").setValue(enableNotifications)
                .addOnSuccessListener {
                    // Il valore è stato aggiornato con successo nel database
                    // Puoi fare qualcosa qui se necessario
                }
                .addOnFailureListener { e ->
                    // Gestisci eventuali errori nell'aggiornamento del valore
                    Log.e("Firebase", "Errore nell'aggiornamento del valore: ${e.message}")
                }
        }

        fun saveSelectedAP(context: Context, accountPrivacy: String) {
            val sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString("selectedAccountPrivacy", accountPrivacy)
            editor.apply()
        }
        fun updateAPOption(context: Context, userId: String, selectedOption: String) {
            val userRef: DatabaseReference = FirebaseDatabase.getInstance()
                .reference
                .child("users")
                .child(userId)
                .child("privacy")
                .child("account")

            // Recupera l'elenco delle opzioni di privacy
            val privacyOptions =
                listOf(context.getString(R.string.everyone), context.getString(R.string.followers))

            // Imposta tutte le opzioni su false, tranne quella selezionata su true
            for (option in privacyOptions) {
                val isOptionSelected = option.equals(selectedOption, ignoreCase = true)
                userRef.child(option).setValue(isOptionSelected)
                    .addOnSuccessListener {
                        // Il valore è stato aggiornato con successo nel database
                        // Puoi fare qualcosa qui se necessario
                    }
                    .addOnFailureListener { e ->
                        // Gestisci eventuali errori nell'aggiornamento del valore
                        Log.e("Firebase", "Errore nell'aggiornamento del valore: ${e.message}")
                    }
            }
        }

        fun saveSelectedRP(context: Context, reviewPrivacy: String) {
            val sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString("selectedReviewPrivacy", reviewPrivacy)
            editor.apply()
        }
        fun updateRPOption(context: Context, userId: String, selectedOption: String) {
            val userRef: DatabaseReference = Firebase.database.reference
                .child("users")
                .child(userId)
                .child("privacy")
                .child("review")

            // Recupera l'elenco delle opzioni di privacy
            val privacyOptions = listOf(
                context.getString(R.string.everyone),
                context.getString(R.string.followers),
                context.getString(R.string.nobody)
            )
            // Imposta tutte le opzioni su false, tranne quella selezionata su true
            for (option in privacyOptions) {
                val isOptionSelected = option.equals(selectedOption, ignoreCase = true)
                userRef.child(option).setValue(isOptionSelected)
                    .addOnSuccessListener {
                        // Il valore è stato aggiornato con successo nel database
                        // Puoi fare qualcosa qui se necessario
                    }
                    .addOnFailureListener { e ->
                        // Gestisci eventuali errori nell'aggiornamento del valore
                        Log.e("Firebase", "Errore nell'aggiornamento del valore: ${e.message}")
                    }
            }
        }
    }
}
