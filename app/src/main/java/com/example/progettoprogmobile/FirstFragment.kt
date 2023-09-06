package com.example.progettoprogmobile

import androidx.navigation.Navigation
import android.widget.Button
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class FirstFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View?     {
        val rootView = inflater.inflate(R.layout.fragment_first, container, false)

        val nextButton1 = rootView.findViewById<Button>(R.id.nextButton1) //per ottenere il Button dal layout
        nextButton1.setOnClickListener { //setOnClickListener con una lambda che contiene l'azione da eseguire quando l'elemento viene cliccato
            val navController = Navigation.findNavController(requireView())
            navController.navigate(R.id.navigateToSecondFragment)
        }

        val nextButton3 = rootView.findViewById<Button>(R.id.nextButton3) //per ottenere il Button dal layout
        nextButton3.setOnClickListener { //setOnClickListener con una lambda che contiene l'azione da eseguire quando l'elemento viene cliccato
            val navController = Navigation.findNavController(requireView())
            navController.navigate(R.id.navigateToThirdFragment)
        }

    return rootView
    }
}

