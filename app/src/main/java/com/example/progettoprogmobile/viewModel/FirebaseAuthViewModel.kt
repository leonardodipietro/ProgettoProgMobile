package com.example.progettoprogmobile.viewModel

import android.content.Context
import androidx.activity.result.ActivityResult
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.firebase.ui.auth.AuthUI
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.example.progettoprogmobile.MainActivity
import com.google.firebase.auth.FirebaseAuth
import android.util.Log



class FirebaseAuthViewModel : ViewModel() {

    // Definisci i possibili risultati dell'accesso
    enum class SignInResult {
        SUCCESS,
        FAILURE
    }

    enum class SignOutResult {
        SUCCESS,
        FAILURE
    }

    enum class DeleteResult {
        SUCCESS,
        FAILURE
    }

    lateinit var signInIntent: Intent // L'intento per l'accesso

    // Variabile LiveData per osservare il risultato dell'accesso
    val signInResult = MutableLiveData<SignInResult>()
     val signOutResult = MutableLiveData<SignOutResult>()
    val deleteResult = MutableLiveData<DeleteResult>()

    private val auth = FirebaseAuth.getInstance()

    fun checkAuthenticationStatus() {
        if (auth.currentUser != null) {
            // Utente già autenticato
            signInResult.value = SignInResult.SUCCESS
        } else {
            // Utente non autenticato
            signInResult.value = SignInResult.FAILURE
        }
    }

    // Crea l'intento per l'accesso con email
    fun createSignInIntent() {
        // Choose authentication providers
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        // Create and launch sign-in intent
        signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setIsSmartLockEnabled(true)
            .build()
    }

    // Gestisci il risultato dell'accesso
    fun handleSignInResult(result: ActivityResult) {
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            // Accesso riuscito, impostare il valore LiveData su SUCCESS
            signInResult.value = SignInResult.SUCCESS
        } else {
            // Accesso fallito, impostare il valore LiveData su FAILURE
            signInResult.value = SignInResult.FAILURE
        }
    }

    fun signOut(context:Context) {
        Log.d("MyApp", "Before signOut")
        AuthUI.getInstance()
            .signOut(context)
            .addOnCompleteListener { task ->
                Log.d("MyApp", "SignOut completed")
                if (task.isSuccessful) {
                    val intent = Intent(context, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    context.startActivity(intent)
                } else {
                    // Logout non riuscito, gestisci l'errore se necessario
                    signOutResult.value = SignOutResult.FAILURE
                }
            }
        Log.d("MyApp", "After signOut")
    }

    fun delete(context:Context) {
        Log.d("MyApp", "Before delete")
        AuthUI.getInstance()
            .delete(context)
            .addOnCompleteListener { task ->
                Log.d("MyApp", "Delete completed")
                if (task.isSuccessful) {
                    deleteResult.value = DeleteResult.SUCCESS
                    val intent = Intent(context, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    context.startActivity(intent)
                } else {
                    deleteResult.value = DeleteResult.FAILURE
                }
            }
        Log.d("MyApp", "After delete")
    }

}