package com.example.progettoprogmobile
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.progettoprogmobile.viewModel.FirebaseAuthViewModel
import com.google.firebase.FirebaseApp
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class MainActivity : AppCompatActivity() {

    private val viewModel = FirebaseAuthViewModel()
    private lateinit var auth: FirebaseAuth
    private var user: FirebaseUser? = null //per evitare il tipo mismatch error


    var ui: AuthUI = AuthUI.getInstance()

    private lateinit var firebaseviewModel: FirebaseAuthViewModel
    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        firebaseviewModel.handleSignInResult(result)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel.checkAuthenticationStatus()

        auth = FirebaseAuth.getInstance()
        user = auth.currentUser


        if (user != null) {
            // L'utente è già autenticato, vai direttamente alla SecondActivity
            val intent = Intent(applicationContext, SecondActivity::class.java)
            startActivity(intent)
            finish()
        }


        // Inizializza la ViewModel
        firebaseviewModel = ViewModelProvider(this)[FirebaseAuthViewModel::class.java]

        val signInButton = findViewById<Button>(R.id.signInButton)

        signInButton.setOnClickListener {
            // Avvia l'intento di accesso
            firebaseviewModel.createSignInIntent(R.style.Theme_ProgettoProgMobile)
            signInLauncher.launch(firebaseviewModel.signInIntent)
        }

        // Osserva il risultato dell'accesso
        firebaseviewModel.signInResult.observe(this) { result ->
            if (result == FirebaseAuthViewModel.SignInResult.SUCCESS) {
                // Accesso riuscito, vai alla SecondActivity
                val intent = Intent(this, SecondActivity::class.java)
                startActivity(intent)
            } else {
                // Gestisci l'errore di accesso
            }
        }

        FirebaseApp.initializeApp(this)
    }
}