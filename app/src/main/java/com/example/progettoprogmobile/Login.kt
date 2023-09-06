package com.example.progettoprogmobile

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

class Login : AppCompatActivity() {

    private lateinit var editTextEmail: TextInputEditText
    private lateinit var editTextPassword: TextInputEditText
    private lateinit var buttonLogin: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var progressBar: ProgressBar
    private lateinit var textView: TextView

    public override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        // Inizializzazione delle variabili per i campi di testo ed il pulsante
        editTextEmail = findViewById(R.id.email)
        editTextPassword = findViewById(R.id.password)
        buttonLogin = findViewById(R.id.btn_login)
        progressBar = findViewById(R.id.progressBar)
        textView = findViewById(R.id.registerNow)
        textView.setOnClickListener {
            val intent = Intent(applicationContext, Register::class.java) // Crea un'istanza di Intent per passare da questa attività a LoginActivity
            startActivity(intent) // Avvia l'attività LoginActivity
            finish()
        }

        buttonLogin.setOnClickListener{
            progressBar.visibility = View.VISIBLE

            // Ottieni i valori dai campi di testo
            val email = editTextEmail.text.toString()
            val password = editTextPassword.text.toString()

            // Controlla se l'email è vuota
            if (TextUtils.isEmpty(email)) {
                // Mostra un messaggio Toast se l'email è vuota
                Toast.makeText(this@Login, "Enter email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // Controlla se la password è vuota
            if (TextUtils.isEmpty(password)) {
                // Mostra un messaggio Toast se la password è vuota
                Toast.makeText(this@Login, "Enter password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Autenticazione con Firebase usando email e password
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    progressBar.visibility = View.GONE // Nascondi la barra di avanzamento (progressBar) quando il task è completato
                    if (task.isSuccessful) {
                        Toast.makeText(applicationContext, "Login successfull", Toast.LENGTH_SHORT).show()
                        val intent = Intent(applicationContext, MainActivity::class.java) // Reindirizza all'activity principale (MainActivity)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@Login, "Authentication failed.",
                            Toast.LENGTH_SHORT,).show()

                    }
                }
        }

    }
}