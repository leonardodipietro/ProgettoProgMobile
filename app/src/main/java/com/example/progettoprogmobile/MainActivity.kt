package com.example.progettoprogmobile
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.progettoprogmobile.viewModel.FirebaseAuthViewModel
import com.google.firebase.FirebaseApp

class MainActivity : AppCompatActivity() {

    private val viewModel = FirebaseAuthViewModel()

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

        // Inizializza la ViewModel
        firebaseviewModel = ViewModelProvider(this)[FirebaseAuthViewModel::class.java]

        // Ottieni una referenza al pulsante di accesso
        val signInButton = findViewById<Button>(R.id.signInButton)


        // Gestisci il click sul pulsante
        signInButton.setOnClickListener {
            // Avvia l'intento di accesso
            firebaseviewModel.createSignInIntent()
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