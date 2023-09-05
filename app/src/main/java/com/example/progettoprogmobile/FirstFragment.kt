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
        savedInstanceState: Bundle?
    ): View?     {
        val rootView = inflater.inflate(R.layout.fragment_first, container, false)

        val nextButton1 = rootView.findViewById<Button>(R.id.nextButton1)
        nextButton1.setOnClickListener {
            val navController = Navigation.findNavController(requireView())
            navController.navigate(R.id.navigateToSecondFragment)
        }

    return rootView
}
}

