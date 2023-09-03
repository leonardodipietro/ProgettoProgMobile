package com.example.progettoprogmobile.viewModel

import androidx.activity.result.ActivityResult
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.firebase.ui.auth.AuthUI
import android.content.Intent
import android.app.PendingIntent
import androidx.appcompat.app.AppCompatActivity

class FirebaseAuthViewModel : ViewModel() {

    // Definisci i possibili risultati dell'accesso
    enum class SignInResult {
        SUCCESS,
        FAILURE
    }

    lateinit var signInIntent: Intent // L'intento per l'accesso

    // Variabile LiveData per osservare il risultato dell'accesso
    val signInResult = MutableLiveData<SignInResult>()

    // Crea l'intento per l'accesso con email
    fun createSignInIntent() {
        // Choose authentication providers
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build()
        )

        // Create and launch sign-in intent
        signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
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
}

