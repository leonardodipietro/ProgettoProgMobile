package com.example.progettoprogmobile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.widget.Button
import androidx.lifecycle.ViewModelProvider
import com.example.progettoprogmobile.viewModel.FirebaseAuthViewModel
import com.google.firebase.auth.FirebaseAuth

class ThirdFragment : Fragment() {


private lateinit var firebaseauthviewModel: FirebaseAuthViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       val rootView = inflater.inflate(R.layout.fragment_third, container, false)

        val currentUser = FirebaseAuth.getInstance().currentUser

        firebaseauthviewModel = ViewModelProvider(this)[FirebaseAuthViewModel::class.java]



        val signOut:Button  = rootView.findViewById(R.id.signOut)
        val deleteButton :Button= rootView.findViewById(R.id.delete)
        //  val delete = requireView().findViewById<Button>(R.id.delete)


      signOut.setOnClickListener {
            firebaseauthviewModel.signOut(requireContext().applicationContext)
        }

        deleteButton.setOnClickListener {
        currentUser?.delete()
            ?.addOnSuccessListener {
                val intent = Intent(context, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                context?.startActivity(intent)
                Log.d("MyApp", "Eliminazione account avvenuta")
            }
            ?.addOnFailureListener {  }
        }

    /*    signOut.setOnClickListener {
            if (userId != null) {
                // Esegui il logout solo se userId non è null
                FirebaseAuth.getInstance().signOut()

                // Ora l'utente è stato disconnesso e puoi riportarlo alla MainActivity
                val intent = Intent(requireActivity(), MainActivity::class.java)
                startActivity(intent)
                requireActivity().finish() // Opzionale: chiudi la SecondActivity se necessario
            } else {
                // Gestisci il caso in cui userId è null
                // Potresti mostrare un messaggio all'utente o eseguire altre azioni a tua discrezione
            }
        }




      delete.setOnClickListener{
          firebaseauthviewModel.deleteResult.observe(viewLifecycleOwner) { result ->
              if (result == FirebaseAuthViewModel.DeleteResult.SUCCESS) {
                  val intent = Intent(requireActivity(), MainActivity::class.java)
                  startActivity(intent)
              } else {
                  //eliminazione dell'account non riuscita
              }
          }

        }
*/

     /*   firebaseauthviewModel.signOutResult.observe(viewLifecycleOwner) { result ->
            if (result == FirebaseAuthViewModel.SignOutResult.SUCCESS) {
                val intent = Intent(requireActivity(), MainActivity::class.java)
                startActivity(intent)
            } else {
                // Logout non riuscito
            }
        }
  */

    /*    firebaseauthviewModel.deleteResult.observe(viewLifecycleOwner) { result ->
            if (result == FirebaseAuthViewModel.DeleteResult.SUCCESS) {
                val intent = Intent(requireActivity(), MainActivity::class.java)
                startActivity(intent)
            } else {
                //eliminazione dell'account non riuscita
            }
        } */

        /*CODICE CHE GESTISCE I BOTTONI PER MUOVERSI TRA I FRAGMENT
        val prevButton2 = rootView.findViewById<Button>(R.id.prevButton2) //per ottenere il Button dal layout
        prevButton2.setOnClickListener { //setOnClickListener con una lambda che contiene l'azione da eseguire quando l'elemento viene cliccato
            val navController = Navigation.findNavController(requireView())
            navController.navigate(R.id.navigateToFirstFragment)
        }
        */
        return rootView

    }

}