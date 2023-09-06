package com.example.progettoprogmobile
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.progettoprogmobile.viewModel.FirebaseAuthViewModel
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var button: Button
    private lateinit var textView: TextView
    private var user: FirebaseUser? = null //per evitare il tipo mismatch error

    // Initialize the FirebaseUI Widget using Firebase
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

        auth = FirebaseAuth.getInstance()
        button = findViewById(R.id.logout)
        textView = findViewById(R.id.user_details)
        user = auth.currentUser
        if (user == null) {
            // L'utente non è autenticato, reindirizza alla schermata di login
            val intent = Intent(applicationContext, Login::class.java)
            startActivity(intent)
            finish()
        } else {
            textView.text = user?.email //per accedere alle proprietà di user in modo sicuro, poiché può essere nullo

            // Configura il pulsante per eseguire il logout
            button.setOnClickListener {
                // Esegui il logout dall'account corrente
                FirebaseAuth.getInstance().signOut()

                // Reindirizza nuovamente alla schermata di login dopo il logout
                val intent = Intent(applicationContext, Login::class.java)
                startActivity(intent)
                finish()
            }
        }

        // Inizializza la ViewModel
        firebaseviewModel = ViewModelProvider(this)[FirebaseAuthViewModel::class.java]

        // Ottieni una referenza al pulsante di accesso con email
        val emailSignInButton = findViewById<Button>(R.id.emailSignInButton)

        // Gestisci il click sul pulsante
        emailSignInButton.setOnClickListener {
            // Avvia l'intento di accesso con email
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
    }
}